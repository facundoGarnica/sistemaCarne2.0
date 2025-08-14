/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import Util.HibernateUtil;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.hibernate.Session;
import model.Producto;

/**
 * FXML Controller class
 *
 * @author facun
 */
public class MenuController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private AnchorPane barraLateral;
    @FXML
    private AnchorPane contenidoPrincipal;
    @FXML
    private VBox menuContainer;

    // Secciones principales
    @FXML
    private VBox clienteSection;
    @FXML
    private VBox productoSection;
    @FXML
    private VBox stockSection;
    @FXML
    private VBox ventasSection;
    @FXML
    private VBox configSection;

    // Botones principales
    @FXML
    private Button btnCliente;
    @FXML
    private Button btnProducto;
    @FXML
    private Button btnStock;
    @FXML
    private Button btnVentas;
    @FXML
    private Button btnConfiguracion;

    // Submenús
    @FXML
    private VBox clienteSubmenu;
    @FXML
    private VBox productoSubmenu;
    @FXML
    private VBox stockSubmenu;
    @FXML
    private VBox ventasSubmenu;

    // Botones de submenú - Cliente
    @FXML
    private Button btnFiado;
    @FXML
    private Button btnPedido;

    // Botones de submenú - Producto
    @FXML
    private Button btnVisualizarProductos;

    // Botones de submenú - Stock
    @FXML
    private Button btnMediaRes;
    @FXML
    private Button btnPollo;
    @FXML
    private Button btnIngresarIndividual;

    // Botones de submenú - Ventas
    @FXML
    private Button btnCrearVenta;
    @FXML
    private Button btnHistorialVentas;

    //anchorPane para usarlo de Overlay o SPA
    @FXML
    private AnchorPane overlayClientes;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Esto fuerza a que Hibernate cargue las configuraciones y cree las tablas si es necesario
            HibernateUtil.getSessionFactory();
            System.out.println("Hibernate inicializado desde MenuController.");
        } catch (Exception e) {
            System.err.println("Error al inicializar Hibernate: " + e.getMessage());
        }

        // Inicializar todos los submenús como ocultos
        clienteSubmenu.setVisible(false);
        productoSubmenu.setVisible(false);
        stockSubmenu.setVisible(false);
        ventasSubmenu.setVisible(false);

        // Configurar la altura inicial de los submenús
        clienteSubmenu.setMaxHeight(0);
        productoSubmenu.setMaxHeight(0);
        stockSubmenu.setMaxHeight(0);
        ventasSubmenu.setMaxHeight(0);
    }

    // Métodos para toggle de submenús
    @FXML
    private void toggleClienteMenu() {
        toggleSubmenu(clienteSubmenu);
        // Cerrar otros submenús
        closeSubmenu(productoSubmenu);
        closeSubmenu(stockSubmenu);
        closeSubmenu(ventasSubmenu);
    }

    @FXML
    private void toggleProductoMenu() {
        toggleSubmenu(productoSubmenu);
        // Cerrar otros submenús
        closeSubmenu(clienteSubmenu);
        closeSubmenu(stockSubmenu);
        closeSubmenu(ventasSubmenu);
    }

    @FXML
    private void toggleStockMenu() {
        toggleSubmenu(stockSubmenu);
        // Cerrar otros submenús
        closeSubmenu(clienteSubmenu);
        closeSubmenu(productoSubmenu);
        closeSubmenu(ventasSubmenu);
    }

    @FXML
    private void toggleVentasMenu() {
        toggleSubmenu(ventasSubmenu);
        // Cerrar otros submenús
        closeSubmenu(clienteSubmenu);
        closeSubmenu(productoSubmenu);
        closeSubmenu(stockSubmenu);
    }

    // Método auxiliar para toggle de submenús con animación
    private void toggleSubmenu(VBox submenu) {
        if (submenu.isVisible()) {
            closeSubmenu(submenu);
        } else {
            openSubmenu(submenu);
        }
    }

    private void openSubmenu(VBox submenu) {
        submenu.setVisible(true);
        submenu.setMaxHeight(VBox.USE_COMPUTED_SIZE);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), submenu);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void closeSubmenu(VBox submenu) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), submenu);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            submenu.setVisible(false);
            submenu.setMaxHeight(0);
        });
        fadeOut.play();
    }

    // Handlers para Cliente
    @FXML
    private void handleFiado() {
        System.out.println("Navegando a Fiado...");
        // Método duplicado será manejado por handleFiado(ActionEvent event)
    }

    @FXML
    private void handlePedido() {
        System.out.println("Navegando a Pedido...");
        // Método duplicado será manejado por handlePedido(ActionEvent event)
    }

    // Handlers para Producto
    @FXML
    private void handleVisualizarProductos() {
        try {
            // Cargar producto_menu.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/producto_menu.fxml"));
            AnchorPane vistaProductos = loader.load();

            // Limpiar lo anterior y añadir la nueva vista
            overlayClientes.getChildren().clear();
            overlayClientes.getChildren().add(vistaProductos);

            // Hacer que se ajuste al tamaño del overlay
            AnchorPane.setTopAnchor(vistaProductos, 0.0);
            AnchorPane.setBottomAnchor(vistaProductos, 0.0);
            AnchorPane.setLeftAnchor(vistaProductos, 0.0);
            AnchorPane.setRightAnchor(vistaProductos, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Handlers para Stock
    @FXML
    private void handleMediaRes() {
        try {
            // Cargar producto_menu.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/stockMediaRes.fxml"));
            AnchorPane vistaProductos = loader.load();

            // Limpiar lo anterior y añadir la nueva vista
            overlayClientes.getChildren().clear();
            overlayClientes.getChildren().add(vistaProductos);

            // Hacer que se ajuste al tamaño del overlay
            AnchorPane.setTopAnchor(vistaProductos, 0.0);
            AnchorPane.setBottomAnchor(vistaProductos, 0.0);
            AnchorPane.setLeftAnchor(vistaProductos, 0.0);
            AnchorPane.setRightAnchor(vistaProductos, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePollo() {
        try {
            // Cargar producto_menu.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/stockCajonPollo.fxml"));
            AnchorPane vistaProductos = loader.load();

            // Limpiar lo anterior y añadir la nueva vista
            overlayClientes.getChildren().clear();
            overlayClientes.getChildren().add(vistaProductos);

            // Hacer que se ajuste al tamaño del overlay
            AnchorPane.setTopAnchor(vistaProductos, 0.0);
            AnchorPane.setBottomAnchor(vistaProductos, 0.0);
            AnchorPane.setLeftAnchor(vistaProductos, 0.0);
            AnchorPane.setRightAnchor(vistaProductos, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleIngresarIndividual() {
        try {
            // Cargar producto_menu.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/stockProducto.fxml"));
            AnchorPane vistaProductos = loader.load();

            // Limpiar lo anterior y añadir la nueva vista
            overlayClientes.getChildren().clear();
            overlayClientes.getChildren().add(vistaProductos);

            // Hacer que se ajuste al tamaño del overlay
            AnchorPane.setTopAnchor(vistaProductos, 0.0);
            AnchorPane.setBottomAnchor(vistaProductos, 0.0);
            AnchorPane.setLeftAnchor(vistaProductos, 0.0);
            AnchorPane.setRightAnchor(vistaProductos, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Handlers para Ventas
    @FXML
    private void handleCrearVenta(ActionEvent event) {
        try {
            // Cargar el archivo FXML de la nueva ventana
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crear_ventas.fxml"));
            Parent root = loader.load();

            // Obtener la resolución de la pantalla
            Screen screen = Screen.getPrimary();
            double width = screen.getVisualBounds().getWidth();  // Ancho de la pantalla
            double height = screen.getVisualBounds().getHeight(); // Alto de la pantalla

            // Crear la escena y agregarla al escenario con tamaño completo
            Scene scene = new Scene(root, width, height);

            // Crear una nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setTitle("Crear Ventas"); // Título de la ventana
            stage.setScene(scene);
            stage.setMaximized(true); // Maximizar la ventana para que ocupe toda la pantalla

            stage.show(); // Mostrar la nueva ventana

            // Cerrar la ventana actual (la ventana de la que se hizo clic)
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close(); // Cerrar la ventana actual

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHistorialVentas() {
        try {
            // Cargar producto_menu.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/historialVentas.fxml"));
            AnchorPane vistaProductos = loader.load();

            // Limpiar lo anterior y añadir la nueva vista
            overlayClientes.getChildren().clear();
            overlayClientes.getChildren().add(vistaProductos);

            // Hacer que se ajuste al tamaño del overlay
            AnchorPane.setTopAnchor(vistaProductos, 0.0);
            AnchorPane.setBottomAnchor(vistaProductos, 0.0);
            AnchorPane.setLeftAnchor(vistaProductos, 0.0);
            AnchorPane.setRightAnchor(vistaProductos, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Handler para Configuración
    @FXML
    private void handleConfiguracion() {
        System.out.println("Navegando a Configuración...");
        // loadView("configuracion.fxml");
    }

    // Método helper para cargar vistas (implementar según tu arquitectura)
    private void loadView(String fxmlFile) {
        try {
            // Aquí implementarías la lógica para cargar las diferentes vistas
            // en el contenidoPrincipal
            System.out.println("Cargando vista: " + fxmlFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //metodo para invocar la ventana cliente en el overlay
    @FXML
    private void handleFiado(ActionEvent event) {
        try {
            // Cargar clientes.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/clientes.fxml"));
            AnchorPane vistaClientes = loader.load();

            // Limpiar lo anterior y añadir la nueva vista
            overlayClientes.getChildren().clear();
            overlayClientes.getChildren().add(vistaClientes);

            // Hacer que se ajuste al tamaño del overlay
            AnchorPane.setTopAnchor(vistaClientes, 0.0);
            AnchorPane.setBottomAnchor(vistaClientes, 0.0);
            AnchorPane.setLeftAnchor(vistaClientes, 0.0);
            AnchorPane.setRightAnchor(vistaClientes, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //metodo para invocar la ventana cliente pedido en el overlay
    @FXML
    private void handlePedido(ActionEvent event) {
        try {
            // Cargar cliente_pedido.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cliente_pedido.fxml"));
            AnchorPane vistaClientes = loader.load();

            // Limpiar lo anterior y añadir la nueva vista
            overlayClientes.getChildren().clear();
            overlayClientes.getChildren().add(vistaClientes);

            // Hacer que se ajuste al tamaño del overlay
            AnchorPane.setTopAnchor(vistaClientes, 0.0);
            AnchorPane.setBottomAnchor(vistaClientes, 0.0);
            AnchorPane.setLeftAnchor(vistaClientes, 0.0);
            AnchorPane.setRightAnchor(vistaClientes, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
