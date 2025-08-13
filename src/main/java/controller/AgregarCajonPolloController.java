/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import dao.CajonPolloDAO;
import dao.DetalleCajonPolloDAO;
import dao.ProductoDAO;
import dao.StockDAO;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import model.CajonPollo;
import model.DetalleCajonPollo;
import model.Producto;

/**
 * FXML Controller class
 *
 * @author garca
 */
public class AgregarCajonPolloController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private StockCajonPolloController stockCajonPolloController;
    private StockDAO stockDao;
    private ProductoDAO productoDao;
    private CajonPolloDAO cajonPolloDao;
    private DetalleCajonPollo detalleCajonPollo;
    private DetalleCajonPolloDAO detalleCajonPolloDao;
    private List<Producto> productosDePollo;
    private CajonPollo cajonPollo;
    @FXML
    private CheckBox checkEditar;
    @FXML
    private DatePicker datePickerFecha;
    @FXML
    private TextField txtPrecioCajon;
    @FXML
    private TextField txtPesoCajon;
    @FXML
    private TextField txtProveedor;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        datePickerFecha.setValue(LocalDate.now());
        // Configurar txtPesoCajon
        txtPesoCajon.setText("20"); // Valor por defecto
        txtPesoCajon.setDisable(true); // Deshabilitado por defecto

        // Listener para el CheckBox
        checkEditar.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // CheckBox marcado (true) = NO se puede modificar
                txtPesoCajon.setDisable(true);
                txtPesoCajon.setText("20"); // Resetear a valor por defecto
            } else {
                // CheckBox desmarcado (false) = SÍ se puede modificar
                txtPesoCajon.setDisable(false);
            }
        });

        // Configurar estado inicial del CheckBox
        checkEditar.setSelected(true); // Marcado por defecto
    }

    public void setSpa_stockCajonPolloController(StockCajonPolloController s) {
        stockCajonPolloController = s;
    }

    public void guardarYsiguiente() {
        agregarCajon();
        limpiarFormulario();
    }

    public void guardarYSalir() {
        agregarCajon();
        cerrarOverlay();
    }

    @FXML
    public void cerrarOverlay() {
        if (stockCajonPolloController != null) {
            stockCajonPolloController.CerrarDifuminarYSpa();
            stockCajonPolloController.recargarTablaProductos();
        }
    }

    public void agregarCajon() {
        try {
            // Validar campos de entrada
            if (datePickerFecha.getValue() == null) {
                System.out.println("La fecha es obligatoria.");
                return;
            }
            if (txtPrecioCajon.getText().isBlank() || txtPesoCajon.getText().isBlank() || txtProveedor.getText().isBlank()) {
                System.out.println("Precio, peso y proveedor son obligatorios.");
                return;
            }

            // Parseo seguro con manejo de excepciones
            double precioCajon;
            double pesoCajon;
            try {
                precioCajon = Double.parseDouble(txtPrecioCajon.getText());
                pesoCajon = Double.parseDouble(txtPesoCajon.getText());
            } catch (NumberFormatException e) {
                System.out.println("Precio o peso inválidos.");
                return;
            }

            if (precioCajon <= 0 || pesoCajon <= 0) {
                System.out.println("Precio y peso deben ser mayores que cero.");
                return;
            }

            LocalDate fecha = datePickerFecha.getValue();
            String proveedor = txtProveedor.getText().trim();

            // Crear y guardar CajonPollo
            cajonPollo = new CajonPollo();
            cajonPollo.setFecha(fecha);
            cajonPollo.setPesoCajon(pesoCajon);
            cajonPollo.setPrecio(precioCajon);
            cajonPollo.setProveedor(proveedor);

            cajonPolloDao = new CajonPolloDAO();
            cajonPolloDao.guardar(cajonPollo);

            productoDao = new ProductoDAO();
            detalleCajonPolloDao = new DetalleCajonPolloDAO();
            List<Producto> productosDePollo = productoDao.obtenerProductoPorTipo("Pollo");

            // CALCULAR PESO APROVECHABLE DESCONTANDO DESPERDICIO
            // Tu cajón de referencia: 19kg → productos aprovechables: 18.033kg
            // Porcentaje de aprovechamiento: (18.033 / 19) = 94.91%
            double porcentajeAprovechamiento = 0.9491; // 94.91%

            // Peso aprovechable del cajón actual (descontando desperdicio)
            double pesoAprovechable = pesoCajon * porcentajeAprovechamiento;

            // Peso de referencia de productos vendibles
            double pesoReferenciaAprovechable = 18.033;

            System.out.println(String.format("Cajón total: %.2f kg - Peso aprovechable: %.2f kg - Desperdicio: %.2f kg",
                    pesoCajon, pesoAprovechable, (pesoCajon - pesoAprovechable)));

            for (Producto p : productosDePollo) {
                detalleCajonPollo = new DetalleCajonPollo();
                detalleCajonPollo.setCajonPollo(cajonPollo);
                detalleCajonPollo.setProducto(p);

                // Calcular el peso ajustado usando el peso aprovechable (sin desperdicio)
                double pesoAjustadoProducto = p.getPesoPorUnidad() * (pesoAprovechable / pesoReferenciaAprovechable);

                // Redondear usando Math.round() a dos decimales
                double pesoAjustadoRedondeado = Math.round(pesoAjustadoProducto * 100.0) / 100.0;

                // Calcular porcentaje para guardar en detalle (sobre peso total del cajón)
                double porcentaje = (pesoAjustadoProducto / pesoCajon) * 100;
                double porcentajeRedondeado = Math.round(porcentaje * 100.0) / 100.0;

                detalleCajonPollo.setPorcentajeCorte(porcentajeRedondeado);

                detalleCajonPolloDao.guardar(detalleCajonPollo);

                // Actualizar stock
                stockDao = new StockDAO();
                stockDao.sumarOCrearStockPorNombreProducto(p.getNombre(), pesoAjustadoRedondeado);

                System.out.println(String.format("Producto: %s - Peso base: %.3f kg - Peso ajustado: %.3f kg",
                        p.getNombre(), p.getPesoPorUnidad(), pesoAjustadoRedondeado));
            }

        } catch (Exception e) {
            System.out.println("Error al agregar cajón: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void limpiarFormulario() {
        txtPrecioCajon.clear();
        txtPesoCajon.clear();
        txtProveedor.clear();
        datePickerFecha.setValue(LocalDate.now());
    }
}
