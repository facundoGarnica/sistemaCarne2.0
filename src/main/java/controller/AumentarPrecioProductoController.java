package controller;

import dao.ProductoDAO;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Producto;

public class AumentarPrecioProductoController implements Initializable {

    @FXML
    private TableView<Producto> tablaProductos;

    @FXML
    private TableColumn<Producto, Integer> colCodigo;

    @FXML
    private TableColumn<Producto, String> colNombre;

    @FXML
    private TableColumn<Producto, Double> colPrecioActual;

    @FXML
    private TableColumn<Producto, Double> colAumento;

    @FXML
    private TableColumn<Producto, Double> colPrecioNuevo;

    @FXML
    private TableColumn<Producto, String> colCategoria;

    @FXML
    private ComboBox<String> cmbCategoria;

    @FXML
    private TextField txtPorcentaje;

    private Producto_menuController productoController;

    private List<Producto> productosPorTipo;
    private ProductoDAO productoDao;
    private Double porcentajeActual = 0.0;

    private final DecimalFormat df = new DecimalFormat("#,##0"); 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cargar opciones del ComboBox
        cmbCategoria.getItems().addAll(
                "Carniceria", "Cerdo", "Pollo", "Varios", "Seco", "Preparados"
        );

        // Valor por defecto
        cmbCategoria.setValue("Carniceria");

        // Configurar columnas normales
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colPrecioActual.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        // Columnas calculadas con base en porcentajeActual
        colAumento.setCellValueFactory(cellData -> {
            Producto p = cellData.getValue();
            Double precioOriginal = p.getPrecio() != null ? p.getPrecio() : 0.0;
            Double aumento = precioOriginal * (porcentajeActual / 100);
            return new SimpleObjectProperty<>(aumento);
        });
        // Formatear columna Aumento a 2 decimales
        colAumento.setCellFactory(column -> new TableCell<Producto, Double>() {
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

        colPrecioNuevo.setCellValueFactory(cellData -> {
            Producto p = cellData.getValue();
            Double precioOriginal = p.getPrecio() != null ? p.getPrecio() : 0.0;
            Double precioConAumento = precioOriginal * (1 + porcentajeActual / 100);
            return new SimpleObjectProperty<>(precioConAumento);
        });
        // Formatear columna Precio Nuevo a 2 decimales
        colPrecioNuevo.setCellFactory(column -> new TableCell<Producto, Double>() {
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
    }

    public void setSpa_productoController(Producto_menuController c) {
        productoController = c;
    }

    @FXML
    public void cerrarOverlay() {
        if (productoController != null) {
            productoController.CerrarDifuminarYSpa();
        }
    }

    @FXML
    public void aumentarPrecios() {
        String tipoObtenido = String.valueOf(cmbCategoria.getValue());

        try {
            porcentajeActual = Double.parseDouble(txtPorcentaje.getText());
        } catch (NumberFormatException e) {
            System.out.println("Porcentaje inválido");
            porcentajeActual = 0.0;
            return;
        }

        productoDao = new ProductoDAO();
        productosPorTipo = productoDao.obtenerProductoPorTipo(tipoObtenido);

        tablaProductos.setItems(FXCollections.observableArrayList(productosPorTipo));
        tablaProductos.refresh();
    }
}
