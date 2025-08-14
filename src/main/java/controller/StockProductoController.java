package controller;

import Util.HibernateUtil;
import dao.ProductoDAO;
import dao.StockDAO;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.Producto;
import model.Stock;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class StockProductoController implements Initializable {

    private ProductoDAO productoDao;
    private StockDAO stockDao;
    private List<Producto> listaProductos;

    // Campos originales
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

    // Nuevos campos FXML para las mejoras
    @FXML
    private TextField txtBuscarProducto;
    @FXML
    private ComboBox<String> cbxFiltroEstado;
    @FXML
    private ComboBox<String> cbxOrdenar;
    @FXML
    private Label lblTotalProductos;
    @FXML
    private Label lblStockBajo;
    @FXML
    private Label lblSinStock;
    @FXML
    private Label lblStockNormal;
    @FXML
    private VBox panelAlertas;
    @FXML
    private Label lblAlertas;
    @FXML
    private TableColumn<Stock, String> colEstado;
    @FXML
    private TableColumn<Stock, Integer> colCodigo;
    @FXML
    private TableColumn<Stock, String> colUnidad;
    @FXML
    private TableColumn<Stock, String> colUltimaActualizacion;

    // Variables para filtros
    private List<Stock> listaStockCompleta;
    private FilteredList<Stock> listaStockFiltrada;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        productoDao = new ProductoDAO();
        stockDao = new StockDAO();

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

        // Configuración del checkbox
        txtCantidadMinima.setEditable(false);
        checkActivarCantidad.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                txtCantidadMinima.setEditable(false);
                txtCantidadMinima.setDisable(true);
            } else {
                txtCantidadMinima.setEditable(true);
                txtCantidadMinima.setDisable(false);
            }
        });

        // Inicializar filtros
        if (cbxFiltroEstado != null) {
            cbxFiltroEstado.getItems().addAll("Todos", "Sin Stock", "Stock Crítico", "Stock Bajo", "Stock Normal");
            cbxFiltroEstado.setValue("Todos");
        }

        if (cbxOrdenar != null) {
            cbxOrdenar.getItems().addAll("Nombre A-Z", "Nombre Z-A", "Stock (Mayor)", "Stock (Menor)", "Código");
            cbxOrdenar.setValue("Nombre A-Z");
        }

        configurarColumnas();
        cargarDatosTabla();
        configurarFiltros();
        actualizarEstadisticas();
    }

    private void configurarColumnas() {
        DecimalFormat df = new DecimalFormat("#0.00");

        // Columnas originales
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

        // Nuevas columnas
        if (colEstado != null) {
            colEstado.setCellValueFactory(cellData -> {
                Stock stock = cellData.getValue();
                String estado = determinarEstadoStock(stock);
                return new SimpleStringProperty(estado);
            });

            colEstado.setCellFactory(column -> new TableCell<Stock, String>() {
                @Override
                protected void updateItem(String estado, boolean empty) {
                    super.updateItem(estado, empty);
                    if (empty || estado == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        setText(estado);
                        switch (estado) {
                            case "🔴":
                                setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                                break;
                            case "🟠":
                                setStyle("-fx-text-fill: #fd7e14; -fx-font-weight: bold;");
                                break;
                            case "🟡":
                                setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                                break;
                            case "🟢":
                                setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            });
        }

        if (colCodigo != null) {
            colCodigo.setCellValueFactory(cellData
                    -> new SimpleIntegerProperty(cellData.getValue().getProducto().getCodigo()).asObject());
        }

        if (colUnidad != null) {
            colUnidad.setCellValueFactory(cellData -> {
                // Asumiendo que puedes determinar la unidad de alguna manera
                // Por ahora usamos una lógica simple
                return new SimpleStringProperty("Kg"); // Cambiar según tu modelo
            });
        }

        if (colUltimaActualizacion != null) {
            colUltimaActualizacion.setCellValueFactory(cellData -> {
                LocalDateTime fecha = cellData.getValue().getFecha();
                if (fecha != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
                    return new SimpleStringProperty(fecha.format(formatter));
                }
                return new SimpleStringProperty("--");
            });
        }
    }

    private void configurarFiltros() {
        // Aplicar estilos a las filas según el estado del stock
        tablaStock.setRowFactory(tv -> {
            TableRow<Stock> row = new TableRow<>();
            row.itemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null) {
                    row.getStyleClass().removeAll("stock-critico", "stock-bajo", "stock-normal", "sin-stock");
                } else {
                    row.getStyleClass().removeAll("stock-critico", "stock-bajo", "stock-normal", "sin-stock");
                    String estado = determinarEstadoStock(newValue);
                    switch (estado) {
                        case "🔴":
                            row.getStyleClass().add("sin-stock");
                            break;
                        case "🟠":
                            row.getStyleClass().add("stock-critico");
                            break;
                        case "🟡":
                            row.getStyleClass().add("stock-bajo");
                            break;
                        case "🟢":
                            row.getStyleClass().add("stock-normal");
                            break;
                    }
                }
            });
            return row;
        });
    }

    private String determinarEstadoStock(Stock stock) {
        if (stock == null) return "🔴";

        double cantidad = stock.getCantidad();
        double minima = stock.getCantidadMinima();

        if (cantidad == 0) return "🔴"; // Sin stock
        if (cantidad <= minima) return "🟠"; // Stock crítico
        if (cantidad <= minima * 1.5) return "🟡"; // Stock bajo
        return "🟢"; // Stock normal
    }

    private void cargarDatosTabla() {
        List<Stock> listaStock = stockDao.buscarTodos();
        listaStockCompleta = listaStock;
        
        if (listaStockFiltrada != null) {
            listaStockFiltrada = new FilteredList<>(FXCollections.observableArrayList(listaStockCompleta));
            tablaStock.setItems(listaStockFiltrada);
        } else {
            tablaStock.getItems().setAll(listaStock);
        }
        
        actualizarEstadisticas();
    }

    @FXML
    private void filtrarProductos() {
        aplicarFiltros();
    }

    @FXML
    private void filtrarPorEstado() {
        aplicarFiltros();
    }

    @FXML
    private void ordenarTabla() {
        aplicarOrdenamiento();
    }

    @FXML
    private void limpiarFiltros() {
        if (txtBuscarProducto != null) txtBuscarProducto.clear();
        if (cbxFiltroEstado != null) cbxFiltroEstado.setValue("Todos");
        if (cbxOrdenar != null) cbxOrdenar.setValue("Nombre A-Z");
        aplicarFiltros();
    }

    @FXML
    private void actualizarTabla() {
        cargarDatosTabla();
        System.out.println("Tabla actualizada correctamente.");
    }

    @FXML
    private void exportarDatos() {
        // Implementar exportación de datos
        System.out.println("Función de exportar pendiente de implementar.");
    }

    @FXML
    private void verHistorial() {
        // Implementar visualización de historial
        System.out.println("Función de historial pendiente de implementar.");
    }

    private void aplicarFiltros() {
        if (listaStockCompleta == null || tablaStock == null) return;

        String textoBusqueda = (txtBuscarProducto != null) ? txtBuscarProducto.getText().toLowerCase() : "";
        String estadoFiltro = (cbxFiltroEstado != null) ? cbxFiltroEstado.getValue() : "Todos";

        List<Stock> listaFiltrada = listaStockCompleta.stream()
                .filter(stock -> {
                    // Filtro por texto de búsqueda
                    boolean coincideTexto = textoBusqueda.isEmpty() ||
                            stock.getProducto().getNombre().toLowerCase().contains(textoBusqueda) ||
                            String.valueOf(stock.getProducto().getCodigo()).contains(textoBusqueda);

                    // Filtro por estado
                    boolean coincideEstado = true;
                    if (!"Todos".equals(estadoFiltro)) {
                        String estado = determinarEstadoStock(stock);
                        switch (estadoFiltro) {
                            case "Sin Stock":
                                coincideEstado = "🔴".equals(estado);
                                break;
                            case "Stock Crítico":
                                coincideEstado = "🟠".equals(estado);
                                break;
                            case "Stock Bajo":
                                coincideEstado = "🟡".equals(estado);
                                break;
                            case "Stock Normal":
                                coincideEstado = "🟢".equals(estado);
                                break;
                        }
                    }

                    return coincideTexto && coincideEstado;
                })
                .collect(Collectors.toList());

        tablaStock.getItems().setAll(listaFiltrada);
        aplicarOrdenamiento();
    }

    private void aplicarOrdenamiento() {
        if (cbxOrdenar == null || tablaStock.getItems().isEmpty()) return;

        String criterio = cbxOrdenar.getValue();
        List<Stock> items = tablaStock.getItems();

        switch (criterio) {
            case "Nombre A-Z":
                items.sort(Comparator.comparing(s -> s.getProducto().getNombre()));
                break;
            case "Nombre Z-A":
                items.sort(Comparator.comparing((Stock s) -> s.getProducto().getNombre()).reversed());
                break;
            case "Stock (Mayor)":
                items.sort(Comparator.comparing((Stock s) -> s.getCantidad()).reversed());
                break;
            case "Stock (Menor)":
                items.sort(Comparator.comparing(Stock::getCantidad));
                break;
            case "Código":
                items.sort(Comparator.comparing(s -> s.getProducto().getCodigo()));
                break;
        }

        tablaStock.refresh();
    }

    private void actualizarEstadisticas() {
        if (listaStockCompleta == null) return;

        int total = listaStockCompleta.size();
        int sinStock = 0;
        int stockBajo = 0;
        int stockCritico = 0;
        int stockNormal = 0;

        for (Stock stock : listaStockCompleta) {
            String estado = determinarEstadoStock(stock);
            switch (estado) {
                case "🔴":
                    sinStock++;
                    break;
                case "🟠":
                    stockCritico++;
                    break;
                case "🟡":
                    stockBajo++;
                    break;
                case "🟢":
                    stockNormal++;
                    break;
            }
        }

        if (lblTotalProductos != null) lblTotalProductos.setText(String.valueOf(total));
        if (lblSinStock != null) lblSinStock.setText(String.valueOf(sinStock));
        if (lblStockBajo != null) lblStockBajo.setText(String.valueOf(stockBajo + stockCritico));
        if (lblStockNormal != null) lblStockNormal.setText(String.valueOf(stockNormal));

        // Mostrar/ocultar panel de alertas
        if (panelAlertas != null && lblAlertas != null) {
            if (sinStock > 0 || stockCritico > 0) {
                panelAlertas.setVisible(true);
                String mensaje = "";
                if (sinStock > 0) {
                    mensaje += sinStock + " productos sin stock. ";
                }
                if (stockCritico > 0) {
                    mensaje += stockCritico + " productos con stock crítico.";
                }
                lblAlertas.setText(mensaje);
            } else {
                panelAlertas.setVisible(false);
            }
        }
    }

    // Métodos originales mantenidos
    public void mostrarStockProducto() {
        productoSeleccionado = cbxSeleccionProducto.getValue();

        if (productoSeleccionado == null) {
            labelStock.setText("Seleccionar");
            txtCantidadMinima.clear();
            checkActivarCantidad.setSelected(false);
            txtCantidadMinima.setEditable(true);
            txtCantidadMinima.setDisable(false);
            return;
        }

        if (productoSeleccionado.getStock() == null) {
            labelStock.setText("Sin stock");
            txtCantidadMinima.clear();
            checkActivarCantidad.setSelected(false);
            txtCantidadMinima.setEditable(true);
            txtCantidadMinima.setDisable(false);
            return;
        }

        String stockStr = String.valueOf(productoSeleccionado.getStock().getCantidad());
        labelStock.setText(stockStr);

        Double cantidadMinima = productoSeleccionado.getStock().getCantidadMinima();

        if (cantidadMinima != null) {
            txtCantidadMinima.setText(String.valueOf(cantidadMinima));
        } else {
            txtCantidadMinima.clear();
        }

        if (cantidadMinima != null && cantidadMinima > 0) {
            checkActivarCantidad.setSelected(true);
            txtCantidadMinima.setEditable(false);
            txtCantidadMinima.setDisable(true);
        } else {
            checkActivarCantidad.setSelected(false);
            txtCantidadMinima.setEditable(true);
            txtCantidadMinima.setDisable(false);
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