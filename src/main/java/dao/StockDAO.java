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
import java.time.LocalDateTime;
import java.util.List;
import model.DetalleMediaRes;
import model.MediaRes;
import model.Producto;
import model.Stock;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class StockDAO {

    public List<Stock> buscarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Stock", Stock.class).list();
        }
    }

    public void eliminar(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Stock stock = session.get(Stock.class, id);
            if (stock != null) {
                session.delete(stock);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    public void guardar(Stock stock) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(stock);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    public void actualizar(Stock stock) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(stock);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    public Stock obtenerPorProducto(Long productoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Stock s WHERE s.producto.id = :prodId", Stock.class)
                    .setParameter("prodId", productoId)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean existeStockPorNombreProducto(String nombreProducto) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(s) FROM Stock s WHERE s.producto.nombre = :nombre", Long.class)
                    .setParameter("nombre", nombreProducto)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sumarOCrearStockPorNombreProducto(String nombreProducto, double cantidadASumar) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Buscar el stock por nombre de producto
            Stock stock = session.createQuery(
                    "FROM Stock s WHERE s.producto.nombre = :nombre", Stock.class)
                    .setParameter("nombre", nombreProducto)
                    .uniqueResult();

            if (stock != null) {
                // Sumar cantidad
                stock.setCantidad(stock.getCantidad() + cantidadASumar);
                session.update(stock);
            } else {
                // No existe stock, crear uno nuevo

                // Primero buscar el producto por nombre
                Producto producto = session.createQuery(
                        "FROM Producto p WHERE p.nombre = :nombre", Producto.class)
                        .setParameter("nombre", nombreProducto)
                        .uniqueResult();

                if (producto != null) {
                    Stock nuevoStock = new Stock();
                    nuevoStock.setProducto(producto);
                    nuevoStock.setCantidad(cantidadASumar);
                    nuevoStock.setFecha(LocalDateTime.now());
                    // Podés setear cantidadMinima si querés, o dejar 0
                    nuevoStock.setCantidadMinima(0);

                    session.save(nuevoStock);
                } else {
                    System.out.println("No existe producto con nombre: " + nombreProducto);
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    public void restarStockPorMediaRes(MediaRes mediaRes) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            List<DetalleMediaRes> detalles = session.createQuery(
                    "FROM DetalleMediaRes d WHERE d.mediaRes.id = :mediaId", DetalleMediaRes.class)
                    .setParameter("mediaId", mediaRes.getId())
                    .list();

            for (DetalleMediaRes detalle : detalles) {
                Producto producto = detalle.getProducto();

                Stock stock = session.createQuery(
                        "FROM Stock s WHERE s.producto.id = :prodId", Stock.class)
                        .setParameter("prodId", producto.getId())
                        .uniqueResult();

                if (stock != null) {
                    double pesoAjustado = producto.getPesoPorUnidad() * (mediaRes.getPesoFinal() / 90.0);
                    double pesoAjustadoRedondeado = Math.round(pesoAjustado * 100.0) / 100.0;

                    double nuevaCantidad = stock.getCantidad() - pesoAjustadoRedondeado;
                    nuevaCantidad = Math.round(nuevaCantidad * 100.0) / 100.0;

                    if (nuevaCantidad <= 0) {
                        session.delete(stock);
                    } else {
                        stock.setCantidad(nuevaCantidad);
                        session.update(stock);
                    }
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    public Stock buscarPorProducto(Producto producto) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Stock s WHERE s.producto.id = :productoId", Stock.class)
                    .setParameter("productoId", producto.getId())
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
