/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author garca
 */
import Util.HibernateUtil;
import java.util.List;
import model.DetalleCajonPollo;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class DetalleCajonPolloDAO {
    
    public List<DetalleCajonPollo> buscarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM DetalleCajonPollo", DetalleCajonPollo.class).list();
        }
    }
    
    public List<DetalleCajonPollo> obtenerPorCajonPollo(Long idCajonPollo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM DetalleCajonPollo d WHERE d.cajonPollo.id = :idCajonPollo";
            return session.createQuery(hql, DetalleCajonPollo.class)
                         .setParameter("idCajonPollo", idCajonPollo)
                         .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public DetalleCajonPollo obtenerPorId(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(DetalleCajonPollo.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void eliminar(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            DetalleCajonPollo detalle = session.get(DetalleCajonPollo.class, id);
            if (detalle != null) {
                session.delete(detalle);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public void guardar(DetalleCajonPollo detalle) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(detalle);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }
    
    public void actualizar(DetalleCajonPollo detalle) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(detalle);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }
}