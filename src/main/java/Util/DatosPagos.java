/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Util;

/**
 *
 * @author garca
 */
public class DatosPagos {
    private String Cliente;
    private String Fecha;
    private Double Monto;
    private String Estado;

    public DatosPagos(String Cliente, String Fecha, Double Monto, String Estado) {
        this.Cliente = Cliente;
        this.Fecha = Fecha;
        this.Monto = Monto;
        this.Estado = Estado;
    }

    
    public String getCliente() {
        return Cliente;
    }

    public void setCliente(String Cliente) {
        this.Cliente = Cliente;
    }

    public String getFecha() {
        return Fecha;
    }

    public void setFecha(String Fecha) {
        this.Fecha = Fecha;
    }

    public Double getMonto() {
        return Monto;
    }

    public void setMonto(Double Monto) {
        this.Monto = Monto;
    }

    public String getEstado() {
        return Estado;
    }

    public void setEstado(String Estado) {
        this.Estado = Estado;
    }
    
    
}

