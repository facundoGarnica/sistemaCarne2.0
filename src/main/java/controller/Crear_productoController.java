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

    @FXML
    private TextField txtNombre;
    @FXML
    private int codigo;
    @FXML
    private TextField txtPrecio;
    @FXML
    private ComboBox cbxCategoria;
    @FXML
    private TextField txtPeso;
    @FXML
    Spinner<Integer> spinnerCodigo;

    private Producto_menuController productoMenuController;
    private Producto producto;
    private ProductoDAO productoDao;
    private Producto productoActual = null;

    public void initialize(URL url, ResourceBundle rb) {
        productoDao = new ProductoDAO();
        cbxCategoria.getItems().addAll(
                "Carniceria", "Cerdo", "Pollo", "Varios", "Seco", "Preparados", "Achuras"
        );
        cbxCategoria.setValue("Carniceria");
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
        limpiarFormulario();
    }

    private boolean guardarProductoBase() {
        if (txtNombre.getText().trim().isEmpty()) {
            System.out.println("El nombre no puede estar vacío");
            return false;
        }

        double precio;
        try {
            precio = Double.parseDouble(txtPrecio.getText());
            if (precio <= 0) {
                System.out.println("El precio debe ser mayor que 0");
                return false;
            }
        } catch (NumberFormatException e) {
            System.out.println("Precio inválido");
            return false;
        }

        double kgPromedio;
        try {
            kgPromedio = Double.parseDouble(txtPeso.getText());
            if (kgPromedio <= 0) {
                System.out.println("El peso debe ser mayor que 0");
                return false;
            }
        } catch (NumberFormatException e) {
            System.out.println("Peso inválido");
            return false;
        }

        if (cbxCategoria.getValue() == null) {
            System.out.println("Debe seleccionar una categoría");
            return false;
        }

        int codigo = spinnerCodigo.getValue();
        if (codigo <= 0) {
            System.out.println("Código inválido");
            return false;
        }

        if (productoActual == null) {
            productoActual = new Producto();
        }

        productoActual.setNombre(txtNombre.getText().trim());
        productoActual.setCodigo(codigo);
        productoActual.setPrecio(precio);
        productoActual.setTipo(String.valueOf(cbxCategoria.getValue()));
        productoActual.setPesoPorUnidad(kgPromedio);

        if (productoActual.getId() == null) {
            productoDao.guardar(productoActual);
            System.out.println("Producto guardado correctamente");
        } else {
            productoDao.actualizar(productoActual);
            System.out.println("Producto actualizado correctamente");
        }

        if (productoMenuController != null) {
            productoMenuController.recargarTablaProductos();
            productoMenuController.cargarContador();
        }

        return true;
    }

    @FXML
    public void guardarProducto() {
        if (guardarProductoBase()) {
            limpiarFormulario();
        }
    }

    public void asignarProductoEditable(Producto producto) {
        this.productoActual = producto;
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

    private void limpiarFormularioParcial() {
        txtNombre.clear();
        txtPrecio.clear();
        txtPeso.clear();
        productoActual = null; // Esto es importante para el siguiente producto
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
            spinnerCodigo.getValueFactory().setValue(1);
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

    public void guardarYsalir() {
        if (guardarProductoBase()) {
            cerrarOverlay();
            limpiarFormulario();
        }
    }

    public void guardarYSiguiente() {
        if (guardarProductoBase()) {
            // PRIMERO: Capturar los valores ANTES de que se modifique productoActual
            String categoriaActual = (String) cbxCategoria.getValue(); // Mejor usar el ComboBox directamente
            int codigoActual = spinnerCodigo.getValue(); // Usar el Spinner directamente

            // SEGUNDO: Limpiar el formulario parcialmente
            limpiarFormularioParcial();

            // TERCERO: Asignar los valores para el siguiente producto
            // Incrementar código
            int siguienteCodigo = codigoActual + 1;
            if (siguienteCodigo <= 100) {
                spinnerCodigo.getValueFactory().setValue(siguienteCodigo);
            } else {
                spinnerCodigo.getValueFactory().setValue(1);
            }

            // Mantener la categoría anterior
            cbxCategoria.setValue(categoriaActual);
        }
    }

}
