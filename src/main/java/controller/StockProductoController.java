/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import dao.ProductoDAO;
import dao.StockDAO;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
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
    private TableColumn<Stock, Double> colCantidad;
    @FXML
    private TableColumn<Stock, Double> colCantidadMinima;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        productoDao = new ProductoDAO();
        listaProductos = productoDao.buscarTodos();
        editable = false;
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
        DecimalFormat df = new DecimalFormat("#0.00");
        // Configurar las columnas
        colProducto.setCellValueFactory(cellData
                -> new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));

        // Cambiar tipo de la columna en la definición (FXML o código) a TableColumn<Stock, Double>
        colCantidadMinima.setCellValueFactory(cellData
                -> new SimpleDoubleProperty(cellData.getValue().getCantidadMinima()).asObject());

        colCantidadMinima.setCellFactory(column -> new TableCell<Stock, Double>() {
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

        colCantidad.setCellValueFactory(cellData
                -> new SimpleDoubleProperty(cellData.getValue().getCantidad()).asObject());

        colCantidad.setCellFactory(column -> new TableCell<Stock, Double>() {
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

        txtAgregarStock.setText(String.format("%.2f", seleccionado.getCantidad()));
        txtCantidadMinima.setText(String.format("%.2f", seleccionado.getCantidadMinima())); // CAMBIO: formato decimal

        editable = true;
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

        double cantidad;
        double cantidadMinima; // CAMBIO: de int a double
        try {
            String textoCantidad = txtAgregarStock.getText().replace(',', '.');
            cantidad = Double.parseDouble(textoCantidad);
            if (cantidad < 0) {
                System.out.println("La cantidad no puede ser negativa.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingrese una cantidad válida.");
            return;
        }

        try {
            String textoCantidadMinima = txtCantidadMinima.getText().replace(',', '.');
            cantidadMinima = Double.parseDouble(textoCantidadMinima); // CAMBIO: parseDouble directo
            if (cantidadMinima < 0) {
                System.out.println("La cantidad mínima no puede ser negativa.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingrese una cantidad mínima válida.");
            return;
        }

        seleccionado.setCantidad(cantidad);
        seleccionado.setCantidadMinima(cantidadMinima); // CAMBIO: ahora es double
        seleccionado.setFecha(LocalDateTime.now());

        stockDao.actualizar(seleccionado);
        System.out.println("Stock actualizado correctamente.");

        cargarDatosTabla();
        limpiarFormulario();

        editable = false;
    }

// En agregarStock, limpiar formulario y asegurar editable = false después de agregar
    public void agregarStock() {
        if (productoSeleccionado == null) {
            System.out.println("Debe seleccionar un producto.");
            return;
        }

        double cantidad;
        try {
            cantidad = Double.parseDouble(txtAgregarStock.getText());
            if (cantidad < 0) {
                System.out.println("La cantidad no puede ser negativa.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingrese una cantidad válida.");
            return;
        }

        double cantidadMinima; // CAMBIO: de int a double
        try {
            String textoCantidadMinima = txtCantidadMinima.getText().replace(',', '.');
            cantidadMinima = Double.parseDouble(textoCantidadMinima); // CAMBIO: parseDouble directo
            if (cantidadMinima < 0) {
                System.out.println("La cantidad mínima no puede ser negativa.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingrese una cantidad mínima válida.");
            return;
        }

        // Usar el método que suma o crea stock
        stockDao.sumarOCrearStockPorNombreProducto(productoSeleccionado.getNombre(), cantidad);

        // Actualizar cantidad mínima para el producto
        Stock stockActual = stockDao.buscarPorProducto(productoSeleccionado);
        if (stockActual != null) {
            stockActual.setCantidadMinima(cantidadMinima); // CAMBIO: ahora es double
            stockDao.actualizar(stockActual);
        }

        cargarDatosTabla();
        System.out.println("Stock agregado o actualizado correctamente.");
        limpiarFormulario();

        editable = false;
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
