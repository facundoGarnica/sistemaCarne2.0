module carniceria.sistemacarne {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.naming;
    requires java.sql;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    // ===== MERCADOPAGO & JSON =====
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires java.base;
    // ===== OPENS =====
    opens carniceria.sistemacarne to javafx.fxml;
    opens model to org.hibernate.orm.core, javafx.fxml, javafx.base;
    opens controller to javafx.fxml, javafx.base;  // <--- se agregó javafx.base
    opens Util to javafx.fxml, com.fasterxml.jackson.databind, javafx.base;
    opens Dto to javafx.base, javafx.fxml;  // <--- AGREGADO: permite acceso a AlertaStockDTO
    // ===== EXPORTS =====
    exports carniceria.sistemacarne;
}