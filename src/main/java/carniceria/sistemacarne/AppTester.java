/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carniceria.sistemacarne;

import dao.ClienteDAO;
import dao.ProductoDAO;
import java.util.List;
import model.Cliente;
import model.Producto;

/**
 *
 * @author garca
 */
public class AppTester {

    public static void main(String[] args) {
        ProductoDAO productoDao = new ProductoDAO();
        Producto producto = new Producto();
        
        
      /*  producto.setNombre("asado");
        producto.setCodigo(33);
        producto.setPrecio(12500.00);
        producto.setTipo("carne");
        productoDao.guardar(producto);/*
      
       //  clienteDao.eliminar(36L);
       
       
       /*cliente.setNombre("gordo");
        cliente.setAlias("mami");
        cliente.setId(34L);
        clienteDao.actualizar(cliente);*/

        List<Producto> listaProductos = productoDao.buscarTodos();
        
        for (Producto c : listaProductos){
           System.out.println(c.getNombre()) ;
            System.out.println(c.getPrecio());
            System.out.println(c.getId());
        }
        
        
    }
}
