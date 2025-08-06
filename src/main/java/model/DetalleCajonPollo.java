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
@Table(name = "detalleCajonPollo")
public class DetalleCajonPollo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn (name = "cajonPollo_id")
    private CajonPollo cajonPollo;
    @ManyToOne
    @JoinColumn (name = "producto_id")
    private Producto producto;
    private Double porcentajeCorte;  /*Este atributo va a calcular cuando % va a tener el corte en la media,
                                        usando como base el peso del producto*/

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CajonPollo getCajonPollo() {
        return cajonPollo;
    }

    public void setCajonPollo(CajonPollo cajonPollo) {
        this.cajonPollo = cajonPollo;
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
