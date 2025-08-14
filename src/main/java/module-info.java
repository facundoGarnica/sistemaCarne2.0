module carniceria.sistemacarne {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.naming;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    
    // ========== MÓDULOS PARA MERCADOPAGO ==========
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    
    // ========== OPENS ==========
    opens carniceria.sistemacarne to javafx.fxml;
    opens model to org.hibernate.orm.core, javafx.fxml, javafx.base; // <-- agregado javafx.base
    opens controller to javafx.fxml;
    opens Util to javafx.fxml, com.fasterxml.jackson.databind, javafx.base;
    
    // ========== EXPORTS ==========
    exports carniceria.sistemacarne;
}
