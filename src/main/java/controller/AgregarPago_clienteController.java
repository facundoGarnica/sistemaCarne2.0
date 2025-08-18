/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import dao.FiadoParcialDAO;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import model.Cliente;
import model.Fiado;
import model.FiadoParcial;

/**
 * FXML Controller class
 *
 * @author garca
 */
public class AgregarPago_clienteController implements Initializable {

    /**
     * Initializes the controller class.
     */
    //variables
    // ✅ Forzar formato con punto y sin decimales
    private final DecimalFormat decimalFormatter = new DecimalFormat("#,###",
            DecimalFormatSymbols.getInstance(Locale.US));

    @FXML
    private ComboBox<String> cmbMedioPago;
    @FXML
    private Label lblFecha;
    @FXML
    private Label lblClienteAsociado;
    @FXML
    private Label lblRestoAPagar;
    @FXML
    private TextField txtDineroIngresar;
    @FXML
    private Label lblError; // Opcional: para mostrar mensajes de error en la interfaz

    //Dao
    private FiadoParcialDAO fiadoParcialDao;

    //Objetos
    private Cliente clienteRecibido;
    private Fiado fiadoRecibido;
    private FiadoParcial fiadoParcial;
    //controller
    @FXML
    private ClientesController clientesController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Agregar Strings al combobox
        cmbMedioPago.setItems(FXCollections.observableArrayList(
                "Efectivo", "Transferencia", "QR", "Tarjeta"
        ));

        //Mostrar fecha actual en el Label del titulo
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yy");
        String fechaActual = LocalDate.now().format(formato);
        lblFecha.setText(fechaActual);

        // Configurar listener para validar en tiempo real
        configurarValidacionTiempoReal();
    }

    private void configurarValidacionTiempoReal() {
        txtDineroIngresar.textProperty().addListener((observable, oldValue, newValue) -> {
            limpiarMensajeError();
            if (newValue != null && !newValue.isEmpty() && fiadoRecibido != null) {
                try {
                    Double monto = Double.valueOf(newValue);
                    Double restoAPagar = fiadoRecibido.getResto();

                    if (monto > restoAPagar) {
                        mostrarMensajeError("El monto no puede ser mayor al resto a pagar: $" + restoAPagar);
                        txtDineroIngresar.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    } else if (monto <= 0) {
                        mostrarMensajeError("El monto debe ser mayor a cero");
                        txtDineroIngresar.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    } else {
                        txtDineroIngresar.setStyle(""); // Restablecer estilo normal
                    }
                } catch (NumberFormatException e) {
                    if (!newValue.isEmpty()) {
                        mostrarMensajeError("Ingrese un número válido");
                        txtDineroIngresar.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    }
                }
            }
        });
    }

    //recibe al cliente
    public void setClienteRecibido(Cliente clienteRecibido) {
        this.clienteRecibido = clienteRecibido;
        System.out.println("Cliente recibido: " + clienteRecibido.getNombre());
    }

    public void setFiadoRecibido(Fiado fiadoRecibido) {
        this.fiadoRecibido = fiadoRecibido;
        System.out.println("Fiado recibido: " + fiadoRecibido.getId());
    }

    public void mostrarDatos() {
        lblClienteAsociado.setText(
                clienteRecibido.getNombre()
                + ((clienteRecibido.getAlias() == null || clienteRecibido.getAlias().isEmpty())
                ? ""
                : " - " + clienteRecibido.getAlias())
        );

        // ✅ Formatear el resto a pagar con $
        lblRestoAPagar.setText("$ " + decimalFormatter.format(fiadoRecibido.getResto()));

        // Limpiar el campo de texto y mensajes de error al mostrar datos nuevos
        txtDineroIngresar.clear();
        limpiarMensajeError();
        txtDineroIngresar.setStyle("");
    }

    private void mostrarMensajeError(String mensaje) {
        if (lblError != null) {
            lblError.setText(mensaje);
            lblError.setTextFill(Color.RED);
            lblError.setVisible(true);
        }
        System.out.println("Error: " + mensaje);
    }

    private void limpiarMensajeError() {
        if (lblError != null) {
            lblError.setText("");
            lblError.setVisible(false);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void guardarAnticipo() {
        // Limpiar mensajes previos
        limpiarMensajeError();
        txtDineroIngresar.setStyle("");

        // Validar que se ingresó un monto
        if (txtDineroIngresar.getText() == null || txtDineroIngresar.getText().trim().isEmpty()) {
            mostrarMensajeError("Debe ingresar un monto");
            mostrarAlerta("Error", "Debe ingresar un monto", Alert.AlertType.WARNING);
            txtDineroIngresar.requestFocus();
            return;
        }

        Double dinero;
        try {
            dinero = Double.valueOf(txtDineroIngresar.getText().trim());
        } catch (NumberFormatException e) {
            mostrarMensajeError("Monto inválido. Ingrese un número válido");
            mostrarAlerta("Error", "Monto inválido. Ingrese un número válido", Alert.AlertType.ERROR);
            txtDineroIngresar.requestFocus();
            return;
        }

        // Validar que el monto sea mayor a cero
        if (dinero <= 0) {
            mostrarMensajeError("El monto debe ser mayor a cero");
            mostrarAlerta("Error", "El monto debe ser mayor a cero", Alert.AlertType.WARNING);
            txtDineroIngresar.requestFocus();
            return;
        }

        // VALIDACIÓN PRINCIPAL: El anticipo no puede ser mayor al resto a pagar
        Double restoAPagar = fiadoRecibido.getResto();
        if (dinero > restoAPagar) {
            String mensaje = String.format(
                    "El monto ingresado no puede ser mayor al resto a pagar ($%s)",
                    decimalFormatter.format(restoAPagar)
            );
            mostrarMensajeError(mensaje);
            mostrarAlerta("Error", mensaje, Alert.AlertType.WARNING);
            txtDineroIngresar.requestFocus();
            return;
        }

        // Validar medio de pago seleccionado
        String medioPago = cmbMedioPago.getValue();
        if (medioPago == null || medioPago.isEmpty()) {
            mostrarMensajeError("Debe seleccionar un medio de pago");
            mostrarAlerta("Error", "Debe seleccionar un medio de pago", Alert.AlertType.WARNING);
            cmbMedioPago.requestFocus();
            return;
        }

        if (fiadoRecibido == null) {
            mostrarMensajeError("No se ha seleccionado un fiado");
            mostrarAlerta("Error", "No se ha seleccionado un fiado", Alert.AlertType.ERROR);
            return;
        }

        // Confirmar si el pago completa la deuda
        boolean completaDeuda = dinero.equals(restoAPagar);
        if (completaDeuda) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar pago");
            confirmAlert.setHeaderText("Pago completo");
            confirmAlert.setContentText("Este pago completará la deuda. ¿Desea continuar?");

            if (confirmAlert.showAndWait().get() != javafx.scene.control.ButtonType.OK) {
                return;
            }
        }

        try {
            // Guardar anticipo
            fiadoParcialDao = new FiadoParcialDAO();
            fiadoParcial = new FiadoParcial();
            fiadoParcial.setAnticipo((double) Math.round(dinero)); // ✅ redondea y guarda sin decimales
            fiadoParcial.setFecha(LocalDateTime.now());
            fiadoParcial.setMedioAbonado(medioPago);
            fiadoParcial.setFiado(fiadoRecibido);
            fiadoParcialDao.guardar(fiadoParcial);

            System.out.println("Anticipo guardado correctamente");

            // Mostrar mensaje de éxito
            String mensajeExito = completaDeuda
                    ? "¡Pago registrado correctamente! La deuda ha sido completada."
                    : String.format("¡Anticipo de $%s registrado correctamente!", decimalFormatter.format(dinero));

            mostrarAlerta("Éxito", mensajeExito, Alert.AlertType.INFORMATION);

            cerrarOverlay();

            // Refrescar los datos en el controller principal
            if (clientesController != null) {
                clientesController.refrescarDatos();
            }

        } catch (Exception e) {
            System.err.println("Error al guardar el anticipo: " + e.getMessage());
            mostrarAlerta("Error", "Error al guardar el anticipo. Intente nuevamente.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void setClienteController(ClientesController c) {
        clientesController = c;
    }

    @FXML
    public void cerrarOverlay() {
        if (clientesController != null) {
            clientesController.CerrarDifuminarYSpa();
        }
    }

    public void ingresarTodo() {
        if (fiadoRecibido != null) {
            double resto = fiadoRecibido.getResto();
            // Redondear si quieres igual que al guardar
            resto = Math.round(resto);
            // Poner en el TextField
            txtDineroIngresar.setText(String.valueOf((int) resto));
            // Limpiar cualquier error previo y resetear estilo
            limpiarMensajeError();
            txtDineroIngresar.setStyle("");
        } else {
            mostrarMensajeError("No se ha seleccionado un fiado");
        }
    }

}
