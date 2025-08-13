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
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author garca
 */
@Entity
@Table(name = "cajonPollo")
public class CajonPollo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double pesoCajon;
    private LocalDate fecha;
    private Double precio;
    private String proveedor;
    @OneToMany(mappedBy = "cajonPollo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleCajonPollo> detalleCajonPollos;

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

    public Double getPesoCajon() {
        return pesoCajon;
    }

    public void setPesoCajon(Double pesoCajon) {
        this.pesoCajon = pesoCajon;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public List<DetalleCajonPollo> getDetalleCajonPollos() {
        return detalleCajonPollos;
    }

    public void setDetalleCajonPollos(List<DetalleCajonPollo> detalleCajonPollos) {
        this.detalleCajonPollos = detalleCajonPollos;
    }
            
    
}
