package controller;

import dao.ClienteDAO;
import dao.DetallePedidoDAO;
import dao.PedidoDAO;
import dao.ProductoDAO;
import dao.SeniaDAO;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import model.Cliente;
import model.DetallePedido;
import model.Pedido;
import model.Producto;
import model.Senia;

public class Cliente_CrearPedidoController implements Initializable {

    //Clases y Dao
    private Cliente cliente;
    private Pedido pedido;
    private DetallePedido detallePedido;
    private Producto producto;
    private Senia senia;
    private SeniaDAO seniaDao;
    private ClienteDAO clienteDao;
    private PedidoDAO pedidoDao;
    private ProductoDAO productoDao;
    private DetallePedidoDAO detallePedidoDAO;
    //variables
    List<Producto> listaProductosCarne;

    @FXML
    private TableView<ProductoPedido> tablaProductos;
    @FXML
    private TableColumn<ProductoPedido, Producto> colProducto;
    @FXML
    private TableColumn<ProductoPedido, String> colMedida;
    @FXML
    private TableColumn<ProductoPedido, Double> colCantidad;
    @FXML
    private TableColumn<ProductoPedido, Double> colPrecioUnitario;
    @FXML
    private TableColumn<ProductoPedido, Double> colTotal;

    @FXML
    private ComboBox<String> cmbHoraEntrega;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtCelular;
    @FXML
    private TextField txtSena;
    @FXML
    private DatePicker fechaEntrega;
    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnEliminarProducto1;
    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnGuardar;
    @FXML
    private Label lblTotalPedido11;  // Cambiado para coincidir con el FXML
    @FXML
    private Label cantProductos; // Label para mostrar cantidad de productos
    @FXML
    private Label totalKilos; // Label para mostrar total en kilos

    private ObservableList<ProductoPedido> listaProductos = FXCollections.observableArrayList();

    private DecimalFormat formatoMoneda = new DecimalFormat("$#,##0.00");

    private Cliente_pedidoController clienteController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        productoDao = new ProductoDAO();
        listaProductosCarne = productoDao.buscarTodos();
        // Ordenar por código (ascendente)
        listaProductosCarne.sort(Comparator.comparingInt(Producto::getCodigo));

        configurarComboHoras();
        configurarTablaProductos();
        configurarEventosBotones();

        agregarFilaProducto();
    }

    public void setSpa_clientePedidoController(Cliente_pedidoController c) {
        this.clienteController = c;
    }

    private void configurarComboHoras() {
        if (cmbHoraEntrega != null) {
            cmbHoraEntrega.getItems().addAll("08:00", "09:00", "10:00", "11:00", "12:00");
            cmbHoraEntrega.setValue("08:00");
        }
    }

    private void configurarTablaProductos() {
        tablaProductos.setItems(listaProductos);
        tablaProductos.setEditable(true);

        // --- Producto siempre editable con objetos Producto ---
        colProducto.setCellValueFactory(cellData -> cellData.getValue().productoProperty());
        colProducto.setCellFactory(column -> new TableCell<ProductoPedido, Producto>() {
            private final ComboBox<Producto> combo = new ComboBox<>();

            {
                // Configurar los items del combo
                combo.setItems(FXCollections.observableArrayList(listaProductosCarne));

                // Configurar cómo se muestra cada producto en el combo
                combo.setConverter(new StringConverter<Producto>() {
                    @Override
                    public String toString(Producto producto) {
                        if (producto == null) {
                            return null;
                        }
                        DecimalFormat formatoPrecio = new DecimalFormat("$#,##0.00");
                        return producto.getCodigo() + " - " + producto.getNombre() + " - " + formatoPrecio.format(producto.getPrecio());
                    }

                    @Override
                    public Producto fromString(String string) {
                        // Este método generalmente no se usa en ComboBox
                        return null;
                    }
                });

                // Evento cuando se selecciona un producto
                combo.setOnAction(e -> {
                    ProductoPedido p = getTableView().getItems().get(getIndex());
                    if (p != null) {
                        Producto productoSeleccionado = combo.getValue();
                        p.setProducto(productoSeleccionado);

                        // Establecer precio unitario usando el objeto producto
                        if (productoSeleccionado != null) {
                            p.setPrecioUnitario(productoSeleccionado.getPrecio());
                        } else {
                            p.setPrecioUnitario(0.0);
                        }

                        actualizarContadoresYTotales();
                        getTableView().refresh();
                    }
                });
            }

            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    combo.setValue(item);
                    setGraphic(combo);
                }
            }
        });

        // --- Medida siempre editable ---
        ObservableList<String> medidas = FXCollections.observableArrayList("Kilo", "Unidad");
        colMedida.setCellValueFactory(cellData -> cellData.getValue().medidaProperty());
        colMedida.setCellFactory(column -> new TableCell<ProductoPedido, String>() {
            private final ComboBox<String> combo = new ComboBox<>(medidas);

            {
                combo.setOnAction(e -> {
                    ProductoPedido p = getTableView().getItems().get(getIndex());
                    if (p != null) {
                        p.setMedida(combo.getValue());
                        // Recalcular total cuando cambia la medida
                        actualizarContadoresYTotales();
                        getTableView().refresh();
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
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

                // Actualizar cantidad cuando se presiona Enter
                textField.setOnAction(e -> commitEditFromTextField());

                // Actualizar cantidad cuando se pierde el foco
                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        commitEditFromTextField();
                    }
                });
            }

            private void commitEditFromTextField() {
                try {
                    // Verificar que el índice sea válido antes de acceder
                    int index = getIndex();
                    if (index < 0 || index >= getTableView().getItems().size()) {
                        return; // Salir si el índice no es válido
                    }

                    ProductoPedido p = getTableView().getItems().get(index);
                    if (p != null) {
                        try {
                            // Reemplazar coma por punto para aceptar ambos formatos
                            String text = textField.getText().replace(',', '.');
                            double valor = Double.parseDouble(text);
                            p.setCantidad(valor);
                            // Recalcular total cuando cambia la cantidad
                            actualizarContadoresYTotales();
                            getTableView().refresh();
                        } catch (NumberFormatException ex) {
                            // Restaurar valor actual si el input es inválido
                            textField.setText(String.valueOf(p.getCantidad()));
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    // Manejo silencioso del error de índice
                    System.out.println("Error de índice manejado en cantidad: " + e.getMessage());
                }
            }

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ProductoPedido p = getTableView().getItems().get(getIndex());
                    if (p != null) {
                        // Mostrar vacío si cantidad es 0, sino mostrar el valor
                        if (p.getCantidad() == 0.0) {
                            textField.setText("");
                        } else {
                            textField.setText(String.valueOf(p.getCantidad()));
                        }
                    }
                    setGraphic(textField);
                }
            }
        });

        // --- Columnas solo lectura con formato ---
        colPrecioUnitario.setCellValueFactory(cellData -> cellData.getValue().precioUnitarioProperty().asObject());
        colPrecioUnitario.setCellFactory(column -> new TableCell<ProductoPedido, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatoMoneda.format(item));
                }
            }
        });

        colTotal.setCellValueFactory(cellData -> cellData.getValue().totalProperty().asObject());
        colTotal.setCellFactory(column -> new TableCell<ProductoPedido, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatoMoneda.format(item));
                }
            }
        });
    }

    private void configurarEventosBotones() {
        if (btnAgregar != null) {
            btnAgregar.setOnAction(e -> agregarFilaProducto());
        }
        if (btnEliminarProducto1 != null) {
            btnEliminarProducto1.setOnAction(e -> eliminarFilaSeleccionada());
        }
        if (btnCancelar != null) {
            btnCancelar.setOnAction(e -> cerrarOverlay());
        }
        if (btnGuardar != null) {
            btnGuardar.setOnAction(e -> guardarPedido());
        }
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

    @FXML
    public void cerrarOverlay() {
        System.out.println("Cerrando...");
    }

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
                    .filter(p -> p.getMedida() != null
                    && (p.getMedida().equalsIgnoreCase("Kilo") || p.getMedida().equalsIgnoreCase("KIlo")))
                    .mapToDouble(ProductoPedido::getCantidad)
                    .sum();
            totalKilos.setText(String.format("%.2f", totalEnKilos));
        }
    }

    // Método para verificar si un producto está completo (tiene todos sus datos)
    private boolean esProductoCompleto(ProductoPedido producto) {
        return producto.getProducto() != null
                && producto.getMedida() != null
                && !producto.getMedida().trim().isEmpty()
                && producto.getCantidad() > 0
                && producto.getPrecioUnitario() > 0;
    }

    public void guardarPedido() {
        try {
            // Validaciones básicas
            if (!validarDatos()) {
                return;
            }

            // Mostrar cartel de confirmación antes de guardar
            if (!mostrarConfirmacionGuardar()) {
                return; // Si el usuario cancela, no guardar
            }

            // 1. Crear y guardar el cliente
            try {
                cliente = new Cliente();
                clienteDao = new ClienteDAO();

                String nombreCliente = txtNombre.getText().trim();
                String celularCliente = txtCelular.getText().trim();

                // Celular es opcional - solo guardarlo si no está vacío
                if (!celularCliente.isEmpty()) {
                    cliente.setCelular(celularCliente);
                } else {
                    cliente.setCelular(null); // o simplemente no setearlo
                }
                
                cliente.setNombre(nombreCliente);
                clienteDao.guardar(cliente);

                System.out.println("Cliente guardado exitosamente");

            } catch (Exception e) {
                mostrarError("Error al guardar el cliente: " + e.getMessage());
                return;
            }

            // 2. Crear y guardar el pedido (con fecha y hora separadas)
            try {
                pedido = new Pedido();
                pedidoDao = new PedidoDAO();

                // Obtener fecha y hora por separado
                LocalDate fechaSeleccionada = this.fechaEntrega.getValue();
                String horaSeleccionada = cmbHoraEntrega.getValue();
                
                System.out.println("Fecha seleccionada: " + fechaSeleccionada);
                System.out.println("Hora seleccionada: " + horaSeleccionada);

                // Configurar el pedido con fecha y hora separadas
                pedido.setFecha(LocalDateTime.now()); // Fecha y hora actual de creación del pedido
                pedido.setCliente(cliente);
                pedido.setFechaEntrega(fechaSeleccionada); // Solo la fecha como LocalDate
                pedido.setHoraEntrega(horaSeleccionada);   // Solo la hora como String
                pedido.setEstado(false); // false = pendiente, true = completado

                pedidoDao.guardar(pedido);

                System.out.println("Pedido guardado exitosamente");

            } catch (Exception e) {
                mostrarError("Error al guardar el pedido: " + e.getMessage());
                return;
            }

            // 3. Crear y guardar la seña vinculada al pedido (OPCIONAL)
            try {
                String textoSenia = txtSena.getText().trim();
                
                // Solo crear y guardar seña si se ingresó un valor
                if (!textoSenia.isEmpty()) {
                    senia = new Senia();
                    seniaDao = new SeniaDAO();

                    Double montoSenia = Double.valueOf(textoSenia);
                    senia.setMonto(montoSenia);
                    senia.setFecha(LocalDateTime.now());
                    senia.setPedido(pedido); // Vincula al pedido
                    
                    seniaDao.guardar(senia);
                    System.out.println("Seña guardada exitosamente");
                } else {
                    System.out.println("No se guardó seña (campo vacío)");
                }

            } catch (NumberFormatException e) {
                mostrarError("Error en el formato de la seña: debe ser un número válido");
                return;
            } catch (Exception e) {
                mostrarError("Error al guardar la seña: " + e.getMessage());
                return;
            }

            // 4. Crear y guardar los detalles del pedido
            try {
                detallePedidoDAO = new DetallePedidoDAO();
                int productosCompletos = 0;

                for (ProductoPedido productoPedido : listaProductos) {
                    // Solo guardar productos completos
                    if (esProductoCompleto(productoPedido)) {
                        detallePedido = new DetallePedido();

                        detallePedido.setPedido(pedido);
                        detallePedido.setProducto(productoPedido.getProducto());
                        detallePedido.setCantidad(productoPedido.getCantidad());
                        detallePedido.setUnidadMedida(productoPedido.getMedida());
                        detallePedido.setPrecio(productoPedido.getPrecioUnitario());

                        detallePedidoDAO.guardar(detallePedido);
                        productosCompletos++;
                    }
                }

                System.out.println("Detalles del pedido guardados exitosamente: " + productosCompletos + " productos");

                // Verificar que se guardó al menos un producto
                if (productosCompletos == 0) {
                    mostrarError("No se guardaron productos en el pedido");
                    return;
                }

            } catch (Exception e) {
                mostrarError("Error al guardar los detalles del pedido: " + e.getMessage());
                return;
            }

            // 5. Si llegamos hasta aquí, todo se guardó correctamente
            System.out.println("Pedido completo guardado exitosamente");
            mostrarMensajeExito();

            // Limpiar formulario después del guardado exitoso
            limpiarFormulario();

            cerrarOverlay();

            // 6. Refrescar la vista padre si existe
            /* if (clienteController != null) {
            clienteController.refrescarTablaPedidos();
        }*/
        } catch (NumberFormatException e) {
            mostrarError("Error en formato de número: " + e.getMessage());
        } catch (Exception e) {
            mostrarError("Error inesperado al guardar el pedido: " + e.getMessage());
            e.printStackTrace();
        }
        cerrarOverlay();
    }

    // Método para mostrar confirmación antes de guardar
    private boolean mostrarConfirmacionGuardar() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Guardado");
        alert.setHeaderText("¿Desea guardar el pedido?");

        // Calcular información del pedido para mostrar en la confirmación
        int productosCompletos = (int) listaProductos.stream()
                .filter(this::esProductoCompleto)
                .count();
        double totalPedido = listaProductos.stream()
                .mapToDouble(ProductoPedido::getTotal)
                .sum();

        // Manejar campos opcionales
        String celularTexto = txtCelular.getText().trim().isEmpty() ? "No especificado" : txtCelular.getText();
        String seniaTexto = txtSena.getText().trim().isEmpty() ? "Sin seña" : "$" + txtSena.getText();

        String mensaje = String.format(
                "Se guardará el siguiente pedido:\n\n"
                + "Cliente: %s\n"
                + "Celular: %s\n"
                + "Productos: %d\n"
                + "Total: %s\n"
                + "Seña: %s\n"
                + "Fecha de entrega: %s\n"
                + "Hora de entrega: %s\n\n"
                + "¿Confirma que desea guardar este pedido?",
                txtNombre.getText(),
                celularTexto,
                productosCompletos,
                formatoMoneda.format(totalPedido),
                seniaTexto,
                fechaEntrega.getValue().toString(),
                cmbHoraEntrega.getValue()
        );

        alert.setContentText(mensaje);

        // Configurar botones personalizados
        ButtonType buttonTypeGuardar = new ButtonType("Sí, Guardar");
        ButtonType buttonTypeCancelar = new ButtonType("No, Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeGuardar, buttonTypeCancelar);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonTypeGuardar;
    }

    // Método mejorado para mostrar mensaje de éxito con más información
    private void mostrarMensajeExito() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("¡Éxito!");
        alert.setHeaderText("Pedido realizado con éxito");

        // Calcular información del pedido para mostrar
        int productosCompletos = (int) listaProductos.stream()
                .filter(this::esProductoCompleto)
                .count();
        double totalPedido = listaProductos.stream()
                .mapToDouble(ProductoPedido::getTotal)
                .sum();

        // Manejar campos opcionales
        String celularTexto = txtCelular.getText().trim().isEmpty() ? "No especificado" : txtCelular.getText();
        String seniaTexto = txtSena.getText().trim().isEmpty() ? "Sin seña" : formatoMoneda.format(Double.valueOf(txtSena.getText()));

        String mensaje = String.format(
                "✓ Cliente: %s\n"
                + "✓ Celular: %s\n"
                + "✓ Productos: %d\n"
                + "✓ Total: %s\n"
                + "✓ Seña: %s\n"
                + "✓ Fecha de entrega: %s\n"
                + "✓ Hora de entrega: %s\n\n"
                + "El pedido ha sido guardado correctamente en la base de datos.",
                txtNombre.getText(),
                celularTexto,
                productosCompletos,
                formatoMoneda.format(totalPedido),
                seniaTexto,
                fechaEntrega.getValue().toString(),
                cmbHoraEntrega.getValue()
        );

        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Método para limpiar el formulario después de guardar exitosamente
    private void limpiarFormulario() {
        txtNombre.clear();
        txtCelular.clear();
        txtSena.clear();
        fechaEntrega.setValue(null);
        cmbHoraEntrega.setValue("08:00");
        listaProductos.clear();
        agregarFilaProducto(); // Agregar una fila vacía para el próximo pedido
        actualizarContadoresYTotales();
    }

    private boolean validarDatos() {
        // Validar nombre del cliente (OBLIGATORIO)
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre del cliente es obligatorio");
            return false;
        }

        // Celular es OPCIONAL - no validar

        // Validar seña SOLO si se ingresó algo
        String textoSenia = txtSena.getText().trim();
        if (!textoSenia.isEmpty()) {
            try {
                Double.valueOf(textoSenia);
            } catch (NumberFormatException e) {
                mostrarError("La seña debe ser un número válido o estar vacía");
                return false;
            }
        }

        // Validar fecha de entrega (OBLIGATORIO)
        if (this.fechaEntrega.getValue() == null) {
            mostrarError("La fecha de entrega es obligatoria");
            return false;
        }

        // Validar que la fecha de entrega no sea anterior a hoy
        if (this.fechaEntrega.getValue().isBefore(java.time.LocalDate.now())) {
            mostrarError("La fecha de entrega no puede ser anterior a hoy");
            return false;
        }

        // Validar que haya al menos un producto completo
        long productosCompletos = listaProductos.stream()
                .filter(this::esProductoCompleto)
                .count();

        if (productosCompletos == 0) {
            mostrarError("Debe agregar al menos un producto completo al pedido");
            return false;
        }

        return true;
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static class ProductoPedido {

        private final SimpleObjectProperty<Producto> producto = new SimpleObjectProperty<>(null);
        private final SimpleStringProperty medida = new SimpleStringProperty("Kilo");
        private final SimpleDoubleProperty cantidad = new SimpleDoubleProperty(0.0);
        private final SimpleDoubleProperty precioUnitario = new SimpleDoubleProperty(0.0);
        private final SimpleDoubleProperty total = new SimpleDoubleProperty(0.0);

        public ProductoPedido() {
            // Listeners para recalcular el total cuando cambian cantidad, precio o medida
            cantidad.addListener((obs, oldVal, newVal) -> {
                calcularTotal();
            });
            precioUnitario.addListener((obs, oldVal, newVal) -> {
                calcularTotal();
            });
            medida.addListener((obs, oldVal, newVal) -> {
                calcularTotal();
            });
        }

        private void calcularTotal() {
            if (getProducto() != null && getCantidad() > 0) {
                double precioBase = getPrecioUnitario();
                double cantidadProducto = getCantidad();
                double totalCalculado = 0.0;

                if ("Unidad".equalsIgnoreCase(getMedida())) {
                    // Para unidades: (cantidad × pesoPorUnidad) × precio
                    Double pesoPorUnidad = getProducto().getPesoPorUnidad();
                    if (pesoPorUnidad != null && pesoPorUnidad > 0) {
                        double kilosTotales = cantidadProducto * pesoPorUnidad;
                        totalCalculado = kilosTotales * precioBase;
                    }
                } else {
                    // Para kilos: cantidad × precio
                    totalCalculado = cantidadProducto * precioBase;
                }

                setTotal(totalCalculado);
            } else {
                setTotal(0.0);
            }
        }

        public Producto getProducto() {
            return producto.get();
        }

        public void setProducto(Producto value) {
            producto.set(value);
        }

        public SimpleObjectProperty<Producto> productoProperty() {
            return producto;
        }

        public String getMedida() {
            return medida.get();
        }

        public void setMedida(String value) {
            medida.set(value);
        }

        public SimpleStringProperty medidaProperty() {
            return medida;
        }

        public double getCantidad() {
            return cantidad.get();
        }

        public void setCantidad(double value) {
            cantidad.set(value);
        }

        public SimpleDoubleProperty cantidadProperty() {
            return cantidad;
        }

        public double getPrecioUnitario() {
            return precioUnitario.get();
        }

        public void setPrecioUnitario(double value) {
            precioUnitario.set(value);
        }

        public SimpleDoubleProperty precioUnitarioProperty() {
            return precioUnitario;
        }

        public double getTotal() {
            return total.get();
        }

        public void setTotal(double value) {
            total.set(value);
        }

        public SimpleDoubleProperty totalProperty() {
            return total;
        }
    }
}