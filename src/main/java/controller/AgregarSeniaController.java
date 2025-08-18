package controller;

import dao.SeniaDAO;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import model.Cliente;
import model.Pedido;
import model.Senia;

public class AgregarSeniaController implements Initializable {

    // Labels y TextField actualizados
    @FXML
    private Label lblFecha;
    @FXML
    private Label lblClienteAsociado;
    @FXML
    private TextField txtDineroIngresar;

    private Cliente cliente;
    private Pedido pedido;
    private Cliente_pedidoController clientePedidoController;
    private SeniaDAO seniaDao;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        seniaDao = new SeniaDAO();
        // Mostrar fecha actual
        actualizarFecha();
    }

    private void actualizarFecha() {
        LocalDateTime fechaActual = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lblFecha.setText(formatter.format(fechaActual));
    }

    public void setCliente(Cliente c) {
        this.cliente = c;
        // Actualizar el label del cliente cuando se asigna
        if (c != null) {
            lblClienteAsociado.setText(c.getNombre());
            System.out.println("Cliente asignado: " + c.getNombre());
        }
    }

    public void setPedido(Pedido p) {
        this.pedido = p;
        if (p != null) {
            System.out.println("Pedido asignado ID: " + p.getId());
        }
    }

    public void setClientePedidoController(Cliente_pedidoController c) {
        this.clientePedidoController = c;
    }

    @FXML
    public void cerrarOverlay() {
        if (clientePedidoController != null) {
            clientePedidoController.cerrarSpaAgregarSenia();
        }
    }

    @FXML
    public void guardarSenia() {
        try {
            System.out.println("=== INICIANDO GUARDAR SEÑA ===");
            
            // Validar que hay un pedido seleccionado
            if (clientePedidoController == null || clientePedidoController.getPedidoSeleccionado() == null) {
                mostrarAlerta("Error", "No hay un pedido seleccionado", Alert.AlertType.ERROR);
                return;
            }

            // Validar que tenemos cliente y pedido
            if (cliente == null) {
                mostrarAlerta("Error", "No se ha asignado un cliente", Alert.AlertType.ERROR);
                return;
            }

            if (pedido == null) {
                mostrarAlerta("Error", "No se ha asignado un pedido", Alert.AlertType.ERROR);
                return;
            }

            // Validar monto - usando txtDineroIngresar
            if (txtDineroIngresar.getText() == null || txtDineroIngresar.getText().trim().isEmpty()) {
                mostrarAlerta("Error", "Debe ingresar un monto", Alert.AlertType.ERROR);
                return;
            }

            double monto;
            try {
                monto = Double.parseDouble(txtDineroIngresar.getText().trim());
                if (monto <= 0) {
                    mostrarAlerta("Error", "El monto debe ser mayor a 0", Alert.AlertType.ERROR);
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El monto debe ser un número válido", Alert.AlertType.ERROR);
                return;
            }

            // Crear la seña
            Senia nuevaSenia = new Senia();
            nuevaSenia.setFecha(LocalDateTime.now());
            nuevaSenia.setMonto(monto);
            nuevaSenia.setPedido(pedido); // Usar el pedido asignado directamente

            System.out.println("=== DATOS DE LA SEÑA ===");
            System.out.println("Cliente: " + cliente.getNombre());
            System.out.println("Pedido ID: " + pedido.getId());
            System.out.println("Monto: $" + monto);
            System.out.println("Fecha: " + nuevaSenia.getFecha());

            // Guardar en la base de datos usando el método crear
            boolean guardado = seniaDao.crear(nuevaSenia);
            
            if (guardado) {
                System.out.println("✓ Seña guardada correctamente en la BD");
                mostrarAlerta("Éxito", 
                    "Seña guardada correctamente:\n" +
                    "Cliente: " + cliente.getNombre() + "\n" +
                    "Monto: $" + String.format("%.2f", monto), 
                    Alert.AlertType.INFORMATION);
                cerrarOverlay();
            } else {
                System.err.println("✗ Error al guardar seña en la BD");
                mostrarAlerta("Error", "No se pudo guardar la seña en la base de datos", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO al guardar seña: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Error inesperado al guardar la seña: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}