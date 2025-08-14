/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author garca
 */
import java.text.DecimalFormat;

public class CalcularVuelto {

    static final int[] BILLETES = {20000, 10000, 2000, 1000, 500, 200, 100, 50};
    List<String> listaOrdenada;
    private List<String> listaPagos;  // Almacena las posibles formas de pago
    private List<String> listaVueltos; // Almacena los posibles vueltos a dar
    private List<Transaccion> listaTransacciones; // Almacena las transacciones (Dinero recibido y vuelto)

    // Crear el formato con punto para miles y sin decimales
    private static final DecimalFormat formato = new DecimalFormat("#,##0");

    public CalcularVuelto() {
        listaPagos = new ArrayList<>();
        listaVueltos = new ArrayList<>();
        listaTransacciones = new ArrayList<>();
    }

    public List<String> GetListaOrdenada() {
        return this.listaOrdenada;
    }

    public List<String> GetListaPagos() {
        return this.listaPagos;
    }

    public List<String> GetListaVueltos() {
        return this.listaVueltos;
    }

    public List<Transaccion> GetListaTransacciones() {
        return this.listaTransacciones;
    }

    public void CalcularVuelto(double Suma) {
        int monto = (int) Math.round(Suma); // Redondea el double a un entero
        Set<String> resultados = new HashSet<>();
        listaTransacciones.clear();  // Limpia las listas antes de calcular

        generarCombinaciones(new ArrayList<>(), monto, 0, resultados);

        // Convierte a lista y ordena de mayor a menor basado en la cantidad de billetes grandes
        listaOrdenada = resultados.stream()
                .sorted((a, b) -> b.compareTo(a))
                .limit(8) // Modificar esto si quiero aumentar la cantidad de combinaciones
                .collect(Collectors.toList());

        // Separar pagos y vueltos y almacenar en una lista de Transaccion
        for (String resultado : listaOrdenada) {
            String dineroRecibido = "";
            String vuelto = "";

            if (resultado.contains("sin vuelto")) {
                // Si no hay vuelto, solo es un pago (en este caso mostramos solo el número)
                dineroRecibido = resultado.replace("Pago con ", "").replace(", sin vuelto", "").trim();
                vuelto = "Ninguno";
            } else {
                // Si hay vuelto, separamos la parte de "devolver"
                String[] partes = resultado.split(", devolver ");
                if (partes.length > 1) {
                    dineroRecibido = partes[0].replace("Pago con ", "").trim();  // El dinero recibido
                    vuelto = partes[1];  // El vuelto
                }
            }

            // Agregamos la transacción (dinero recibido, vuelto)
            listaTransacciones.add(new Transaccion(formatearMonto(dineroRecibido), vuelto));
        }
    }

    // Función para devolver el monto formateado con separador de miles y sin decimales
    private String formatearMonto(String monto) {
        // Convertir el monto a un número, luego formatearlo con el patrón decimal
        try {
            double montoDouble = Double.parseDouble(monto.replace(",", "").trim());
            return formato.format(montoDouble); // Aplicar formato (sin decimales)
        } catch (NumberFormatException e) {
            return monto;  // En caso de error, devolver el monto original
        }
    }

    // Método auxiliar para generar las combinaciones
    private void generarCombinaciones(List<Integer> actual, int montoRestante, int indice, Set<String> resultados) {
        if (montoRestante <= 0) {
            int sumaBilletes = actual.stream().mapToInt(Integer::intValue).sum();
            String resultado;
            if (montoRestante == 0) {
                resultado = "Pago con " + sumaBilletes + ", sin vuelto";
            } else {
                resultado = "Pago con " + sumaBilletes + ", devolver " + (-montoRestante);
            }
            resultados.add(resultado);
            return;
        }

        for (int i = indice; i < BILLETES.length; i++) {
            if (BILLETES[i] < 1000 && montoRestante >= 1000) {
                continue;
            }
            List<Integer> nuevaLista = new ArrayList<>(actual);
            nuevaLista.add(BILLETES[i]);
            generarCombinaciones(nuevaLista, montoRestante - BILLETES[i], i, resultados);
        }
    }
}
