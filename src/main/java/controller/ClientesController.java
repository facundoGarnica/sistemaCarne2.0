package controller;

import dao.ClienteDAO;
import dao.FiadoDAO;
import dao.FiadoParcialDAO;
import dao.VentaDAO;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import model.Cliente;
import model.Fiado;
import model.FiadoParcial;
import model.Venta;

public class ClientesController implements Initializable {

    //Objetos
    private Cliente cliente;
    private Fiado fiado;
    private FiadoParcial fiadoParcial;
    private Venta venta;
    
    //Dao
    private ClienteDAO clienteDao;
    private FiadoDAO fiadoDao;
    private FiadoParcialDAO fiadoParcialDao;
    private VentaDAO ventaDao;
    
    
    //Variables
    List<Fiado> listaDeFiadosCliente;
    List<Cliente> listaClientes;
    @FXML
    private AnchorPane difuminar;
    @FXML
    private AnchorPane spaDetalle; //el spa que trae la ventana de pagos de clientes
    @FXML
    private Spa_clientesController spaClienteController;

    
    //tablas
    
    @FXML
    private TableView tablaClientes;
    @FXML 
    private TableColumn colNombre;
    @FXML 
    private TableColumn colFecha;
    @FXML 
    private TableColumn colCelular;
    @FXML 
    private TableColumn colTotal;
    @FXML 
    private TableColumn colAnticipos;
    @FXML 
    private TableColumn colResto;
    @FXML 
    private TableColumn colPago;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clienteDao = new ClienteDAO();
        fiadoDao = new FiadoDAO();
        ventaDao = new VentaDAO();
        fiadoParcialDao = new FiadoParcialDAO();
        listaClientes = clienteDao.buscarTodos();
       
    }
    
    public void llenarTablaClientes(){
        for (Cliente c : listaClientes){
            listaDeFiadosCliente = clienteDao.obtenerFiadosDeCliente(c.getId());
        }
        
    }

    public void difuminarTodo() {
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
        });
    }

    public void CerrarDifuminarYSpa() {
        System.out.println("Cerrando overlay...");
        difuminar.setDisable(true);
        difuminar.setVisible(false);

        spaDetalle.setDisable(true);
        spaDetalle.setVisible(false);
    }

    @FXML
    public void invocarSpaDetalle() {  //llama a detalle de fiados
        try {
            spaDetalle.getChildren().clear(); // Limpiar el AnchorPane destino

            // Cargar el FXML spa_clientes.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/spa_clientes.fxml"));
            Parent root = loader.load();
            spaClienteController = loader.getController();
            spaClienteController.setClientesController(this);
            // controller.setClientesController(this); // Por ejemplo
            // Insertar el contenido cargado en el AnchorPane
            spaDetalle.getChildren().add(root);

            // Hacer que el contenido cargado se ajuste al tamaño del AnchorPane
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
