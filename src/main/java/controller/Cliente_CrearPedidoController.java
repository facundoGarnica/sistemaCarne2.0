/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

/**
 * FXML Controller class
 *
 * @author facun
 */
public class Cliente_CrearPedidoController implements Initializable {

    /**
     * Initializes the controller class.
     */
    
    @FXML
    private ComboBox cmbHoraEntrega;
    private Cliente_pedidoController clientePedidoController;
    @FXML
    private Label lblCantidad;
    @FXML
    private TextField txtCantidad;
    @FXML
    private Spinner<Integer> spinnerUnidades;
    @FXML
    private ToggleGroup grupoMedida;
    @FXML
    private RadioButton radioUnidad;
    @FXML
    private RadioButton radioKilos;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configurar el spinner (rango de 1 a 99, valor inicial 1)
        SpinnerValueFactory<Integer> valueFactory
                = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1);
        spinnerUnidades.setValueFactory(valueFactory);

        // Listener para cambiar entre TextField y Spinner
        grupoMedida.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == radioKilos) {
                lblCantidad.setText("Cantidad:");
                txtCantidad.setVisible(true);
                spinnerUnidades.setVisible(false);
            } else if (newToggle == radioUnidad) {
                lblCantidad.setText("Unidad:");
                txtCantidad.setVisible(false);
                spinnerUnidades.setVisible(true);
            }
        });
        cmbHoraEntrega.getItems().addAll(
                "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
                "11:00", "11:30", "12:00", "12:30", "13:00",
                "17:00", "17:30", "18:00", "18:30", "19:00", "19:30",
                "20:00", "20:30", "21:00"
        );

        // Valor por defecto
        cmbHoraEntrega.setValue("08:00");
    }

    public void setSpa_clientePedidoController(Cliente_pedidoController c) {
        clientePedidoController = c;
    }

    @FXML
    public void cerrarOverlay() {
        if (clientePedidoController != null) {
            clientePedidoController.CerrarDifuminarYSpa();
        }
    }

}
