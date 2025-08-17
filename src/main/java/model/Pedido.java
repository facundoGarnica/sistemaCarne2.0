/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author facun
 */
@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    private LocalDateTime fecha;
    private LocalDate fechaEntrega;
    private String horaEntrega;
    private String comentario;
    private Boolean estado;
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Senia> senias;
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetallePedido> detallePedidos;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public String getHoraEntrega() {
        return horaEntrega;
    }

    public void setHoraEntrega(String horaEntrega) {
        this.horaEntrega = horaEntrega;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public List<Senia> getSenias() {
        return senias;
    }

    public void setSenias(List<Senia> senias) {
        this.senias = senias;
    }

    public List<DetallePedido> getDetallePedidos() {
        return detallePedidos;
    }

    public void setDetallePedidos(List<DetallePedido> detallePedidos) {
        this.detallePedidos = detallePedidos;
    }

    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public Double getTotal() {
        if (detallePedidos == null || detallePedidos.isEmpty()) {
            return 0.0;
        }

        return detallePedidos.stream()
                .mapToDouble(detalle -> {
                    Double precio = detalle.getPrecio() != null ? detalle.getPrecio() : 0.0;
                    Double cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0.0;
                    String unidad = detalle.getUnidadMedida() != null ? detalle.getUnidadMedida() : "";

                    if ("kg".equalsIgnoreCase(unidad)) {
                        // Si la unidad es kg, el total es cantidad * precio
                        return cantidad * precio;
                    } else if ("Unidad".equalsIgnoreCase(unidad)) {
                        // Si es por unidad, multiplicar cantidad * precio * pesoPorUnidad si existe
                        Double pesoPorUnidad = (detalle.getProducto() != null && detalle.getProducto().getPesoPorUnidad() != null)
                                ? detalle.getProducto().getPesoPorUnidad()
                                : 1.0; // fallback si no hay pesoPorUnidad
                        return cantidad * precio * pesoPorUnidad;
                    } else {
                        // fallback genérico para otras unidades
                        return cantidad * precio;
                    }
                })
                .sum();
    }

    public String getTotalFormateado() {
        return String.format("$%.2f", getTotal());
    }

    /**
     * Calcula el total de señas realizadas para este pedido
     *
     * @return Total de señas
     */
    public Double getTotalSenas() {
        if (senias == null || senias.isEmpty()) {
            return 0.0;
        }

        return senias.stream()
                .mapToDouble(senia -> {
                    // IMPORTANTE: Ajusta "getMonto()" según el nombre real del campo en tu clase Senia
                    Double monto = senia.getMonto(); // Cambia esto por el método correcto
                    return monto != null ? monto : 0.0;
                })
                .sum();
    }

    /**
     * Calcula el resto que falta pagar (Total - Señas)
     *
     * @return Resto pendiente
     */
    public Double getResto() {
        return getTotal() - getTotalSenas();
    }

    /**
     * Método auxiliar para mostrar el estado como texto legible
     *
     * @return "Completado" o "Pendiente"
     */
    public String getEstadoTexto() {
        if (estado == null) {
            return "Sin estado";
        }
        return estado ? "Completado" : "Pendiente";
    }
}
