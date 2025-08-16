/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import Util.HibernateUtil;
import java.util.List;
import model.Cliente;
import model.Fiado;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author garca
 */
public class ClienteDAO {

    public List<Cliente> buscarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Cliente", Cliente.class).list();
        }
    }

    public void eliminar(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Cliente cliente = session.get(Cliente.class, id); // Buscar por ID
            if (cliente != null) {
                session.delete(cliente); // Eliminar si existe
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    public void guardar(Cliente cliente) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(cliente); //aca se guarda
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    public void actualizar(Cliente cliente) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(cliente); // Actualiza el registro
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    public List<Fiado> obtenerFiadosDeCliente(Long clienteId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Cliente cliente = session.get(Cliente.class, clienteId);
            if (cliente != null) {
                // Inicializar la lista si está lazy
                cliente.getFiados().size();
                return cliente.getFiados();
            } else {
                System.out.println("No se encontró el cliente con ID: " + clienteId);
                return List.of();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // MÉTODO CORREGIDO: Solo clientes que SÍ tienen fiados
    public List<Cliente> buscarClientesConFiados() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // INNER JOIN = solo clientes que tienen al menos un fiado
            return session.createQuery(
                    "SELECT DISTINCT c FROM Cliente c INNER JOIN c.fiados f", Cliente.class
            ).list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

// MÉTODO ALTERNATIVO: Clientes con fiados usando EXISTS (más eficiente)
    public List<Cliente> buscarClientesConFiadosExists() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT c FROM Cliente c WHERE EXISTS (SELECT 1 FROM Fiado f WHERE f.cliente.id = c.id)",
                    Cliente.class
            ).list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

// MÉTODO CORREGIDO: Todos los clientes CON sus fiados (incluso si no tienen)
    public List<Cliente> buscarTodosConFiados() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // LEFT JOIN FETCH = trae todos los clientes y sus fiados (si tienen)
            return session.createQuery(
                    "SELECT c FROM Cliente c LEFT JOIN FETCH c.fiados", Cliente.class
            ).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

// MÉTODO NUEVO: Solo clientes con fiados PENDIENTES (estado = false)
    public List<Cliente> buscarClientesConFiadosPendientes() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT c FROM Cliente c INNER JOIN c.fiados f WHERE f.estado = false",
                    Cliente.class
            ).list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

// MÉTODO NUEVO: Clientes con fiados, incluyendo información del fiado
    public List<Cliente> buscarClientesConFiadosCompleto() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT c FROM Cliente c INNER JOIN FETCH c.fiados f", Cliente.class
            ).list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

}
