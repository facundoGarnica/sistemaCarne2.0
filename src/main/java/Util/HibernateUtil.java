package Util;

import java.io.File;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Crear la carpeta de la base de datos si no existe
            File dbDir = new File("C:/SistemaCarne/data");
            if (!dbDir.exists()) {
                dbDir.mkdirs(); // crea SistemaCarne y data
                System.out.println("Carpeta de base de datos creada en: " + dbDir.getAbsolutePath());
            }

            // Construir SessionFactory desde hibernate.cfg.xml
            return new Configuration()
                    .configure("hibernate.cfg.xml")
                    .buildSessionFactory();

        } catch (Throwable ex) {
            System.err.println("Error creando SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
