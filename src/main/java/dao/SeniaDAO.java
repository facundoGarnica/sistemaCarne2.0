package dao;
import Util.HibernateUtil;
import java.util.List;
import model.Senia;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class SeniaDAO {
    
    public List<Senia> buscarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Senia", Senia.class).list();
        }
    }
    
    public Senia buscarPorId(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Senia.class, id);
        }
    }
    
    // MÉTODO CREAR AGREGADO - devuelve boolean para indicar éxito
    public boolean crear(Senia senia) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(senia);
            tx.commit();
            return true; // Éxito
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            System.err.println("Error al crear seña: " + e.getMessage());
            e.printStackTrace();
            return false; // Fallo
        }
    }
    
    public void guardar(Senia senia) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(senia);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public void actualizar(Senia senia) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(senia);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public void eliminar(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Senia senia = session.get(Senia.class, id);
            if (senia != null) {
                session.delete(senia);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public List<Senia> buscarSeniasPorPedido(Long pedidoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Senia s WHERE s.pedido.id = :pedidoId", Senia.class)
                    .setParameter("pedidoId", pedidoId)
                    .list();
        }
    }
}