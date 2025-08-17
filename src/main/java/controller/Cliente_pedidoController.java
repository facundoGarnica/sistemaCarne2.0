/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import dao.ClienteDAO;
import dao.PedidoDAO;
import dao.SeniaDAO;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import model.Cliente;
import model.DetallePedido;
import model.Pedido;
import model.Senia;

/**
 * FXML Controller class
 *
 * @author facun
 */
public class Cliente_pedidoController implements Initializable {

    /**
     * Initializes the controller class.
     */
    //clases y Dao
    private Cliente cliente;
    private ClienteDAO clienteDao;
    private Pedido pedido;
    private PedidoDAO pedidoDao;
    private Senia senia;
    private SeniaDAO seniaDao;

    //tablas
    @FXML
    private TableView<Cliente> tablaClientes;
    @FXML
    private TableColumn<Cliente, String> colNombre;
    @FXML
    private TableColumn<Cliente, String> colCelular;
    @FXML
    private AnchorPane overlayPedido;
    @FXML
    private AnchorPane difuminar;

    //tabla pedidos - TIPOS CORREGIDOS
    @FXML
    private TableView<Pedido> tablaPedidos;
    @FXML
    private TableColumn<Pedido, LocalDateTime> colFechaPedido;
    @FXML
    private TableColumn<Pedido, Double> colMonto;
    @FXML
    private TableColumn<Pedido, Double> colSena;
    @FXML
    private TableColumn<Pedido, Double> colResto;
    @FXML
    private TableColumn<Pedido, Boolean> colEstado;
    @FXML
    private TableColumn<Pedido, LocalDate> colDiaEntrega;

    //tabla senias - CONFIGURACIÓN CORREGIDA
    @FXML
    private TableView<Senia> tblSenia;
    @FXML
    private TableColumn<Senia, LocalDateTime> colFechaSenia;
    @FXML
    private TableColumn<Senia, String> colHoraSenia;
    @FXML
    private TableColumn<Senia, Double> colMontoSenia;
    @FXML
    private TableColumn<Senia, String> colMedioPago;

    //variables
    List<Cliente> listaClientePedidos;
    private Pedido pedidoSeleccionado;

    //Tabla productos detallePedido - TIPOS CORREGIDOS
    @FXML
    private TableView<DetallePedido> tblProductos;
    @FXML
    private TableColumn<DetallePedido, String> colProducto;
    @FXML
    private TableColumn<DetallePedido, Double> colPrecio;
    @FXML
    private TableColumn<DetallePedido, Double> colCantidad;
    @FXML
    private TableColumn<DetallePedido, String> colUnidadMedida; // Nueva columna
    @FXML
    private TableColumn<DetallePedido, Double> colTotal;

    //controller
    @FXML
    private Cliente_CrearPedidoController crearPedidoController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configuración de columnas de clientes
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCelular.setCellValueFactory(new PropertyValueFactory<>("celular"));

        // Configuración de columnas de pedidos
        configurarColumnaFecha();
        configurarColumnaMonto();
        configurarColumnaSena();
        configurarColumnaResto();
        configurarColumnaEstado();
        configurarColumnaFechaEntrega();

        configurarColumnasSeniasSimple();
        configurarColumnasProductos();

        cargarClientesConPedidos();
    }

    private void configurarColumnasSeniasSimple() {
        // FECHA COMPLETA
        colFechaSenia.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colFechaSenia.setCellFactory(column -> {
            return new TableCell<Senia, LocalDateTime>() {
                private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : formatter.format(item));
                }
            };
        });

        // MONTO
        colMontoSenia.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colMontoSenia.setCellFactory(column -> {
            return new TableCell<Senia, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : String.format("$%.2f", item));
                }
            };
        });

        // OCULTAR las columnas que no tienen datos
        if (colHoraSenia != null) {
            colHoraSenia.setVisible(false);
        }
        if (colMedioPago != null) {
            colMedioPago.setVisible(false);
        }
    }

    private void configurarColumnasProductos() {
        // PRODUCTO - Mostrar el nombre del producto
        colProducto.setCellValueFactory(cellData -> {
            DetallePedido detalle = cellData.getValue();
            if (detalle != null && detalle.getProducto() != null) {
                return new SimpleStringProperty(detalle.getProducto().getNombre());
            }
            return new SimpleStringProperty("Sin producto");
        });

        // PRECIO UNITARIO - Mostrar información contextual según unidad
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPrecio.setCellFactory(column -> {
            return new TableCell<DetallePedido, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        DetallePedido detalle = getTableView().getItems().get(getIndex());
                        String unidad = detalle != null ? detalle.getUnidadMedida() : "";
                        
                        if ("kg".equalsIgnoreCase(unidad)) {
                            setText(String.format("$%.2f/kg", item));
                        } else if ("Unidad".equalsIgnoreCase(unidad)) {
                            setText(String.format("$%.2f/kg", item));
                        } else {
                            setText(String.format("$%.2f", item));
                        }
                    }
                }
            };
        });

        // CANTIDAD - Mostrar según unidad de medida
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCantidad.setCellFactory(column -> {
            return new TableCell<DetallePedido, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        DetallePedido detalle = getTableView().getItems().get(getIndex());
                        String unidad = detalle != null ? detalle.getUnidadMedida() : "";
                        
                        if ("kg".equalsIgnoreCase(unidad)) {
                            setText(String.format("%.3f kg", item));
                        } else if ("Unidad".equalsIgnoreCase(unidad)) {
                            // Si es un número entero de unidades, mostrarlo sin decimales
                            if (item == Math.floor(item)) {
                                setText(String.format("%.0f unid.", item));
                            } else {
                                setText(String.format("%.2f unid.", item));
                            }
                        } else {
                            // Fallback genérico
                            if (item == Math.floor(item)) {
                                setText(String.format("%.0f", item));
                            } else {
                                setText(String.format("%.2f", item));
                            }
                        }
                    }
                }
            };
        });

        // UNIDAD DE MEDIDA - Nueva columna
        if (colUnidadMedida != null) {
            colUnidadMedida.setCellValueFactory(new PropertyValueFactory<>("unidadMedida"));
            colUnidadMedida.setCellFactory(column -> {
                return new TableCell<DetallePedido, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item);
                        }
                    }
                };
            });
        }

        // TOTAL - CORREGIDO con UnidadMedida
        colTotal.setCellValueFactory(cellData -> {
            DetallePedido detalle = cellData.getValue();
            if (detalle != null) {
                Double precio = detalle.getPrecio();
                Double cantidad = detalle.getCantidad();
                String unidadMedida = detalle.getUnidadMedida();

                if (precio != null && cantidad != null && unidadMedida != null) {
                    double total = 0.0;
                    
                    if (unidadMedida.equalsIgnoreCase("kg")) {
                        // Si es por kg: cantidad * precio
                        total = cantidad * precio;
                    } else if (unidadMedida.equalsIgnoreCase("Unidad")) {
                        // Si es por unidad: cantidad * precio * pesoPorUnidad
                        if (detalle.getProducto() != null && detalle.getProducto().getPesoPorUnidad() != null) {
                            Double pesoPorUnidad = detalle.getProducto().getPesoPorUnidad();
                            total = cantidad * precio * pesoPorUnidad;
                        } else {
                            // Fallback si no hay pesoPorUnidad
                            total = cantidad * precio;
                        }
                    } else {
                        // Fallback para otras unidades
                        total = cantidad * precio;
                    }
                    
                    return new SimpleObjectProperty<>(total);
                }
            }
            return new SimpleObjectProperty<>(0.0);
        });

        colTotal.setCellFactory(column -> {
            return new TableCell<DetallePedido, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", item));
                    }
                }
            };
        });
    }

    private void configurarColumnaFecha() {
        colFechaPedido.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colFechaPedido.setCellFactory(column -> {
            return new TableCell<Pedido, LocalDateTime>() {
                private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };
        });
    }

    private void configurarColumnaMonto() {
        colMonto.setCellValueFactory(new PropertyValueFactory<>("total"));
        colMonto.setCellFactory(column -> {
            return new TableCell<Pedido, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", item));
                    }
                }
            };
        });
    }

    private void configurarColumnaSena() {
        colSena.setCellValueFactory(new PropertyValueFactory<>("totalSenas"));
        colSena.setCellFactory(column -> {
            return new TableCell<Pedido, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", item));
                    }
                }
            };
        });
    }

    private void configurarColumnaResto() {
        colResto.setCellValueFactory(new PropertyValueFactory<>("resto"));
        colResto.setCellFactory(column -> {
            return new TableCell<Pedido, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("$%.2f", item));
                    }
                }
            };
        });
    }

    private void configurarColumnaEstado() {
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setCellFactory(column -> {
            return new TableCell<Pedido, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item ? "Completado" : "Pendiente");
                        if (item) {
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        }
                    }
                }
            };
        });
    }

    private void configurarColumnaFechaEntrega() {
        colDiaEntrega.setCellValueFactory(new PropertyValueFactory<>("fechaEntrega"));
        colDiaEntrega.setCellFactory(column -> {
            return new TableCell<Pedido, LocalDate>() {
                private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };
        });
    }

    private void cargarClientesConPedidos() {
        try {
            clienteDao = new ClienteDAO();
            listaClientePedidos = clienteDao.buscarClientesConPedidos();

            ObservableList<Cliente> clientesObservable = FXCollections.observableArrayList(listaClientePedidos);
            tablaClientes.setItems(clientesObservable);

            // Listener para selección de cliente
            tablaClientes.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, selectedCliente) -> {
                        if (selectedCliente != null) {
                            cargarPedidosDeCliente(selectedCliente);
                        } else {
                            // Limpiar tablas si no hay cliente seleccionado
                            limpiarTablasPedidoRelacionadas();
                        }
                    }
            );
        } catch (Exception e) {
            System.err.println("Error al cargar clientes con pedidos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarPedidosDeCliente(Cliente clienteSeleccionado) {
        try {
            if (pedidoDao == null) {
                pedidoDao = new PedidoDAO();
            }

            List<Pedido> pedidos = pedidoDao.buscarPedidosPorCliente(clienteSeleccionado.getId());

            ObservableList<Pedido> pedidosObservable = FXCollections.observableArrayList(pedidos);
            tablaPedidos.setItems(pedidosObservable);

            // Limpiar selección anterior
            tablaPedidos.getSelectionModel().clearSelection();
            pedidoSeleccionado = null;

            // Limpiar tablas relacionadas
            tblSenia.setItems(FXCollections.observableArrayList());
            tblProductos.setItems(FXCollections.observableArrayList());

            // Listener para selección de pedido - MEJORADO
            tablaPedidos.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, selectedPedido) -> {
                        if (selectedPedido != null && !selectedPedido.equals(oldValue)) {
                            pedidoSeleccionado = selectedPedido;
                            // Cargar datos relacionados
                            cargarDatosDelPedido(selectedPedido);
                        } else if (selectedPedido == null) {
                            // Limpiar si no hay pedido seleccionado
                            pedidoSeleccionado = null;
                            tblSenia.setItems(FXCollections.observableArrayList());
                            tblProductos.setItems(FXCollections.observableArrayList());
                        }
                    }
            );

            // Refrescar la tabla
            tablaPedidos.refresh();

        } catch (Exception e) {
            System.err.println("Error al cargar pedidos del cliente: " + e.getMessage());
            e.printStackTrace();
            // Limpiar en caso de error
            limpiarTablasPedidoRelacionadas();
        }
    }

    // NUEVO MÉTODO para cargar todos los datos del pedido
    private void cargarDatosDelPedido(Pedido pedido) {
        if (pedido == null) {
            return;
        }

        // Ejecutar en hilo separado para evitar bloqueos
        Platform.runLater(() -> {
            try {
                // Cargar señas y productos en paralelo
                cargarSeniasDelPedido(pedido);
                cargarProductosDelPedido(pedido);
            } catch (Exception e) {
                System.err.println("Error al cargar datos del pedido: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void cargarSeniasDelPedido(Pedido pedidoSeleccionado) {
        try {
            if (seniaDao == null) {
                seniaDao = new SeniaDAO();
            }

            List<Senia> senias = seniaDao.buscarSeniasPorPedido(pedidoSeleccionado.getId());

            ObservableList<Senia> seniasObservable = FXCollections.observableArrayList();
            if (senias != null && !senias.isEmpty()) {
                seniasObservable.addAll(senias);
            }
            
            tblSenia.setItems(seniasObservable);
            tblSenia.refresh();

        } catch (Exception e) {
            System.err.println("Error al cargar señas del pedido: " + e.getMessage());
            e.printStackTrace();
            tblSenia.setItems(FXCollections.observableArrayList());
        }
    }

    private void cargarProductosDelPedido(Pedido pedidoSeleccionado) {
        try {
            // MEJORADO: Recargar el pedido con sus detalles desde la BD
            if (pedidoDao == null) {
                pedidoDao = new PedidoDAO();
            }
            
            Pedido pedidoCompleto = pedidoDao.buscarPorIdConDetalles(pedidoSeleccionado.getId());
            
            ObservableList<DetallePedido> detallesObservable = FXCollections.observableArrayList();
            
            if (pedidoCompleto != null && pedidoCompleto.getDetallePedidos() != null && !pedidoCompleto.getDetallePedidos().isEmpty()) {
                detallesObservable.addAll(pedidoCompleto.getDetallePedidos());
                
                // Debug mejorado con información de unidades
                System.out.println("=== PRODUCTOS CARGADOS ===");
                System.out.println("Productos cargados: " + detallesObservable.size());
                for (DetallePedido detalle : detallesObservable) {
                    String producto = detalle.getProducto() != null ? detalle.getProducto().getNombre() : "Sin producto";
                    Double cantidad = detalle.getCantidad();
                    Double precio = detalle.getPrecio();
                    String unidad = detalle.getUnidadMedida();
                    Double pesoPorUnidad = detalle.getProducto() != null ? detalle.getProducto().getPesoPorUnidad() : null;
                    
                    // Calcular total según unidad
                    double total = 0.0;
                    if ("kg".equalsIgnoreCase(unidad)) {
                        total = cantidad * precio;
                    } else if ("Unidad".equalsIgnoreCase(unidad) && pesoPorUnidad != null) {
                        total = cantidad * precio * pesoPorUnidad;
                    } else {
                        total = cantidad * precio; // fallback
                    }
                    
                    System.out.printf("- %s: %.3f %s × $%.2f", producto, cantidad, unidad, precio);
                    if ("Unidad".equalsIgnoreCase(unidad) && pesoPorUnidad != null) {
                        System.out.printf(" × %.3f kg/unid", pesoPorUnidad);
                    }
                    System.out.printf(" = $%.2f%n", total);
                }
                System.out.println("========================");
            } else {
                System.out.println("No se encontraron productos para el pedido ID: " + pedidoSeleccionado.getId());
            }
            
            tblProductos.setItems(detallesObservable);
            tblProductos.refresh();

        } catch (Exception e) {
            System.err.println("Error al cargar productos del pedido: " + e.getMessage());
            e.printStackTrace();
            tblProductos.setItems(FXCollections.observableArrayList());
        }
    }

    // NUEVO MÉTODO para limpiar tablas relacionadas
    private void limpiarTablasPedidoRelacionadas() {
        tablaPedidos.setItems(FXCollections.observableArrayList());
        tblSenia.setItems(FXCollections.observableArrayList());
        tblProductos.setItems(FXCollections.observableArrayList());
        pedidoSeleccionado = null;
    }

    // MEJORADO: Método de refresco más robusto
    public void refrescarDatos() {
        try {
            Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
            
            if (clienteSeleccionado != null) {
                // Recargar pedidos del cliente
                cargarPedidosDeCliente(clienteSeleccionado);
                
                // Si había un pedido seleccionado, intentar mantener la selección
                if (pedidoSeleccionado != null) {
                    Platform.runLater(() -> {
                        // Buscar el pedido en la lista actualizada
                        ObservableList<Pedido> pedidosActualizados = tablaPedidos.getItems();
                        for (int i = 0; i < pedidosActualizados.size(); i++) {
                            if (pedidosActualizados.get(i).getId().equals(pedidoSeleccionado.getId())) {
                                tablaPedidos.getSelectionModel().select(i);
                                break;
                            }
                        }
                    });
                }
            } else {
                // Recargar lista completa de clientes
                cargarClientesConPedidos();
            }
        } catch (Exception e) {
            System.err.println("Error al refrescar datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void difuminarTodo() {
        difuminar.setVisible(true);
        difuminar.setDisable(false);

        overlayPedido.setVisible(true);
        overlayPedido.setDisable(false);

        invocarSpaCrearPedido();

        Platform.runLater(() -> {
            try {
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
            } catch (Exception e) {
                System.err.println("Error al aplicar efecto de difuminado: " + e.getMessage());
            }
        });
    }

    public void CerrarDifuminarYSpa() {
        difuminar.setDisable(true);
        difuminar.setVisible(false);

        overlayPedido.setDisable(true);
        overlayPedido.setVisible(false);
        
        // Limpiar el efecto de difuminado
        difuminar.getChildren().removeIf(node -> node instanceof ImageView);
    }

    @FXML
    public void invocarSpaCrearPedido() {
        try {
            overlayPedido.getChildren().clear();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cliente_CrearPedido.fxml"));
            Parent root = loader.load();
            crearPedidoController = loader.getController();
            crearPedidoController.setSpa_clientePedidoController(this);

            overlayPedido.getChildren().add(root);

            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (IOException e) {
            System.err.println("Error al cargar el SPA de crear pedido: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getters para acceder desde otros controllers
    public Pedido getPedidoSeleccionado() {
        return pedidoSeleccionado;
    }

    public Cliente getClienteSeleccionado() {
        return tablaClientes.getSelectionModel().getSelectedItem();
    }
}