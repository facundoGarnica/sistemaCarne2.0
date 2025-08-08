/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author garca
 */
public class Producto_menuController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private Crear_productoController crearProductoController;
    private AumentarPrecioProductoController aumentarPrecioProductoController;
    @FXML
    private AnchorPane overlayCrearProducto;
    @FXML
    private AnchorPane difuminar;
    private String guardarRuta;
    private String rutaAsociada;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void difuminarTodo() {
        difuminar.setVisible(true);
        difuminar.setDisable(false);

        overlayCrearProducto.setVisible(true);
        overlayCrearProducto.setDisable(false);

        invocarSpa();

        Platform.runLater(() -> {
            Parent root = difuminar.getScene().getRoot();

            Bounds boundsInScene = difuminar.localToScene(difuminar.getBoundsInLocal());

            SnapshotParameters params = new SnapshotParameters();
            params.setViewport(new javafx.geometry.Rectangle2D(
                    boundsInScene.getMinX(),
                    boundsInScene.getMinY(),
                    boundsInScene.getWidth(),
                    boundsInScene.getHeight()
            ));

            WritableImage snapshot = new WritableImage(
                    (int) boundsInScene.getWidth(),
                    (int) boundsInScene.getHeight()
            );

            root.snapshot(params, snapshot);

            ImageView img = new ImageView(snapshot);
            img.setFitWidth(boundsInScene.getWidth());
            img.setFitHeight(boundsInScene.getHeight());
            img.setEffect(new GaussianBlur(20));

            img.setLayoutX(0);
            img.setLayoutY(0);

            if (difuminar.getChildren().stream().noneMatch(node -> node instanceof ImageView)) {
                difuminar.getChildren().add(0, img);
            }

            difuminar.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3);");
        });
    }

    public void CerrarDifuminarYSpa() {
        System.out.println("Cerrando overlay...");
        difuminar.setDisable(true);
        difuminar.setVisible(false);

        overlayCrearProducto.setDisable(true);
        overlayCrearProducto.setVisible(false);
        overlayCrearProducto.getChildren().clear();
    }

    @FXML
    public void setRutaProducto() {
        this.guardarRuta = "/fxml/crear_producto.fxml";
        rutaAsociada = "producto";
        iniciarDifuminar();
    }

    @FXML
    public void setRutaPorcentaje() {
        this.guardarRuta = "/fxml/aumentarPrecioProducto.fxml";
        rutaAsociada = "porcentaje";
        iniciarDifuminar();
    }

    public void iniciarDifuminar() {
        if (rutaAsociada == null) {
            System.out.println("no hay ruta");
        } else {
            difuminarTodo();
        }
    }

    @FXML
    public void invocarSpa() {
        try {
            overlayCrearProducto.getChildren().clear(); // Limpiar contenido
            crearProductoController = null;
            aumentarPrecioProductoController = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource(guardarRuta));
            Parent root = loader.load();

            if ("producto".equals(rutaAsociada)) {
                crearProductoController = loader.getController();
                crearProductoController.setSpa_productoController(this);

                // Tamaño reducido
                AnchorPane.setTopAnchor(root, 168.0);
                AnchorPane.setBottomAnchor(root, 167.0);
                AnchorPane.setLeftAnchor(root, 403.0);
                AnchorPane.setRightAnchor(root, 403.0);

            } else if ("porcentaje".equals(rutaAsociada)) {
                aumentarPrecioProductoController = loader.getController();
                aumentarPrecioProductoController.setSpa_productoController(this);

                // Tamaño grande
                AnchorPane.setTopAnchor(root, 59.0);
                AnchorPane.setBottomAnchor(root, 59.0);
                AnchorPane.setLeftAnchor(root, 93.0);
                AnchorPane.setRightAnchor(root, 93.0);
            }

            overlayCrearProducto.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
