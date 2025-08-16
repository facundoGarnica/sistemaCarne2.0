package controller;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class Cliente_CrearPedidoController implements Initializable {

    @FXML private TableView<ProductoPedido> tablaProductos;
    @FXML private TableColumn<ProductoPedido, String> colProducto;
    @FXML private TableColumn<ProductoPedido, String> colMedida;
    @FXML private TableColumn<ProductoPedido, Double> colCantidad;
    @FXML private TableColumn<ProductoPedido, Double> colPrecioUnitario;
    @FXML private TableColumn<ProductoPedido, Double> colTotal;

    @FXML private ComboBox<String> cmbHoraEntrega;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCelular;
    @FXML private TextField txtSena;
    @FXML private DatePicker fechaEntrega;
    @FXML private Button btnAgregar;
    @FXML private Button btnEliminarProducto1;
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;
    @FXML private Label lblTotalPedido11;  // Cambiado para coincidir con el FXML
    @FXML private Label cantProductos; // Label para mostrar cantidad de productos
    @FXML private Label totalKilos; // Label para mostrar total en kilos

    private ObservableList<ProductoPedido> listaProductos;
    private ObservableList<String> productosDisponibles;
    private DecimalFormat formatoMoneda = new DecimalFormat("$#,##0.00");

    
    private Cliente_pedidoController clienteController;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listaProductos = FXCollections.observableArrayList();
        productosDisponibles = FXCollections.observableArrayList(
                "Torta Chocolate - $15000",
                "Harina de Trigo - $2500",
                "Cupcakes - $3500",
                "Azúcar - $1800",
                "Galletas - $5000"
        );

        configurarComboHoras();
        configurarTablaProductos();
        configurarEventosBotones();

        agregarFilaProducto();
    }

    public void setSpa_clientePedidoController(Cliente_pedidoController c){
        this.clienteController = c;
    }
    
    private void configurarComboHoras() {
        if (cmbHoraEntrega != null) {
            cmbHoraEntrega.getItems().addAll("08:00","09:00","10:00","11:00","12:00");
            cmbHoraEntrega.setValue("08:00");
        }
    }

    private void configurarTablaProductos() {
        tablaProductos.setItems(listaProductos);
        tablaProductos.setEditable(true);

        // --- Producto siempre editable ---
        colProducto.setCellValueFactory(cellData -> cellData.getValue().productoProperty());
        colProducto.setCellFactory(column -> new TableCell<ProductoPedido, String>() {
            private final ComboBox<String> combo = new ComboBox<>(productosDisponibles);
            {
                combo.setOnAction(e -> {
                    ProductoPedido p = getTableView().getItems().get(getIndex());
                    if (p != null) {
                        String seleccionado = combo.getValue();
                        p.setProducto(seleccionado);
                        try {
                            String precio = seleccionado.split("\\$")[1];
                            p.setPrecioUnitario(Double.parseDouble(precio));
                        } catch (Exception ex) {
                            p.setPrecioUnitario(0.0);
                        }
                        actualizarContadoresYTotales();
                        getTableView().refresh();
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    combo.setValue(item);
                    setGraphic(combo);
                }
            }
        });

        // --- Medida siempre editable ---
        ObservableList<String> medidas = FXCollections.observableArrayList("KIlo","Unidad");
        colMedida.setCellValueFactory(cellData -> cellData.getValue().medidaProperty());
        colMedida.setCellFactory(column -> new TableCell<ProductoPedido, String>() {
            private final ComboBox<String> combo = new ComboBox<>(medidas);
            {
                combo.setOnAction(e -> {
                    ProductoPedido p = getTableView().getItems().get(getIndex());
                    if (p != null) {
                        p.setMedida(combo.getValue());
                        actualizarContadoresYTotales();
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    combo.setValue(item);
                    setGraphic(combo);
                }
            }
        });

        // --- Cantidad siempre editable ---
        colCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty().asObject());
        colCantidad.setCellFactory(column -> new TableCell<ProductoPedido, Double>() {
            private final TextField textField = new TextField();
            {
                textField.setAlignment(Pos.CENTER_RIGHT);
                textField.textProperty().addListener((obs, oldVal, newVal) -> {
                    try {
                        if (newVal != null && !newVal.trim().isEmpty()) {
                            double valor = Double.parseDouble(newVal);
                            ProductoPedido p = getTableView().getItems().get(getIndex());
                            if (p != null) {
                                p.setCantidad(valor);
                                actualizarContadoresYTotales();
                            }
                        }
                    } catch (NumberFormatException ex) {
                        // ignorar input inválido
                    }
                });
            }
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    textField.setText(item != null ? item.toString() : "0");
                    setGraphic(textField);
                }
            }
        });

        // --- Columnas solo lectura ---
        colPrecioUnitario.setCellValueFactory(cellData -> cellData.getValue().precioUnitarioProperty().asObject());
        colTotal.setCellValueFactory(cellData -> cellData.getValue().totalProperty().asObject());
    }

    private void configurarEventosBotones() {
        if (btnAgregar != null) btnAgregar.setOnAction(e -> agregarFilaProducto());
        if (btnEliminarProducto1 != null) btnEliminarProducto1.setOnAction(e -> eliminarFilaSeleccionada());
        if (btnCancelar != null) btnCancelar.setOnAction(e -> cerrarOverlay());
        if (btnGuardar != null) btnGuardar.setOnAction(e -> guardarPedido());
    }

    private void agregarFilaProducto() {
        ProductoPedido nuevo = new ProductoPedido();
        listaProductos.add(nuevo);
        actualizarContadoresYTotales();
    }

    private void eliminarFilaSeleccionada() {
        int selectedIndex = tablaProductos.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            listaProductos.remove(selectedIndex);
            actualizarContadoresYTotales();
        }
    }

    private void guardarPedido() {
        System.out.println("=== DATOS DEL PEDIDO ===");
        for (ProductoPedido p : listaProductos) {
            System.out.println(p.getProducto() + " - " + p.getCantidad() + " " + p.getMedida() + " - $" + p.getTotal());
        }
    }

    private void cerrarOverlay() { System.out.println("Cerrando..."); }

    // Método unificado para actualizar tanto el total como el contador de productos
    private void actualizarContadoresYTotales() {
        actualizarTotalPedido();
        actualizarContadorProductos();
        actualizarTotalKilos();
    }

    private void actualizarTotalPedido() {
        if (lblTotalPedido11 != null) {
            double total = listaProductos.stream().mapToDouble(ProductoPedido::getTotal).sum();
            lblTotalPedido11.setText(formatoMoneda.format(total));
        }
    }

    // Método para actualizar el contador de productos completos
    private void actualizarContadorProductos() {
        if (cantProductos != null) {
            int productosCompletos = (int) listaProductos.stream()
                    .filter(this::esProductoCompleto)
                    .count();
            cantProductos.setText(String.valueOf(productosCompletos));
        }
    }

    // Nuevo método para actualizar el total en kilos
    private void actualizarTotalKilos() {
        if (totalKilos != null) {
            double totalEnKilos = listaProductos.stream()
                    .filter(p -> p.getMedida() != null && 
                               (p.getMedida().equalsIgnoreCase("Kilo") || p.getMedida().equalsIgnoreCase("KIlo")))
                    .mapToDouble(ProductoPedido::getCantidad)
                    .sum();
            totalKilos.setText(String.format("%.2f", totalEnKilos));
        }
    }

    // Método para verificar si un producto está completo (tiene todos sus datos)
    private boolean esProductoCompleto(ProductoPedido producto) {
        return producto.getProducto() != null && 
               !producto.getProducto().trim().isEmpty() &&
               producto.getMedida() != null && 
               !producto.getMedida().trim().isEmpty() &&
               producto.getCantidad() > 0 &&
               producto.getPrecioUnitario() > 0;
    }

    public static class ProductoPedido {
        private final SimpleStringProperty producto = new SimpleStringProperty("");
        private final SimpleStringProperty medida = new SimpleStringProperty("Kilo");
        private final SimpleDoubleProperty cantidad = new SimpleDoubleProperty(0.0);
        private final SimpleDoubleProperty precioUnitario = new SimpleDoubleProperty(0.0);
        private final SimpleDoubleProperty total = new SimpleDoubleProperty(0.0);

        public ProductoPedido() {
            // Listeners para recalcular el total cuando cambian cantidad o precio
            cantidad.addListener((obs, oldVal, newVal) -> {
                setTotal(newVal.doubleValue() * getPrecioUnitario());
            });
            precioUnitario.addListener((obs, oldVal, newVal) -> {
                setTotal(getCantidad() * newVal.doubleValue());
            });
        }

        public String getProducto() { return producto.get(); }
        public void setProducto(String value) { producto.set(value); }
        public SimpleStringProperty productoProperty() { return producto; }

        public String getMedida() { return medida.get(); }
        public void setMedida(String value) { medida.set(value); }
        public SimpleStringProperty medidaProperty() { return medida; }

        public double getCantidad() { return cantidad.get(); }
        public void setCantidad(double value) { cantidad.set(value); }
        public SimpleDoubleProperty cantidadProperty() { return cantidad; }

        public double getPrecioUnitario() { return precioUnitario.get(); }
        public void setPrecioUnitario(double value) { precioUnitario.set(value); }
        public SimpleDoubleProperty precioUnitarioProperty() { return precioUnitario; }

        public double getTotal() { return total.get(); }
        public void setTotal(double value) { total.set(value); }
        public SimpleDoubleProperty totalProperty() { return total; }
    }
}