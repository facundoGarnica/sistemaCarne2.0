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
import model.Pedido;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class PedidoDAO {
    
    public List<Pedido> buscarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Pedido", Pedido.class).list();
        }
    }
    
    public void eliminar(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Pedido pedido = session.get(Pedido.class, id);
            if (pedido != null) {
                session.delete(pedido);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
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
     * Busca pedidos por cliente cargando los detalles y señas necesarios para los cálculos
     * @param clienteId ID del cliente
     * @return Lista de pedidos con detalles y señas cargados
     */
    public List<Pedido> buscarPedidosPorCliente(Long clienteId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // ESTRATEGIA 1: Cargar primero los pedidos con detalles
            String hqlDetalles = "SELECT DISTINCT p FROM Pedido p " +
                                "LEFT JOIN FETCH p.detallePedidos dp " +
                                "LEFT JOIN FETCH dp.producto " +
                                "WHERE p.cliente.id = :clienteId " +
                                "ORDER BY p.fecha DESC";
            
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
                String hqlSenias = "SELECT DISTINCT p FROM Pedido p " +
                                  "LEFT JOIN FETCH p.senias " +
                                  "WHERE p.id IN (:pedidoIds)";
                
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
     * @param id ID del pedido
     * @return Pedido con detalles cargados o null si no existe
     */
    public Pedido buscarPorIdConDetalles(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Cargar primero el pedido con detalles
            String hqlDetalles = "SELECT p FROM Pedido p " +
                                "LEFT JOIN FETCH p.detallePedidos dp " +
                                "LEFT JOIN FETCH dp.producto " +
                                "WHERE p.id = :id";
            
            Pedido pedido = session.createQuery(hqlDetalles, Pedido.class)
                    .setParameter("id", id)
                    .uniqueResult();
            
            // Si existe el pedido, cargar las señas
            if (pedido != null) {
                String hqlSenias = "SELECT p FROM Pedido p " +
                                  "LEFT JOIN FETCH p.senias " +
                                  "WHERE p.id = :id";
                
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
     * Busca todos los pedidos cargando detalles (útil si necesitas mostrar todos)
     * @return Lista de todos los pedidos con detalles cargados
     */
    public List<Pedido> buscarTodosConDetalles() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Cargar primero todos los pedidos con detalles
            String hqlDetalles = "SELECT DISTINCT p FROM Pedido p " +
                                "LEFT JOIN FETCH p.detallePedidos dp " +
                                "LEFT JOIN FETCH dp.producto " +
                                "LEFT JOIN FETCH p.cliente " +
                                "ORDER BY p.fecha DESC";
            
            List<Pedido> pedidos = session.createQuery(hqlDetalles, Pedido.class).list();
            
            // Si hay pedidos, cargar las señas
            if (!pedidos.isEmpty()) {
                List<Long> pedidoIds = pedidos.stream()
                        .map(Pedido::getId)
                        .toList();
                
                String hqlSenias = "SELECT DISTINCT p FROM Pedido p " +
                                  "LEFT JOIN FETCH p.senias " +
                                  "WHERE p.id IN (:pedidoIds)";
                
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
}