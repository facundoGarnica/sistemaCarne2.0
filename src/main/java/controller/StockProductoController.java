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
import javafx.scene.control.CheckBox;
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

public class StockProductoController implements Initializable {

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
    @FXML
    private CheckBox checkActivarCantidad;
    private Boolean editable;

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
        listaProductos.sort((p1, p2) -> Integer.compare(p1.getCodigo(), p2.getCodigo()));

        editable = false;
        cbxSeleccionProducto.getItems().setAll(listaProductos);

        cbxSeleccionProducto.setConverter(new StringConverter<Producto>() {
            @Override
            public String toString(Producto producto) {
                return (producto != null) ? producto.getCodigo() + " - " + producto.getNombre() : "";
            }

            @Override
            public Producto fromString(String string) {
                return null;
            }
        });

        cbxSeleccionProducto.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Producto producto, boolean empty) {
                super.updateItem(producto, empty);
                if (empty || producto == null) {
                    setText(null);
                } else {
                    setText(producto.getCodigo() + " - " + producto.getNombre());
                }
            }
        });

        // Inicialmente el campo no es editable
        txtCantidadMinima.setEditable(false);

        // Listener para habilitar o deshabilitar la edición
        // Listener para habilitar o deshabilitar la edición
        checkActivarCantidad.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                // Checkbox activado: bloquear edición
                txtCantidadMinima.setEditable(false);
                txtCantidadMinima.setDisable(true);
            } else {
                // Checkbox desactivado: permitir edición
                txtCantidadMinima.setEditable(true);
                txtCantidadMinima.setDisable(false);
                // NO limpiar el campo, mantener el valor actual
            }
        });

        DecimalFormat df = new DecimalFormat("#0.00");

        colProducto.setCellValueFactory(cellData
                -> new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));

        colCantidadMinima.setCellValueFactory(cellData
                -> new SimpleDoubleProperty(cellData.getValue().getCantidadMinima()).asObject());

        colCantidadMinima.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : df.format(item));
            }
        });

        colCantidad.setCellValueFactory(cellData
                -> new SimpleDoubleProperty(cellData.getValue().getCantidad()).asObject());

        colCantidad.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : df.format(item));
            }
        });

        cargarDatosTabla();
    }

    private void cargarDatosTabla() {
        stockDao = new StockDAO();
        List<Stock> listaStock = stockDao.buscarTodos();
        tablaStock.getItems().setAll(listaStock);
    }

    public void mostrarStockProducto() {
        productoSeleccionado = cbxSeleccionProducto.getValue();

        if (productoSeleccionado == null) {
            labelStock.setText("Seleccionar");
            txtCantidadMinima.clear();
            // Reset del checkbox y habilitar campo para nuevo ingreso
            checkActivarCantidad.setSelected(false);
            txtCantidadMinima.setEditable(true);
            txtCantidadMinima.setDisable(false);
            return;
        }

        if (productoSeleccionado.getStock() == null) {
            labelStock.setText("Sin stock");
            txtCantidadMinima.clear();
            // Reset del checkbox y habilitar campo para nuevo ingreso
            checkActivarCantidad.setSelected(false);
            txtCantidadMinima.setEditable(true);
            txtCantidadMinima.setDisable(false);
            return;
        }

        String stockStr = String.valueOf(productoSeleccionado.getStock().getCantidad());
        labelStock.setText(stockStr);

        Double cantidadMinima = productoSeleccionado.getStock().getCantidadMinima();

        // Mostrar la cantidad mínima si existe
        if (cantidadMinima != null) {
            txtCantidadMinima.setText(String.valueOf(cantidadMinima));
        } else {
            txtCantidadMinima.clear();
        }

        // 🔹 Si tiene cantidad mínima establecida (no null y > 0), bloquear el campo
        if (cantidadMinima != null && cantidadMinima > 0) {
            checkActivarCantidad.setSelected(true);
            txtCantidadMinima.setEditable(false);  // Bloquea el campo
            txtCantidadMinima.setDisable(true);    // Deshabilitado
        } else {
            checkActivarCantidad.setSelected(false);
            txtCantidadMinima.setEditable(true);   // Permite edición
            txtCantidadMinima.setDisable(false);   // Habilitado
        }

        System.out.println(
                "Stock " + productoSeleccionado.getNombre() + ", " + stockStr
                + " | Mínimo: " + (cantidadMinima != null ? cantidadMinima : "Sin definir")
        );
    }

    public void cargarParaEditar() {
        Stock seleccionado = tablaStock.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            System.out.println("Seleccione un stock para editar.");
            return;
        }

        cbxSeleccionProducto.setValue(seleccionado.getProducto());
        productoSeleccionado = seleccionado.getProducto();

        txtAgregarStock.setText(String.format("%.2f", seleccionado.getCantidad()));
        txtCantidadMinima.setText(String.format("%.2f", seleccionado.getCantidadMinima()));
        editable = true;
    }

    public void GuardarOEditar() {
        if (editable) {
            editarSeleccionado();
        } else {
            agregarStock();
        }
    }

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
            cantidadMinima = Double.parseDouble(textoCantidadMinima);
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
        seleccionado.setFecha(LocalDateTime.now());

        stockDao.actualizar(seleccionado);
        System.out.println("Stock actualizado correctamente.");

        cargarDatosTabla();
        limpiarFormulario();
        editable = false;
    }

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
            stockDao.sumarOCrearStockPorNombreProducto(productoSeleccionado.getNombre(), cantidad);
            Stock stockActualizado = stockDao.buscarPorProducto(productoSeleccionado);
            if (stockActualizado != null && cantidadMinima != stockActualizado.getCantidadMinima()) {
                stockActualizado.setCantidadMinima(cantidadMinima);
                stockDao.actualizar(stockActualizado);
            }

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

    public void limpiarFormulario() {
        txtCantidadMinima.clear();
        txtAgregarStock.clear();
        cbxSeleccionProducto.setValue(null);
        productoSeleccionado = null;
        editable = false;

        // 🔹 Reset del checkbox y bloqueo del campo
        checkActivarCantidad.setSelected(false);
        txtCantidadMinima.setEditable(false);
    }

    public void borrarSeleccionado() {
        Stock seleccionado = tablaStock.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            System.out.println("Seleccione un stock de la tabla para borrar.");
            return;
        }

        Alert alertaConfirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        alertaConfirmacion.setTitle("Confirmar eliminación");
        alertaConfirmacion.setHeaderText(null);
        alertaConfirmacion.setContentText("¿Está seguro que desea eliminar el stock seleccionado?\nEl producto se mantendrá sin stock.");

        Optional<ButtonType> resultado = alertaConfirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            Transaction tx = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                tx = session.beginTransaction();

                Stock stock = session.get(Stock.class, seleccionado.getId());
                if (stock != null && stock.getProducto() != null) {
                    Producto producto = session.get(Producto.class, stock.getProducto().getId());

                    producto.setStock(null);
                    session.update(producto);

                    stock.setProducto(null);
                    session.update(stock);

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
