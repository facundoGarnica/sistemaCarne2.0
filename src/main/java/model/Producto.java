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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;

/**
 *
 * @author facun
 */
@Entity
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private int codigo;
    private Double precio;
    private Double pesoParaVender;
    private String tipo;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stock_id", referencedColumnName = "id")
    private Stock stock;
    private Double pesoPorUnidad; //Este atributo es un promedio que pesa un producto, ejemplo un vacio tiene un promedio de 4 kg
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<DetallePedido> detallePedidos;
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<DetalleCajonPollo> detalleCajonPollos;
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<DetalleMediaRes> detalleMediaRes;
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<DetalleVenta> detalleVentas;
    public Long getId() {
        return id;
    }

    public int getCodigo() {
        return codigo;
    }

    public Double getPesoParaVender() {
        return pesoParaVender;
    }

    public void setPesoParaVender(Double pesoParaVender) {
        this.pesoParaVender = pesoParaVender;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public Double getPesoPorUnidad() {
        return pesoPorUnidad;
    }

    public void setPesoPorUnidad(Double pesoPorUnidad) {
        this.pesoPorUnidad = pesoPorUnidad;
    }

    public List<DetallePedido> getDetallePedidos() {
        return detallePedidos;
    }

    public void setDetallePedidos(List<DetallePedido> detallePedidos) {
        this.detallePedidos = detallePedidos;
    }

    public List<DetalleCajonPollo> getDetalleCajonPollos() {
        return detalleCajonPollos;
    }

    public void setDetalleCajonPollos(List<DetalleCajonPollo> detalleCajonPollos) {
        this.detalleCajonPollos = detalleCajonPollos;
    }

    public List<DetalleMediaRes> getDetalleMediaRes() {
        return detalleMediaRes;
    }

    public void setDetalleMediaRes(List<DetalleMediaRes> detalleMediaRes) {
        this.detalleMediaRes = detalleMediaRes;
    }

    public List<DetalleVenta> getDetalleVentas() {
        return detalleVentas;
    }

    public void setDetalleVentas(List<DetalleVenta> detalleVentas) {
        this.detalleVentas = detalleVentas;
    }
    
    
}
