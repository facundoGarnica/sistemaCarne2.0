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
import java.util.ArrayList;
import java.util.List;
import model.Cliente;
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

    public boolean eliminarFiadoPorId(Long fiadoId) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Obtener el fiado con sus relaciones
            Fiado fiado = session.createQuery(
                    "FROM Fiado f LEFT JOIN FETCH f.fiadoParciales LEFT JOIN FETCH f.venta WHERE f.id = :fiadoId",
                    Fiado.class)
                    .setParameter("fiadoId", fiadoId)
                    .uniqueResult();

            if (fiado == null) {
                tx.rollback();
                System.out.println("No se encontró el fiado con ID: " + fiadoId);
                return false;
            }

            // 1. PRIMERO: Desconectar la venta del fiado (sin eliminar la venta)
            if (fiado.getVenta() != null) {
                fiado.getVenta().setFiado(null);
                session.update(fiado.getVenta());
                System.out.println("Venta desconectada del fiado (venta conservada)");
            }

            // 2. SEGUNDO: Limpiar la colección de parciales del fiado antes de eliminarlos
            if (fiado.getFiadoParciales() != null && !fiado.getFiadoParciales().isEmpty()) {
                List<FiadoParcial> parciales = new ArrayList<>(fiado.getFiadoParciales());
                fiado.getFiadoParciales().clear(); // Limpiar la colección del padre

                // Ahora eliminar los parciales individualmente
                for (FiadoParcial fp : parciales) {
                    fp.setFiado(null); // Romper la relación bidireccional
                    session.delete(fp);
                }
                System.out.println("Eliminados " + parciales.size() + " pagos parciales");
            }

            // 3. TERCERO: Flush intermedio para aplicar cambios
            session.flush();

            // 4. FINALMENTE: Eliminar el fiado
            session.delete(fiado);

            tx.commit();
            System.out.println("Fiado eliminado correctamente - ID: " + fiadoId);
            return true;

        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar el fiado: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

// MÉTODO ALTERNATIVO - Más simple usando eliminación en cascada
    public boolean eliminarFiadoPorIdSimple(Long fiadoId) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Obtener el fiado
            Fiado fiado = session.get(Fiado.class, fiadoId);
            if (fiado == null) {
                tx.rollback();
                System.out.println("No se encontró el fiado con ID: " + fiadoId);
                return false;
            }

            // Solo desconectar la venta (mantenerla en la BD)
            if (fiado.getVenta() != null) {
                fiado.getVenta().setFiado(null);
                session.merge(fiado.getVenta()); // Usar merge en lugar de update
            }

            // Eliminar el fiado (los FiadoParcial se eliminarán por cascada si está configurado)
            session.delete(fiado);

            tx.commit();
            System.out.println("Fiado eliminado correctamente - ID: " + fiadoId);
            return true;

        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar el fiado: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
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

    // Método mejorado que elimina el cliente si se queda sin fiados
    public boolean eliminarFiadoPorIdYCliente(Long fiadoId) {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Obtener el fiado con sus relaciones
            Fiado fiado = session.createQuery(
                    "FROM Fiado f LEFT JOIN FETCH f.fiadoParciales LEFT JOIN FETCH f.venta LEFT JOIN FETCH f.cliente WHERE f.id = :fiadoId",
                    Fiado.class)
                    .setParameter("fiadoId", fiadoId)
                    .uniqueResult();

            if (fiado == null) {
                tx.rollback();
                System.out.println("No se encontró el fiado con ID: " + fiadoId);
                return false;
            }

            Cliente cliente = fiado.getCliente();
            Long clienteId = cliente.getId();
            String nombreCliente = cliente.getNombre() + (cliente.getAlias() != null ? " - " + cliente.getAlias() : "");

            // 1. PRIMERO: Desconectar la venta del fiado (sin eliminar la venta)
            if (fiado.getVenta() != null) {
                fiado.getVenta().setFiado(null);
                session.update(fiado.getVenta());
                System.out.println("Venta desconectada del fiado (venta conservada)");
            }

            // 2. SEGUNDO: Eliminar los FiadoParcial
            if (fiado.getFiadoParciales() != null && !fiado.getFiadoParciales().isEmpty()) {
                List<FiadoParcial> parciales = new ArrayList<>(fiado.getFiadoParciales());
                fiado.getFiadoParciales().clear();

                for (FiadoParcial fp : parciales) {
                    fp.setFiado(null);
                    session.delete(fp);
                }
                System.out.println("Eliminados " + parciales.size() + " pagos parciales");
            }

            // 3. TERCERO: Flush intermedio
            session.flush();

            // 4. CUARTO: Eliminar el fiado
            session.delete(fiado);
            session.flush();

            // 5. VERIFICAR SI EL CLIENTE TIENE MÁS FIADOS
            Long cantidadFiadosRestantes = session.createQuery(
                    "SELECT COUNT(f) FROM Fiado f WHERE f.cliente.id = :clienteId", Long.class)
                    .setParameter("clienteId", clienteId)
                    .uniqueResult();

            boolean clienteEliminado = false;
            if (cantidadFiadosRestantes == 0) {
                // El cliente no tiene más fiados, eliminarlo
                session.delete(cliente);
                clienteEliminado = true;
                System.out.println("Cliente eliminado automáticamente: " + nombreCliente + " (no tenía más fiados)");
            } else {
                System.out.println("Cliente conservado: " + nombreCliente + " (tiene " + cantidadFiadosRestantes + " fiados restantes)");
            }

            tx.commit();
            System.out.println("Fiado eliminado correctamente - ID: " + fiadoId);

            return true;

        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar el fiado: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

// Método para eliminar TODOS los fiados de un cliente Y el cliente
    public boolean eliminarTodosFiadosYCliente(Long clienteId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Obtener el cliente
            Cliente cliente = session.get(Cliente.class, clienteId);
            if (cliente == null) {
                tx.rollback();
                System.out.println("No se encontró el cliente con ID: " + clienteId);
                return false;
            }

            String nombreCliente = cliente.getNombre() + (cliente.getAlias() != null ? " - " + cliente.getAlias() : "");

            // Obtener todos los fiados del cliente
            List<Fiado> listaFiados = session.createQuery(
                    "FROM Fiado f LEFT JOIN FETCH f.fiadoParciales WHERE f.cliente.id = :clienteId", Fiado.class)
                    .setParameter("clienteId", clienteId)
                    .list();

            int totalEliminados = 0;
            int totalParcialesEliminados = 0;
            int totalVentasDesconectadas = 0;

            // Para cada fiado, eliminar en el orden correcto
            for (Fiado fiado : listaFiados) {

                // 1. Desconectar la venta del fiado
                if (fiado.getVenta() != null) {
                    fiado.getVenta().setFiado(null);
                    session.update(fiado.getVenta());
                    totalVentasDesconectadas++;
                }

                // 2. Eliminar FiadoParcial de este fiado
                if (fiado.getFiadoParciales() != null) {
                    List<FiadoParcial> parciales = new ArrayList<>(fiado.getFiadoParciales());
                    fiado.getFiadoParciales().clear();

                    for (FiadoParcial fp : parciales) {
                        fp.setFiado(null);
                        session.delete(fp);
                        totalParcialesEliminados++;
                    }
                }

                // 3. Eliminar el Fiado
                session.delete(fiado);
                totalEliminados++;
            }

            // 4. Flush intermedio
            session.flush();

            // 5. Eliminar el cliente
            session.delete(cliente);

            tx.commit();
            System.out.println("Eliminados " + totalEliminados + " fiado(s), "
                    + totalParcialesEliminados + " pagos parciales, desconectadas "
                    + totalVentasDesconectadas + " ventas, y eliminado el cliente: " + nombreCliente);
            return true;

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar fiados y cliente: " + e.getMessage(), e);
        }
    }

// Método de utilidad para verificar si un cliente tiene fiados
    public boolean clienteTieneFiados(Long clienteId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(f) FROM Fiado f WHERE f.cliente.id = :clienteId", Long.class)
                    .setParameter("clienteId", clienteId)
                    .uniqueResult();
            return count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
