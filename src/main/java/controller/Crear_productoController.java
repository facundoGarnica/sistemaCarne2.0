/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import dao.ProductoDAO;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import model.Producto;

/**
 * FXML Controller class
 *
 * @author garca
 */
public class Crear_productoController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private TextField txtNombre;
    @FXML
    private int codigo;
    @FXML
    private TextField txtPrecio;
    @FXML
    private ComboBox cbxCategoria; //combobox
    @FXML
    private TextField txtPeso;
    @FXML
    Spinner<Integer> spinnerCodigo; //variable spinner

    private Producto_menuController productoMenuController;
    private Producto producto;
    private ProductoDAO productoDao;
    private Producto productoActual = null; // Variable para saber si se edita o crea

    public void initialize(URL url, ResourceBundle rb) {
        productoDao = new ProductoDAO();
        // Cargar opciones del ComboBox
        cbxCategoria.getItems().addAll(
                "Carniceria", "Cerdo", "Pollo", "Varios", "Seco", "Preparados"
        );

        // Valor por defecto
        cbxCategoria.setValue("Carniceria");

        // Configurar Spinner (no crear uno nuevo)
        SpinnerValueFactory<Integer> valueFactory
                = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        spinnerCodigo.setValueFactory(valueFactory);
    }

    public void setSpa_productoController(Producto_menuController p) {
        productoMenuController = p;
    }

    @FXML
    public void cerrarOverlay() {
        if (productoMenuController != null) {
            productoMenuController.CerrarDifuminarYSpa();
        }
    }

    //guardar producto
    @FXML
    public void guardarProducto() {
        // Validar nombre
        if (txtNombre.getText().trim().isEmpty()) {
            System.out.println("El nombre no puede estar vacío");
            return;
        }

        // Validar precio
        double precio;
        try {
            precio = Double.parseDouble(txtPrecio.getText());
            if (precio <= 0) {
                System.out.println("El precio debe ser mayor que 0");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Precio inválido");
            return;
        }

        // Validar peso
        double kgPromedio;
        try {
            kgPromedio = Double.parseDouble(txtPeso.getText());
            if (kgPromedio <= 0) {
                System.out.println("El peso debe ser mayor que 0");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Peso inválido");
            return;
        }

        // Validar categoría
        if (cbxCategoria.getValue() == null) {
            System.out.println("Debe seleccionar una categoría");
            return;
        }

        // Validar código
        int codigo = spinnerCodigo.getValue();
        if (codigo <= 0) {
            System.out.println("Código inválido");
            return;
        }

        // Si productoActual es null, significa que estamos creando uno nuevo
        if (productoActual == null) {
            productoActual = new Producto();
        }

        // Asignar valores al productoActual
        productoActual.setNombre(txtNombre.getText().trim());
        productoActual.setCodigo(codigo);
        productoActual.setPrecio(precio);
        productoActual.setTipo(String.valueOf(cbxCategoria.getValue()));
        productoActual.setPesoPorUnidad(kgPromedio);

        // Guardar o actualizar según corresponda
        if (productoActual.getId() == null) { // Nuevo producto
            productoDao.guardar(productoActual);
            System.out.println("Producto guardado correctamente");
        } else { // Producto existente
            productoDao.actualizar(productoActual);
            System.out.println("Producto actualizado correctamente");
        }

        cerrarOverlay();
        limpiarFormulario(); // Limpio formulario para la próxima vez
    }

    public void asignarProductoEditable(Producto producto) {
        this.productoActual = producto; // guardo referencia para editar
        if (producto != null) {
            txtNombre.setText(producto.getNombre() != null ? producto.getNombre() : "");
            spinnerCodigo.getValueFactory().setValue(producto.getCodigo());
            txtPrecio.setText(producto.getPrecio() != null ? producto.getPrecio().toString() : "");
            cbxCategoria.setValue(producto.getTipo());
            txtPeso.setText(producto.getPesoPorUnidad() != null ? producto.getPesoPorUnidad().toString() : "");
        } else {
            limpiarFormulario();
        }
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        spinnerCodigo.getValueFactory().setValue(1);
        txtPrecio.clear();
        cbxCategoria.setValue(null);
        txtPeso.clear();
        productoActual = null;
    }

    public void asignarDatos(String nombre, Integer codigo, Double precio, String tipo, Double peso) {
        if (nombre != null) {
            txtNombre.setText(nombre);
        } else {
            txtNombre.clear();
        }

        if (codigo != null) {
            spinnerCodigo.getValueFactory().setValue(codigo);
        } else {
            spinnerCodigo.getValueFactory().setValue(1);  // valor por defecto
        }

        if (precio != null) {
            txtPrecio.setText(String.valueOf(precio));
        } else {
            txtPrecio.clear();
        }

        if (tipo != null) {
            cbxCategoria.setValue(tipo);
        } else {
            cbxCategoria.setValue(null);
        }

        if (peso != null) {
            txtPeso.setText(String.valueOf(peso));
        } else {
            txtPeso.clear();
        }
    }

}
