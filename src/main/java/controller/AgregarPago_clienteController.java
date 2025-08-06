/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * FXML Controller class
 *
 * @author garca
 */
public class AgregarPago_clienteController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private ComboBox<String> cmbMedioPago;
    @FXML
    private Label lblFecha;
    
    @FXML
    private Spa_clientesController spaClienteController;
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
    }
    
    public void setSpa_ClientesController(Spa_clientesController c) {
        spaClienteController = c;
    }
    
    @FXML
    public void cerrarOverlay() {
        if (spaClienteController != null) {
            spaClienteController.CerrarDifuminarYSpa();
        }
    }
}
