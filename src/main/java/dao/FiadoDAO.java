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
import model.Fiado;
import model.FiadoParcial;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class FiadoDAO {

    public List<Fiado> buscarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Fiado", Fiado.class).list();
        }
    }

    public void eliminar(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Fiado fiado = session.get(Fiado.class, id);
            if (fiado != null) {
                session.delete(fiado);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    public void guardar(Fiado fiado) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(fiado);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    public void actualizar(Fiado fiado) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(fiado);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    public List<Fiado> obtenerFiadosPorClienteId(Long clienteId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Fiado f LEFT JOIN FETCH f.fiadoParciales WHERE f.cliente.id = :id", Fiado.class)
                    .setParameter("id", clienteId)
                    .getResultList();
        }
    }

    // MÉTODO NUEVO: Eliminar UN fiado específico por ID
    // MÉTODO CORREGIDO: Eliminar UN fiado específico por ID SIN borrar la venta
    public boolean eliminarFiadoPorId(Long fiadoId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Obtener el fiado primero para verificar que existe
            Fiado fiado = session.get(Fiado.class, fiadoId);
            if (fiado == null) {
                tx.rollback();
                System.out.println("No se encontró el fiado con ID: " + fiadoId);
                return false;
            }

            // 1. Eliminar los FiadoParcial asociados
            List<FiadoParcial> listaParciales = session.createQuery(
                    "FROM FiadoParcial fp WHERE fp.fiado.id = :fiadoId", FiadoParcial.class)
                    .setParameter("fiadoId", fiadoId)
                    .list();

            for (FiadoParcial fp : listaParciales) {
                session.delete(fp);
            }
            System.out.println("Eliminados " + listaParciales.size() + " pagos parciales");

            // 2. DESCONECTAR las Ventas de este fiado (NO ELIMINAR, solo poner fiado_id = NULL)
            int ventasDesconectadas = session.createQuery(
                    "UPDATE Venta v SET v.fiado = NULL WHERE v.fiado.id = :fiadoId")
                    .setParameter("fiadoId", fiadoId)
                    .executeUpdate();

            System.out.println("Desconectadas " + ventasDesconectadas + " ventas del fiado (ventas conservadas)");

            // 3. Finalmente eliminar el Fiado
            session.delete(fiado);

            tx.commit();
            System.out.println("Fiado eliminado correctamente - ID: " + fiadoId + " (ventas conservadas)");
            return true;

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar el fiado: " + e.getMessage(), e);
        }
    }

    // MÉTODO NUEVO: Eliminar TODOS los fiados de un cliente
    public boolean eliminarTodosFiadosPorCliente(Long clienteId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Obtener todos los fiados del cliente
            List<Fiado> listaFiados = session.createQuery(
                    "FROM Fiado f WHERE f.cliente.id = :clienteId", Fiado.class)
                    .setParameter("clienteId", clienteId)
                    .list();

            int totalEliminados = 0;
            int totalParcialesEliminados = 0;
            int totalVentasDesconectadas = 0;

            // Para cada fiado, eliminar en el orden correcto
            for (Fiado fiado : listaFiados) {

                // 1. Eliminar FiadoParcial de este fiado
                List<FiadoParcial> parciales = session.createQuery(
                        "FROM FiadoParcial fp WHERE fp.fiado.id = :fiadoId", FiadoParcial.class)
                        .setParameter("fiadoId", fiado.getId())
                        .list();

                for (FiadoParcial fp : parciales) {
                    session.delete(fp);
                    totalParcialesEliminados++;
                }

                // 2. Desconectar las Ventas de este fiado (poner fiado_id = NULL)
                int ventasDesconectadas = session.createQuery(
                        "UPDATE Venta v SET v.fiado = NULL WHERE v.fiado.id = :fiadoId")
                        .setParameter("fiadoId", fiado.getId())
                        .executeUpdate();

                totalVentasDesconectadas += ventasDesconectadas;

                // 3. Eliminar el Fiado
                session.delete(fiado);
                totalEliminados++;
            }

            tx.commit();
            System.out.println("Eliminados " + totalEliminados + " fiado(s), "
                    + totalParcialesEliminados + " pagos parciales, y desconectadas "
                    + totalVentasDesconectadas + " ventas del cliente ID: " + clienteId);
            return totalEliminados > 0;

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar los fiados del cliente: " + e.getMessage(), e);
        }
    }

    // MÉTODO CORREGIDO: Tu método original pero arreglado
    public void eliminarFiadosYParcialesPorCliente(Long clienteId) {
        // Usar el nuevo método que retorna boolean para mejor control
        eliminarTodosFiadosPorCliente(clienteId);
    }
}
