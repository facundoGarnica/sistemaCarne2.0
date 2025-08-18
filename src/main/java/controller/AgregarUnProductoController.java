/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import dao.DetallePedidoDAO;
import dao.PedidoDAO;
import dao.ProductoDAO;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import model.Cliente;
import model.DetallePedido;
import model.Pedido;
import model.Producto;

/**
 * FXML Controller class
 *
 * @author garca
 */
public class AgregarUnProductoController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private Cliente_pedidoController clientePedidoController;
    @FXML
    private Label lblClienteAsociado;
    @FXML
    private ComboBox<Producto> cmbProducto;
    @FXML
    private ComboBox<String> cmbMedida;
    @FXML
    private TextField txtCantidad;

    List<Producto> listaProductos;
    private Cliente cliente;
    private Pedido pedido;
    private DetallePedido detallePedido;
    private Producto producto;
    private ProductoDAO productoDao;
    private PedidoDAO pedidoDao;
    private DetallePedidoDAO detallePedidoDao;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        productoDao = new ProductoDAO();
        listaProductos = productoDao.buscarTodos();
        ObservableList<Producto> productosObservable = FXCollections.observableArrayList(listaProductos);

        cmbProducto.setItems(productosObservable);

        // Mostrar nombre en la lista desplegable
        cmbProducto.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Producto producto, boolean empty) {
                super.updateItem(producto, empty);
                setText(empty || producto == null ? null : producto.getNombre());
            }
        });

        // Mostrar nombre también cuando ya está seleccionado
        cmbProducto.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Producto producto, boolean empty) {
                super.updateItem(producto, empty);
                setText(empty || producto == null ? null : producto.getNombre());
            }
        });

        // ComboBox de kilos o unidad
        cmbMedida.setItems(FXCollections.observableArrayList("Kilos", "Unidad"));
        cmbMedida.getSelectionModel().selectFirst();
    }

    public void guardarProducto() {
        // validar producto
        Producto productoSeleccionado = cmbProducto.getValue();
        if (productoSeleccionado == null) {
            System.out.println("⚠️ Debes seleccionar un producto");
            return;
        }

        // validar medida
        String medidaSeleccionada = cmbMedida.getValue();
        if (medidaSeleccionada == null || medidaSeleccionada.isEmpty()) {
            System.out.println("⚠️ Debes seleccionar la medida (Kilos o Unidad)");
            return;
        }

        // validar cantidad
        Double cantidadSeleccionada;
        try {
            if (txtCantidad.getText() == null || txtCantidad.getText().trim().isEmpty()) {
                System.out.println("⚠️ Debes ingresar una cantidad");
                return;
            }
            cantidadSeleccionada = Double.valueOf(txtCantidad.getText().trim());

            if (cantidadSeleccionada <= 0) {
                System.out.println("⚠️ La cantidad debe ser mayor a 0");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("⚠️ La cantidad debe ser un número válido");
            return;
        }

        // ✅ si todo está correcto, armar el detalle
        detallePedido = new DetallePedido();
        detallePedido.setProducto(productoSeleccionado);
        detallePedido.setCantidad(cantidadSeleccionada);
        detallePedido.setUnidadMedida(medidaSeleccionada);
        detallePedido.setPedido(pedido);
        detallePedido.setPrecio(productoSeleccionado.getPrecio());
        detallePedidoDao = new DetallePedidoDAO();
        detallePedidoDao.guardar(detallePedido);

        System.out.println("✅ Producto agregado correctamente: "
                + productoSeleccionado.getNombre()
                + " - " + cantidadSeleccionada + " " + medidaSeleccionada);
        cerrarOverlay();
    }

    public void setClientePedidoController(Cliente_pedidoController c) {
        this.clientePedidoController = c;
    }

    public void setPedido(Pedido p) {
        this.pedido = p;
    }

    public void setCliente(Cliente c) {
        this.cliente = c;
    }

    @FXML
    public void cerrarOverlay() {
        if (clientePedidoController != null) {
            clientePedidoController.cerrarSpaAgregarUnProducto();
        }
    }
}
