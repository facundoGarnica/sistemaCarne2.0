/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import dao.ClienteDAO;
import dao.PedidoDAO;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import model.Cliente;
import model.Pedido;

/**
 * FXML Controller class
 *
 * @author facun
 */
public class Cliente_pedidoController implements Initializable {

    /**
     * Initializes the controller class.
     */
    //clases y Dao
    private Cliente cliente;
    private ClienteDAO clienteDao;
    private Pedido pedido;
    private PedidoDAO pedidoDao;

    //tablas
    @FXML
    private TableView<Cliente> tablaClientes;
    @FXML
    private TableColumn<Cliente, String> colNombre;
    @FXML
    private TableColumn<Cliente, String> colCelular;
    @FXML
    private AnchorPane overlayPedido;
    @FXML
    private AnchorPane difuminar;

    //tabla pedidos
    @FXML
    private TableView <Pedido> tablaPedidos;
    @FXML
    private TableColumn<Cliente, String> colFechaPedido;
    @FXML
    private TableColumn<Cliente, String> colMonto;
    @FXML
    private TableColumn<Cliente, String> colSena;
    @FXML
    private TableColumn<Cliente, String> colResto;
    @FXML
    private TableColumn<Cliente, String> colEstado;
    @FXML
    private TableColumn<Cliente, String> colDiaEntrega;
    
    
    //variables
    List<Cliente> listaClientePedidos;

    //controller
    @FXML
    private Cliente_CrearPedidoController crearPedidoController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCelular.setCellValueFactory(new PropertyValueFactory<>("celular"));
        
        //tabla de pedidos
        
        colFechaPedido.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        
        cargarClientesConPedidos();
        cargarClientesConPedidos();
    }

    private void cargarClientesConPedidos() {
        clienteDao = new ClienteDAO();
        listaClientePedidos = clienteDao.buscarClientesConPedidos();

        // Pasar la lista a un ObservableList (lo que TableView entiende)
        ObservableList<Cliente> clientesObservable = FXCollections.observableArrayList(listaClientePedidos);

        // Asignar a la tabla
        tablaClientes.setItems(clientesObservable);

        //listener para capturar selección
        tablaClientes.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selectedCliente) -> {
                    if (selectedCliente != null) {
                        cargarPedidosDeCliente(selectedCliente);
                    }
                }
        );
    }

    private void cargarPedidosDeCliente(Cliente clienteSeleccionado) {
        pedidoDao = new PedidoDAO();

        List<Pedido> pedidos = pedidoDao.buscarPedidosPorCliente(clienteSeleccionado.getId());

        ObservableList<Pedido> pedidosObservable = FXCollections.observableArrayList(pedidos);
        tablaPedidos.setItems(pedidosObservable);
    }

    public void difuminarTodo() {
        difuminar.setVisible(true);
        difuminar.setDisable(false);

        overlayPedido.setVisible(true);
        overlayPedido.setDisable(false);

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

        overlayPedido.setDisable(true);
        overlayPedido.setVisible(false);
    }

    @FXML
    public void invocarSpaCrearPedido() {  //llama a detalle de fiados
        try {
            overlayPedido.getChildren().clear(); // Limpiar el AnchorPane destino

            // Cargar el FXML spa_clientes.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cliente_CrearPedido.fxml"));
            Parent root = loader.load();
            crearPedidoController = loader.getController();
            crearPedidoController.setSpa_clientePedidoController(this);
            // controller.setClientesController(this); // Por ejemplo
            // Insertar el contenido cargado en el AnchorPane
            overlayPedido.getChildren().add(root);

            // Hacer que el contenido cargado se ajuste al tamaño del AnchorPane
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
