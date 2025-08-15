package controller;

import dao.ClienteDAO;
import dao.FiadoDAO;
import dao.FiadoParcialDAO;
import dao.VentaDAO;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import model.Cliente;
import model.DetalleVenta;
import model.Fiado;
import model.FiadoParcial;
import model.Venta;

public class ClientesController implements Initializable {

    // Objetos
    private Cliente cliente;
    private Fiado fiado;
    private FiadoParcial fiadoParcial;
    private Venta venta;

    // DAO
    private ClienteDAO clienteDao;
    private FiadoDAO fiadoDao;
    private FiadoParcialDAO fiadoParcialDao;
    private VentaDAO ventaDao;

    // Variables
    DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm");
    List<Cliente> listaClientes;

    @FXML private AnchorPane difuminar;
    @FXML private AnchorPane spaDetalle;
    @FXML private Spa_clientesController spaClienteController;
    @FXML private Label lblClienteSeleccionado;

    // Tabla clientes
    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colAlias;
    @FXML private TableColumn<Cliente, String> colCelular;

    // Tabla fiados
    @FXML private TableView<Fiado> tablaFiados;
    @FXML private TableColumn<Fiado, String> colFecha;
    @FXML private TableColumn<Fiado, String> colHora;
    @FXML private TableColumn<Fiado, Double> colMonto;
    @FXML private TableColumn<Fiado, Double> colAnticipo;
    @FXML private TableColumn<Fiado, Double> colResto;
    @FXML private TableColumn<Fiado, Boolean> colEstado;

    // Tabla anticipos
    @FXML private TableView<FiadoParcial> tablaAnticipos;
    @FXML private TableColumn<FiadoParcial, String> colDiaAnticipo;
    @FXML private TableColumn<FiadoParcial, String> colHoraAnticipo;
    @FXML private TableColumn<FiadoParcial, Double> colDineroAnticipo;
    @FXML private TableColumn<FiadoParcial, String> colMedioAnticipo;

    // Tabla productos
    @FXML private TableView<DetalleVenta> tablaProductos;
    @FXML private TableColumn<DetalleVenta, String> colNombreProducto;
    @FXML private TableColumn<DetalleVenta, Double> colPrecio;
    @FXML private TableColumn<DetalleVenta, Double> colPesoCantidad;
    @FXML private TableColumn<DetalleVenta, Double> colTotal;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clienteDao = new ClienteDAO();
        fiadoDao = new FiadoDAO();
        ventaDao = new VentaDAO();
        fiadoParcialDao = new FiadoParcialDAO();

        listaClientes = clienteDao.buscarTodosConFiados();
        llenarTablaClientes();

        tblClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                DatosClienteSeleccionado();
            }
        });
    }

    public void DatosClienteSeleccionado() {
    Cliente clienteSeleccionado = tblClientes.getSelectionModel().getSelectedItem();
    if (clienteSeleccionado == null) return;

    lblClienteSeleccionado.setText("Cliente: " + clienteSeleccionado.getNombre()
            + ((clienteSeleccionado.getAlias() == null || clienteSeleccionado.getAlias().isEmpty())
               ? "" : " - " + clienteSeleccionado.getAlias()));

    List<Fiado> fiadosCliente = fiadoDao.obtenerFiadosPorClienteId(clienteSeleccionado.getId());

    // Configurar columnas de fiados...
    colFecha.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFecha().toLocalDate().format(fechaFormatter)));
    colHora.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFecha().toLocalTime().format(horaFormatter)));
    colMonto.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getVenta().getTotal()).asObject());
    colAnticipo.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getTotalParciales()).asObject());
    colResto.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getResto()).asObject());
    colEstado.setCellValueFactory(cd -> new SimpleBooleanProperty(cd.getValue().getEstado()).asObject());

    colEstado.setCellFactory(column -> new TableCell<Fiado, Boolean>() {
        @Override
        protected void updateItem(Boolean estado, boolean empty) {
            super.updateItem(estado, empty);
            if (empty || estado == null) { setText(null); setStyle(""); }
            else if (estado) { setText("Finalizado"); setStyle("-fx-background-color: #d5f4e6; -fx-text-fill: #27ae60; -fx-alignment: center;"); }
            else { setText("Pendiente"); setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #f39c12; -fx-alignment: center;"); }
        }
    });

    tablaFiados.getItems().setAll(fiadosCliente);

    // Limpiar tablas
    tablaProductos.getItems().clear();
    tablaAnticipos.getItems().clear();

    // Listener solo se agrega **una vez**, no cada vez que seleccionamos un cliente
    if (tablaFiados.getSelectionModel().getSelectedItem() == null && !fiadosCliente.isEmpty()) {
        // Seleccionar el primer fiado automáticamente
        tablaFiados.getSelectionModel().selectFirst();
        Fiado primerFiado = tablaFiados.getSelectionModel().getSelectedItem();
        llenarTablaProductos(primerFiado.getVenta());
        llenarTablaAnticipo(primerFiado.getFiadoParciales());
    }
}


    public void llenarTablaClientes() {
        colNombre.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNombre()));
        colAlias.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getAlias()));
        colCelular.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCelular()));
        tblClientes.getItems().setAll(listaClientes);
    }

    public void llenarTablaProductos(Venta venta) {
        if (venta == null || venta.getDetalleVentas() == null) {
            tablaProductos.getItems().clear();
            return;
        }

        colNombreProducto.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProducto().getNombre()));
        colPrecio.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getPrecio()).asObject());
        colPesoCantidad.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getPeso()).asObject());
        colTotal.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getPrecio() * cd.getValue().getPeso()).asObject());

        tablaProductos.getItems().setAll(venta.getDetalleVentas());
    }

    public void llenarTablaAnticipo(List<FiadoParcial> listaAnticipos) {
        colDiaAnticipo.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFecha().toLocalDate().format(fechaFormatter)));
        colHoraAnticipo.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFecha().toLocalTime().format(horaFormatter)));
        colDineroAnticipo.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getAnticipo()).asObject());
        colMedioAnticipo.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMedioAbonado()));
        tablaAnticipos.getItems().setAll(listaAnticipos);
    }

    // Métodos para difuminar pantalla (igual que tenías)
    public void difuminarTodo() {
        difuminar.setVisible(true);
        difuminar.setDisable(false);
        spaDetalle.setVisible(true);
        spaDetalle.setDisable(false);
        invocarSpaDetalle();

        Platform.runLater(() -> {
            Parent root = difuminar.getScene().getRoot();
            Bounds boundsInScene = difuminar.localToScene(difuminar.getBoundsInLocal());
            SnapshotParameters params = new SnapshotParameters();
            params.setViewport(new javafx.geometry.Rectangle2D(boundsInScene.getMinX(), boundsInScene.getMinY(), boundsInScene.getWidth(), boundsInScene.getHeight()));
            WritableImage snapshot = new WritableImage((int) boundsInScene.getWidth(), (int) boundsInScene.getHeight());
            root.snapshot(params, snapshot);
            ImageView img = new ImageView(snapshot);
            img.setFitWidth(boundsInScene.getWidth());
            img.setFitHeight(boundsInScene.getHeight());
            img.setEffect(new GaussianBlur(20));
            img.setLayoutX(0); img.setLayoutY(0);
            if (difuminar.getChildren().stream().noneMatch(node -> node instanceof ImageView)) {
                difuminar.getChildren().add(0, img);
            }
            difuminar.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3);");
        });
    }

    public void CerrarDifuminarYSpa() {
        difuminar.setDisable(true); difuminar.setVisible(false);
        spaDetalle.setDisable(true); spaDetalle.setVisible(false);
    }

    @FXML
    public void invocarSpaDetalle() {
        try {
            spaDetalle.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/spa_clientes.fxml"));
            Parent root = loader.load();
            spaClienteController = loader.getController();
            spaClienteController.setClientesController(this);
            spaDetalle.getChildren().add(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
