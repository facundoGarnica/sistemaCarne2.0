module carniceria.sistemacarne {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires java.base;
    requires jakarta.persistence;    
    requires org.hibernate.orm.core;
    requires java.naming;

    opens carniceria.sistemacarne to javafx.fxml;
    opens model to org.hibernate.orm.core, javafx.base, javafx.fxml;


    opens controller to javafx.fxml;
    exports carniceria.sistemacarne;
}
