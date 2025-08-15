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
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
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

    //FXML
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtAlias;
    @FXML
    private TextField txtCelular;
    @FXML
    private TextField txtAnticipo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
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

    public void guardarFiado() {
        String nombre = txtNombre.getText();
        String alias = txtAlias.getText();
        String celular = txtCelular.getText();
        String anticipo = txtAnticipo.getText();

        // Validar que nombre no esté vacío
        if (nombre == null || nombre.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Campo obligatorio");
            alert.setHeaderText(null);
            alert.setContentText("El nombre es obligatorio.");
            alert.showAndWait();
            return;
        }

        try {
            // Crear y guardar cliente
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

            // Guardar la venta

            crearVentasController.guardarVenta();
            ventaDao = new VentaDAO();
            venta = ventaDao.buscarUltimaVenta();

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
                try {
                    Double anticipoToDouble = Double.valueOf(anticipo.trim());
                    fiadoParcial = new FiadoParcial();
                    fiadoParcial.setFiado(fiado);
                    fiadoParcial.setAnticipo(anticipoToDouble);
                    fiadoParcial.setFecha(LocalDateTime.now());

                    fiadoParcialDao = new FiadoParcialDAO();
                    fiadoParcialDao.guardar(fiadoParcial);
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error de formato");
                    alert.setHeaderText(null);
                    alert.setContentText("El anticipo ingresado no es un número válido.");
                    alert.showAndWait();
                }
            }

            // Mensaje final y cierre de overlay
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText("Cliente guardado: " + cliente.getNombre());
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
