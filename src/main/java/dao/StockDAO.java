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
import model.CajonPollo;
import model.DetalleCajonPollo;
import model.DetalleMediaRes;
import model.DetalleVenta;
import model.MediaRes;
import model.Producto;
import model.Stock;
import model.Venta;
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
                // Si existe stock, solo sumar la cantidad
                stock.setCantidad(stock.getCantidad() + cantidadASumar);
                stock.setFecha(LocalDateTime.now()); // Actualizar fecha
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
                    nuevoStock.setCantidadMinima(0); // Valor por defecto

                    // Establecer la relación bidireccional
                    producto.setStock(nuevoStock);

                    // Guardar ambas entidades
                    session.save(nuevoStock);
                    session.update(producto);

                    System.out.println("Stock creado para producto: " + nombreProducto + " con cantidad: " + cantidadASumar);
                } else {
                    System.out.println("No existe producto con nombre: " + nombreProducto);
                    throw new RuntimeException("Producto no encontrado: " + nombreProducto);
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al crear/actualizar stock", e);
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
                        // PRIMERO: Actualizar el producto para que no referencie al stock
                        producto.setStock(null);
                        session.update(producto);

                        // DESPUÉS: Eliminar el stock
                        session.delete(stock);

                        System.out.println("Stock eliminado para producto: " + producto.getNombre() + " (cantidad <= 0)");
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
            System.out.println("Error al restar stock por media res: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al restar stock por media res", e);
        }
    }

    public void restarStockPorCajonPollo(CajonPollo cajonPollo) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Obtengo la lista de detalles asociados al cajonPollo
            List<DetalleCajonPollo> detalles = session.createQuery(
                    "FROM DetalleCajonPollo d WHERE d.cajonPollo.id = :cajonId", DetalleCajonPollo.class)
                    .setParameter("cajonId", cajonPollo.getId())
                    .list();

            // USAR LA MISMA LÓGICA QUE EN EL CONTROLLER
            // Porcentaje de aprovechamiento: (18.033 / 19) = 94.91%
            double porcentajeAprovechamiento = 0.9491;

            // Peso aprovechable del cajón (descontando desperdicio)
            double pesoAprovechable = cajonPollo.getPesoCajon() * porcentajeAprovechamiento;

            // Peso de referencia de productos vendibles
            double pesoReferenciaAprovechable = 18.033;

            System.out.println(String.format("RESTANDO STOCK - Cajón: %.2f kg - Peso aprovechable: %.2f kg",
                    cajonPollo.getPesoCajon(), pesoAprovechable));

            for (DetalleCajonPollo detalle : detalles) {
                Producto producto = detalle.getProducto();

                Stock stock = session.createQuery(
                        "FROM Stock s WHERE s.producto.id = :prodId", Stock.class)
                        .setParameter("prodId", producto.getId())
                        .uniqueResult();

                if (stock != null) {
                    // CALCULAR EL PESO EXACTO QUE SE AGREGÓ AL STOCK (misma fórmula del controller)
                    double pesoAjustadoProducto = producto.getPesoPorUnidad() * (pesoAprovechable / pesoReferenciaAprovechable);
                    double pesoAjustadoRedondeado = Math.round(pesoAjustadoProducto * 100.0) / 100.0;

                    double nuevaCantidad = stock.getCantidad() - pesoAjustadoRedondeado;
                    nuevaCantidad = Math.round(nuevaCantidad * 100.0) / 100.0;

                    System.out.println(String.format("Producto: %s - Stock actual: %.3f kg - A restar: %.3f kg - Nuevo stock: %.3f kg",
                            producto.getNombre(), stock.getCantidad(), pesoAjustadoRedondeado, nuevaCantidad));

                    if (nuevaCantidad <= 0) {
                        // PRIMERO: Actualizar el producto para que no referencie al stock
                        producto.setStock(null);
                        session.update(producto);

                        // DESPUÉS: Eliminar el stock
                        session.delete(stock);

                        System.out.println("Stock eliminado para producto: " + producto.getNombre() + " (cantidad <= 0)");
                    } else {
                        stock.setCantidad(nuevaCantidad);
                        session.update(stock);
                    }
                } else {
                    System.out.println("No se encontró stock para el producto: " + producto.getNombre());
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            System.out.println("Error al restar stock por cajón de pollo: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al restar stock por cajón de pollo", e);
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

    public void descontarStockPorProducto(Producto producto) {
        if (producto.getStock() == null) {
            System.out.println("El producto no tiene stock asignado: " + producto.getNombre());
            return;
        }

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Stock stock = session.get(Stock.class, producto.getStock().getId());

            if (stock != null) {
                double cantidadADescontar = producto.getPesoParaVender() != null ? producto.getPesoParaVender() : 0;

                double nuevaCantidad = stock.getCantidad() - cantidadADescontar;
                if (nuevaCantidad < 0) {
                    nuevaCantidad = 0;
                }

                stock.setCantidad(nuevaCantidad);
                stock.setFecha(LocalDateTime.now());

                session.update(stock);

                System.out.println(String.format("Stock actualizado para %s: %.2f", producto.getNombre(), nuevaCantidad));
            } else {
                System.out.println("No se encontró stock en la base de datos para el producto: " + producto.getNombre());
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al descontar stock por producto", e);
        }
    }
public void eliminarVenta(Long ventaId) {
    Transaction tx = null;
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        tx = session.beginTransaction();

        // 1. Obtener la venta
        Venta venta = session.get(Venta.class, ventaId);
        if (venta == null) {
            System.out.println("No se encontró la venta con ID: " + ventaId);
            return;
        }

        // 2. Obtener todos los detalles de la venta para restaurar el stock
        List<DetalleVenta> detalles = session.createQuery(
                "FROM DetalleVenta dv WHERE dv.venta.id = :ventaId", DetalleVenta.class)
                .setParameter("ventaId", ventaId)
                .list();

        System.out.println("Eliminando venta ID: " + ventaId + " con " + detalles.size() + " detalles");

        // 3. Restaurar el stock para cada producto vendido
        for (DetalleVenta detalle : detalles) {
            restaurarStockPorDetalle(session, detalle);
        }

        // 4. Eliminar todos los detalles de venta
        session.createQuery("DELETE FROM DetalleVenta dv WHERE dv.venta.id = :ventaId")
                .setParameter("ventaId", ventaId)
                .executeUpdate();

        // 5. Eliminar la venta
        session.delete(venta);

        tx.commit();
        System.out.println("Venta eliminada exitosamente con ID: " + ventaId);

    } catch (Exception e) {
        if (tx != null) {
            tx.rollback();
        }
        System.err.println("Error al eliminar venta: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Error al eliminar venta con ID: " + ventaId, e);
    }
}

private void restaurarStockPorDetalle(Session session, DetalleVenta detalle) {
    try {
        Producto producto = detalle.getProducto();
        double pesoVendido = detalle.getPeso(); // Peso que se vendió

        // Buscar o crear stock para este producto
        Stock stock = session.createQuery(
                "FROM Stock s WHERE s.producto.id = :prodId", Stock.class)
                .setParameter("prodId", producto.getId())
                .uniqueResult();

        if (stock != null) {
            // Si existe stock, sumar el peso vendido
            double nuevaCantidad = stock.getCantidad() + pesoVendido;
            nuevaCantidad = Math.round(nuevaCantidad * 100.0) / 100.0;
            
            stock.setCantidad(nuevaCantidad);
            stock.setFecha(LocalDateTime.now());
            session.update(stock);
            
            System.out.println(String.format("Stock restaurado para %s: +%.2f kg (Total: %.2f kg)", 
                    producto.getNombre(), pesoVendido, nuevaCantidad));
        } else {
            // No existe stock, crear uno nuevo con el peso vendido
            Stock nuevoStock = new Stock();
            nuevoStock.setProducto(producto);
            nuevoStock.setCantidad(pesoVendido);
            nuevoStock.setFecha(LocalDateTime.now());
            nuevoStock.setCantidadMinima(0); // Valor por defecto

            // Establecer relación bidireccional
            producto.setStock(nuevoStock);

            session.save(nuevoStock);
            session.update(producto);

            System.out.println(String.format("Stock creado para %s: %.2f kg (producto sin stock previo)", 
                    producto.getNombre(), pesoVendido));
        }

    } catch (Exception e) {
        System.err.println("Error al restaurar stock para producto ID " + 
                detalle.getProducto().getId() + ": " + e.getMessage());
        throw new RuntimeException("Error al restaurar stock", e);
    }
}
}
