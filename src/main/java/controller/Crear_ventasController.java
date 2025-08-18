/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import Dto.AlertaStockDTO;
import Util.CalcularVuelto;
import Util.DatosPagos;
import Util.HibernateUtil;
import Util.MercadoPagoApi;
import Util.Transaccion;
import dao.DetalleVentaDAO;
import dao.ProductoDAO;
import dao.StockDAO;
import dao.VentaDAO;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.DetalleVenta;
import model.Producto;
import model.Stock;
import model.Venta;

/**
 * FXML Controller class
 *
 * @author garca
 */
public class Crear_ventasController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private boolean overlayActivo = false;
    private Timeline temporizadorActual;
    private int indicePagoActual = 0;
    StockProductoController stockController = new StockProductoController();
    private List<AlertaStockDTO> listaStockBajo;
    private MercadoPagoApi MP;
    private List<DatosPagos> datos;
    private Venta venta;
    private NombreFiadoClienteController nombreFiadoClienteController;
    private ObservableList<Producto> productosEnVenta;
    private double SumarPreciosAPagar = 0.0;
    private VentaDAO ventaDao;
    private DetalleVentaDAO detalleVentaDao;
    // Cache para evitar crear DAOs repetidamente
    private ProductoDAO productoDAO;
    private StockDAO stockDao;

    @FXML
    private ImageView imagenSigno;
    @FXML
    private TextField txtCodigoDeBarra;

    @FXML
    private AnchorPane overlayNombre;
    @FXML
    private AnchorPane difuminar;
    @FXML
    private TableView<Producto> tblVistaProductos;
    @FXML
    private TableColumn<Producto, String> colProducto;
    @FXML
    private TableColumn<Producto, Double> colPrecio;
    @FXML
    private TableColumn<Producto, Double> colPeso;
    @FXML
    private TableColumn<Producto, Double> colTotal;
    @FXML
    private Label lblCantidadPagar;
    @FXML
    private Label lblMedioPago;

    @FXML
    private TableView<Transaccion> tbolVueltos;

    @FXML
    private TableColumn<Transaccion, String> colRecibido;

    @FXML
    private TableColumn<Transaccion, String> colVuelto;

    //Variables para guardar cliente
    //variables para mostrar mercadoPago
    @FXML
    private Label lblMonto;
    @FXML
    private Label lblHora;
    @FXML
    private Label lblEstado;
    @FXML
    private VBox vBoxAprobado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        tbolVueltos.setPlaceholder(new Label("")); // deja el placeholder vacío
        tblVistaProductos.setPlaceholder(new Label("")); // deja el placeholder vacío

        // Inicializar DAOs una sola vez para mejorar rendimiento
        ventaDao = new VentaDAO();
        detalleVentaDao = new DetalleVentaDAO();
        productoDAO = new ProductoDAO(); // Cache del ProductoDAO
        //tablas de calcular vueltos
        colRecibido.setCellValueFactory(new PropertyValueFactory<>("dineroRecibido"));
        colVuelto.setCellValueFactory(new PropertyValueFactory<>("vuelto"));
        // Inicializar la lista observable
        productosEnVenta = FXCollections.observableArrayList();

        // Configurar las columnas de la tabla
        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPeso.setCellValueFactory(new PropertyValueFactory<>("pesoParaVender"));

        // Configurar columna Total con cálculo personalizado
        colTotal.setCellValueFactory(cellData -> {
            Producto producto = cellData.getValue();
            double total = producto.getPrecio() * producto.getPesoParaVender();
            double totalRedondeado = Math.round(total); // Redondeo simple
            return new javafx.beans.property.SimpleDoubleProperty(totalRedondeado).asObject();
        });

        // Configurar la tabla con la lista
        tblVistaProductos.setItems(productosEnVenta);

        // Mantener foco siempre en el TextField
        Platform.runLater(() -> txtCodigoDeBarra.requestFocus());

        // Deshabilitar selección en las tablas y evitar que capture el foco
        tblVistaProductos.setFocusTraversable(false);
        tblVistaProductos.setSelectionModel(null);
        tblVistaProductos.getColumns().forEach(column -> column.setSortable(false));
        tblVistaProductos.setOnMouseClicked(event -> event.consume());

        // Evitar que el TextField capture el foco y bloquee eventos
        txtCodigoDeBarra.setFocusTraversable(false);

        // Listener para mantener el foco en el TextField
        // MODIFICAR este listener para considerar el estado del overlay
        txtCodigoDeBarra.focusedProperty().addListener((obs, oldFocused, newFocused) -> {
            if (!newFocused && !overlayActivo) { // Solo refocus si no hay overlay activo
                Platform.runLater(() -> txtCodigoDeBarra.requestFocus());
            }
        });

        // Inicializar valores por defecto
        lblCantidadPagar.setText("$ 0.00");
        lblMedioPago.setText("---");

        // Configurar eventos globales de teclado cuando la escena esté lista
        txtCodigoDeBarra.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                System.out.println("Escena detectada. Activando eventos de teclado.");

                // Usar addEventFilter para capturar eventos antes que otros elementos
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    switch (event.getCode()) {
                        case F1:
                            lblMedioPago.setText("Efectivo");
                            event.consume();
                            break;
                        case F2:
                            lblMedioPago.setText("Virtual");
                            event.consume();
                            break;
                        case F4:
                            MostrarTransferencias();
                            event.consume();
                            break;
                        case F5:
                            guardarVenta();
                            event.consume();
                            break;
                        case F7:
                            AgregarHuesos();
                            event.consume();
                            break;
                        case F8:
                            AgregarCarbon();
                            event.consume();
                            break;
                        case F9:
                            AgregarHuevos();
                            event.consume();
                            break;
                        case F11:
                            difuminarTodo();
                            event.consume();
                            break;
                        case F12:
                            limpiarTodo();
                            event.consume();
                            break;
                        case LEFT:
                            pagoAnterior();
                            event.consume();  // Bloquea propagación
                            break;
                        case RIGHT:
                            pagoSiguiente();
                            event.consume();  // Bloquea propagación
                            break;
                        case ENTER:
                            if (event.getTarget() == txtCodigoDeBarra) {
                                SepararCodigo();
                                event.consume();
                            }
                            break;
                        case ESCAPE:
                            // Limpiar todo con ESC
                            limpiarTodo();
                            event.consume();
                            break;
                    }
                });
            }
        });
        new Thread(() -> {
            try {
                System.out.println("Inicializando Hibernate...");
                long inicio = System.currentTimeMillis();

                // Forzar inicialización de SessionFactory
                HibernateUtil.getSessionFactory();

                // Hacer una consulta simple para calentar todo
                productoDAO.buscarPorCodigo(-1); // Código inexistente

                long tiempo = System.currentTimeMillis() - inicio;
                System.out.println("✓ Hibernate inicializado en " + tiempo + "ms");

            } catch (Exception e) {
                System.out.println("Precalentamiento completado");
            }
        }).start();
        ImagenStock();
        productosBajos();
    }

    public void productosBajos() {
        try {
            System.out.println("=== Verificando productos con stock bajo ===");

            // Usar los DAOs directamente
            StockDAO stockDao = new StockDAO();
            List<Stock> listaStock = stockDao.buscarTodos();

            listaStockBajo = new ArrayList<>();

            for (Stock stock : listaStock) {
                // Lógica igual que en StockProductoController
                double cantidad = stock.getCantidad();
                double minima = stock.getCantidadMinima();

                String estado = "";
                boolean esStockBajo = false;

                if (cantidad == 0) {
                    // Sin stock - no incluir en alertas
                    continue;
                } else if (cantidad <= minima) {
                    estado = "Crítico";
                    esStockBajo = true;
                } else if (cantidad <= minima * 1.5) {
                    estado = "Bajo";
                    esStockBajo = true;
                }

                if (esStockBajo) {
                    listaStockBajo.add(new AlertaStockDTO(
                            stock.getProducto().getNombre(),
                            estado,
                            stock.getCantidad()
                    ));
                }
            }

            if (!listaStockBajo.isEmpty()) {
                System.out.println("✓ Encontrados " + listaStockBajo.size() + " productos con stock bajo:");
                for (AlertaStockDTO alerta : listaStockBajo) {
                    System.out.println("  - " + alerta.getNombreProducto()
                            + " | Estado: " + alerta.getEstado()
                            + " | Cantidad: " + alerta.getCantidad());
                }
            } else {
                System.out.println("ℹ️  No hay productos con stock bajo actualmente");
            }

        } catch (Exception e) {
            System.err.println("❌ Error al verificar stock bajo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void ImagenStock() {
        ScaleTransition st = new ScaleTransition(Duration.seconds(1), imagenSigno);
        st.setToX(1.2); // 20% más ancho
        st.setToY(1.2); // 20% más alto
        st.setAutoReverse(true);
        st.setCycleCount(ScaleTransition.INDEFINITE);

        // Cargar solo la imagen roja
        Image rojo = new Image(getClass().getResource("/images/peligroRojo.png").toExternalForm());

        // Al iniciar la animación, siempre usa la roja
        imagenSigno.setImage(rojo);

        st.play();
    }

    public String getMedioPago() {
        return lblMedioPago.getText();
    }

    public void AgregarHuesos() {
        txtCodigoDeBarra.setText("2100480000014");
        SepararCodigo();
    }

    public void AgregarCarbon() {
        txtCodigoDeBarra.setText("2100590000010");
        SepararCodigo();
    }

    public void AgregarHuevos() {
        txtCodigoDeBarra.setText("2100620000010");
        SepararCodigo();
    }

    public boolean isOverlayActivo() {
        return overlayActivo;
    }

    public void setMedioPagoFiado() {
        lblMedioPago.setText("Fiado");
    }

    @FXML
    public void MostrarTransferencias() {
        MP = new MercadoPagoApi();
        MP.obtenerUltimosPagos();
        datos = MP.getListaDatos();

        // Mostrar el primer pago (más reciente) por defecto
        indicePagoActual = 0;
        actualizarPago();
        // Iniciar el temporizador que limpia la información después de 10 segundos
        iniciarTemporizadorLimpieza();
    }

    private void limpiarDatosMP() {
        // Limpiar los labels
        lblMonto.setText("--");
        lblHora.setText("--");
        lblEstado.setText("--");

        // Cambiar el fondo de AnchorPane a blanco
        vBoxAprobado.setStyle("-fx-background-color: white;");
    }

    private void actualizarPago() {
        if (datos.isEmpty() || indicePagoActual < 0 || indicePagoActual >= datos.size()) {
            return; // Evita errores si la lista está vacía o el índice es inválido
        }

        DatosPagos pago = datos.get(indicePagoActual);
        lblMonto.setText(String.valueOf(pago.getMonto()));
        lblHora.setText(pago.getFecha());

        if (pago.getEstado().equals("approved")) {
            lblEstado.setText("Si");
            vBoxAprobado.setStyle("-fx-background-color: #7FFFD4;");
        } else {
            lblEstado.setText("No");
            vBoxAprobado.setStyle("-fx-background-color: #FF6347;"); // Cambié el color para diferenciar estados
        }
    }

    @FXML
    public void pagoSiguiente() {
        if (indicePagoActual > 0) {
            indicePagoActual--;
            actualizarPago();
            reiniciarTemporizadorLimpieza();
        }
    }

    @FXML
    public void pagoAnterior() {
        if (indicePagoActual < datos.size() - 1) {
            indicePagoActual++;
            actualizarPago();
            reiniciarTemporizadorLimpieza();
        }
    }
// Nuevo método para reiniciar el temporizador

    private void reiniciarTemporizadorLimpieza() {
        // Detener el temporizador anterior si existe
        if (temporizadorActual != null) {
            temporizadorActual.stop();
        }

        // Crear nuevo temporizador
        temporizadorActual = new Timeline(new KeyFrame(Duration.seconds(10), e -> limpiarDatosMP()));
        temporizadorActual.setCycleCount(1);
        temporizadorActual.play();
    }

// Método corregido iniciarTemporizadorLimpieza
    private void iniciarTemporizadorLimpieza() {
        reiniciarTemporizadorLimpieza(); // Usar el nuevo método
    }

    @FXML
    private void cerrarVentas(ActionEvent event) {
        // Verificar si hay una venta en progreso
        if (!productosEnVenta.isEmpty() && SumarPreciosAPagar > 0) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar salida");
            confirmAlert.setHeaderText("Hay una venta en progreso");
            confirmAlert.setContentText("¿Está seguro que desea salir? Se perderán los datos no guardados.");

            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent root = loader.load();

            // Obtener el stage desde el botón que dispara el evento
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo cargar el menú principal.");
        }
    }

    public void difuminarTodo() {
        setMedioPagoFiado();
        // Validar que haya productos en la venta
        if (!validarVentaParaProcesar()) {
            return;
        }

        // Marcar que el overlay está activo
        overlayActivo = true;

        // Quitar el foco del campo de código de barras
        txtCodigoDeBarra.setFocusTraversable(false);

        difuminar.setVisible(true);
        difuminar.setDisable(false);

        overlayNombre.setVisible(true);
        overlayNombre.setDisable(false);

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

        // Marcar que el overlay ya no está activo
        overlayActivo = false;

        difuminar.setDisable(true);
        difuminar.setVisible(false);

        overlayNombre.setDisable(true);
        overlayNombre.setVisible(false);

        // Restaurar la capacidad de recibir foco del campo de código de barras
        txtCodigoDeBarra.setFocusTraversable(true);

        // Retomar foco en el campo de código de barras
        Platform.runLater(() -> txtCodigoDeBarra.requestFocus());
    }

    @FXML
    public void invocarSpaCrearPedido() {  //llama a detalle de fiados
        try {
            overlayNombre.getChildren().clear(); // Limpiar el AnchorPane destino

            // Cargar el FXML spa_clientes.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/nombreFiadoCliente.fxml"));
            Parent root = loader.load();
            nombreFiadoClienteController = loader.getController();
            nombreFiadoClienteController.setSpa_creaVentasController(this);

            // Insertar el contenido cargado en el AnchorPane
            overlayNombre.getChildren().add(root);

            // Hacer que el contenido cargado se ajuste al tamaño del AnchorPane
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error", "No se pudo cargar la ventana de cliente fiado.");
            CerrarDifuminarYSpa();
        }
    }

    //Metodo para separar el codigo de barra
    @FXML
    public void SepararCodigo() {
        String codigo = txtCodigoDeBarra.getText().trim();

        // Validar que el código no esté vacío
        if (codigo.isEmpty()) {
            mostrarAdvertencia("Campo vacío", "Por favor ingrese un código de barras.");
            return;
        }

        if (codigo.length() != 13) {
            // Error si el código no tiene 13 dígitos
            mostrarError("Código inválido",
                    "El código ingresado no es válido. Debe tener exactamente 13 dígitos.\n"
                    + "Código ingresado: " + codigo + " (longitud: " + codigo.length() + ")");
            limpiarCampoCodigoYEnfocar();
        } else {
            try {
                // Validar que todos los caracteres sean números
                if (!codigo.matches("\\d{13}")) {
                    mostrarError("Código inválido", "El código debe contener solo números.");
                    limpiarCampoCodigoYEnfocar();
                    return;
                }

                // Extraer partes del código
                String tipoProducto = codigo.substring(0, 2);  // Primeros 2 dígitos
                String codigoProducto = codigo.substring(2, 6); // 4 dígitos 
                String pesoImporte = codigo.substring(6, 12);   // 6 dígitos para peso/importe
                String digitoControl = codigo.substring(12);    // Último dígito

                int codigoInt = Integer.parseInt(codigoProducto);
                Double pesoImporteFloat = Double.valueOf(pesoImporte);

                // Convertir peso a formato decimal
                Double peso = pesoImporteFloat / 1000.0; // Dividir para obtener kg

                System.out.println("Tipo de producto: " + tipoProducto);
                System.out.println("Código del producto: " + codigoInt);
                System.out.println("Peso: " + peso + " kg");
                System.out.println("Dígito de control: " + digitoControl);

                // Llamar a BuscarProducto
                BuscarProducto(codigoInt, tipoProducto, peso);

            } catch (NumberFormatException e) {
                System.out.println("Error: El código contiene caracteres no numéricos.");
                mostrarError("Error de formato", "El código contiene caracteres inválidos.");
                limpiarCampoCodigoYEnfocar();
            } catch (Exception e) {
                System.out.println("Error inesperado al procesar código: " + e.getMessage());
                mostrarError("Error", "Ocurrió un error al procesar el código de barras.");
                limpiarCampoCodigoYEnfocar();
            }
        }
    }

    public void BuscarProducto(int codigo, String tipoProducto, Double peso) {
        System.out.println("Buscando producto con código: " + codigo); // Debug

        try {
            // Usar el DAO cacheado en lugar de crear uno nuevo cada vez
            Producto ProductoEncontrado = productoDAO.buscarPorCodigo(codigo);

            if (ProductoEncontrado == null) {
                System.out.println("No se encontró el producto con código: " + codigo);
                mostrarAdvertencia("Producto no encontrado",
                        "No se encontró ningún producto con el código: " + codigo);
                limpiarCampoCodigoYEnfocar();
                return;
            }

            System.out.println("Producto encontrado: " + ProductoEncontrado.getNombre());

            // CAMBIO IMPORTANTE: Usar setPeso en lugar de setPesoParaVender
            if (tipoProducto.equals("21")) {
                // Producto por unidad
                ProductoEncontrado.setPesoParaVender(1.00);
            } else {
                // Producto por peso
                if (peso <= 0) {
                    mostrarAdvertencia("Peso inválido", "El peso del producto debe ser mayor a 0.");
                    limpiarCampoCodigoYEnfocar();
                    return;
                }
                ProductoEncontrado.setPesoParaVender(peso);
            }

            // Agregar el producto a la tabla
            agregarProductoATabla(ProductoEncontrado);

        } catch (Exception e) {
            System.out.println("Error al buscar producto: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error de base de datos", "No se pudo consultar el producto en la base de datos.");
            limpiarCampoCodigoYEnfocar();
        }
    }

    // Método para agregar producto a la tabla
    private void agregarProductoATabla(Producto producto) {
        try {
            productosEnVenta.add(producto);

            // Sumar el subtotal
            SumarPreciosAPagar += producto.getPesoParaVender() * producto.getPrecio();

            // Aplicar el redondeo especial
            int entero = (int) Math.round(SumarPreciosAPagar);
            int unidades = entero % 10;

            if (unidades >= 6) {
                entero = (entero / 10) * 10 + 10; // redondea hacia arriba
            } else {
                entero = (entero / 10) * 10; // redondea hacia abajo
            }

            // Actualizar el total redondeado
            double totalRedondeado = entero;

            // Mostrar en label
            lblCantidadPagar.setText(String.format("$ %, .0f", totalRedondeado));

            // Calcular vueltos
            CalcularVuelto calculador = new CalcularVuelto();
            calculador.CalcularVuelto(totalRedondeado);

            // Llenar la tabla con los posibles pagos/vueltos
            ObservableList<Transaccion> lista = FXCollections.observableArrayList(calculador.GetListaTransacciones());
            tbolVueltos.setItems(lista);

            // Limpiar campo y mantener foco
            limpiarCampoCodigoYEnfocar();

        } catch (Exception e) {
            System.out.println("Error al agregar producto a la tabla: " + e.getMessage());
            mostrarError("Error", "No se pudo agregar el producto a la venta.");
        }
    }

    // Método para limpiar la tabla (útil para nuevas ventas)
    public void limpiarTabla() {
        productosEnVenta.clear();
        SumarPreciosAPagar = 0.0;
        lblCantidadPagar.setText("$ 0.00");
        System.out.println("Tabla de productos limpiada.");
    }

    // Método para obtener la lista de productos (lo uso para procesar la venta)
    public ObservableList<Producto> getProductosEnVenta() {
        return productosEnVenta;
    }

    // Método para validar que la venta esté lista para procesar
    private boolean validarVentaParaProcesar() {
        if (productosEnVenta.isEmpty()) {
            mostrarAdvertencia("Venta vacía", "Debe agregar al menos un producto para generar la venta.");
            return false;
        }

        if (SumarPreciosAPagar <= 0) {
            mostrarAdvertencia("Total inválido", "El total de la venta debe ser mayor a $0.00");
            return false;
        }

        if (lblMedioPago.getText().equals("---")) {
            mostrarAdvertencia("Medio de pago",
                    "Debe seleccionar un medio de pago.\n"
                    + "Presione F1 para Efectivo, F2 para Virtual o F11 para Fiado.");
            return false;
        }

        return true;
    }

    public void guardarVenta() {
        if (!validarVentaParaProcesar()) {
            return;
        }

        try {
            System.out.println("=== INICIANDO GUARDADO DE VENTA ===");

            // Calcular total redondeado tal como se muestra en lblCantidadPagar
            int entero = (int) Math.round(SumarPreciosAPagar);
            int unidades = entero % 10;
            if (unidades >= 6) {
                entero = (entero / 10) * 10 + 10; // redondea hacia arriba
            } else {
                entero = (entero / 10) * 10;       // redondea hacia abajo
            }
            double totalRedondeado = entero;
            System.out.println("Total redondeado a guardar: $" + totalRedondeado);

            // Crear nueva venta
            venta = new Venta();
            venta.setFecha(LocalDateTime.now());
            venta.setMedioPago(lblMedioPago.getText());
            venta.setTotal(totalRedondeado);

            System.out.println("Datos de la venta:");
            System.out.println("- Total: $" + totalRedondeado);
            System.out.println("- Medio de pago: " + lblMedioPago.getText());
            System.out.println("- Fecha: " + venta.getFecha());
            System.out.println("- Productos a procesar: " + productosEnVenta.size());

            // Guardar venta principal en base de datos
            ventaDao.guardar(venta);
            System.out.println("✓ Venta principal guardada");
            System.out.println("✓ ID de venta: " + venta.getId());

            // Guardar detalles de venta
            System.out.println("\n=== GUARDANDO DETALLES DE VENTA ===");
            GenerarDetalleVenta();

            System.out.println("✓ VENTA PROCESADA CORRECTAMENTE");

            // Limpiar todo después de guardar exitosamente
            limpiarTodo();
            System.out.println("✓ Sistema limpiado - listo para nueva venta");

        } catch (Exception e) {
            System.err.println("\n✗ ERROR CRÍTICO EN GUARDADO DE VENTA");
            System.err.println("✗ Mensaje: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al procesar venta",
                    "No se pudo completar el procesamiento de la venta:\n\n" + e.getMessage()
                    + "\n\nVerifique:\n"
                    + "• Conexión a la base de datos\n"
                    + "• Configuración de entidades (Venta, DetalleVenta, Producto)\n"
                    + "• Relaciones en Hibernate/JPA\n"
                    + "• Que todos los productos tengan ID válido\n"
                    + "• Transacciones de base de datos");
        }
    }

    // Método extraído del controlador de referencia para generar detalles
    public void GenerarDetalleVenta() {
        for (Producto producto : productosEnVenta) {
            DetalleVenta nuevoDetalle = new DetalleVenta();
            nuevoDetalle.setVenta(venta);
            nuevoDetalle.setProducto(producto);
            nuevoDetalle.setPeso(producto.getPesoParaVender());
            nuevoDetalle.setPrecio(producto.getPrecio());

            // Guardar el detalle de la venta en la base de datos
            detalleVentaDao.guardar(nuevoDetalle);
            System.out.println("Detalle ID: " + nuevoDetalle.getId());

            // --- DESCONTAR STOCK ---
            if (producto.getStock() != null && producto.getPesoParaVender() != null) {
                stockDao = new StockDAO();
                stockDao.descontarStockPorProducto(producto);
                // Opcional: reiniciar pesoParaVender después de descontar
                producto.setPesoParaVender(0.0);
            }
        }
    }

    public void limpiarTodo() {
        try {
            // Limpiar tabla y totales
            limpiarTabla();

            // Resetear medio de pago
            lblMedioPago.setText("---");

            // Limpiar campo de código de barras
            txtCodigoDeBarra.clear();

            // Resetear venta
            venta = null;

            tbolVueltos.getItems().clear();
            tbolVueltos.setPlaceholder(new Label("")); // deja el placeholder vacío
            tblVistaProductos.setPlaceholder(new Label("")); // deja el placeholder vacío
            // Retomar foco en el campo
            Platform.runLater(() -> txtCodigoDeBarra.requestFocus());

            System.out.println("Sistema limpiado - listo para nueva venta.");

        } catch (Exception e) {
            System.out.println("Error al limpiar sistema: " + e.getMessage());
        }
    }

    // Métodos de utilidad para mostrar alertas
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void limpiarCampoCodigoYEnfocar() {
        txtCodigoDeBarra.clear();
        Platform.runLater(() -> txtCodigoDeBarra.requestFocus());
    }

    // Getter para el total (útil para otros controladores)
    public double getTotalVenta() {
        return SumarPreciosAPagar;
    }

    // Método público para ser llamado desde otros controladores (ej: nombreFiadoClienteController)
    public void procesarVentaFiado() {
        guardarVenta();
    }

    // Método para obtener información de la venta actual
    public String getResumenVenta() {
        if (productosEnVenta.isEmpty()) {
            return "No hay productos en la venta actual";
        }

        StringBuilder resumen = new StringBuilder();
        resumen.append("=== RESUMEN DE VENTA ===\n");
        resumen.append("Productos: ").append(productosEnVenta.size()).append("\n");
        resumen.append("Total: $").append(String.format("%.2f", SumarPreciosAPagar)).append("\n");
        resumen.append("Medio de pago: ").append(lblMedioPago.getText()).append("\n");
        resumen.append("\nDetalle:\n");

        for (Producto p : productosEnVenta) {
            resumen.append("- ").append(p.getNombre())
                    .append(" x $").append(p.getPrecio())
                    .append("\n");
        }

        return resumen.toString();
    }
}
