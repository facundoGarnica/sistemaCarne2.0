/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import dao.CajonPolloDAO;
import dao.DetalleCajonPolloDAO;
import dao.MediaResDAO;
import dao.StockDAO;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import model.CajonPollo;
import model.DetalleCajonPollo;
import model.MediaRes;
import model.Producto;
import model.Stock;

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
    private DetalleCajonPolloDAO detalleCajonPolloDao;
    private CajonPollo seleccionado;
    private Double sumaGanancia = 0.0;
    @FXML
    private DatePicker datePickerFecha;
    @FXML
    private DatePicker datePickerDesde;
    @FXML
    private DatePicker datePickerHasta;
    @FXML
    private Label lblGanancia;
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
    private TableView<DetalleCajonPollo> tblStockProductos;

    @FXML
    private TableColumn<DetalleCajonPollo, String> colNombreProducto;
    @FXML
    private TableColumn<DetalleCajonPollo, Double> colKilosObtenidos;
    @FXML
    private TableColumn<DetalleCajonPollo, Double> colPorcentajeCajon;
    @FXML
    private TableColumn<DetalleCajonPollo, Double> colStockFinal;
    @FXML
    private TableColumn<DetalleCajonPollo, Double> colPrecioVenta;
    @FXML
    private TableColumn<DetalleCajonPollo, Double> colValorStock;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cajonPolloDao = new CajonPolloDAO();
        detalleCajonPolloDao = new DetalleCajonPolloDAO();

        // Configuración de la primera tabla (Cajones de Pollo)
        colPrecioCajon.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedor"));
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

        // Configuración de la segunda tabla (Detalles de Productos)
        // Nombre del producto
        colNombreProducto.setCellValueFactory(data
                -> new SimpleStringProperty(data.getValue().getNombreProducto())
        );

        // Kilos obtenidos = pesoCajon * porcentajeCorte / 100
        colKilosObtenidos.setCellValueFactory(data -> {
            DetalleCajonPollo detalle = data.getValue();
            double kilos = detalle.getCajonPollo().getPesoCajon() * (detalle.getPorcentajeCorte() / 100.0);
            return new SimpleDoubleProperty(kilos).asObject();
        });

        // Porcentaje del cajón
        colPorcentajeCajon.setCellValueFactory(data
                -> new SimpleDoubleProperty(data.getValue().getPorcentajeCorte()).asObject()
        );

        // Stock final (actual del producto)
        colStockFinal.setCellValueFactory(data -> {
            DetalleCajonPollo detalle = data.getValue();
            double stockCantidad = obtenerCantidadStock(detalle.getProducto());
            return new SimpleDoubleProperty(stockCantidad).asObject();
        });

        // Precio de venta
        colPrecioVenta.setCellValueFactory(data
                -> new SimpleDoubleProperty(data.getValue().getProducto().getPrecio()).asObject()
        );

        // Valor del stock (calculado: stockFinal * precioVenta)
        colValorStock.setCellValueFactory(data -> {
            DetalleCajonPollo detalle = data.getValue();
            double stockCantidad = obtenerCantidadStock(detalle.getProducto());
            double valorStock = stockCantidad * detalle.getProducto().getPrecio();
            return new SimpleDoubleProperty(valorStock).asObject();
        });

        // Formatear columnas numéricas con 2 decimales
        DecimalFormat dfDetalle = new DecimalFormat("#0.00");

        // Formatear kilos obtenidos
        colKilosObtenidos.setCellFactory(column -> new TableCell<DetalleCajonPollo, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dfDetalle.format(item) + " kg");
                }
            }
        });

        // Formatear porcentaje del cajón
        colPorcentajeCajon.setCellFactory(column -> new TableCell<DetalleCajonPollo, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dfDetalle.format(item) + "%");
                }
            }
        });

        // Formatear stock final
        colStockFinal.setCellFactory(column -> new TableCell<DetalleCajonPollo, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dfDetalle.format(item) + " kg");
                }
            }
        });

        // Formatear precio de venta
        colPrecioVenta.setCellFactory(column -> new TableCell<DetalleCajonPollo, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("$" + dfDetalle.format(item));
                }
            }
        });

        // Formatear valor del stock
        colValorStock.setCellFactory(column -> new TableCell<DetalleCajonPollo, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("$" + dfDetalle.format(item));
                }
            }
        });

        // Listener para cargar detalles cuando se selecciona un cajón
        tblRegistroCajonPollo.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            cargarDetallesDeCajonSeleccionado();
        });

        recargarTablaProductos();
    }

    // Método para obtener stock como en MediaRes
    private double obtenerCantidadStock(Producto producto) {
        StockDAO stockDao = new StockDAO();
        Stock stock = stockDao.obtenerPorProducto(producto.getId());
        return stock != null ? stock.getCantidad() : 0.0;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // Método para cargar detalles del cajón seleccionado
    @FXML
    public void cargarDetallesDeCajonSeleccionado() {
        seleccionado = tblRegistroCajonPollo.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            tblStockProductos.setItems(FXCollections.emptyObservableList());
            sumaGanancia = 0.0;
            lblGanancia.setText("Ganancia: $0.00");
            return;
        }

        List<DetalleCajonPollo> detalles = detalleCajonPolloDao.obtenerPorCajonPollo(seleccionado.getId());
        ObservableList<DetalleCajonPollo> listaDetalles = FXCollections.observableArrayList(detalles);
        tblStockProductos.setItems(listaDetalles);

        // Total en dinero (precio de venta de todo el stock)
        double totalStockEnDinero = listaDetalles.stream()
                .mapToDouble(detalle -> {
                    double stockCantidad = obtenerCantidadStock(detalle.getProducto());
                    return stockCantidad * detalle.getProducto().getPrecio();
                })
                .sum();

        // Costo original del cajón
        double costoCajon = seleccionado.getPrecio();

        // Ganancia = venta - costo
        sumaGanancia = totalStockEnDinero - costoCajon;

        lblGanancia.setText(String.format("Ganancia: $%.2f", sumaGanancia));
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
            alerta.setContentText("Por favor seleccione un cajon pollo para eliminar.");
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
            // Restar stock según detalles del cajón de pollo
            StockDAO stockDao = new StockDAO();
            stockDao.restarStockPorCajonPollo(seleccionada);

            // Luego eliminar el cajón de pollo
            cajonPolloDao.eliminar(seleccionada.getId());

            recargarTablaProductos();
        }
    }

    public void buscarMediaPorFecha() {
        LocalDate fechaSeleccionada = datePickerFecha.getValue();

        if (fechaSeleccionada == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Buscar por fecha");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor seleccione una fecha.");
            alerta.showAndWait();
            return;
        }

        // Traer resultados del DAO
        List<CajonPollo> resultados = cajonPolloDao.buscarPorFecha(fechaSeleccionada);

        if (resultados == null || resultados.isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Sin resultados");
            alerta.setHeaderText(null);
            alerta.setContentText("No se encontraron medias res para la fecha seleccionada.");
            alerta.showAndWait();
            tblRegistroCajonPollo.setItems(FXCollections.emptyObservableList());
        } else {
            tblRegistroCajonPollo.setItems(FXCollections.observableArrayList(resultados));
        }

        tblRegistroCajonPollo.refresh();
    }

    public void buscarMediaPorRango() {
        LocalDate fechaInicio = datePickerDesde.getValue();
        LocalDate fechaFin = datePickerHasta.getValue();

        if (fechaInicio == null || fechaFin == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Búsqueda por rango", "Debe seleccionar ambas fechas.");
            return;
        }

        if (fechaInicio.isAfter(fechaFin)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Búsqueda por rango", "La fecha de inicio no puede ser posterior a la fecha de fin.");
            return;
        }

        List<CajonPollo> resultados = cajonPolloDao.buscarEntreFechas(fechaInicio, fechaFin);

        if (resultados == null || resultados.isEmpty()) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Sin resultados", "No se encontraron medias res en el rango seleccionado.");
            tblRegistroCajonPollo.getItems().clear();
        } else {
            tblRegistroCajonPollo.setItems(FXCollections.observableArrayList(resultados));
        }
    }

    public void mostrarTodo() {
        CajonPolloDAO dao = new CajonPolloDAO();
        List<CajonPollo> lista = dao.buscarTodos(); // Paso 1: traer todos los registros

        ObservableList<CajonPollo> observableList = FXCollections.observableArrayList(lista); // Paso 2

        tblRegistroCajonPollo.setItems(observableList); // Paso 3
    }
}
