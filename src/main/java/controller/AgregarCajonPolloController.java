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
public class AgregarCajonPolloController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private StockCajonPolloController stockCajonPolloController;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    public void setSpa_stockCajonPolloController(StockCajonPolloController s){
        stockCajonPolloController = s;
    }
    @FXML
    public void cerrarOverlay() {
        if (stockCajonPolloController != null) {
            stockCajonPolloController.CerrarDifuminarYSpa();
        }
    }
}
