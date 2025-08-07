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
 * @author facun
 */
public class Cliente_CrearPedidoController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private Cliente_pedidoController clientePedidoController;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
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
