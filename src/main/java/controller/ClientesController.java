package controller;

import dao.ClienteDAO;
import dao.FiadoDAO;
import dao.FiadoParcialDAO;
import dao.VentaDAO;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import model.Cliente;
import model.DetalleVenta;
import model.Fiado;
import model.FiadoParcial;
import model.Venta;

public class ClientesController implements Initializable {

    // Objetos
    private Cliente cliente;
    private Fiado fiado;
    private FiadoParcial fiadoParcial;
    private Venta venta;

    // DAO
    private ClienteDAO clienteDao;
    private FiadoDAO fiadoDao;
    private FiadoParcialDAO fiadoParcialDao;
    private VentaDAO ventaDao;

    // Variables
    DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm");
    List<Cliente> listaClientes;
    private Cliente clienteSeleccionado;
    private boolean listenerFiadosConfigurado = false; // Flag para evitar múltiples listeners

    @FXML
    private AnchorPane difuminar;
    @FXML
    private AnchorPane spaDetalle;
    @FXML
    private AgregarPago_clienteController agregarPagoClienteController;
    @FXML
    private Label lblClienteSeleccionado;
    @FXML
    private CheckBox checkTelefono;
    @FXML
    private AnchorPane anchorPaneHide;

    // Tabla clientes
    @FXML
    private TableView<Cliente> tblClientes;
    @FXML
    private TableColumn<Cliente, String> colNombre;
    @FXML
    private TableColumn<Cliente, String> colAlias;
    @FXML
    private TableColumn<Cliente, String> colCelular;

    // Tabla fiados
    @FXML
    private TableView<Fiado> tablaFiados;
    @FXML
    private TableColumn<Fiado, String> colFecha;
    @FXML
    private TableColumn<Fiado, String> colHora;
    @FXML
    private TableColumn<Fiado, Double> colMonto;
    @FXML
    private TableColumn<Fiado, Double> colAnticipo;
    @FXML
    private TableColumn<Fiado, Double> colResto;
    @FXML
    private TableColumn<Fiado, Boolean> colEstado;

    // Tabla anticipos
    @FXML
    private TableView<FiadoParcial> tablaAnticipos;
    @FXML
    private TableColumn<FiadoParcial, String> colDiaAnticipo;
    @FXML
    private TableColumn<FiadoParcial, String> colHoraAnticipo;
    @FXML
    private TableColumn<FiadoParcial, Double> colDineroAnticipo;
    @FXML
    private TableColumn<FiadoParcial, String> colMedioAnticipo;

    // Tabla productos
    @FXML
    private TableView<DetalleVenta> tablaProductos;
    @FXML
    private TableColumn<DetalleVenta, String> colNombreProducto;
    @FXML
    private TableColumn<DetalleVenta, Double> colPrecio;
    @FXML
    private TableColumn<DetalleVenta, Double> colPesoCantidad;
    @FXML
    private TableColumn<DetalleVenta, Double> colTotal;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clienteDao = new ClienteDAO();
        fiadoDao = new FiadoDAO();
        ventaDao = new VentaDAO();
        fiadoParcialDao = new FiadoParcialDAO();

        listaClientes = clienteDao.buscarClientesConFiados();
        llenarTablaClientes();

        // Listener para la selección de cliente
        tblClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                DatosClienteSeleccionado();
            }
        });

        // Configurar el listener de fiados UNA SOLA VEZ en initialize
        configurarListenerFiados();
        configurarCheckBoxTelefonoConVariable();
    }

    private void configurarListenerFiados() {
        if (!listenerFiadosConfigurado) {
            tablaFiados.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    // Actualizar las tablas cuando se selecciona un fiado diferente
                    llenarTablaProductos(newSelection.getVenta());
                    llenarTablaAnticipo(newSelection.getFiadoParciales());
                }
            });
            listenerFiadosConfigurado = true;
        }
    }

    public void DatosClienteSeleccionado() {
        clienteSeleccionado = tblClientes.getSelectionModel().getSelectedItem();

        if (clienteSeleccionado == null) {
            return;
        }

        lblClienteSeleccionado.setText("Cliente: " + clienteSeleccionado.getNombre()
                + ((clienteSeleccionado.getAlias() == null || clienteSeleccionado.getAlias().isEmpty())
                ? "" : " - " + clienteSeleccionado.getAlias()));

        List<Fiado> fiadosCliente = fiadoDao.obtenerFiadosPorClienteId(clienteSeleccionado.getId());

        // Configurar columnas de fiados (esto se puede mover a initialize si siempre es igual)
        configurarColumnasTablaFiados();

        tablaFiados.getItems().setAll(fiadosCliente);

        // Limpiar tablas de detalles
        tablaProductos.getItems().clear();
        tablaAnticipos.getItems().clear();

        // Seleccionar automáticamente el primer fiado si existe
        if (!fiadosCliente.isEmpty()) {
            Platform.runLater(() -> {
                tablaFiados.getSelectionModel().selectFirst();
                // El listener se encargará de actualizar las tablas automáticamente
            });
        }
    }

    private void configurarColumnasTablaFiados() {
        colFecha.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getFecha().toLocalDate().format(fechaFormatter)));
        colHora.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getFecha().toLocalTime().format(horaFormatter)));
        colMonto.setCellValueFactory(cd -> new SimpleDoubleProperty(
                cd.getValue().getVenta().getTotal()).asObject());
        colAnticipo.setCellValueFactory(cd -> new SimpleDoubleProperty(
                cd.getValue().getTotalParciales()).asObject());
        colResto.setCellValueFactory(cd -> new SimpleDoubleProperty(
                cd.getValue().getResto()).asObject());
        colEstado.setCellValueFactory(cd -> new SimpleBooleanProperty(
                cd.getValue().getEstado()).asObject());

        colEstado.setCellFactory(column -> new TableCell<Fiado, Boolean>() {
            @Override
            protected void updateItem(Boolean estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else if (estado) {
                    setText("Finalizado");
                    setStyle("-fx-background-color: #d5f4e6; -fx-text-fill: #27ae60; -fx-alignment: center; -fx-font-weight: bold;");
                } else {
                    setText("Pendiente");
                    setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #f39c12; -fx-alignment: center; -fx-font-weight: bold;");
                }
            }
        });

        // Agregar estilo a toda la fila basado en el estado del fiado
        tablaFiados.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Fiado> row = new javafx.scene.control.TableRow<Fiado>() {
                @Override
                protected void updateItem(Fiado fiado, boolean empty) {
                    super.updateItem(fiado, empty);
                    if (empty || fiado == null) {
                        setStyle("");
                        setDisable(false);
                    } else if (fiado.getEstado()) {
                        // Fiado finalizado - darle un aspecto más tenue
                        setStyle("-fx-opacity: 0.7; -fx-background-color: #f8f9fa;");
                        setDisable(false); // No deshabilitamos para que se pueda ver la info
                    } else {
                        // Fiado pendiente - estilo normal
                        setStyle("");
                        setDisable(false);
                    }
                }
            };
            return row;
        });
    }

    public void llenarTablaClientes() {
        colNombre.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNombre()));
        colAlias.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getAlias()));
        colCelular.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCelular()));
        tblClientes.getItems().setAll(listaClientes);
    }

    public void llenarTablaProductos(Venta venta) {
        if (venta == null || venta.getDetalleVentas() == null) {
            tablaProductos.getItems().clear();
            return;
        }

        // Configurar columnas solo una vez (esto también se puede mover a initialize)
        colNombreProducto.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getProducto().getNombre()));
        colPrecio.setCellValueFactory(cd -> new SimpleDoubleProperty(
                cd.getValue().getPrecio()).asObject());
        colPesoCantidad.setCellValueFactory(cd -> new SimpleDoubleProperty(
                cd.getValue().getPeso()).asObject());
        colTotal.setCellValueFactory(cd -> new SimpleDoubleProperty(
                cd.getValue().getPrecio() * cd.getValue().getPeso()).asObject());

        tablaProductos.getItems().setAll(venta.getDetalleVentas());
    }

    public void llenarTablaAnticipo(List<FiadoParcial> listaAnticipos) {
        if (listaAnticipos == null) {
            tablaAnticipos.getItems().clear();
            return;
        }

        // Configurar columnas (esto también se puede mover a initialize)
        colDiaAnticipo.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getFecha().toLocalDate().format(fechaFormatter)));
        colHoraAnticipo.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getFecha().toLocalTime().format(horaFormatter)));
        colDineroAnticipo.setCellValueFactory(cd -> new SimpleDoubleProperty(
                cd.getValue().getAnticipo()).asObject());
        colMedioAnticipo.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getMedioAbonado()));

        tablaAnticipos.getItems().setAll(listaAnticipos);
    }

    // Método para refrescar datos después de agregar un pago
    public void refrescarDatos() {
        if (clienteSeleccionado != null) {
            // Volver a cargar los fiados del cliente
            List<Fiado> fiadosActualizados = fiadoDao.obtenerFiadosPorClienteId(clienteSeleccionado.getId());
            tablaFiados.getItems().setAll(fiadosActualizados);

            // Mantener la selección del fiado actual si es posible
            Fiado fiadoActual = tablaFiados.getSelectionModel().getSelectedItem();
            if (fiadoActual != null) {
                // Buscar el fiado actualizado y seleccionarlo
                for (Fiado fiado : fiadosActualizados) {
                    if (fiado.getId().equals(fiadoActual.getId())) {
                        Platform.runLater(() -> {
                            tablaFiados.getSelectionModel().select(fiado);
                        });
                        break;
                    }
                }
            }
        }
    }

    // Método para verificar si se puede agregar pago al fiado seleccionado
    public boolean puedeAgregarPago() {
        Fiado fiadoSeleccionado = tablaFiados.getSelectionModel().getSelectedItem();
        return clienteSeleccionado != null
                && fiadoSeleccionado != null
                && !fiadoSeleccionado.getEstado(); // true si está pendiente
    }

    // Método para obtener el estado del fiado seleccionado (útil para la UI)
    public String getEstadoFiadoSeleccionado() {
        Fiado fiadoSeleccionado = tablaFiados.getSelectionModel().getSelectedItem();
        if (fiadoSeleccionado == null) {
            return "No hay fiado seleccionado";
        }
        return fiadoSeleccionado.getEstado() ? "Finalizado" : "Pendiente";
    }

    // Métodos para difuminar pantalla
    public void difuminarTodo() {
        // PRIMERO validar antes de hacer cualquier cosa
        Fiado fiadoSeleccionado = tablaFiados.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado == null || fiadoSeleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un cliente y un fiado", javafx.scene.control.Alert.AlertType.WARNING);
            return; // SALIR sin hacer nada más
        }

        // Validar que el fiado no esté finalizado
        if (fiadoSeleccionado.getEstado()) {
            mostrarAlerta("Fiado Finalizado",
                    "Este fiado ya ha sido completado. No se pueden agregar más pagos.\n\n"
                    + "Cliente: " + clienteSeleccionado.getNombre()
                    + (clienteSeleccionado.getAlias() != null && !clienteSeleccionado.getAlias().isEmpty()
                    ? " - " + clienteSeleccionado.getAlias() : "")
                    + "\nFecha del fiado: " + fiadoSeleccionado.getFecha().toLocalDate().format(fechaFormatter)
                    + "\nMonto total: $" + fiadoSeleccionado.getVenta().getTotal(),
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            return; // SALIR sin hacer nada más
        }

        // Si llegamos aquí, todo está OK, proceder con el difuminado
        difuminar.setVisible(true);
        difuminar.setDisable(false);
        spaDetalle.setVisible(true);
        spaDetalle.setDisable(false);
        invocarSpaDetalle();

        Platform.runLater(() -> {
            Parent root = difuminar.getScene().getRoot();
            Bounds boundsInScene = difuminar.localToScene(difuminar.getBoundsInLocal());
            SnapshotParameters params = new SnapshotParameters();
            params.setViewport(new javafx.geometry.Rectangle2D(
                    boundsInScene.getMinX(), boundsInScene.getMinY(),
                    boundsInScene.getWidth(), boundsInScene.getHeight()));
            WritableImage snapshot = new WritableImage(
                    (int) boundsInScene.getWidth(), (int) boundsInScene.getHeight());
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
        difuminar.setDisable(true);
        difuminar.setVisible(false);
        spaDetalle.setDisable(true);
        spaDetalle.setVisible(false);
    }

    @FXML
    public void invocarSpaDetalle() {
        // En este punto ya sabemos que las validaciones pasaron en difuminarTodo()
        try {
            spaDetalle.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/agregarPago_cliente.fxml"));
            Parent root = loader.load();
            agregarPagoClienteController = loader.getController();
            agregarPagoClienteController.setClienteController(this);

            // Pasar cliente y fiado seleccionados al controller del overlay
            Fiado fiadoSeleccionado = tablaFiados.getSelectionModel().getSelectedItem();
            agregarPagoClienteController.setClienteRecibido(clienteSeleccionado);
            agregarPagoClienteController.setFiadoRecibido(fiadoSeleccionado);
            agregarPagoClienteController.mostrarDatos();

            spaDetalle.getChildren().add(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar la ventana de pago: " + e.getMessage(),
                    javafx.scene.control.Alert.AlertType.ERROR);
            // Si hay error, cerrar el difuminado
            CerrarDifuminarYSpa();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, javafx.scene.control.Alert.AlertType tipo) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void borrarFiadoSeleccionado() {
        // Validar que hay un cliente seleccionado
        if (clienteSeleccionado == null) {
            mostrarAlerta("Error", "No hay cliente seleccionado", javafx.scene.control.Alert.AlertType.WARNING);
            return;
        }

        // Validar que hay un fiado seleccionado
        Fiado fiadoSeleccionado = tablaFiados.getSelectionModel().getSelectedItem();
        if (fiadoSeleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un fiado para eliminar", javafx.scene.control.Alert.AlertType.WARNING);
            return;
        }

        // NUEVA VALIDACIÓN DE CONTRASEÑA
        if (!validarContraseña()) {
            return; // Si no ingresa la contraseña correcta, salir del método
        }

        // Confirmación antes de borrar
        String mensajeConfirmacion = String.format(
                "¿Está seguro que desea eliminar el fiado seleccionado?\n\n"
                + "Cliente: %s%s\n"
                + "Fecha: %s\n"
                + "Monto: $%.2f\n"
                + "Estado: %s\n\n"
                + "Esta acción no se puede deshacer.",
                clienteSeleccionado.getNombre(),
                (clienteSeleccionado.getAlias() != null && !clienteSeleccionado.getAlias().isEmpty()
                ? " - " + clienteSeleccionado.getAlias() : ""),
                fiadoSeleccionado.getFecha().toLocalDate().format(fechaFormatter),
                fiadoSeleccionado.getVenta().getTotal(),
                fiadoSeleccionado.getEstado() ? "Finalizado" : "Pendiente"
        );

        javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION,
                mensajeConfirmacion,
                javafx.scene.control.ButtonType.YES,
                javafx.scene.control.ButtonType.NO
        );

        alerta.setTitle("Confirmar eliminación");
        alerta.setHeaderText("Eliminar Fiado");

        alerta.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.YES) {
                try {
                    // Borrar el fiado específico (esto debería borrar en cascada los FiadoParcial)
                    boolean eliminado = fiadoDao.eliminarFiadoPorId(fiadoSeleccionado.getId());

                    if (eliminado) {
                        System.out.println("Fiado eliminado correctamente - ID: " + fiadoSeleccionado.getId());

                        // Refrescar los datos del cliente seleccionado
                        DatosClienteSeleccionado();

                        // Mostrar mensaje de éxito
                        mostrarAlerta("Éxito", "El fiado ha sido eliminado correctamente",
                                javafx.scene.control.Alert.AlertType.INFORMATION);

                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar el fiado",
                                javafx.scene.control.Alert.AlertType.ERROR);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarAlerta("Error", "Error al eliminar el fiado: " + e.getMessage(),
                            javafx.scene.control.Alert.AlertType.ERROR);
                }
            }
        });
    }

// Método para validar la contraseña
    private boolean validarContraseña() {
        final String CONTRASEÑA_CORRECTA = "FordFocus2808#";

        // Crear dialog para ingresar contraseña
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Validación de Seguridad");
        dialog.setHeaderText("Eliminar Fiado");
        dialog.setContentText("Ingrese la contraseña para continuar:");

        // Hacer que el campo sea de contraseña (ocultar caracteres)
        javafx.scene.control.TextField textField = dialog.getEditor();
        javafx.scene.control.PasswordField passwordField = new javafx.scene.control.PasswordField();
        passwordField.setPromptText("Contraseña");

        // Reemplazar el TextField por un PasswordField
        dialog.getDialogPane().setContent(new javafx.scene.layout.VBox(8,
                new javafx.scene.control.Label("Ingrese la contraseña para continuar:"),
                passwordField));

        // Configurar el resultado del diálogo
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == javafx.scene.control.ButtonType.OK) {
                return passwordField.getText();
            }
            return null;
        });

        // Mostrar diálogo y validar
        java.util.Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String passwordIngresada = result.get();
            if (CONTRASEÑA_CORRECTA.equals(passwordIngresada)) {
                return true; // Contraseña correcta
            } else {
                // Contraseña incorrecta
                mostrarAlerta("Error de Autenticación",
                        "Contraseña incorrecta. No se puede eliminar el fiado.",
                        javafx.scene.control.Alert.AlertType.ERROR);
                return false;
            }
        }

        // Usuario canceló el diálogo
        return false;
    }

// Versión alternativa más simple del validador de contraseña
    private boolean validarContraseñaSimple() {
        final String CONTRASEÑA_CORRECTA = "FordFocus2808#";

        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Contraseña Requerida");
        dialog.setHeaderText("Validación de Seguridad");
        dialog.setContentText("Contraseña:");

        java.util.Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && CONTRASEÑA_CORRECTA.equals(result.get())) {
            return true;
        } else if (result.isPresent()) {
            mostrarAlerta("Error", "Contraseña incorrecta", javafx.scene.control.Alert.AlertType.ERROR);
        }

        return false;
    }
// Método adicional para borrar TODOS los fiados del cliente (renombrado para claridad)

    public void borrarTodosFiadosDelCliente() {
        if (clienteSeleccionado == null) {
            mostrarAlerta("Error", "No hay cliente seleccionado", javafx.scene.control.Alert.AlertType.WARNING);
            return;
        }

        // Verificar si el cliente tiene fiados
        List<Fiado> fiadosCliente = fiadoDao.obtenerFiadosPorClienteId(clienteSeleccionado.getId());
        if (fiadosCliente.isEmpty()) {
            mostrarAlerta("Información", "El cliente seleccionado no tiene fiados para eliminar",
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            return;
        }

        // Confirmación antes de borrar
        javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar TODOS los fiados del cliente " + clienteSeleccionado.getNombre()
                + (clienteSeleccionado.getAlias() != null && !clienteSeleccionado.getAlias().isEmpty()
                ? " - " + clienteSeleccionado.getAlias() : "") + "?\n\n"
                + "Total de fiados a eliminar: " + fiadosCliente.size() + "\n\n"
                + "Esta acción no se puede deshacer.",
                javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO
        );

        alerta.setTitle("Confirmar eliminación masiva");
        alerta.setHeaderText("Eliminar todos los fiados");

        alerta.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.YES) {
                try {
                    // Borrar todos los fiados del cliente
                    boolean eliminados = fiadoDao.eliminarTodosFiadosPorCliente(clienteSeleccionado.getId());

                    if (eliminados) {
                        System.out.println("Todos los fiados del cliente eliminados correctamente");

                        // Limpiar tablas y UI
                        tablaFiados.getItems().clear();
                        tablaAnticipos.getItems().clear();
                        tablaProductos.getItems().clear();

                        // Refrescar lista de clientes
                        listaClientes = clienteDao.buscarClientesConFiados();
                        llenarTablaClientes();

                        mostrarAlerta("Éxito", "Todos los fiados han sido eliminados correctamente",
                                javafx.scene.control.Alert.AlertType.INFORMATION);

                    } else {
                        mostrarAlerta("Error", "No se pudieron eliminar los fiados",
                                javafx.scene.control.Alert.AlertType.ERROR);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarAlerta("Error", "Error al eliminar los fiados: " + e.getMessage(),
                            javafx.scene.control.Alert.AlertType.ERROR);
                }
            }
        });
    }
// Método para hacer invisible solo el texto de la columna Celular

    private void configurarCheckBoxTelefono() {
        // Configurar estado inicial - CheckBox activado y texto invisible
        checkTelefono.setSelected(true);
        configurarVisibilidadTextoColumna(false);

        // Listener para el CheckBox
        checkTelefono.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Ocultar solo el texto de la columna
                configurarVisibilidadTextoColumna(false);
            } else {
                // Mostrar el texto de la columna
                configurarVisibilidadTextoColumna(true);
            }
        });
    }

    private void configurarVisibilidadTextoColumna(boolean mostrarTexto) {
        // Configurar la celda de la columna para mostrar/ocultar texto
        colCelular.setCellFactory(column -> new javafx.scene.control.TableCell<Cliente, String>() {
            @Override
            protected void updateItem(String celular, boolean empty) {
                super.updateItem(celular, empty);
                if (empty || celular == null) {
                    setText(null);
                } else {
                    if (mostrarTexto) {
                        setText(celular); // Mostrar el número
                    } else {
                        setText(""); // Texto vacío (invisible)
                    }
                }
            }
        });
    }

// Alternativa usando una variable de instancia para controlar el estado
    private boolean mostrarTelefonos = false; // Variable de control

    private void configurarCheckBoxTelefonoConVariable() {
        // Estado inicial
        checkTelefono.setSelected(true);
        mostrarTelefonos = false;

        // Configurar la cellFactory UNA SOLA VEZ
        colCelular.setCellFactory(column -> new javafx.scene.control.TableCell<Cliente, String>() {
            @Override
            protected void updateItem(String celular, boolean empty) {
                super.updateItem(celular, empty);
                if (empty || celular == null) {
                    setText(null);
                } else {
                    setText(mostrarTelefonos ? celular : ""); // Usar la variable de control
                }
            }
        });

        // Listener del CheckBox
        checkTelefono.selectedProperty().addListener((observable, oldValue, newValue) -> {
            mostrarTelefonos = !newValue; // true cuando checkbox está desmarcado
            tblClientes.refresh(); // Refrescar la tabla para aplicar cambios
        });
    }

// Opción más visual - mostrar asteriscos en lugar de texto vacío
    private void configurarCheckBoxTelefonoConAsteriscos() {
        checkTelefono.setSelected(true);

        colCelular.setCellFactory(column -> new javafx.scene.control.TableCell<Cliente, String>() {
            @Override
            protected void updateItem(String celular, boolean empty) {
                super.updateItem(celular, empty);
                if (empty || celular == null) {
                    setText(null);
                } else {
                    if (checkTelefono.isSelected()) {
                        // Mostrar asteriscos según la longitud del número
                        setText("*".repeat(Math.min(celular.length(), 8)));
                    } else {
                        setText(celular); // Mostrar número real
                    }
                }
            }
        });

        checkTelefono.selectedProperty().addListener((observable, oldValue, newValue) -> {
            tblClientes.refresh();
        });
    }

// Solo agrega esta línea al final de tu método initialize():
//  // Recomendado
}
