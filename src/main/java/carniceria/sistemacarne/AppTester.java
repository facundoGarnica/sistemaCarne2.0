/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carniceria.sistemacarne;

import dao.ClienteDAO;
import java.util.List;
import model.Cliente;

/**
 *
 * @author garca
 */
public class AppTester {

    public static void main(String[] args) {
        ClienteDAO clienteDao = new ClienteDAO();
        Cliente cliente = new Cliente();
        
        
        cliente.setNombre("gordo");
        cliente.setAlias("bebu");
        clienteDao.guardar(cliente);
      
       //  clienteDao.eliminar(36L);
       
       
       /*cliente.setNombre("gordo");
        cliente.setAlias("mami");
        cliente.setId(34L);
        clienteDao.actualizar(cliente);*/

        List<Cliente> listaClientes = clienteDao.buscarTodos();
        
        for (Cliente c : listaClientes){
           System.out.println(c.getNombre()) ;
            System.out.println(c.getAlias());
            System.out.println(c.getId());
        }
        
        
    }
}
