/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import dao.ClienteDAO;
import dao.FiadoDAO;
import dao.FiadoParcialDAO;
import dao.VentaDAO;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import model.Cliente;
import model.Fiado;
import model.FiadoParcial;
import model.Venta;

/**
 * FXML Controller class
 *
 * @author garca
 */
public class NombreFiadoClienteController implements Initializable {

    /**
     * Initializes the controller class.
     */
    //Controller
    private Crear_ventasController crearVentasController;

    // objetos
    private Cliente cliente;
    private Fiado fiado;
    private Venta venta;
    private FiadoParcial fiadoParcial;
    //Dao
    private ClienteDAO clienteDao;
    private FiadoDAO fiadoDao;
    private VentaDAO ventaDao;
    private FiadoParcialDAO fiadoParcialDao;

    //Variables
    List<Cliente> listaClientesFiados;
    //FXML
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtAlias;
    @FXML
    private TextField txtCelular;
    @FXML
    private TextField txtAnticipo;
    @FXML
    private ComboBox<Cliente> cmbClientesExistentes;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarClientes();

        // Mostrar el nombre del cliente y alias en el combo
        cmbClientesExistentes.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Cliente cliente) {
                if (cliente != null) {
                    String display = cliente.getNombre();
                    if (cliente.getAlias() != null && !cliente.getAlias().trim().isEmpty()) {
                        display += " - " + cliente.getAlias();
                    }
                    return display;
                }
                return "";
            }

            @Override
            public Cliente fromString(String string) {
                return null; // No necesitamos convertir texto a objeto aquí
            }
        });
    }

    public void limpiarClientes() {
        cmbClientesExistentes.getSelectionModel().clearSelection();
    }

    public void setSpa_creaVentasController(Crear_ventasController c) {
        crearVentasController = c;
    }

    @FXML
    public void cerrarOverlay() {
        if (crearVentasController != null) {
            crearVentasController.CerrarDifuminarYSpa();
        }
    }

    public void cargarClientes() {
        clienteDao = new ClienteDAO();
        listaClientesFiados = clienteDao.buscarClientesConFiados();

        ObservableList<Cliente> clientesObservable
                = FXCollections.observableArrayList(listaClientesFiados);

        cmbClientesExistentes.setItems(clientesObservable);

    }

    public void guardarFiado() {
        String nombre = txtNombre.getText();
        String alias = txtAlias.getText();
        String celular = txtCelular.getText();
        String anticipo = txtAnticipo.getText();
        Cliente clienteSeleccionado = cmbClientesExistentes.getValue();

        // Determinar el nombre del cliente para la confirmación
        String nombreCliente;
        if (clienteSeleccionado != null) {
            nombreCliente = clienteSeleccionado.getNombre();
        } else {
            // Validar que nombre no esté vacío para cliente nuevo
            if (nombre == null || nombre.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Campo obligatorio");
                alert.setHeaderText(null);
                alert.setContentText("El nombre es obligatorio para crear un nuevo cliente.");
                alert.showAndWait();
                return;
            }
            nombreCliente = nombre.trim();
        }

        // Cartel de confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar registro de fiado");
        confirmacion.setHeaderText("Confirmación de operación");

        String mensajeConfirmacion = "Cliente: " + nombreCliente;
        if (anticipo != null && !anticipo.trim().isEmpty()) {
            try {
                Double anticipoDouble = Double.valueOf(anticipo.trim());
                Double anticipoRedondeado = (double) Math.round(anticipoDouble);

                mensajeConfirmacion += "\nAnticipo a registrar: $" + String.format("%.0f", anticipoRedondeado);
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de formato");
                alert.setHeaderText(null);
                alert.setContentText("El anticipo ingresado no es un número válido.");
                alert.showAndWait();
                return;
            }
        } else {
            mensajeConfirmacion += "\nSin anticipo";
        }
        mensajeConfirmacion += "\n\n¿Desea proceder con el registro del fiado?";

        confirmacion.setContentText(mensajeConfirmacion);

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.get() != ButtonType.OK) {
            return; // El usuario canceló
        }

        try {
            // Determinar si usar cliente existente o crear uno nuevo
            if (clienteSeleccionado != null) {
                // Usar cliente existente del combo box
                cliente = clienteSeleccionado;
            } else {
                // Crear y guardar nuevo cliente
                cliente = new Cliente();
                cliente.setNombre(nombre.trim());
                if (alias != null && !alias.trim().isEmpty()) {
                    cliente.setAlias(alias.trim());
                }
                if (celular != null && !celular.trim().isEmpty()) {
                    cliente.setCelular(celular.trim());
                }
                clienteDao = new ClienteDAO();
                clienteDao.guardar(cliente);
            }

            // Guardar la venta
            String medioPago = crearVentasController.getMedioPago();
            crearVentasController.guardarVenta();
            ventaDao = new VentaDAO();
            venta = ventaDao.buscarUltimaVenta();

            // ✅ Redondear total de la venta antes de asignarlo
            if (venta != null && venta.getTotal() != null) {
                double totalRedondeado = Math.round(venta.getTotal());
                venta.setTotal(totalRedondeado);
            }

            // Crear y guardar fiado
            fiado = new Fiado();
            fiado.setCliente(cliente);
            fiado.setVenta(venta);
            fiado.setFecha(LocalDateTime.now());
            fiadoDao = new FiadoDAO();
            fiadoDao.guardar(fiado);
            ventaDao.asignarFiadoALaUltimaVenta(fiado);

            // Guardar anticipo si se ingresó
            if (anticipo != null && !anticipo.trim().isEmpty()) {
                Double anticipoToDouble = Double.valueOf(anticipo.trim());
                Double anticipoRedondeado = (double) Math.round(anticipoToDouble);

                fiadoParcial = new FiadoParcial();
                fiadoParcial.setFiado(fiado);
                fiadoParcial.setAnticipo(anticipoRedondeado);
                fiadoParcial.setFecha(LocalDateTime.now());
                fiadoParcial.setMedioAbonado(medioPago);

                fiadoParcialDao = new FiadoParcialDAO();
                fiadoParcialDao.guardar(fiadoParcial);
            }

            // Mensaje de éxito final
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText("Fiado guardado exitosamente para: " + cliente.getNombre());
            alert.showAndWait();

            cerrarOverlay();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error al guardar fiado");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

}
