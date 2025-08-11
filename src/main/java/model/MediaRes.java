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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author garca
 */
@Entity
@Table(name = "mediaRes")
public class MediaRes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double pesoPilon;
    private Double pesoBoleta;
    private Double pesoFinal;
    private String proveedor;
    private LocalDateTime fecha;
    private Double precio;
    @OneToMany(mappedBy = "mediaRes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleMediaRes> detalleMediaRes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    
    public Double getPesoPilon() {
        return pesoPilon;
    }

    public void setPesoPilon(Double pesoPilon) {
        this.pesoPilon = pesoPilon;
    }

    public Double getPesoBoleta() {
        return pesoBoleta;
    }

    public void setPesoBoleta(Double pesoBoleta) {
        this.pesoBoleta = pesoBoleta;
    }

    public Double getPesoFinal() {
        return pesoFinal;
    }

    public void setPesoFinal(Double pesoFinal) {
        this.pesoFinal = pesoFinal;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public List<DetalleMediaRes> getDetalleMediaRes() {
        return detalleMediaRes;
    }

    public void setDetalleMediaRes(List<DetalleMediaRes> detalleMediaRes) {
        this.detalleMediaRes = detalleMediaRes;
    }
    
    
    
}
