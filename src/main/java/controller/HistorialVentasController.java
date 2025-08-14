/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import dao.VentaDAO;
import dao.DetalleVentaDAO;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.scene.control.TableCell;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import model.DetalleVenta;
import model.Venta;
import model.Producto;

/**
 * FXML Controller class
 *
 * @author garca
 */
public class HistorialVentasController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private Venta venta;
    private DetalleVenta detalleVenta;
    private VentaDAO ventaDao;
    private DetalleVentaDAO detalleVentaDao;
    private List<Venta> listaVentas;
    private List<DetalleVenta> listaDestalleVentas;
    private ObservableList<Venta> observableVentas;
    private ObservableList<DetalleVenta> observableDetalles;

    //id de fxml
    @FXML
    private DatePicker datePickerHasta;
    @FXML
    private DatePicker datePickerDesde;
    @FXML
    private DatePicker datePickerfecha;
    @FXML
    private Label lblClientes;
    @FXML
    private Label lblTotalVendido;
    @FXML
    private Label lblEfectivo;
    @FXML
    private Label lblVirtual;
    // Tabla de Ventas
    @FXML
    private TableView<Venta> tlbVerVentas;
    @FXML
    private TableColumn<Venta, java.time.LocalDateTime> colFecha;
    @FXML
    private TableColumn<Venta, java.time.LocalDateTime> colHora;
    @FXML
    private TableColumn<Venta, String> colMedioPago;
    @FXML
    private TableColumn<Venta, Double> colTotal;

    // Tabla de Productos
    @FXML
    private TableView<DetalleVenta> tblProductos;
    @FXML
    private TableColumn<DetalleVenta, Producto> colNombre;
    @FXML
    private TableColumn<DetalleVenta, Double> colPrecio;
    @FXML
    private TableColumn<DetalleVenta, Double> colKilos;
    @FXML
    private TableColumn<DetalleVenta, Double> colTotalProducto;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ventaDao = new VentaDAO();
        detalleVentaDao = new DetalleVentaDAO();
        listaVentas = ventaDao.buscarTodos();

        // Configurar columnas de ambas tablas
        configurarColumnasVentas();
        configurarColumnasDetalles();

        // Cargar datos en la tabla de ventas
        cargarDatosVentas();

        // Configurar evento de selección para mostrar detalles
        configurarSeleccionVenta();
    }

    private void configurarColumnasVentas() {
        // Configurar las columnas para que se vinculen con las propiedades del modelo Venta
        // Usando el campo "fecha" que corresponde al getter getFecha()
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("fecha")); // Mismo campo para ambas
        colMedioPago.setCellValueFactory(new PropertyValueFactory<>("medioPago"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Configurar formato personalizado para la fecha
        colFecha.setCellFactory(new Callback<TableColumn<Venta, java.time.LocalDateTime>, TableCell<Venta, java.time.LocalDateTime>>() {
            @Override
            public TableCell<Venta, java.time.LocalDateTime> call(TableColumn<Venta, java.time.LocalDateTime> param) {
                return new TableCell<Venta, java.time.LocalDateTime>() {
                    @Override
                    protected void updateItem(java.time.LocalDateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            // Formato solo fecha: dd/MM/yyyy
                            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            setText(item.format(dateFormatter));
                        }
                    }
                };
            }
        });

        // Configurar formato personalizado para la hora
        colHora.setCellFactory(new Callback<TableColumn<Venta, java.time.LocalDateTime>, TableCell<Venta, java.time.LocalDateTime>>() {
            @Override
            public TableCell<Venta, java.time.LocalDateTime> call(TableColumn<Venta, java.time.LocalDateTime> param) {
                return new TableCell<Venta, java.time.LocalDateTime>() {
                    @Override
                    protected void updateItem(java.time.LocalDateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            // Formato solo hora: HH:mm:ss
                            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                            setText(item.format(timeFormatter));
                        }
                    }
                };
            }
        });
    }

    private void cargarDatosVentas() {
        // Convertir la lista a ObservableList para que se muestre en la tabla
        observableVentas = FXCollections.observableArrayList(listaVentas);
        tlbVerVentas.setItems(observableVentas);
        actualizarEstadisticas();
    }

    private void configurarColumnasDetalles() {
        // Configurar las columnas de la tabla de detalles
        colNombre.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colKilos.setCellValueFactory(new PropertyValueFactory<>("peso"));

        // Configurar formato personalizado para mostrar el nombre del producto
        colNombre.setCellFactory(new Callback<TableColumn<DetalleVenta, Producto>, TableCell<DetalleVenta, Producto>>() {
            @Override
            public TableCell<DetalleVenta, Producto> call(TableColumn<DetalleVenta, Producto> param) {
                return new TableCell<DetalleVenta, Producto>() {
                    @Override
                    protected void updateItem(Producto item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getNombre()); // Asumiendo que Producto tiene getNombre()
                        }
                    }
                };
            }
        });

        // Configurar cálculo del total por producto (precio * peso)
        colTotalProducto.setCellValueFactory(cellData -> {
            DetalleVenta detalle = cellData.getValue();
            Double total = detalle.getPrecio() * detalle.getPeso();
            return new javafx.beans.property.SimpleObjectProperty<>(total);
        });
    }

    private void configurarSeleccionVenta() {
        // Agregar listener para cuando se selecciona una venta
        tlbVerVentas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cargarDetallesVenta(newSelection);
            }
        });
    }

    private void cargarDetallesVenta(Venta ventaSeleccionada) {
        try {
            // Limpiar la tabla antes de cargar nuevos datos
            tblProductos.setItems(null);

            // Obtener los detalles de la venta seleccionada
            List<DetalleVenta> detalles = detalleVentaDao.buscarPorVenta(ventaSeleccionada);

            // Debug: imprimir cuántos detalles se encontraron
            System.out.println("Detalles encontrados para venta ID " + ventaSeleccionada.getId() + ": " + detalles.size());

            // Convertir a ObservableList y mostrar en la tabla
            if (detalles != null && !detalles.isEmpty()) {
                observableDetalles = FXCollections.observableArrayList(detalles);
                tblProductos.setItems(observableDetalles);

                // Forzar actualización de la tabla
                tblProductos.refresh();
            } else {
                // Si no hay detalles, crear una lista vacía
                observableDetalles = FXCollections.observableArrayList();
                tblProductos.setItems(observableDetalles);
                System.out.println("No se encontraron detalles para esta venta");
            }

        } catch (Exception e) {
            System.err.println("Error al cargar detalles de venta: " + e.getMessage());
            e.printStackTrace();

            // En caso de error, mostrar tabla vacía
            observableDetalles = FXCollections.observableArrayList();
            tblProductos.setItems(observableDetalles);
        }
    }
    // Método para eliminar una venta seleccionada

    public void eliminarVentaSeleccionada() {
        Venta ventaSeleccionada = tlbVerVentas.getSelectionModel().getSelectedItem();

        if (ventaSeleccionada == null) {
            // Aquí podrías mostrar un Alert informando que no hay venta seleccionada
            System.out.println("No hay venta seleccionada para eliminar");
            return;
        }

        try {
            System.out.println("=== INICIANDO ELIMINACIÓN DE VENTA ===");
            System.out.println("Venta ID: " + ventaSeleccionada.getId());
            System.out.println("Fecha: " + ventaSeleccionada.getFecha());
            System.out.println("Total: $" + ventaSeleccionada.getTotal());

            // ✅ CAMBIO IMPORTANTE: Usar eliminarVenta() en lugar de eliminar()
            ventaDao.eliminarVenta(ventaSeleccionada.getId());

            // Actualizar la tabla de ventas
            actualizarTablaVentas();

            // Limpiar la tabla de detalles
            tblProductos.setItems(FXCollections.observableArrayList());

            System.out.println("=== VENTA ELIMINADA EXITOSAMENTE ===");

        } catch (Exception e) {
            System.err.println("=== ERROR AL ELIMINAR VENTA ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();

        }
        actualizarEstadisticas();
    }

    @FXML
    public void buscarVentasPorFecha() {
        try {
            System.out.println("=== BUSCANDO VENTAS POR FECHA ESPECÍFICA ===");

            // Obtener la fecha seleccionada del DatePicker
            LocalDate fechaSeleccionada = datePickerfecha.getValue();

            // Validar que se haya seleccionado una fecha
            if (fechaSeleccionada == null) {
                System.out.println("Error: Debe seleccionar una fecha");
                // Aquí podrías mostrar un Alert al usuario
                return;
            }

            System.out.println("Fecha seleccionada: " + fechaSeleccionada);

            // Buscar ventas para esa fecha específica
            // Usamos el mismo rango (misma fecha para inicio y fin)
            List<Venta> ventasFecha = ventaDao.buscarVentasPorFecha(fechaSeleccionada, fechaSeleccionada);

            // Actualizar la tabla con las ventas encontradas
            observableVentas = FXCollections.observableArrayList(ventasFecha);
            tlbVerVentas.setItems(observableVentas);
            tlbVerVentas.refresh();

            // Limpiar tabla de detalles
            tblProductos.setItems(FXCollections.observableArrayList());

            // Limpiar los otros DatePickers para evitar confusión
            datePickerDesde.setValue(null);
            datePickerHasta.setValue(null);

            // Actualizar estadísticas
            actualizarEstadisticas();

            System.out.println("Mostrando " + ventasFecha.size() + " ventas para la fecha: " + fechaSeleccionada);

        } catch (Exception e) {
            System.err.println("Error al buscar ventas por fecha: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método alternativo que recibe la fecha como parámetro Útil si quieres
     * llamar este método desde otro lugar con una fecha específica
     */
    public void buscarVentasPorFecha(LocalDate fecha) {
        try {
            System.out.println("=== BUSCANDO VENTAS PARA FECHA: " + fecha + " ===");

            if (fecha == null) {
                System.out.println("Error: La fecha no puede ser null");
                return;
            }

            // Buscar ventas para esa fecha específica
            List<Venta> ventasFecha = ventaDao.buscarVentasPorFecha(fecha, fecha);

            // Actualizar la tabla con las ventas encontradas
            observableVentas = FXCollections.observableArrayList(ventasFecha);
            tlbVerVentas.setItems(observableVentas);
            tlbVerVentas.refresh();

            // Limpiar tabla de detalles
            tblProductos.setItems(FXCollections.observableArrayList());

            // Establecer la fecha en el DatePicker
            datePickerfecha.setValue(fecha);

            // Limpiar los otros DatePickers
            datePickerDesde.setValue(null);
            datePickerHasta.setValue(null);

            // Actualizar estadísticas
            actualizarEstadisticas();

            System.out.println("Mostrando " + ventasFecha.size() + " ventas para la fecha: " + fecha);

        } catch (Exception e) {
            System.err.println("Error al buscar ventas por fecha: " + e.getMessage());
            e.printStackTrace();
        }
    }
// Método auxiliar para actualizar la tabla de ventas

    private void actualizarTablaVentas() {
        try {
            // Recargar la lista de ventas desde la base de datos
            listaVentas = ventaDao.buscarTodos();

            // Actualizar la ObservableList
            observableVentas = FXCollections.observableArrayList(listaVentas);
            tlbVerVentas.setItems(observableVentas);

            // Refrescar la tabla
            tlbVerVentas.refresh();

        } catch (Exception e) {
            System.err.println("Error al actualizar tabla de ventas: " + e.getMessage());
            e.printStackTrace();
        }
        actualizarEstadisticas();
    }

    @FXML
    public void buscarVentasDeHoy() {
        try {
            System.out.println("=== BUSCANDO VENTAS DE HOY ===");

            // Obtener ventas de hoy desde el DAO
            List<Venta> ventasHoy = ventaDao.buscarVentasDeHoy();

            // Actualizar la tabla con las ventas de hoy
            observableVentas = FXCollections.observableArrayList(ventasHoy);
            tlbVerVentas.setItems(observableVentas);
            tlbVerVentas.refresh();

            // Limpiar tabla de detalles
            tblProductos.setItems(FXCollections.observableArrayList());

            System.out.println("Mostrando " + ventasHoy.size() + " ventas de hoy");

        } catch (Exception e) {
            System.err.println("Error al buscar ventas de hoy: " + e.getMessage());
            e.printStackTrace();
        }
        actualizarEstadisticas();
    }

    @FXML
    public void buscarVentasPorFiltro() {
        LocalDate fechaDesde = datePickerDesde.getValue();
        LocalDate fechaHasta = datePickerHasta.getValue();

        if (fechaDesde == null || fechaHasta == null) {
            System.out.println("Debe seleccionar ambas fechas");
            return;
        }

        buscarVentasEnRango(fechaDesde, fechaHasta);
    }

    public void buscarVentasEnRango(LocalDate fechaDesde, LocalDate fechaHasta) {
        try {
            System.out.println("=== BUSCANDO VENTAS POR FILTRO ===");
            System.out.println("Desde: " + fechaDesde + " Hasta: " + fechaHasta);

            // Validar que la fecha desde no sea posterior a fecha hasta
            if (fechaDesde.isAfter(fechaHasta)) {
                System.out.println("Error: La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
                return;
            }

            // Obtener ventas filtradas desde el DAO
            List<Venta> ventasFiltradas = ventaDao.buscarVentasPorFecha(fechaDesde, fechaHasta);

            // Actualizar la tabla con las ventas filtradas
            observableVentas = FXCollections.observableArrayList(ventasFiltradas);
            tlbVerVentas.setItems(observableVentas);
            tlbVerVentas.refresh();

            // Limpiar tabla de detalles
            tblProductos.setItems(FXCollections.observableArrayList());

            System.out.println("Mostrando " + ventasFiltradas.size() + " ventas en el rango seleccionado");

        } catch (Exception e) {
            System.err.println("Error al buscar ventas por filtro: " + e.getMessage());
            e.printStackTrace();
        }
        actualizarEstadisticas();
    }

    @FXML
    public void mostrarTodasLasVentas() {
        try {
            System.out.println("=== MOSTRANDO TODAS LAS VENTAS ===");

            // Recargar todas las ventas
            listaVentas = ventaDao.buscarTodos();
            observableVentas = FXCollections.observableArrayList(listaVentas);
            tlbVerVentas.setItems(observableVentas);
            tlbVerVentas.refresh();

            // Limpiar tabla de detalles
            tblProductos.setItems(FXCollections.observableArrayList());

            System.out.println("Mostrando todas las " + listaVentas.size() + " ventas");
            datePickerDesde.setValue(null);
            datePickerHasta.setValue(null);
            datePickerfecha.setValue(null);
        } catch (Exception e) {
            System.err.println("Error al mostrar todas las ventas: " + e.getMessage());
            e.printStackTrace();
        }
        actualizarEstadisticas();
    }

    private void actualizarEstadisticas() {
        try {
            // Obtener las ventas actualmente mostradas en la tabla
            ObservableList<Venta> ventasActuales = tlbVerVentas.getItems();

            if (ventasActuales == null || ventasActuales.isEmpty()) {
                // Si no hay ventas, mostrar valores en cero
                lblClientes.setText("0");
                lblTotalVendido.setText("$0.00");
                lblEfectivo.setText("$0.00");
                lblVirtual.setText("$0.00");
                return;
            }

            // Variables para los cálculos
            int totalClientes = ventasActuales.size(); // Cada venta representa un cliente
            double totalVendido = 0.0;
            double totalEfectivo = 0.0;
            double totalVirtual = 0.0;

            // Recorrer todas las ventas para calcular los totales
            for (Venta venta : ventasActuales) {
                double totalVenta = venta.getTotal();
                String medioPago = venta.getMedioPago();

                // Sumar al total general
                totalVendido += totalVenta;

                // Clasificar por método de pago
                if (medioPago != null) {
                    // Convertir a minúsculas para comparación más flexible
                    String medioLower = medioPago.toLowerCase();

                    if (medioLower.contains("efectivo") || medioLower.equals("efectivo")) {
                        totalEfectivo += totalVenta;
                    } else if (medioLower.contains("virtual") || medioLower.contains("tarjeta")
                            || medioLower.contains("transferencia") || medioLower.equals("virtual")) {
                        totalVirtual += totalVenta;
                    } else {
                        // Si no coincide con ninguno, asumimos que es virtual
                        // (puedes cambiar esta lógica según tu implementación)
                        totalVirtual += totalVenta;
                    }
                }
            }

            // Actualizar los labels con formato de moneda
            lblClientes.setText(String.valueOf(totalClientes));
            lblTotalVendido.setText(String.format("$%.2f", totalVendido));
            lblEfectivo.setText(String.format("$%.2f", totalEfectivo));
            lblVirtual.setText(String.format("$%.2f", totalVirtual));

            // Debug para verificar los cálculos
            System.out.println("=== ESTADÍSTICAS ACTUALIZADAS ===");
            System.out.println("Total clientes: " + totalClientes);
            System.out.println("Total vendido: $" + String.format("%.2f", totalVendido));
            System.out.println("Total efectivo: $" + String.format("%.2f", totalEfectivo));
            System.out.println("Total virtual: $" + String.format("%.2f", totalVirtual));

        } catch (Exception e) {
            System.err.println("Error al actualizar estadísticas: " + e.getMessage());
            e.printStackTrace();

            // En caso de error, mostrar valores por defecto
            lblClientes.setText("Error");
            lblTotalVendido.setText("Error");
            lblEfectivo.setText("Error");
            lblVirtual.setText("Error");
        }
    }
}
