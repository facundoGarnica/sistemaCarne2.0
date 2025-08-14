/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Util;

/**
 *
 * @author garca
 */
public class Transaccion {
    private String dineroRecibido;  // Monto recibido
    private String vuelto;          // Monto a devolver

    // Constructor
    public Transaccion(String dineroRecibido, String vuelto) {
        this.dineroRecibido = dineroRecibido;
        this.vuelto = vuelto;
    }

    // Getters y setters
    public String getDineroRecibido() {
        return dineroRecibido;
    }

    public void setDineroRecibido(String dineroRecibido) {
        this.dineroRecibido = dineroRecibido;
    }

    public String getVuelto() {
        return vuelto;
    }

    public void setVuelto(String vuelto) {
        this.vuelto = vuelto;
    }
}

