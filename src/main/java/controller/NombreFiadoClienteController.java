/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 * FXML Controller class
 *
 * @author garca
 */
public class NombreFiadoClienteController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private Crear_ventasController crearVentasController;

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
}
