/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Dto;

import java.text.DecimalFormat;

/**
 *
 * @author facun
 */
public class AlertaStockDTO {
    private String nombreProducto;
    private String estado;
    private double cantidad;
    private static final DecimalFormat df = new DecimalFormat("#0.00");

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

    // 👉 getter oficial para cálculos y para la tabla (Double)
    public double getCantidad() {
        return cantidad;
    }

    // 👉 getter adicional solo para mostrar formateado
    public String getCantidadFormateada() {
        return df.format(cantidad);
    }

    @Override
    public String toString() {
        return nombreProducto + " | Estado: " + estado + " | Cantidad: " + getCantidadFormateada();
    }
}
