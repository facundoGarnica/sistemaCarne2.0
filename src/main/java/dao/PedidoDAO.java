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
import model.Pedido;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class PedidoDAO {

    public List<Pedido> buscarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Pedido", Pedido.class).list();
        }
    }

    public boolean eliminar(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Pedido pedido = session.get(Pedido.class, id);
            if (pedido != null) {
                session.delete(pedido);
                tx.commit();
                System.out.println("Pedido eliminado correctamente: ID " + id);
                return true;
            } else {
                System.out.println("Pedido no encontrado: ID " + id);
                tx.commit(); // Commit aunque no se eliminó nada
                return false;
            }

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            System.err.println("Error al eliminar Pedido ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void guardar(Pedido pedido) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(pedido);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    public void actualizar(Pedido pedido) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(pedido);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    /**
     * Busca pedidos por cliente cargando los detalles y señas necesarios para
     * los cálculos
     *
     * @param clienteId ID del cliente
     * @return Lista de pedidos con detalles y señas cargados
     */
    public List<Pedido> buscarPedidosPorCliente(Long clienteId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // ESTRATEGIA 1: Cargar primero los pedidos con detalles
            String hqlDetalles = "SELECT DISTINCT p FROM Pedido p "
                    + "LEFT JOIN FETCH p.detallePedidos dp "
                    + "LEFT JOIN FETCH dp.producto "
                    + "WHERE p.cliente.id = :clienteId "
                    + "ORDER BY p.fecha DESC";

            List<Pedido> pedidos = session.createQuery(hqlDetalles, Pedido.class)
                    .setParameter("clienteId", clienteId)
                    .list();

            // ESTRATEGIA 2: Si hay pedidos, cargar las señas en una segunda consulta
            if (!pedidos.isEmpty()) {
                // Extraer los IDs de los pedidos encontrados
                List<Long> pedidoIds = pedidos.stream()
                        .map(Pedido::getId)
                        .toList();

                // Cargar las señas para esos pedidos
                String hqlSenias = "SELECT DISTINCT p FROM Pedido p "
                        + "LEFT JOIN FETCH p.senias "
                        + "WHERE p.id IN (:pedidoIds)";

                session.createQuery(hqlSenias, Pedido.class)
                        .setParameter("pedidoIds", pedidoIds)
                        .list(); // Esta consulta inicializa las señas
            }

            return pedidos;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // Devuelve lista vacía si hay error
        }
    }

    /**
     * Busca un pedido por ID cargando todos sus detalles
     *
     * @param id ID del pedido
     * @return Pedido con detalles cargados o null si no existe
     */
    public Pedido buscarPorIdConDetalles(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Cargar primero el pedido con detalles
            String hqlDetalles = "SELECT p FROM Pedido p "
                    + "LEFT JOIN FETCH p.detallePedidos dp "
                    + "LEFT JOIN FETCH dp.producto "
                    + "WHERE p.id = :id";

            Pedido pedido = session.createQuery(hqlDetalles, Pedido.class)
                    .setParameter("id", id)
                    .uniqueResult();

            // Si existe el pedido, cargar las señas
            if (pedido != null) {
                String hqlSenias = "SELECT p FROM Pedido p "
                        + "LEFT JOIN FETCH p.senias "
                        + "WHERE p.id = :id";

                session.createQuery(hqlSenias, Pedido.class)
                        .setParameter("id", id)
                        .uniqueResult(); // Inicializa las señas
            }

            return pedido;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Busca todos los pedidos cargando detalles (útil si necesitas mostrar
     * todos)
     *
     * @return Lista de todos los pedidos con detalles cargados
     */
    public List<Pedido> buscarTodosConDetalles() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Cargar primero todos los pedidos con detalles
            String hqlDetalles = "SELECT DISTINCT p FROM Pedido p "
                    + "LEFT JOIN FETCH p.detallePedidos dp "
                    + "LEFT JOIN FETCH dp.producto "
                    + "LEFT JOIN FETCH p.cliente "
                    + "ORDER BY p.fecha DESC";

            List<Pedido> pedidos = session.createQuery(hqlDetalles, Pedido.class).list();

            // Si hay pedidos, cargar las señas
            if (!pedidos.isEmpty()) {
                List<Long> pedidoIds = pedidos.stream()
                        .map(Pedido::getId)
                        .toList();

                String hqlSenias = "SELECT DISTINCT p FROM Pedido p "
                        + "LEFT JOIN FETCH p.senias "
                        + "WHERE p.id IN (:pedidoIds)";

                session.createQuery(hqlSenias, Pedido.class)
                        .setParameter("pedidoIds", pedidoIds)
                        .list();
            }

            return pedidos;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Método de diagnóstico para verificar el estado de los DetallePedido
     * Agregar este método TEMPORALMENTE al PedidoDAO para debugging
     */
    public void diagnosticarDetallePedido(Long pedidoId) {
        System.out.println("=== DIAGNÓSTICO DETALLE PEDIDO ===");
        System.out.println("Pedido ID: " + pedidoId);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // 1. Verificar que el pedido existe
            Pedido pedido = session.get(Pedido.class, pedidoId);
            System.out.println("¿Pedido existe? " + (pedido != null));

            if (pedido != null) {
                System.out.println("Pedido encontrado: " + pedido.getFecha());
            }

            // 2. Buscar TODOS los DetallePedido de este pedido
            String hql = "SELECT dp FROM DetallePedido dp WHERE dp.pedido.id = :pedidoId";
            List<DetallePedido> detalles = session.createQuery(hql, DetallePedido.class)
                    .setParameter("pedidoId", pedidoId)
                    .list();

            System.out.println("DetallePedidos encontrados: " + detalles.size());

            for (DetallePedido detalle : detalles) {
                System.out.println("  - ID: " + detalle.getId()
                        + " | Producto: " + (detalle.getProducto() != null ? detalle.getProducto().getNombre() : "NULL")
                        + " | Cantidad: " + detalle.getCantidad()
                        + " | Precio: " + detalle.getPrecio());
            }

            // 3. Verificar si hay DetallePedido huérfanos (sin pedido)
            String hqlHuerfanos = "SELECT dp FROM DetallePedido dp WHERE dp.pedido IS NULL";
            List<DetallePedido> huerfanos = session.createQuery(hqlHuerfanos, DetallePedido.class).list();
            System.out.println("DetallePedidos huérfanos: " + huerfanos.size());

            // 4. Buscar el DetallePedido específico que no se pudo eliminar
            DetallePedido detalleBuscado = session.get(DetallePedido.class, 23L);
            System.out.println("DetallePedido ID 23 encontrado: " + (detalleBuscado != null));

            if (detalleBuscado != null) {
                System.out.println("  - Pertenece al pedido: " + (detalleBuscado.getPedido() != null ? detalleBuscado.getPedido().getId() : "NULL"));
            }

        } catch (Exception e) {
            System.err.println("Error en diagnóstico: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== FIN DIAGNÓSTICO ===");
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
}
