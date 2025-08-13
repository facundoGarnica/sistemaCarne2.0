/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import dao.ProductoDAO;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.DetalleVenta;
import model.Producto;
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
    
    private Venta venta;
    private NombreFiadoClienteController nombreFiadoClienteController;
    private ObservableList<Producto> productosEnVenta;
    private double SumarPreciosAPagar = 0.0;
    
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
            return new javafx.beans.property.SimpleDoubleProperty(total).asObject();
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
        txtCodigoDeBarra.focusedProperty().addListener((obs, oldFocused, newFocused) -> {
            if (!newFocused) {
                Platform.runLater(() -> txtCodigoDeBarra.requestFocus());
            }
        });
        
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
                        case ENTER:
                            if (event.getTarget() == txtCodigoDeBarra) {
                                SepararCodigo();
                                event.consume();
                            }
                            break;
                    }
                });
            }
        });
    }

    @FXML
    private void cerrarVentas(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent root = loader.load();

            // Obtener el stage desde el botón que dispara el evento
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void difuminarTodo() {
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
        difuminar.setDisable(true);
        difuminar.setVisible(false);

        overlayNombre.setDisable(true);
        overlayNombre.setVisible(false);
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
            // controller.setClientesController(this); // Por ejemplo
            // Insertar el contenido cargado en el AnchorPane
            overlayNombre.getChildren().add(root);

            // Hacer que el contenido cargado se ajuste al tamaño del AnchorPane
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Metodo para separar el codigo de barra
    @FXML
    public void SepararCodigo() {
        String codigo = txtCodigoDeBarra.getText();

        if (codigo.length() != 13) {
            // error si el código no tiene 13 dígitos
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Código");
            alert.setHeaderText(null);
            alert.setContentText("El código ingresado no es válido. Debe tener 13 dígitos.");
            alert.showAndWait();  // Mostrar el alert y esperar que el usuario lo cierre

        } else {
            try {
                // Extraer partes del código
                String tipoProducto = codigo.substring(0, 2);  // Primeros 2 dígitos
                String codigoProducto = codigo.substring(2, 6); // 4 dígitos 
                String pesoImporte = codigo.substring(6, 12);
                int codigoInt = Integer.parseInt(codigoProducto);
                Double pesoImporteFloat = Double.valueOf(pesoImporte); // 5 dígitos 
                String digitoControl = codigo.substring(13);   // Último dígito

                // convierto peso a formato decimal usando float
                Double peso = pesoImporteFloat / 1000.0f; // Dividir para obtener kg

                System.out.println("Código del producto: " + codigoInt);
                System.out.println("Peso: " + peso + " kg");

                // Llamar a BuscarProducto
                BuscarProducto(codigoInt, tipoProducto, peso);
            } catch (NumberFormatException e) {
                System.out.println("Error: El código contiene caracteres no numéricos.");
            }
        }
        txtCodigoDeBarra.clear();
    }

    public void BuscarProducto(int codigo, String tipoProducto, Double peso) {
        ProductoDAO productoDAO = new ProductoDAO();
        // Buscar el producto en la base de datos
        Producto ProductoEncontrado = productoDAO.buscarPorCodigo(codigo);
        if (ProductoEncontrado == null) {
            System.out.println("No se encontro el producto ");
            // Mostrar alerta de producto no encontrado
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Producto no encontrado");
            alert.setHeaderText(null);
            alert.setContentText("No se encontró ningún producto con el código: " + codigo);
            alert.showAndWait();
            return;
        } else {
            System.out.println("el produto es: " + ProductoEncontrado.getNombre());
        }
        
        if (tipoProducto.equals("21")) {
            ProductoEncontrado.setPesoParaVender(1.00);
        } else {
            ProductoEncontrado.setPesoParaVender(peso);
        }

        // Agregar el producto a la tabla
        agregarProductoATabla(ProductoEncontrado);
    }
    
    // Método para agregar producto a la tabla
    private void agregarProductoATabla(Producto producto) {
        productosEnVenta.add(producto);
        SumarPreciosAPagar = SumarPreciosAPagar + (producto.getPesoParaVender() * producto.getPrecio());
        lblCantidadPagar.setText(String.valueOf(SumarPreciosAPagar));
        System.out.println("Producto agregado a la tabla: " + producto.getNombre() + " - $" + producto.getPrecio());
        
        // Mantener el foco en el campo de código de barras
        Platform.runLater(() -> {
            txtCodigoDeBarra.requestFocus();
        });
    }
    
    // Método para limpiar la tabla (útil para nuevas ventas)
    public void limpiarTabla() {
        productosEnVenta.clear();
        SumarPreciosAPagar = 0.0;
        lblCantidadPagar.setText("0.0");
    }
    
    // Método para obtener la lista de productos (útil para procesar la venta)
    public ObservableList<Producto> getProductosEnVenta() {
        return productosEnVenta;
    }
}