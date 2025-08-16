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
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
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
// Handlers para Stock
import javafx.scene.control.Alert;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.animation.ParallelTransition;

/**
 * FXML Controller class - Sistema de Ventas Moderno
 *
 * @author facun
 */
public class MenuController implements Initializable {

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

    // Botones principales
    @FXML
    private Button btnCliente;
    @FXML
    private Button btnProducto;
    @FXML
    private Button btnStock;
    @FXML
    private Button btnVentas;

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
            // Inicializar Hibernate
            HibernateUtil.getSessionFactory();
            System.out.println("Hibernate inicializado desde MenuController.");
        } catch (Exception e) {
            System.err.println("Error al inicializar Hibernate: " + e.getMessage());
        }

        // Inicializar todos los submenús como ocultos
        initializeSubmenus();
        
        // Configurar efectos hover para botones principales
        setupHoverEffects();
    }

    /**
     * Inicializa todos los submenús en estado oculto
     */
    private void initializeSubmenus() {
        VBox[] submenus = {clienteSubmenu, productoSubmenu, stockSubmenu, ventasSubmenu};
        
        for (VBox submenu : submenus) {
            submenu.setVisible(false);
            submenu.setMaxHeight(0);
            submenu.setOpacity(0);
        }
    }

    /**
     * Configura efectos hover para los botones principales
     */
    private void setupHoverEffects() {
        Button[] mainButtons = {btnCliente, btnProducto, btnStock, btnVentas};
        
        for (Button btn : mainButtons) {
            btn.setOnMouseEntered(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(150), btn);
                scale.setToX(1.05);
                scale.setToY(1.05);
                scale.play();
            });
            
            btn.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(150), btn);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
            });
        }
    }

    // Métodos para toggle de submenús con animaciones mejoradas
    @FXML
    private void toggleClienteMenu() {
        toggleSubmenu(clienteSubmenu);
        closeOtherSubmenus(clienteSubmenu);
    }

    @FXML
    private void toggleProductoMenu() {
        toggleSubmenu(productoSubmenu);
        closeOtherSubmenus(productoSubmenu);
    }

    @FXML
    private void toggleStockMenu() {
        toggleSubmenu(stockSubmenu);
        closeOtherSubmenus(stockSubmenu);
    }

    @FXML
    private void toggleVentasMenu() {
        toggleSubmenu(ventasSubmenu);
        closeOtherSubmenus(ventasSubmenu);
    }

    /**
     * Cierra todos los submenús excepto el especificado
     */
    private void closeOtherSubmenus(VBox excludeSubmenu) {
        VBox[] submenus = {clienteSubmenu, productoSubmenu, stockSubmenu, ventasSubmenu};
        
        for (VBox submenu : submenus) {
            if (submenu != excludeSubmenu && submenu.isVisible()) {
                closeSubmenu(submenu);
            }
        }
    }

    /**
     * Toggle submenu con animaciones suaves
     */
    private void toggleSubmenu(VBox submenu) {
        if (submenu.isVisible()) {
            closeSubmenu(submenu);
        } else {
            openSubmenu(submenu);
        }
    }

    /**
     * Abre submenu con animación
     */
    private void openSubmenu(VBox submenu) {
        submenu.setVisible(true);
        submenu.setMaxHeight(VBox.USE_COMPUTED_SIZE);

        // Animación de fade y slide
        FadeTransition fade = new FadeTransition(Duration.millis(300), submenu);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(300), submenu);
        slide.setFromY(-20);
        slide.setToY(0);

        ParallelTransition animation = new ParallelTransition(fade, slide);
        animation.play();
    }

    /**
     * Cierra submenu con animación
     */
    private void closeSubmenu(VBox submenu) {
        FadeTransition fade = new FadeTransition(Duration.millis(200), submenu);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(200), submenu);
        slide.setFromY(0);
        slide.setToY(-10);

        ParallelTransition animation = new ParallelTransition(fade, slide);
        animation.setOnFinished(e -> {
            submenu.setVisible(false);
            submenu.setMaxHeight(0);
        });
        animation.play();
    }

    /**
     * Carga una vista en el overlay con animación
     */
    private void loadViewInOverlay(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane vista = loader.load();

            // Hacer visible el overlay si estaba oculto
            overlayClientes.setVisible(true);
            
            // Limpiar contenido anterior
            overlayClientes.getChildren().clear();
            overlayClientes.getChildren().add(vista);

            // Ajustar al tamaño del contenedor
            AnchorPane.setTopAnchor(vista, 0.0);
            AnchorPane.setBottomAnchor(vista, 0.0);
            AnchorPane.setLeftAnchor(vista, 0.0);
            AnchorPane.setRightAnchor(vista, 0.0);

            // Animación de entrada
            vista.setOpacity(0);
            vista.setTranslateX(50);
            
            FadeTransition fade = new FadeTransition(Duration.millis(400), vista);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);

            TranslateTransition slide = new TranslateTransition(Duration.millis(400), vista);
            slide.setFromX(50);
            slide.setToX(0);

            ParallelTransition entrance = new ParallelTransition(fade, slide);
            entrance.play();

            System.out.println("Vista cargada: " + title);

        } catch (IOException e) {
            System.err.println("Error cargando vista: " + fxmlPath);
            e.printStackTrace();
            showErrorAlert("Error", "No se pudo cargar la vista: " + title);
        }
    }

    // Handlers para Cliente
    @FXML
    private void handleFiado(ActionEvent event) {
        loadViewInOverlay("/fxml/clientes.fxml", "Gestión de Fiado");
    }

    @FXML
    private void handlePedido(ActionEvent event) {
        loadViewInOverlay("/fxml/cliente_pedido.fxml", "Gestión de Pedidos");
    }

    // Handlers para Producto
    @FXML
    private void handleVisualizarProductos() {
        loadViewInOverlay("/fxml/producto_menu.fxml", "Catálogo de Productos");
    }

    // Handlers para Stock
    @FXML
    private void handleMediaRes() {
        if (validarContrasena()) {
            loadViewInOverlay("/fxml/stockMediaRes.fxml", "Stock Media Res");
        }
    }

    @FXML
    private void handlePollo() {
        if (validarContrasena()) {
            loadViewInOverlay("/fxml/stockCajonPollo.fxml", "Stock Pollo");
        }
    }

    @FXML
    private void handleIngresarIndividual() {
        loadViewInOverlay("/fxml/stockProducto.fxml", "Ingreso Individual de Stock");
    }

    // Handlers para Ventas
    @FXML
    private void handleCrearVenta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crear_ventas.fxml"));
            Parent root = loader.load();

            Screen screen = Screen.getPrimary();
            double width = screen.getVisualBounds().getWidth();
            double height = screen.getVisualBounds().getHeight();

            Scene scene = new Scene(root, width, height);
            Stage stage = new Stage();
            stage.setTitle("Crear Ventas - Sistema Moderno");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

            // Cerrar ventana actual
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error", "No se pudo abrir la ventana de ventas");
        }
    }

    @FXML
    private void handleHistorialVentas() {
        if (validarContrasena()) {
            loadViewInOverlay("/fxml/historialVentas.fxml", "Historial y Reportes");
        }
    }

    /**
     * Valida contraseña para acceso restringido
     */
    private boolean validarContrasena() {
        final String CONTRASENA_CORRECTA = "FordFocus2808#";

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("🔐 Acceso Restringido");
        dialog.setHeaderText("Se requiere autorización para acceder a esta sección");

        ButtonType loginButtonType = new ButtonType("Acceder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Ingrese la contraseña...");
        passwordField.setPrefWidth(250);

        VBox vbox = new VBox(15);
        vbox.getChildren().addAll(
            new Label("🔑 Contraseña de administrador:"), 
            passwordField
        );
        dialog.getDialogPane().setContent(vbox);

        Platform.runLater(() -> passwordField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return passwordField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            if (CONTRASENA_CORRECTA.equals(result.get())) {
                showInfoAlert("✅ Acceso Autorizado", "Bienvenido administrador");
                return true;
            } else {
                showErrorAlert("❌ Acceso Denegado", "La contraseña ingresada es incorrecta.");
                return false;
            }
        }

        return false;
    }

    /**
     * Muestra alerta de error
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Muestra alerta informativa
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}