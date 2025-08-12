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
import model.DetalleMediaRes;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class DetalleMediaResDAO {

    public List<DetalleMediaRes> buscarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM DetalleMediaRes", DetalleMediaRes.class).list();
        }
    }

    public void eliminar(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            DetalleMediaRes detalle = session.get(DetalleMediaRes.class, id);
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

    public void guardar(DetalleMediaRes detalle) {
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

    public void actualizar(DetalleMediaRes detalle) {
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

    public List<DetalleMediaRes> obtenerPorMediaRes(Long mediaId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM DetalleMediaRes d WHERE d.mediaRes.id = :mediaId";
            return session.createQuery(hql, DetalleMediaRes.class)
                    .setParameter("mediaId", mediaId)
                    .list();
        }
    }

}
