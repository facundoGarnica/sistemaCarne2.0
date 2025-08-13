/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import dao.CajonPolloDAO;
import dao.MediaResDAO;
import dao.StockDAO;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import model.CajonPollo;
import model.MediaRes;

/**
 * FXML Controller class
 *
 * @author garca
 */
public class StockCajonPolloController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private AgregarCajonPolloController agregarCajonPolloController;
    private CajonPollo cajonPollo;
    private List<CajonPollo> listaPollos;
    private CajonPolloDAO cajonPolloDao;
    @FXML
    private AnchorPane overlayPollo;
    @FXML
    private AnchorPane difuminar;
    @FXML
    private TableView<CajonPollo> tblRegistroCajonPollo;

    @FXML
    private TableColumn<CajonPollo, Double> colPrecioCajon;

    @FXML
    private TableColumn<CajonPollo, String> colProveedor;

    @FXML
    private TableColumn<CajonPollo, Double> colPeso;

    @FXML
    private TableColumn<CajonPollo, Double> colPrecioPorKg;

    @FXML
    private TableColumn<CajonPollo, String> colFecha;
    
    @FXML
    private TableView tblStockProductos;
    
    @FXML
    private TableColumn<CajonPollo, String> colNombreProducto;
    @FXML
    private TableColumn<CajonPollo, Double> colKilosObtenidos;
    @FXML
    private TableColumn<CajonPollo, Double> colPorcentajeCajon;
    @FXML
    private TableColumn<CajonPollo, Double> colStockFinal;
    @FXML
    private TableColumn<CajonPollo, Double> colPrecioVenta;
    @FXML
    private TableColumn<CajonPollo, Double> colValorStock;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cajonPolloDao = new CajonPolloDAO();
        // Columna precio cajón directo
        colPrecioCajon.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedor"));
        // Columna peso cajón directo
        colPeso.setCellValueFactory(new PropertyValueFactory<>("pesoCajon"));

        // Columna precio por kilo calculada: precio / peso
        colPrecioPorKg.setCellValueFactory(cellData -> {
            CajonPollo cajon = cellData.getValue();
            Double precio = cajon.getPrecio();
            Double peso = cajon.getPesoCajon();
            double precioPorKg = (peso != null && peso != 0 && precio != null) ? precio / peso : 0;
            return new javafx.beans.property.SimpleDoubleProperty(precioPorKg).asObject();
        });

        // Formatear con 2 decimales la columna precioPorKg
        DecimalFormat df = new DecimalFormat("#.00");
        colPrecioPorKg.setCellFactory(column -> new TableCell<CajonPollo, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(df.format(item));
                }
            }
        });

        // Columna fecha con formateo "dd/MM/yyyy"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colFecha.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFecha() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFecha().format(formatter));
            } else {
                return new javafx.beans.property.SimpleStringProperty("");
            }
        });
        recargarTablaProductos();
    }

    public void recargarTablaProductos() {
        listaPollos = cajonPolloDao.buscarTodos(); // Trae productos actualizados
        tblRegistroCajonPollo.setItems(FXCollections.observableArrayList(listaPollos));
        tblRegistroCajonPollo.refresh();
    }

    public void difuminarTodo() {
        difuminar.setVisible(true);
        difuminar.setDisable(false);

        overlayPollo.setVisible(true);
        overlayPollo.setDisable(false);

        invocarSpaCrearPedido();

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

        overlayPollo.setDisable(true);
        overlayPollo.setVisible(false);
    }

    @FXML
    public void invocarSpaCrearPedido() {  //llama a detalle de fiados
        try {
            overlayPollo.getChildren().clear(); // Limpiar el AnchorPane destino

            // Cargar el FXML spa_clientes.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/agregarCajonPollo.fxml"));
            Parent root = loader.load();
            agregarCajonPolloController = loader.getController();
            agregarCajonPolloController.setSpa_stockCajonPolloController(this);
            // controller.setClientesController(this); // Por ejemplo
            // Insertar el contenido cargado en el AnchorPane
            overlayPollo.getChildren().add(root);

            // Hacer que el contenido cargado se ajuste al tamaño del AnchorPane
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void eliminarPollo() {
        CajonPollo seleccionada = tblRegistroCajonPollo.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Eliminar Cajon pollo");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor seleccione un cajon res para eliminar.");
            alerta.showAndWait();
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar este cajon?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        // Botones personalizados
        ButtonType botonSi = new ButtonType("Sí, eliminar");
        ButtonType botonNo = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirmacion.getButtonTypes().setAll(botonSi, botonNo);

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == botonSi) {
            // Restar stock según detalles de la media res
            StockDAO stockDao = new StockDAO();
            stockDao.restarStockPorCajonPollo(seleccionada);

            // Luego eliminar la media res
            cajonPolloDao = new CajonPolloDAO();
            cajonPolloDao.eliminar(seleccionada.getId());

            recargarTablaProductos();
        }
    }
    
}
