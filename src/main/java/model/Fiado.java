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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author facun
 */
@Entity
@Table(name = "fiado")
public class Fiado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    @OneToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;
    private LocalDateTime fecha;
    @OneToMany(mappedBy = "fiado", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<FiadoParcial> fiadoParciales;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public List<FiadoParcial> getFiadoParciales() {
        return fiadoParciales;
    }

    public void setFiadoParciales(List<FiadoParcial> fiadoParciales) {
        this.fiadoParciales = fiadoParciales;
    }

    // Retorna la suma de todos los parciales pagos
    public double getTotalParciales() {
        if (fiadoParciales == null || fiadoParciales.isEmpty()) {
            return 0.0;
        }
        return fiadoParciales.stream()
                .mapToDouble(fp -> fp.getAnticipo() != null ? fp.getAnticipo() : 0.0)
                .sum();
    }

    // Retorna el resto por pagar
    public double getResto() {
        if (venta == null) {
            return 0.0;
        }
        return venta.getTotal() - getTotalParciales();
    }

    public Boolean getEstado() {
        double totalPagado = fiadoParciales.stream()
                .mapToDouble(FiadoParcial::getAnticipo)
                .sum();
        return (venta.getTotal() - totalPagado) <= 0;
    }

}
