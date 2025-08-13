/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import Util.HibernateUtil;
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
import org.hibernate.Session;
import org.hibernate.Transaction;

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
        System.out.println("Stock " + productoSeleccionado.getNombre() + ", " + stock);
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
        double cantidadMinima;
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
            cantidad = Double.parseDouble(txtAgregarStock.getText().replace(',', '.'));
            if (cantidad < 0) {
                System.out.println("La cantidad no puede ser negativa.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingrese una cantidad válida.");
            return;
        }

        double cantidadMinima;
        try {
            cantidadMinima = Double.parseDouble(txtCantidadMinima.getText().replace(',', '.'));
            if (cantidadMinima < 0) {
                System.out.println("La cantidad mínima no puede ser negativa.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingrese una cantidad mínima válida.");
            return;
        }

        try {
            // Crear o sumar stock (esto ya maneja la relación producto-stock)
            stockDao.sumarOCrearStockPorNombreProducto(productoSeleccionado.getNombre(), cantidad);

            // DESPUÉS actualizar la cantidad mínima si es necesario
            Stock stockActualizado = stockDao.buscarPorProducto(productoSeleccionado);
            if (stockActualizado != null && cantidadMinima != stockActualizado.getCantidadMinima()) {
                stockActualizado.setCantidadMinima(cantidadMinima);
                stockDao.actualizar(stockActualizado);
            }

            // Actualizar la referencia local del producto para reflejar los cambios
            productoSeleccionado = productoDao.obtenerProductoPorId(productoSeleccionado.getId());

            cargarDatosTabla();
            System.out.println("Stock agregado o actualizado correctamente.");
            limpiarFormulario();
            editable = false;

        } catch (Exception e) {
            System.out.println("Error al agregar stock: " + e.getMessage());
            e.printStackTrace();
        }
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
        alertaConfirmacion.setContentText("¿Está seguro que desea eliminar el stock seleccionado?\nEl producto se mantendrá sin stock.");

        Optional<ButtonType> resultado = alertaConfirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            Transaction tx = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                tx = session.beginTransaction();

                // Obtener el stock y producto de la sesión
                Stock stock = session.get(Stock.class, seleccionado.getId());
                if (stock != null && stock.getProducto() != null) {
                    Producto producto = session.get(Producto.class, stock.getProducto().getId());

                    // PASO 1: Desasociar la relación bidireccional
                    // Quitar la referencia del producto al stock
                    producto.setStock(null);
                    session.update(producto);

                    // PASO 2: Quitar la referencia del stock al producto
                    stock.setProducto(null);
                    session.update(stock);

                    // PASO 3: Ahora sí eliminar el stock
                    session.delete(stock);

                    System.out.println("Stock eliminado correctamente. El producto '"
                            + producto.getNombre() + "' quedó sin stock.");
                } else {
                    System.out.println("No se pudo encontrar el stock o su producto asociado.");
                }

                tx.commit();
                cargarDatosTabla();
                limpiarFormulario();

            } catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                }
                System.out.println("Error al eliminar stock: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Eliminación cancelada.");
        }
    }
}
