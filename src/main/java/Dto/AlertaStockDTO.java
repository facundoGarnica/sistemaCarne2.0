/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dto;

/**
 *
 * @author facun
 */
public class AlertaStockDTO {
    private String nombreProducto;
    private String estado;
    private double cantidad;

    public AlertaStockDTO(String nombreProducto, String estado, double cantidad) {
        this.nombreProducto = nombreProducto;
        this.estado = estado;
        this.cantidad = cantidad;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public String getEstado() {
        return estado;
    }

    public double getCantidad() {
        return cantidad;
    }

    @Override
    public String toString() {
        return nombreProducto + " | Estado: " + estado + " | Cantidad: " + cantidad;
    }
}
