/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import Dto.AlertaStockDTO;
import dao.StockDAO;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Stock;

/**
 * FXML Controller class
 *
 * @author facun
 */
public class VentanaStockBajosController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private List<Stock> listaStock;
    private List<AlertaStockDTO> listaStockBajo;
    
    // Referencia al controlador padre para comunicar cambios
    private Crear_ventasController crearVentasController;

    //Tabla para stock de productos
    @FXML
    private TableView<AlertaStockDTO> tlbMostrarStocks;
    @FXML
    private TableColumn<AlertaStockDTO, String> colNombreStock;
    @FXML
    private TableColumn<AlertaStockDTO, String> colEstadoStock;
    @FXML
    private TableColumn<AlertaStockDTO, Double> colCantidadStock;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicializar la lista como vacía para evitar null
        listaStockBajo = new ArrayList<>();
        
        // Vinculamos las columnas con las propiedades de AlertaStockDTO
        colNombreStock.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colEstadoStock.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colCantidadStock.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        // PRIMERO cargar los datos de productos bajos
        productosBajos();
        
        // DESPUÉS cargar la tabla con los datos
        cargarTablaConStocks();
    }

    // Método para establecer la referencia al controlador padre
    public void setCrearVentasController(Crear_ventasController controller) {
        this.crearVentasController = controller;
    }

    public void productosBajos() {
    try {
        System.out.println("=== Verificando productos con stock bajo ===");

        // Usar los DAOs directamente
        StockDAO stockDao = new StockDAO();
        listaStock = stockDao.buscarTodos();

        // Reinicializar la lista para asegurar que esté limpia
        listaStockBajo = new ArrayList<>();

        for (Stock stock : listaStock) {
            double cantidad = stock.getCantidad();
            double minima = stock.getCantidadMinima();

            String estado = "";
            boolean esStockBajo = false;

            // CORREGIDO: Ahora incluye productos con stock en 0 como Crítico
            if (cantidad == 0) {
                estado = "Sin stock";
                esStockBajo = true;
                System.out.println("🚨 Producto SIN STOCK: " + stock.getProducto().getNombre());
            } else if (cantidad <= minima) {
                estado = "Crítico";
                esStockBajo = true;
                System.out.println("⚠️  Producto CRÍTICO: " + stock.getProducto().getNombre() + 
                                 " (Cantidad: " + cantidad + ", Mínima: " + minima + ")");
            } else if (cantidad <= minima * 1.5) {
                estado = "Bajo";
                esStockBajo = true;
                System.out.println("⚠️  Producto BAJO: " + stock.getProducto().getNombre() + 
                                 " (Cantidad: " + cantidad + ", Mínima recomendada: " + (minima * 1.5) + ")");
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
            
            // Separar por categorías para mejor visualización
            int criticos = 0;
            int bajos = 0;
            int sinStock = 0;
            
            for (AlertaStockDTO alerta : listaStockBajo) {
                if (alerta.getCantidad() == 0) {
                    sinStock++;
                } else if (alerta.getEstado().equals("Crítico")) {
                    criticos++;
                } else {
                    bajos++;
                }
                
                System.out.println("  - " + alerta.getNombreProducto()
                        + " | Estado: " + alerta.getEstado()
                        + " | Cantidad: " + alerta.getCantidad());
            }
            
            System.out.println("📊 RESUMEN DE ALERTAS:");
            System.out.println("   🚨 Sin Stock (0): " + sinStock + " productos");
            System.out.println("   ⚠️  Críticos: " + criticos + " productos");
            System.out.println("   🔶 Bajos: " + bajos + " productos");
            System.out.println("   📦 TOTAL ALERTAS: " + listaStockBajo.size() + " productos");
            
        } else {
            System.out.println("✅ No hay productos con stock bajo actualmente");
        }
        
        // Notificar al controlador padre sobre el estado
        notificarEstadoStockBajo();

    } catch (Exception e) {
        System.err.println("❌ Error al verificar stock bajo: " + e.getMessage());
        e.printStackTrace();
        // Inicializar lista vacía en caso de error
        listaStockBajo = new ArrayList<>();
        
        // Notificar que no hay productos (desactivar imagen)
        notificarEstadoStockBajo();
    }
}
    
    // NUEVO: Método para notificar el estado al controlador padre
    private void notificarEstadoStockBajo() {
        if (crearVentasController != null) {
            boolean hayProductosBajos = !listaStockBajo.isEmpty();
            crearVentasController.actualizarEstadoImagenStock(hayProductosBajos);
            System.out.println("✓ Notificado estado de stock bajo: " + 
                             (hayProductosBajos ? "ACTIVO" : "INACTIVO"));
        }
    }

    public void cargarTablaConStocks() {
        // Limpiar la tabla
        tlbMostrarStocks.getItems().clear();

        // Verificar que la lista no sea null
        if (listaStockBajo == null) {
            System.out.println("⚠️  Lista de stock bajo es null, inicializando vacía");
            listaStockBajo = new ArrayList<>();
        }

        ObservableList<AlertaStockDTO> listaDTO = FXCollections.observableArrayList();

        for (AlertaStockDTO a : listaStockBajo) {
            // Convertimos cada AlertaStockDTO a la lista observable
            listaDTO.add(new AlertaStockDTO(
                    a.getNombreProducto(), // nombre
                    a.getEstado(), // estado
                    a.getCantidad() // cantidad
            ));
        }

        // Seteamos los datos en la tabla
        tlbMostrarStocks.setItems(listaDTO);
        
        System.out.println("✓ Tabla cargada con " + listaDTO.size() + " elementos");
    }
    
    // Método público para refrescar los datos si es necesario
    public void refrescarDatos() {
        productosBajos();
        cargarTablaConStocks();
    }
    
    // NUEVO: Método público para obtener el estado actual
    public boolean hayProductosConStockBajo() {
        return listaStockBajo != null && !listaStockBajo.isEmpty();
    }
    
    // NUEVO: Método para obtener la cantidad de productos con stock bajo
    public int getCantidadProductosStockBajo() {
        return listaStockBajo != null ? listaStockBajo.size() : 0;
    }
}