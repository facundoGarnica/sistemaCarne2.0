/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import dao.ProductoDAO;
import dao.StockDAO;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import model.Producto;
import model.Stock;

/**
 * FXML Controller class
 *
 * @author garca
 */
public class StockProductoController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private ProductoDAO productoDao;
    private StockDAO stockDao;
    private List<Producto> listaProductos;
    @FXML
    private ComboBox<Producto> cbxSeleccionProducto;
    @FXML
    private Label labelStock;
    private Producto productoSeleccionado;
    @FXML
    private TextField txtAgregarStock;
    @FXML
    private TextField txtCantidadMinima;
    private Boolean editable;
    /* -----------------
        FXML de la tabla
        ---------------
     */
    @FXML
    private TableView<Stock> tablaStock;
    @FXML
    private TableColumn<Stock, String> colProducto;
    @FXML
    private TableColumn<Stock, Integer> colCantidad;
    @FXML
    private TableColumn<Stock, Integer> colCantidadMinima;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        productoDao = new ProductoDAO();
        listaProductos = productoDao.buscarTodos();
        cbxSeleccionProducto.getItems().addAll(listaProductos);

        cbxSeleccionProducto.setConverter(new StringConverter<Producto>() {
            @Override
            public String toString(Producto producto) {
                return producto != null ? producto.getNombre() : "";
            }

            @Override
            public Producto fromString(String string) {
                return null; // No lo usamos
            }
        });

        // Configurar las columnas
        colProducto.setCellValueFactory(cellData
                -> new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));

        colCantidad.setCellValueFactory(cellData
                -> new SimpleIntegerProperty((int) cellData.getValue().getCantidad()).asObject());

        colCantidadMinima.setCellValueFactory(cellData
                -> new SimpleIntegerProperty((int) cellData.getValue().getCantidadMinima()).asObject());

        cargarDatosTabla();

    }

    private void cargarDatosTabla() {
        stockDao = new StockDAO();
        List<Stock> listaStock = stockDao.buscarTodos(); // Asumiendo que existe este método
        tablaStock.getItems().setAll(listaStock);
    }

    public void mostrarStockProducto() {
        productoSeleccionado = cbxSeleccionProducto.getValue();

        // Validar que haya una selección
        if (productoSeleccionado == null) {
            labelStock.setText("Seleccionar");
            return;
        }

        // Validar que tenga stock
        if (productoSeleccionado.getStock() == null) {
            labelStock.setText("Sin stock");
            return;
        }

        // Mostrar cantidad de stock
        String stock = String.valueOf(productoSeleccionado.getStock().getCantidad());
        labelStock.setText(stock);
    }

    public void cargarParaEditar() {
        Stock seleccionado = tablaStock.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            System.out.println("Seleccione un stock para editar.");
            return;
        }
        // Cargar datos en el formulario
        cbxSeleccionProducto.setValue(seleccionado.getProducto());
        productoSeleccionado = seleccionado.getProducto();
        txtAgregarStock.setText(String.valueOf((int) seleccionado.getCantidad()));
        txtCantidadMinima.setText(String.valueOf((int) seleccionado.getCantidadMinima()));

        editable = true; // Ahora estamos en modo edición
    }

// Método unificado para guardar o editar
    public void GuardarOEditar() {
        if (editable) {
            editarSeleccionado();
        } else {
            agregarStock();
        }
    }

// En editarSeleccionado, una vez guardado, resetear editable y limpiar formulario
    public void editarSeleccionado() {
        Stock seleccionado = tablaStock.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            System.out.println("Seleccione un stock de la tabla para editar.");
            return;
        }

        int cantidad, cantidadMinima;
        try {
            cantidad = Integer.parseInt(txtAgregarStock.getText());
            if (cantidad < 0) {
                System.out.println("La cantidad no puede ser negativa.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingrese una cantidad válida.");
            return;
        }

        try {
            cantidadMinima = Integer.parseInt(txtCantidadMinima.getText());
            if (cantidadMinima < 0) {
                System.out.println("La cantidad mínima no puede ser negativa.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingrese una cantidad mínima válida.");
            return;
        }

        seleccionado.setCantidad(cantidad);
        seleccionado.setCantidadMinima(cantidadMinima);
        seleccionado.setFecha(LocalDate.now());

        stockDao.actualizar(seleccionado);
        System.out.println("Stock actualizado correctamente.");

        cargarDatosTabla();
        limpiarFormulario();

        editable = false;  // Salir modo edición
    }

// En agregarStock, limpiar formulario y asegurar editable = false después de agregar
    public void agregarStock() {
        if (productoSeleccionado == null) {
            System.out.println("Debe seleccionar un producto.");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtAgregarStock.getText());
            if (cantidad < 0) {
                System.out.println("La cantidad no puede ser negativa.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingrese una cantidad válida.");
            return;
        }

        int cantidadMinima;
        try {
            cantidadMinima = Integer.parseInt(txtCantidadMinima.getText());
            if (cantidadMinima < 0) {
                System.out.println("La cantidad mínima no puede ser negativa.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingrese una cantidad mínima válida.");
            return;
        }

        Stock nuevoStock = new Stock();
        nuevoStock.setProducto(productoSeleccionado);
        nuevoStock.setCantidad(cantidad);
        nuevoStock.setFecha(LocalDate.now());
        nuevoStock.setCantidadMinima(cantidadMinima);

        stockDao.guardar(nuevoStock);
        cargarDatosTabla();
        System.out.println("Stock agregado correctamente.");
        limpiarFormulario();

        editable = false; // Confirmar que estamos en modo nuevo
    }

// Limpieza del formulario (opcional: también limpiar el booleano editable)
    public void limpiarFormulario() {
        txtCantidadMinima.clear();
        txtAgregarStock.clear();
        cbxSeleccionProducto.setValue(null);
        productoSeleccionado = null;
        editable = false;
    }

    public void borrarSeleccionado() {
        Stock seleccionado = tablaStock.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            System.out.println("Seleccione un stock de la tabla para borrar.");
            return;
        }

        // Crear alerta de confirmación
        Alert alertaConfirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        alertaConfirmacion.setTitle("Confirmar eliminación");
        alertaConfirmacion.setHeaderText(null);
        alertaConfirmacion.setContentText("¿Está seguro que desea eliminar el stock seleccionado?");

        Optional<ButtonType> resultado = alertaConfirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            stockDao.eliminar(seleccionado.getId());
            System.out.println("Stock eliminado correctamente.");
            cargarDatosTabla();
            limpiarFormulario();
        } else {
            System.out.println("Eliminación cancelada.");
        }
    }

}
