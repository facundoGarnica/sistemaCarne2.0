/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import dao.DetalleMediaResDAO;
import dao.MediaResDAO;
import dao.ProductoDAO;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.DetalleMediaRes;
import model.MediaRes;
import model.Producto;

/**
 * FXML Controller class
 *
 * @author garca
 */
public class AgregarMediaResController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private MediaResDAO mediaResDao;
    private List<Producto> productosCarniceria;
    private ProductoDAO productoDao;
    private DetalleMediaResDAO detalleDAO;
    private StockMediaResController stockMediaResController;
    private MediaRes mediaRes;
    private DetalleMediaRes detalleMediaRes;
    @FXML
    private DatePicker DatePickerFecha;
    @FXML
    private TextField txtProveedor;
    @FXML
    private TextField txtPesoBoleta;
    @FXML
    private TextField txtPesoBalanza;
    @FXML
    private TextField txtPrecioPorKilo;
    @FXML
    private Label labelPesoFinal;
    @FXML
    private Label labelTotal;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ChangeListener<String> recalcularListener = (obs, oldVal, newVal) -> calcularYMostrarTotales();

        txtPesoBalanza.textProperty().addListener(recalcularListener);
        txtPesoBoleta.textProperty().addListener(recalcularListener);
        txtPrecioPorKilo.textProperty().addListener(recalcularListener);
    }


    private void calcularYMostrarTotales() {
        try {
            if (txtPesoBalanza.getText().isEmpty() || txtPesoBoleta.getText().isEmpty() || txtPrecioPorKilo.getText().isEmpty()) {
                labelPesoFinal.setText("---");
                labelTotal.setText("---");
                return;
            }

            double pesoBalanza = Double.parseDouble(txtPesoBalanza.getText());
            double pesoBoleta = Double.parseDouble(txtPesoBoleta.getText());
            double precioMedia = Double.parseDouble(txtPrecioPorKilo.getText());

            // CORRECCIÓN: Usar la misma lógica que en guardarMediaRes()
            double pesoBalanzaConIncremento = pesoBalanza + (pesoBalanza * 0.02);
            double pesoFinal;

            // Si pesoBalanza + 2% es mayor que pesoBoleta, usar pesoBoleta
            // Si pesoBalanza + 2% es menor que pesoBoleta, usar pesoBalanza + 2%
            if (pesoBalanzaConIncremento > pesoBoleta) {
                pesoFinal = pesoBoleta;
            } else {
                pesoFinal = pesoBalanzaConIncremento;
            }

            labelPesoFinal.setText(String.format("%.2f", pesoFinal));
            labelTotal.setText(String.format("%.2f", pesoFinal * precioMedia));
        } catch (NumberFormatException e) {
            labelPesoFinal.setText("---");
            labelTotal.setText("---");
        }
    }

    public void setSpa_stockMediaResController(StockMediaResController m) {
        stockMediaResController = m;
    }

    @FXML
    public void cerrarOverlay() {
        if (stockMediaResController != null) {
            stockMediaResController.CerrarDifuminarYSpa();
            stockMediaResController.recargarTablaProductos();
        }
    }

    public void limpiarFormulario() {
        txtPesoBalanza.clear();
        txtPesoBoleta.clear();
        txtPrecioPorKilo.clear();
        labelPesoFinal.setText("---");
        txtProveedor.clear();
        DatePickerFecha.setValue(null);
    }

    public void guardarYSalir() {
        guardarMediaRes();
        cerrarOverlay();
    }

    public void guardarYsiguiente() {
        guardarMediaRes();
        limpiarFormulario();
    }

    public void guardarMediaRes() {
        try {
            // Validar campos obligatorios
            if (DatePickerFecha.getValue() == null) {
                System.out.println("Debe seleccionar una fecha.");
                return;
            }
            if (txtProveedor.getText().isEmpty()) {
                System.out.println("El proveedor no puede estar vacío.");
                return;
            }
            if (txtPesoBalanza.getText().isEmpty() || txtPesoBoleta.getText().isEmpty() || txtPrecioPorKilo.getText().isEmpty() || labelPesoFinal.getText().equals("---")) {
                System.out.println("Debe completar todos los campos de peso y precio.");
                return;
            }

            LocalDateTime fechaHoraSeleccionada = DatePickerFecha.getValue().atTime(LocalTime.now());
            Double pesoBalanza = Double.valueOf(txtPesoBalanza.getText());
            Double pesoBoleta = Double.valueOf(txtPesoBoleta.getText());
            Double precioMedia = Double.valueOf(txtPrecioPorKilo.getText());

            // CORRECCIÓN: Calcular el incremento basado en pesoBalanza, no pesoBoleta
            double pesoBalanzaConIncremento = pesoBalanza + (pesoBalanza * 0.02);
            double pesoFinal;

            // Si pesoBalanza + 2% es mayor que pesoBoleta, usar pesoBoleta
            // Si pesoBalanza + 2% es menor que pesoBoleta, usar pesoBalanza + 2%
            if (pesoBalanzaConIncremento > pesoBoleta) {
                pesoFinal = pesoBoleta;
            } else {
                pesoFinal = pesoBalanzaConIncremento;
            }

            String proveedor = txtProveedor.getText();

            // Validar valores numéricos positivos
            if (pesoBalanza <= 0 || pesoBoleta <= 0 || precioMedia <= 0 || pesoFinal <= 0) {
                System.out.println("Los valores de peso y precio deben ser mayores que cero.");
                return;
            }

            mediaRes = new MediaRes();
            labelPesoFinal.setText(String.valueOf(pesoFinal));
            labelTotal.setText(String.valueOf(pesoFinal * precioMedia));
            mediaRes.setFecha(fechaHoraSeleccionada);
            mediaRes.setProveedor(proveedor);
            mediaRes.setPesoBoleta(pesoBoleta);
            mediaRes.setPesoPilon(pesoBalanza);
            mediaRes.setPesoFinal(pesoFinal);
            mediaRes.setPrecio(precioMedia);

            mediaResDao = new MediaResDAO();
            mediaResDao.guardar(mediaRes); // guardar la media res en la DB

            productoDao = new ProductoDAO();
            detalleDAO = new DetalleMediaResDAO();
            productosCarniceria = productoDao.obtenerProductoPorTipo("Carniceria");

            for (Producto p : productosCarniceria) {
                detalleMediaRes = new DetalleMediaRes();
                detalleMediaRes.setMediaRes(mediaRes);
                detalleMediaRes.setProducto(p);

                // Calcular el peso ajustado
                double pesoAjustadoProducto = p.getPesoPorUnidad() * (mediaRes.getPesoFinal() / 90.0);

                // Redondear usando Math.round() a dos decimales
                double pesoAjustadoRedondeado = Math.round(pesoAjustadoProducto * 100.0) / 100.0;
                detalleMediaRes.setPorcentajeCorte(pesoAjustadoRedondeado);

                detalleDAO.guardar(detalleMediaRes);
            }

            System.out.println("Media res guardada correctamente.");
        } catch (NumberFormatException e) {
            System.out.println("Error: Asegúrese de ingresar valores numéricos válidos en los campos de peso y precio.");
        } catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
        }
    }

}
