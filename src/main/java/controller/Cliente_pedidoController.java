/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import Util.HibernateUtil;
import dao.ClienteDAO;
import dao.DetallePedidoDAO;
import dao.PedidoDAO;
import dao.SeniaDAO;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import model.Cliente;
import model.DetallePedido;
import model.Pedido;
import model.Senia;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * FXML Controller class
 *
 * @author facun
 */
public class Cliente_pedidoController implements Initializable {

    //clases y Dao
    private Cliente cliente;
    private ClienteDAO clienteDao;
    private Pedido pedido;
    private PedidoDAO pedidoDao;
    private Senia senia;
    private SeniaDAO seniaDao;

    @FXML
    private AnchorPane difuminar3;
    @FXML
    private AnchorPane overLayAgregarUnProducto;

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
    @FXML
    private AnchorPane difuminar1;

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
    @FXML
    private AnchorPane overLayAgregarSenia;
    @FXML
    private CheckBox checkTelefono;

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
    private TableColumn<DetallePedido, String> colUnidadMedida;
    @FXML
    private TableColumn<DetallePedido, Double> colTotal;

    //controller
    @FXML
    private Cliente_CrearPedidoController crearPedidoController;
    private AgregarUnProductoController agregarUnProductoController;
    private AgregarSeniaController agregarSeniaController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCelular.setCellValueFactory(new PropertyValueFactory<>("celular"));

        configurarColumnaFecha();
        configurarColumnaMonto();
        configurarColumnaSena();
        configurarColumnaResto();
        configurarColumnaEstado();
        configurarColumnaFechaEntrega();

        configurarColumnasSeniasSimple();
        configurarColumnasProductos();
        configurarCheckBoxTelefono();
        cargarClientesConPedidos();
    }

    @FXML
    public void borrarProducto() {
        try {
            DetallePedido detalleSeleccionado = tblProductos.getSelectionModel().getSelectedItem();
            if (detalleSeleccionado == null) {
                mostrarAlerta("Advertencia", "Por favor, seleccione un producto de la tabla para eliminar.", Alert.AlertType.WARNING);
                return;
            }

            if (pedidoSeleccionado == null) {
                mostrarAlerta("Error", "No hay un pedido seleccionado.", Alert.AlertType.ERROR);
                return;
            }

            // Confirmación de eliminación
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar eliminación");
            confirmAlert.setHeaderText("¿Está seguro de eliminar este producto?");
            confirmAlert.setContentText(
                    "Producto: " + (detalleSeleccionado.getProducto() != null ? detalleSeleccionado.getProducto().getNombre() : "Sin nombre") + "\n"
                    + "Cantidad: " + detalleSeleccionado.getCantidad() + "\n"
                    + "Esta acción no se puede deshacer."
            );

            Optional<ButtonType> confirmResult = confirmAlert.showAndWait();
            if (confirmResult.isEmpty() || confirmResult.get() != ButtonType.OK) {
                return;
            }

            if (detalleSeleccionado.getId() == null) {
                System.err.println("ERROR: ID del DetallePedido es NULL");
                mostrarAlerta("Error Crítico", "El ID del producto seleccionado es nulo.", Alert.AlertType.ERROR);
                return;
            }

            // Intentar eliminación con múltiples métodos
            DetallePedidoDAO detallePedidoDao = new DetallePedidoDAO();
            boolean eliminado1 = detallePedidoDao.eliminar(detalleSeleccionado.getId());

            boolean eliminado2 = false;
            if (!eliminado1) {
                eliminado2 = pedidoDao.eliminarDetallePedido(detalleSeleccionado.getId());
            }

            boolean eliminado3 = false;
            if (!eliminado1 && !eliminado2) {
                eliminado3 = eliminarConSQLNativo(detalleSeleccionado.getId());
            }

            boolean eliminadoExitoso = eliminado1 || eliminado2 || eliminado3;

            if (eliminadoExitoso) {
                String metodoUsado = eliminado1 ? "DetallePedidoDAO" : (eliminado2 ? "PedidoDAO" : "SQL Nativo");
                refrescarDatos();

                // ✅ Mostrar mensaje de éxito
                mostrarAlerta("Éxito", "El producto fue eliminado correctamente (método: " + metodoUsado + ").", Alert.AlertType.INFORMATION);

            } else {
                System.err.println("ERROR: TODOS LOS MÉTODOS FALLARON");
                mostrarAlerta("Error", "No se pudo eliminar el producto con ningún método disponible.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("ERROR al borrar producto: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error Crítico", "Error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean eliminarConSQLNativo(Long detallePedidoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            String sql = "DELETE FROM detalle_pedido WHERE id = :id";
            int filasEliminadas = session.createNativeQuery(sql)
                    .setParameter("id", detallePedidoId)
                    .executeUpdate();
            tx.commit();
            return filasEliminadas > 0;
        } catch (Exception e) {
            System.err.println("Error con SQL nativo: " + e.getMessage());
            return false;
        }
    }

    @FXML
    public void invocarSpaAgregarUnProducto() {
        try {
            if (pedidoSeleccionado == null) {
                mostrarAlerta("Advertencia", "Por favor, seleccione un pedido antes de agregar un producto.", Alert.AlertType.WARNING);
                return;
            }

            Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
            if (clienteSeleccionado == null) {
                mostrarAlerta("Advertencia", "Por favor, seleccione un cliente antes de agregar un producto.", Alert.AlertType.WARNING);
                return;
            }

            activarDifuminado3();

            if (overLayAgregarUnProducto != null) {
                overLayAgregarUnProducto.getChildren().clear();
            } else {
                System.err.println("ERROR: overLayAgregarUnProducto es nulo");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/agregarUnProducto.fxml"));
            Parent root = loader.load();

            agregarUnProductoController = loader.getController();
            if (agregarUnProductoController != null) {
                agregarUnProductoController.setClientePedidoController(this);
                agregarUnProductoController.setCliente(clienteSeleccionado);
                agregarUnProductoController.setPedido(pedidoSeleccionado);
            }

            overLayAgregarUnProducto.setVisible(true);
            overLayAgregarUnProducto.setDisable(false);
            overLayAgregarUnProducto.toFront();
            overLayAgregarUnProducto.getChildren().add(root);

            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (IOException e) {
            System.err.println("ERROR al cargar agregarUnProducto: " + e.getMessage());
            desactivarDifuminado3();
        } catch (Exception e) {
            System.err.println("ERROR general: " + e.getMessage());
            desactivarDifuminado3();
        }
    }

    private void activarDifuminado3() {
        difuminar3.setVisible(true);
        difuminar3.setDisable(false);

        Platform.runLater(() -> {
            try {
                Parent root = difuminar3.getScene().getRoot();
                Bounds boundsInScene = difuminar3.localToScene(difuminar3.getBoundsInLocal());

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
                img.setEffect(new GaussianBlur(15));

                img.setLayoutX(0);
                img.setLayoutY(0);

                if (difuminar3.getChildren().stream().noneMatch(node -> node instanceof ImageView)) {
                    difuminar3.getChildren().add(0, img);
                }

                difuminar3.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");

            } catch (Exception e) {
                System.err.println("Error al aplicar difuminado3: " + e.getMessage());
            }
        });
    }

    private void desactivarDifuminado3() {
        if (difuminar3 != null) {
            difuminar3.setDisable(true);
            difuminar3.setVisible(false);
            difuminar3.getChildren().removeIf(node -> node instanceof ImageView);
            difuminar3.setStyle("");
        }
    }

    public void cerrarSpaAgregarUnProducto() {
        if (overLayAgregarUnProducto != null) {
            overLayAgregarUnProducto.setDisable(true);
            overLayAgregarUnProducto.setVisible(false);
            overLayAgregarUnProducto.getChildren().clear();
        }
        desactivarDifuminado3();
        refrescarDatos();
    }

    private void configurarCheckBoxTelefono() {
        checkTelefono.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                ocultarDatosTelefono();
            } else {
                mostrarDatosTelefono();
            }
        });
    }

    private void ocultarDatosTelefono() {
        colCelular.setCellFactory(column -> {
            return new TableCell<Cliente, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText("***-***-****");
                        setStyle("-fx-text-fill: #888;");
                    }
                }
            };
        });
        tablaClientes.refresh();
    }

    private void mostrarDatosTelefono() {
        colCelular.setCellFactory(column -> {
            return new TableCell<Cliente, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("");
                    }
                }
            };
        });
        tablaClientes.refresh();
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

        // MEDIO DE PAGO
        if (colMedioPago != null) {
            colMedioPago.setCellValueFactory(new PropertyValueFactory<>("medioPago"));
            colMedioPago.setCellFactory(column -> {
                return new TableCell<Senia, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? "N/A" : item);
                    }
                };
            });
        }

        if (colHoraSenia != null) {
            colHoraSenia.setVisible(false);
        }
    }

    private void configurarColumnasProductos() {
        // PRODUCTO
        colProducto.setCellValueFactory(cellData -> {
            DetallePedido detalle = cellData.getValue();
            if (detalle != null && detalle.getProducto() != null) {
                return new SimpleStringProperty(detalle.getProducto().getNombre());
            }
            return new SimpleStringProperty("Sin producto");
        });

        // PRECIO UNITARIO
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

        // CANTIDAD
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
                            if (item == Math.floor(item)) {
                                setText(String.format("%.0f unid.", item));
                            } else {
                                setText(String.format("%.2f unid.", item));
                            }
                        } else {
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

        // UNIDAD DE MEDIDA
        if (colUnidadMedida != null) {
            colUnidadMedida.setCellValueFactory(new PropertyValueFactory<>("unidadMedida"));
            colUnidadMedida.setCellFactory(column -> {
                return new TableCell<DetallePedido, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item);
                    }
                };
            });
        }

        // TOTAL
        colTotal.setCellValueFactory(cellData -> {
            DetallePedido detalle = cellData.getValue();
            if (detalle != null) {
                Double precio = detalle.getPrecio();
                Double cantidad = detalle.getCantidad();
                String unidadMedida = detalle.getUnidadMedida();

                if (precio != null && cantidad != null && unidadMedida != null) {
                    double total = 0.0;

                    if (unidadMedida.equalsIgnoreCase("kg")) {
                        total = cantidad * precio;
                    } else if (unidadMedida.equalsIgnoreCase("Unidad")) {
                        if (detalle.getProducto() != null && detalle.getProducto().getPesoPorUnidad() != null) {
                            Double pesoPorUnidad = detalle.getProducto().getPesoPorUnidad();
                            total = cantidad * precio * pesoPorUnidad;
                        } else {
                            total = cantidad * precio;
                        }
                    } else {
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
                    setText(empty || item == null ? null : String.format("$%.2f", item));
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
                    setText(empty || item == null ? null : formatter.format(item));
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
                    setText(empty || item == null ? null : String.format("$%.2f", item));
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
                    setText(empty || item == null ? null : String.format("$%.2f", item));
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
                    setText(empty || item == null ? null : String.format("$%.2f", item));
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
                    setText(empty || item == null ? null : formatter.format(item));
                }
            };
        });
    }

    public void cargarClientesConPedidos() {
        try {
            clienteDao = new ClienteDAO();
            listaClientePedidos = clienteDao.buscarClientesConPedidos();

            ObservableList<Cliente> clientesObservable = FXCollections.observableArrayList(listaClientePedidos);
            tablaClientes.setItems(clientesObservable);

            tablaClientes.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, selectedCliente) -> {
                        if (selectedCliente != null) {
                            cargarPedidosDeCliente(selectedCliente);
                        } else {
                            limpiarTablasPedidoRelacionadas();
                        }
                    }
            );
        } catch (Exception e) {
            System.err.println("Error al cargar clientes con pedidos: " + e.getMessage());
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

            tablaPedidos.getSelectionModel().clearSelection();
            pedidoSeleccionado = null;

            tblSenia.setItems(FXCollections.observableArrayList());
            tblProductos.setItems(FXCollections.observableArrayList());

            tablaPedidos.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, selectedPedido) -> {
                        if (selectedPedido != null && !selectedPedido.equals(oldValue)) {
                            pedidoSeleccionado = selectedPedido;
                            cargarDatosDelPedido(selectedPedido);
                        } else if (selectedPedido == null) {
                            pedidoSeleccionado = null;
                            tblSenia.setItems(FXCollections.observableArrayList());
                            tblProductos.setItems(FXCollections.observableArrayList());
                        }
                    }
            );

            tablaPedidos.refresh();

        } catch (Exception e) {
            System.err.println("Error al cargar pedidos del cliente: " + e.getMessage());
            limpiarTablasPedidoRelacionadas();
        }
    }

    private void cargarDatosDelPedido(Pedido pedido) {
        if (pedido == null) {
            return;
        }

        Platform.runLater(() -> {
            try {
                cargarSeniasDelPedido(pedido);
                cargarProductosDelPedido(pedido);
            } catch (Exception e) {
                System.err.println("Error al cargar datos del pedido: " + e.getMessage());
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
            tblSenia.setItems(FXCollections.observableArrayList());
        }
    }

    private void cargarProductosDelPedido(Pedido pedidoSeleccionado) {
        try {
            if (pedidoDao == null) {
                pedidoDao = new PedidoDAO();
            }

            Pedido pedidoCompleto = pedidoDao.buscarPorIdConDetalles(pedidoSeleccionado.getId());

            ObservableList<DetallePedido> detallesObservable = FXCollections.observableArrayList();

            if (pedidoCompleto != null && pedidoCompleto.getDetallePedidos() != null && !pedidoCompleto.getDetallePedidos().isEmpty()) {
                detallesObservable.addAll(pedidoCompleto.getDetallePedidos());
            }

            tblProductos.setItems(detallesObservable);
            tblProductos.refresh();

        } catch (Exception e) {
            System.err.println("Error al cargar productos del pedido: " + e.getMessage());
            tblProductos.setItems(FXCollections.observableArrayList());
        }
    }

    private void limpiarTablasPedidoRelacionadas() {
        tablaPedidos.setItems(FXCollections.observableArrayList());
        tblSenia.setItems(FXCollections.observableArrayList());
        tblProductos.setItems(FXCollections.observableArrayList());
        pedidoSeleccionado = null;
    }

    public void refrescarDatos() {
        try {
            Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();

            if (clienteSeleccionado != null) {
                cargarPedidosDeCliente(clienteSeleccionado);

                if (pedidoSeleccionado != null) {
                    Platform.runLater(() -> {
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
                cargarClientesConPedidos();
            }
        } catch (Exception e) {
            System.err.println("Error al refrescar datos: " + e.getMessage());
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

        difuminar.getChildren().removeIf(node -> node instanceof ImageView);
    }

    @FXML
    public void invocarSpaAgregarSenia() {
        try {
            if (pedidoSeleccionado == null) {
                mostrarAlerta("Advertencia", "Por favor, seleccione un pedido antes de agregar una seña.", Alert.AlertType.WARNING);
                return;
            }

            Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
            if (clienteSeleccionado == null) {
                mostrarAlerta("Advertencia", "Por favor, seleccione un cliente antes de agregar una seña.", Alert.AlertType.WARNING);
                return;
            }

            activarDifuminado1();

            if (overLayAgregarSenia != null) {
                overLayAgregarSenia.getChildren().clear();
            } else {
                System.err.println("ERROR: overLayAgregarSenia es nulo");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/agregarSenia.fxml"));
            Parent root = loader.load();

            agregarSeniaController = loader.getController();
            if (agregarSeniaController != null) {
                agregarSeniaController.setClientePedidoController(this);
                agregarSeniaController.setCliente(clienteSeleccionado);
                agregarSeniaController.setPedido(pedidoSeleccionado);
            }

            overLayAgregarSenia.setVisible(true);
            overLayAgregarSenia.setDisable(false);
            overLayAgregarSenia.toFront();
            overLayAgregarSenia.getChildren().add(root);

            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (IOException e) {
            System.err.println("ERROR al cargar agregarSenia: " + e.getMessage());
            desactivarDifuminado1();
        } catch (Exception e) {
            System.err.println("ERROR general: " + e.getMessage());
            desactivarDifuminado1();
        }
    }

    private void activarDifuminado1() {
        difuminar1.setVisible(true);
        difuminar1.setDisable(false);

        Platform.runLater(() -> {
            try {
                Parent root = difuminar1.getScene().getRoot();
                Bounds boundsInScene = difuminar1.localToScene(difuminar1.getBoundsInLocal());

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
                img.setEffect(new GaussianBlur(15));

                img.setLayoutX(0);
                img.setLayoutY(0);

                if (difuminar1.getChildren().stream().noneMatch(node -> node instanceof ImageView)) {
                    difuminar1.getChildren().add(0, img);
                }

                difuminar1.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");

            } catch (Exception e) {
                System.err.println("Error al aplicar difuminado1: " + e.getMessage());
            }
        });
    }

    private void desactivarDifuminado1() {
        if (difuminar1 != null) {
            difuminar1.setDisable(true);
            difuminar1.setVisible(false);
            difuminar1.getChildren().removeIf(node -> node instanceof ImageView);
            difuminar1.setStyle("");
        }
    }

    @FXML
    public void borrarSenia() {
        try {
            Senia seniaSeleccionada = tblSenia.getSelectionModel().getSelectedItem();
            if (seniaSeleccionada == null) {
                mostrarAlerta("Advertencia", "Por favor, seleccione una seña de la tabla para eliminar.", Alert.AlertType.WARNING);
                return;
            }

            // Solicitar contraseña
            Dialog<String> passwordDialog = new Dialog<>();
            passwordDialog.setTitle("Confirmar eliminación");
            passwordDialog.setHeaderText("Eliminar seña del "
                    + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(seniaSeleccionada.getFecha()));

            ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            passwordDialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("Ingrese la contraseña para confirmar:");
            passwordField.setPrefWidth(300);

            VBox vbox = new VBox(10);
            vbox.getChildren().addAll(
                    new Label("Ingrese la contraseña para confirmar:"),
                    passwordField
            );
            vbox.setPadding(new Insets(20));

            passwordDialog.getDialogPane().setContent(vbox);

            Platform.runLater(() -> passwordField.requestFocus());

            passwordDialog.setResultConverter(dialogButton -> {
                if (dialogButton == loginButtonType) {
                    return passwordField.getText();
                }
                return null;
            });

            Optional<String> passwordResult = passwordDialog.showAndWait();

            if (!passwordResult.isPresent()) {
                return;
            }

            String passwordIngresada = passwordResult.get().trim();

            if (!"2808".equals(passwordIngresada)) {
                mostrarAlerta("Error", "Contraseña incorrecta. No se puede eliminar la seña.", Alert.AlertType.ERROR);
                return;
            }

            // Confirmación final
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmación final");
            confirmAlert.setHeaderText("¿Está seguro de eliminar esta seña?");
            confirmAlert.setContentText(
                    "Fecha: " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(seniaSeleccionada.getFecha()) + "\n"
                    + "Monto: $" + String.format("%.2f", seniaSeleccionada.getMonto()) + "\n"
                    + "\nEsta acción no se puede deshacer."
            );

            Optional<ButtonType> confirmResult = confirmAlert.showAndWait();
            if (confirmResult.get() != ButtonType.OK) {
                return;
            }

            // Eliminar de la base de datos
            if (seniaDao == null) {
                seniaDao = new SeniaDAO();
            }

            seniaDao.eliminar(seniaSeleccionada.getId());

            mostrarAlerta("Éxito",
                    "La seña ha sido eliminada correctamente:\n"
                    + "Fecha: " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(seniaSeleccionada.getFecha()) + "\n"
                    + "Monto: $" + String.format("%.2f", seniaSeleccionada.getMonto()),
                    Alert.AlertType.INFORMATION);

            // Refrescar datos
            if (pedidoSeleccionado != null) {
                cargarSeniasDelPedido(pedidoSeleccionado);

                Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
                if (clienteSeleccionado != null) {
                    cargarPedidosDeCliente(clienteSeleccionado);

                    Platform.runLater(() -> {
                        ObservableList<Pedido> pedidosActualizados = tablaPedidos.getItems();
                        for (int i = 0; i < pedidosActualizados.size(); i++) {
                            if (pedidosActualizados.get(i).getId().equals(pedidoSeleccionado.getId())) {
                                tablaPedidos.getSelectionModel().select(i);
                                break;
                            }
                        }
                    });
                }
            }

        } catch (Exception e) {
            System.err.println("ERROR al borrar seña: " + e.getMessage());
            mostrarAlerta("Error", "Error inesperado al eliminar la seña: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void borrarPedidoCompleto() {
        try {
            Pedido pedidoABorrar = tablaPedidos.getSelectionModel().getSelectedItem();
            if (pedidoABorrar == null) {
                mostrarAlerta("Advertencia", "Por favor, seleccione un pedido de la tabla para eliminar.", Alert.AlertType.WARNING);
                return;
            }

            Cliente clienteAsociado = pedidoABorrar.getCliente();
            if (clienteAsociado == null) {
                mostrarAlerta("Error", "No se puede determinar el cliente asociado al pedido.", Alert.AlertType.ERROR);
                return;
            }

            // Solicitar contraseña de administrador
            Dialog<String> passwordDialog = new Dialog<>();
            passwordDialog.setTitle("ELIMINAR PEDIDO COMPLETO");
            passwordDialog.setHeaderText("ADVERTENCIA: Esta acción eliminará TODO el pedido y datos relacionados");

            ButtonType loginButtonType = new ButtonType("ELIMINAR", ButtonBar.ButtonData.OK_DONE);
            passwordDialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("Contraseña de administrador:");
            passwordField.setPrefWidth(300);

            VBox vbox = new VBox(10);
            Label warningLabel = new Label("⚠️ ESTA ACCIÓN ES IRREVERSIBLE ⚠️");
            warningLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");

            Label infoLabel = new Label("Se eliminarán:");
            Label detailsLabel = new Label(
                    "• Cliente: " + clienteAsociado.getNombre() + "\n"
                    + "• Pedido del: " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(pedidoABorrar.getFecha()) + "\n"
                    + "• Total: $" + String.format("%.2f", pedidoABorrar.getTotal()) + "\n"
                    + "• Todas las señas asociadas\n"
                    + "• Todos los detalles del pedido\n"
                    + "• El registro completo del cliente"
            );
            detailsLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 12px;");

            vbox.getChildren().addAll(
                    warningLabel,
                    new Label(""),
                    infoLabel,
                    detailsLabel,
                    new Label(""),
                    new Label("Ingrese la contraseña de administrador para confirmar:"),
                    passwordField
            );
            vbox.setPadding(new Insets(20));

            passwordDialog.getDialogPane().setContent(vbox);

            Platform.runLater(() -> passwordField.requestFocus());

            passwordDialog.setResultConverter(dialogButton -> {
                if (dialogButton == loginButtonType) {
                    return passwordField.getText();
                }
                return null;
            });

            Optional<String> passwordResult = passwordDialog.showAndWait();

            if (!passwordResult.isPresent()) {
                return;
            }

            String passwordIngresada = passwordResult.get().trim();

            if (!"2808".equals(passwordIngresada)) {
                mostrarAlerta("Error", "Contraseña de administrador incorrecta. Operación cancelada.", Alert.AlertType.ERROR);
                return;
            }

            // Confirmaciones múltiples
            Alert confirmAlert1 = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert1.setTitle("PRIMERA CONFIRMACIÓN");
            confirmAlert1.setHeaderText("¿Está COMPLETAMENTE seguro?");
            confirmAlert1.setContentText(
                    "Se eliminará PERMANENTEMENTE:\n\n"
                    + "Cliente: " + clienteAsociado.getNombre() + "\n"
                    + "Teléfono: " + clienteAsociado.getCelular() + "\n"
                    + "Pedido: " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(pedidoABorrar.getFecha()) + "\n"
                    + "Total: $" + String.format("%.2f", pedidoABorrar.getTotal()) + "\n\n"
                    + "¿Continuar con la eliminación?"
            );

            Optional<ButtonType> confirmResult1 = confirmAlert1.showAndWait();
            if (confirmResult1.get() != ButtonType.OK) {
                return;
            }

            Alert confirmAlert2 = new Alert(Alert.AlertType.WARNING);
            confirmAlert2.setTitle("SEGUNDA CONFIRMACIÓN");
            confirmAlert2.setHeaderText("ÚLTIMA OPORTUNIDAD DE CANCELAR");
            confirmAlert2.setContentText(
                    "Esta acción NO se puede deshacer.\n\n"
                    + "Se perderán todos los datos del cliente y pedido.\n\n"
                    + "¿Proceder con la eliminación DEFINITIVA?"
            );

            Optional<ButtonType> confirmResult2 = confirmAlert2.showAndWait();
            if (confirmResult2.get() != ButtonType.OK) {
                return;
            }

            // Proceder con la eliminación
            if (seniaDao == null) {
                seniaDao = new SeniaDAO();
            }
            if (pedidoDao == null) {
                pedidoDao = new PedidoDAO();
            }
            if (clienteDao == null) {
                clienteDao = new ClienteDAO();
            }

            // 1. Eliminar señas
            List<Senia> seniasPedido = seniaDao.buscarSeniasPorPedido(pedidoABorrar.getId());
            for (Senia senia : seniasPedido) {
                seniaDao.eliminar(senia.getId());
            }

            // 2. Eliminar pedido
            pedidoDao.eliminar(pedidoABorrar.getId());

            // 3. Verificar otros pedidos del cliente
            List<Pedido> otrosPedidos = pedidoDao.buscarPedidosPorCliente(clienteAsociado.getId());

            if (otrosPedidos.isEmpty()) {
                clienteDao.eliminar(clienteAsociado.getId());
            }

            // Mensaje de éxito
            String mensajeExito = "Eliminación completada:\n\n"
                    + "✓ Pedido eliminado\n"
                    + "✓ " + seniasPedido.size() + " señas eliminadas\n"
                    + "✓ Detalles del pedido eliminados";

            if (otrosPedidos.isEmpty()) {
                mensajeExito += "\n✓ Cliente eliminado (no tenía otros pedidos)";
            } else {
                mensajeExito += "\n→ Cliente conservado (" + otrosPedidos.size() + " pedidos restantes)";
            }

            mostrarAlerta("Eliminación Completada", mensajeExito, Alert.AlertType.INFORMATION);

            // Refrescar vista
            tablaPedidos.getSelectionModel().clearSelection();
            tablaClientes.getSelectionModel().clearSelection();
            pedidoSeleccionado = null;

            cargarClientesConPedidos();
            limpiarTablasPedidoRelacionadas();

        } catch (Exception e) {
            System.err.println("ERROR al borrar pedido completo: " + e.getMessage());
            mostrarAlerta("Error Crítico", "Error grave durante la eliminación: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void finalizarPedidoSeleccionado() {
        try {
            Pedido pedidoSeleccionado = tablaPedidos.getSelectionModel().getSelectedItem();
            if (pedidoSeleccionado == null) {
                mostrarAlerta("Advertencia", "Por favor, seleccione un pedido de la tabla para finalizar.", Alert.AlertType.WARNING);
                return;
            }

            // Confirmación
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar finalización");
            confirmAlert.setHeaderText("¿Está seguro de finalizar este pedido?");
            confirmAlert.setContentText(
                    "Cliente: " + pedidoSeleccionado.getCliente().getNombre() + "\n"
                    + "Fecha: " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(pedidoSeleccionado.getFecha()) + "\n"
                    + "Total: $" + String.format("%.2f", pedidoSeleccionado.getTotal()) + "\n\n"
                    + "Una vez finalizado, el pedido cambiará su estado."
            );

            Optional<ButtonType> confirmResult = confirmAlert.showAndWait();
            if (confirmResult.get() != ButtonType.OK) {
                return;
            }

            // Actualizar estado
            if (pedidoDao == null) {
                pedidoDao = new PedidoDAO();
            }

            pedidoSeleccionado.setEstado(true);
            pedidoDao.actualizar(pedidoSeleccionado);

            mostrarAlerta("Éxito",
                    "El pedido ha sido finalizado correctamente:\n"
                    + "Cliente: " + pedidoSeleccionado.getCliente().getNombre() + "\n"
                    + "Fecha: " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(pedidoSeleccionado.getFecha()),
                    Alert.AlertType.INFORMATION);

            // Refrescar datos
            Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
            if (clienteSeleccionado != null) {
                cargarPedidosDeCliente(clienteSeleccionado);

                Platform.runLater(() -> {
                    ObservableList<Pedido> pedidosActualizados = tablaPedidos.getItems();
                    for (int i = 0; i < pedidosActualizados.size(); i++) {
                        if (pedidosActualizados.get(i).getId().equals(pedidoSeleccionado.getId())) {
                            tablaPedidos.getSelectionModel().select(i);
                            break;
                        }
                    }
                });
            }

        } catch (Exception e) {
            System.err.println("ERROR al finalizar pedido: " + e.getMessage());
            mostrarAlerta("Error", "Error inesperado al finalizar el pedido: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void cerrarSpaAgregarSenia() {
        if (overLayAgregarSenia != null) {
            overLayAgregarSenia.setDisable(true);
            overLayAgregarSenia.setVisible(false);
            overLayAgregarSenia.getChildren().clear();
        }
        desactivarDifuminado1();
        refrescarDatos();
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
        }
    }

    // Getters
    public Pedido getPedidoSeleccionado() {
        return pedidoSeleccionado;
    }

    public Cliente getClienteSeleccionado() {
        return tablaClientes.getSelectionModel().getSelectedItem();
    }
}
