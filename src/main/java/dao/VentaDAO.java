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
import javafx.collections.FXCollections;
import model.DetalleVenta;
import model.Fiado;
import model.Producto;
import model.Stock;
import model.Venta;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class VentaDAO {

    public List<Venta> buscarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Venta", Venta.class).list();
        }
    }

    public void eliminar(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Venta venta = session.get(Venta.class, id);
            if (venta != null) {
                session.delete(venta);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    public void guardar(Venta venta) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(venta);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    public void actualizar(Venta venta) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(venta);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        }
    }

    // NUEVO MÉTODO: Eliminar venta con restauración de stock
    public void eliminarVenta(Long ventaId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // 1. Obtener la venta
            Venta venta = session.get(Venta.class, ventaId);
            if (venta == null) {
                System.out.println("No se encontró la venta con ID: " + ventaId);
                tx.rollback();
                return;
            }

            // 2. Obtener todos los detalles de la venta para restaurar el stock
            List<DetalleVenta> detalles = session.createQuery(
                    "FROM DetalleVenta dv WHERE dv.venta.id = :ventaId", DetalleVenta.class)
                    .setParameter("ventaId", ventaId)
                    .list();

            System.out.println("=== ELIMINANDO VENTA ===");
            System.out.println("Venta ID: " + ventaId + " con " + detalles.size() + " detalles");
            System.out.println("Fecha: " + venta.getFecha() + " - Total: $" + venta.getTotal());

            // 3. Restaurar el stock para cada producto vendido
            for (DetalleVenta detalle : detalles) {
                restaurarStockPorDetalle(session, detalle);
            }

            // 4. Eliminar todos los detalles de venta primero (por las foreign keys)
            for (DetalleVenta detalle : detalles) {
                session.delete(detalle);
            }

            // 5. Eliminar la venta
            session.delete(venta);

            tx.commit();
            System.out.println("=== VENTA ELIMINADA EXITOSAMENTE ===");

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            System.err.println("=== ERROR AL ELIMINAR VENTA ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar venta con ID: " + ventaId, e);
        }
    }

    private void restaurarStockPorDetalle(Session session, DetalleVenta detalle) {
        try {
            Producto producto = detalle.getProducto();
            double pesoVendido = detalle.getPeso(); // Peso que se vendió

            System.out.println(String.format("RESTAURANDO STOCK - Producto: %s, Peso a restaurar: %.2f kg",
                    producto.getNombre(), pesoVendido));

            // Buscar stock existente para este producto
            Stock stock = session.createQuery(
                    "FROM Stock s WHERE s.producto.id = :prodId", Stock.class)
                    .setParameter("prodId", producto.getId())
                    .uniqueResult();

            if (stock != null) {
                // Si existe stock, sumar el peso vendido
                double cantidadAnterior = stock.getCantidad();
                double nuevaCantidad = cantidadAnterior + pesoVendido;
                nuevaCantidad = Math.round(nuevaCantidad * 100.0) / 100.0;

                stock.setCantidad(nuevaCantidad);
                stock.setFecha(LocalDateTime.now());
                session.update(stock);

                System.out.println(String.format("✓ Stock restaurado para %s: %.2f kg -> %.2f kg (+%.2f kg)",
                        producto.getNombre(), cantidadAnterior, nuevaCantidad, pesoVendido));
            } else {
                // No existe stock, crear uno nuevo con el peso vendido
                Stock nuevoStock = new Stock();
                nuevoStock.setProducto(producto);
                nuevoStock.setCantidad(Math.round(pesoVendido * 100.0) / 100.0);
                nuevoStock.setFecha(LocalDateTime.now());
                nuevoStock.setCantidadMinima(0); // Valor por defecto

                // Establecer relación bidireccional
                producto.setStock(nuevoStock);

                session.save(nuevoStock);
                session.merge(producto); // Usar merge en lugar de update

                System.out.println(String.format("✓ Stock creado para %s: %.2f kg (producto sin stock previo)",
                        producto.getNombre(), pesoVendido));
            }

        } catch (Exception e) {
            System.err.println("✗ Error al restaurar stock para producto "
                    + detalle.getProducto().getNombre() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al restaurar stock", e);
        }
    }

    // AGREGAR ESTOS MÉTODOS A TU VentaDAO
    /**
     * Busca todas las ventas del día actual
     *
     * @return Lista de ventas de hoy
     */
    public List<Venta> buscarVentasDeHoy() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime inicioDelDia = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime finDelDia = inicioDelDia.plusDays(1).minusNanos(1);

            System.out.println("Buscando ventas desde: " + inicioDelDia + " hasta: " + finDelDia);

            List<Venta> ventas = session.createQuery(
                    "FROM Venta v WHERE v.fecha >= :inicio AND v.fecha <= :fin ORDER BY v.fecha DESC",
                    Venta.class)
                    .setParameter("inicio", inicioDelDia)
                    .setParameter("fin", finDelDia)
                    .list();

            System.out.println("Ventas encontradas hoy: " + ventas.size());
            return ventas;

        } catch (Exception e) {
            System.err.println("Error al buscar ventas de hoy: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Busca ventas en un rango de fechas específico
     *
     * @param fechaDesde Fecha inicial (incluida)
     * @param fechaHasta Fecha final (incluida)
     * @return Lista de ventas en el rango especificado
     */
    public List<Venta> buscarVentasPorFecha(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            // Asegurar que fechaDesde sea el inicio del día y fechaHasta sea el final del día
            LocalDateTime inicioFecha = fechaDesde.toLocalDate().atStartOfDay();
            LocalDateTime finFecha = fechaHasta.toLocalDate().atTime(23, 59, 59, 999999999);

            System.out.println("Buscando ventas desde: " + inicioFecha + " hasta: " + finFecha);

            List<Venta> ventas = session.createQuery(
                    "FROM Venta v WHERE v.fecha >= :desde AND v.fecha <= :hasta ORDER BY v.fecha DESC",
                    Venta.class)
                    .setParameter("desde", inicioFecha)
                    .setParameter("hasta", finFecha)
                    .list();

            System.out.println("Ventas encontradas en el rango: " + ventas.size());
            return ventas;

        } catch (Exception e) {
            System.err.println("Error al buscar ventas por fecha: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Busca ventas por fecha usando LocalDate (más simple)
     *
     * @param fechaDesde Fecha inicial
     * @param fechaHasta Fecha final
     * @return Lista de ventas en el rango
     */
    public List<Venta> buscarVentasPorFecha(java.time.LocalDate fechaDesde, java.time.LocalDate fechaHasta) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            LocalDateTime inicioFecha = fechaDesde.atStartOfDay();
            LocalDateTime finFecha = fechaHasta.atTime(23, 59, 59, 999999999);

            System.out.println("Buscando ventas desde: " + fechaDesde + " hasta: " + fechaHasta);

            List<Venta> ventas = session.createQuery(
                    "FROM Venta v WHERE v.fecha >= :desde AND v.fecha <= :hasta ORDER BY v.fecha DESC",
                    Venta.class)
                    .setParameter("desde", inicioFecha)
                    .setParameter("hasta", finFecha)
                    .list();

            System.out.println("Ventas encontradas: " + ventas.size());
            return ventas;

        } catch (Exception e) {
            System.err.println("Error al buscar ventas por fecha: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    public Venta buscarUltimaVenta() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Ordena por fecha descendente y toma solo el primer resultado
            return session.createQuery(
                    "FROM Venta v ORDER BY v.fecha DESC", Venta.class)
                    .setMaxResults(1)
                    .uniqueResult();
        } catch (Exception e) {
            System.err.println("Error al obtener la última venta: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void asignarFiadoALaUltimaVenta(Fiado fiado) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Obtener la última venta
            Venta ultimaVenta = session.createQuery("FROM Venta v ORDER BY v.fecha DESC", Venta.class)
                    .setMaxResults(1)
                    .uniqueResult();

            if (ultimaVenta == null) {
                System.out.println("No hay ventas registradas.");
                tx.rollback();
                return;
            }

            // Asignar el fiado
            ultimaVenta.setFiado(fiado);
            session.update(ultimaVenta);

            tx.commit();
            System.out.println("Fiado asignado correctamente a la última venta (ID: " + ultimaVenta.getId() + ").");

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            System.err.println("Error al asignar fiado a la última venta: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al asignar fiado", e);
        }
    }
}
