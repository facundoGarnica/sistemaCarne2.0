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

    // ===== OPENS =====
    opens carniceria.sistemacarne to javafx.fxml;
    opens model to org.hibernate.orm.core, javafx.fxml, javafx.base;
    opens controller to javafx.fxml;
    opens Util to javafx.fxml, com.fasterxml.jackson.databind, javafx.base;

    // ===== EXPORTS =====
    exports carniceria.sistemacarne;
}
