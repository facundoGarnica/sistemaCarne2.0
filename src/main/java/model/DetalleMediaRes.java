/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 *
 * @author garca
 */
@Entity
@Table(name = "detalleMediaRes")
public class DetalleMediaRes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn (name = "media_id")
    private MediaRes mediaRes;
    @ManyToOne
    @JoinColumn (name = "producto_id")
    private Producto producto;
    private Double porcentajeCorte; /*Este atributo va a calcular cuando % va a tener el corte en la media,
                                        usando como base el peso del producto*/

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MediaRes getMediaRes() {
        return mediaRes;
    }

    public void setMediaRes(MediaRes mediaRes) {
        this.mediaRes = mediaRes;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Double getPorcentajeCorte() {
        return porcentajeCorte;
    }

    public void setPorcentajeCorte(Double porcentajeCorte) {
        this.porcentajeCorte = porcentajeCorte;
    }
    
    
}
