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
import model.DetallePedido;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class DetallePedidoDAO {

    public List<DetallePedido> buscarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM DetallePedido", DetallePedido.class).list();
        }
    }

    public boolean eliminar(Long id) {
        Transaction tx = null;
        boolean eliminado = false;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            DetallePedido detalle = session.get(DetallePedido.class, id);
            if (detalle != null) {
                session.delete(detalle);
                eliminado = true; // ✅ Se eliminó correctamente
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            eliminado = false;
        }

        return eliminado;
    }

    public boolean eliminarDetallePedido(Long detallePedidoId) {
        System.out.println("=== PedidoDAO.eliminarDetallePedido() ===");
        System.out.println("ID a eliminar: " + detallePedidoId);

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // OPCIÓN 1: Primero verificar si existe el DetallePedido
            System.out.println("Verificando si existe DetallePedido con ID: " + detallePedidoId);
            String hqlVerificar = "SELECT dp FROM DetallePedido dp WHERE dp.id = :id";

            List<?> resultados = session.createQuery(hqlVerificar)
                    .setParameter("id", detallePedidoId)
                    .list();

            System.out.println("Registros encontrados: " + resultados.size());

            if (resultados.isEmpty()) {
                System.out.println("DetallePedido no encontrado con ID: " + detallePedidoId);
                tx.commit();
                return false;
            }

            // OPCIÓN 2: Eliminar usando HQL
            System.out.println("Eliminando usando HQL...");
            String hqlEliminar = "DELETE FROM DetallePedido dp WHERE dp.id = :id";
            int filasEliminadas = session.createQuery(hqlEliminar)
                    .setParameter("id", detallePedidoId)
                    .executeUpdate();

            System.out.println("Filas eliminadas con HQL: " + filasEliminadas);

            if (filasEliminadas > 0) {
                tx.commit();
                System.out.println("✅ DetallePedido eliminado correctamente con HQL");
                return true;
            } else {
                System.out.println("❌ No se eliminó ninguna fila con HQL");

                // OPCIÓN 3: Intentar eliminación tradicional como fallback
                tx.rollback();
                return eliminarDetallePedidoTradicional(detallePedidoId);
            }

        } catch (Exception e) {
            System.err.println("❌ Error en eliminarDetallePedido: " + e.getMessage());
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();

            // OPCIÓN 4: Como último recurso, intentar método tradicional
            return eliminarDetallePedidoTradicional(detallePedidoId);
        }
    }

    /**
     * Método de eliminación tradicional como fallback
     */
    private boolean eliminarDetallePedidoTradicional(Long detallePedidoId) {
        System.out.println("=== Método tradicional de eliminación ===");
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Obtener el objeto y eliminarlo
            Object detallePedido = session.get(DetallePedido.class, detallePedidoId);

            if (detallePedido != null) {
                System.out.println("DetallePedido encontrado, eliminando...");
                session.delete(detallePedido);
                tx.commit();
                System.out.println("✅ DetallePedido eliminado con método tradicional");
                return true;
            } else {
                System.out.println("❌ DetallePedido no encontrado con método tradicional");
                tx.commit();
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error en método tradicional: " + e.getMessage());
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    public void guardar(DetallePedido detalle) {
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

    public void actualizar(DetallePedido detalle) {
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
