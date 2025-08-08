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

/**
 * FXML Controller class
 *
 * @author garca
 */

public class Crear_productoController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private ComboBox cbxCategoria;
    private Producto_menuController productoMenuController;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbxCategoria.getItems().addAll(
                "Carniceria","Cerdo","Pollo","Varios","Seco","Preparados"
        );

        // Valor por defecto
        cbxCategoria.setValue("Carniceria");
    }    
    
    public void setSpa_productoController(Producto_menuController p){
        productoMenuController = p;
    }
    @FXML
    public void cerrarOverlay() {
        if (productoMenuController != null) {
            productoMenuController.CerrarDifuminarYSpa();
        }
    }
}
