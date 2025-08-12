/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import dao.DetalleMediaResDAO;
import dao.MediaResDAO;
import dao.StockDAO;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
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
import model.DetalleMediaRes;
import model.MediaRes;
import model.Producto;
import model.Stock;

/**
 * FXML Controller class
 *
 * @author garca
 */
public class StockMediaResController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private AgregarMediaResController agregarMediaResController;
    @FXML
    private AnchorPane overlayAgregarMedia;
    @FXML
    private AnchorPane difuminar;
    private MediaRes mediaRes;
    private MediaResDAO mediaResDao;
    private DetalleMediaResDAO detalleMediaResDao;
    private List<MediaRes> listaMediasRes;
    private MediaRes seleccionada;
    //tabla
    @FXML
    private TableView<MediaRes> tblRegistroMediaRes;
    @FXML
    private TableColumn<MediaRes, String> colProveedor;
    @FXML
    private TableColumn<MediaRes, Double> colPesoBalanza;
    @FXML
    private TableColumn<MediaRes, Double> colPesoBoleta;
    @FXML
    private TableColumn<MediaRes, Double> colPesoFinal;
    @FXML
    private TableColumn<MediaRes, Double> colPrecioPorKg;
    @FXML
    private TableColumn<MediaRes, Double> colTotal;
    @FXML
    private TableColumn<MediaRes, LocalDateTime> colFecha;

    @FXML
    private TableView tblStockProductos;
    @FXML
    private TableColumn<DetalleMediaRes, String> colNombreProducto;
    @FXML
    private TableColumn<DetalleMediaRes, String> colKilosMedia;
    @FXML
    private TableColumn<DetalleMediaRes, Double> colPorcentajeMedia;
    @FXML
    private TableColumn<DetalleMediaRes, Double> colStockAgregado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mediaResDao = new MediaResDAO();
        // Configurar columnas
        colProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedor"));
        colPesoBoleta.setCellValueFactory(new PropertyValueFactory<>("pesoBoleta"));
        colPrecioPorKg.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPesoBalanza.setCellValueFactory(new PropertyValueFactory<>("pesoPilon"));
        colPesoFinal.setCellValueFactory(new PropertyValueFactory<>("pesoFinal"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colFecha.setCellFactory(column -> {
            return new TableCell<MediaRes, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(formatter));
                    }
                }
            };
        });
        colTotal.setCellValueFactory(cellData -> {
            MediaRes media = cellData.getValue();
            double total = media.getPesoFinal() * media.getPrecio();
            return new javafx.beans.property.SimpleDoubleProperty(total).asObject();
        });

        // Nombre del producto
        colNombreProducto.setCellValueFactory(data
                -> new SimpleStringProperty(data.getValue().getNombreProducto())
        );

        // Kilos en media = pesoFinal de la media * porcentajeCorte / 100
        colKilosMedia.setCellValueFactory(data -> {
            DetalleMediaRes detalle = data.getValue();
            double kilos = detalle.getMediaRes().getPesoFinal() * (detalle.getPorcentajeCorte() / 100.0);
            return new SimpleStringProperty(String.format("%.2f", kilos));
        });

        // Porcentaje en media
        colPorcentajeMedia.setCellValueFactory(data
                -> new SimpleDoubleProperty(data.getValue().getPorcentajeCorte()).asObject()
        );

        // Stock agregado
        colStockAgregado.setCellValueFactory(data -> {
            DetalleMediaRes detalle = data.getValue();
            double stockCantidad = obtenerCantidadStock(detalle.getProducto());
            return new SimpleDoubleProperty(stockCantidad).asObject();
        });
        tblRegistroMediaRes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            cargarDetallesDeMediaSeleccionada();
        });

        recargarTablaProductos();
    }

    private double obtenerCantidadStock(Producto producto) {
        StockDAO stockDao = new StockDAO();
        Stock stock = stockDao.obtenerPorProducto(producto.getId());
        return stock != null ? stock.getCantidad() : 0.0;
    }

    public void eliminar() {
        MediaRes seleccionada = tblRegistroMediaRes.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Eliminar Media Res");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor seleccione una media res para eliminar.");
            alerta.showAndWait();
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar esta media res?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        // Botones personalizados
        ButtonType botonSi = new ButtonType("Sí, eliminar");
        ButtonType botonNo = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirmacion.getButtonTypes().setAll(botonSi, botonNo);

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == botonSi) {
            // Restar stock según detalles de la media res
            StockDAO stockDao = new StockDAO();
            stockDao.restarStockPorMediaRes(seleccionada);

            // Luego eliminar la media res
            mediaResDao = new MediaResDAO();
            mediaResDao.eliminar(seleccionada.getId());

            recargarTablaProductos();
        }
    }

    public void recargarTablaProductos() {
        listaMediasRes = mediaResDao.buscarTodos(); // Trae productos actualizados
        tblRegistroMediaRes.setItems(FXCollections.observableArrayList(listaMediasRes));
        tblRegistroMediaRes.refresh();
    }

    public void difuminarTodo() {
        difuminar.setVisible(true);
        difuminar.setDisable(false);

        overlayAgregarMedia.setVisible(true);
        overlayAgregarMedia.setDisable(false);

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

        overlayAgregarMedia.setDisable(true);
        overlayAgregarMedia.setVisible(false);
    }

    @FXML
    public void invocarSpaCrearPedido() {  //llama a detalle de fiados
        try {
            overlayAgregarMedia.getChildren().clear(); // Limpiar el AnchorPane destino

            // Cargar el FXML spa_clientes.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/agregarMediaRes.fxml"));
            Parent root = loader.load();
            agregarMediaResController = loader.getController();
            agregarMediaResController.setSpa_stockMediaResController(this);
            // controller.setClientesController(this); // Por ejemplo
            // Insertar el contenido cargado en el AnchorPane
            overlayAgregarMedia.getChildren().add(root);

            // Hacer que el contenido cargado se ajuste al tamaño del AnchorPane
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void cargarDetallesDeMediaSeleccionada() {
        seleccionada = tblRegistroMediaRes.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            tblStockProductos.setItems(FXCollections.emptyObservableList());
            return;
        }

        detalleMediaResDao = new DetalleMediaResDAO();
        List<DetalleMediaRes> detalles = detalleMediaResDao.obtenerPorMediaRes(seleccionada.getId());
        tblStockProductos.setItems(FXCollections.observableArrayList(detalles));
    }

}
