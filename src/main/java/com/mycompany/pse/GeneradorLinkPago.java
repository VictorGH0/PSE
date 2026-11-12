package com.mycompany.pse;

import java.util.logging.Logger;

public class GeneradorLinkPago {
    private static final Logger LOGGER = Logger.getLogger(GeneradorLinkPago.class.getName());
    
    public static void main(String[] args) {
    	
        if (args.length < 5) {
            System.err.println("❌ Error: Faltan argumentos");
            System.err.println("Uso: java -jar generador.jar <privateKey> <monto> <idCliente> <idLocal> <numeroFactura>");
            System.err.println("Ejemplo: java -jar generador.jar prv_test_xxxx 100000 60 142 497");
            System.exit(1);
        }
        
    	//String privateKey = "prv_test_U4i6TB6h18F6E9KwegL8cGq5muoFrQbN";
    	String privateKey = args[0];
        int monto;
        int idCliente, idLocal;
        String nroFra = args[4];
        
        
        
        
        /*int monto = 10552;
        int idCliente = 329; 
        int idLocal = 106;
        String nroFra = "13450";
    	*/
        
        try {
            monto = Integer.parseInt(args[1]);
            idCliente = Integer.parseInt(args[2]);
            idLocal = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            System.err.println("❌ Error: Los parámetros monto, idCliente e idLocal deben ser números");
            System.err.println("Recibido: monto=" + args[1] + ", idCliente=" + args[2] + ", idLocal=" + args[3]);
            System.exit(1);
            return; // Para evitar warning del compilador
        }
        
        // Consultar email y acueducto desde base de datos
        String email = DBLink.obtenerEmailCliente(idLocal,idCliente);
        String nombreAcueducto = DBLink.obtenerNombreAcueducto(idLocal);
        
        if (email == null || nombreAcueducto == null) {
            System.err.println("❌ Error: No se encontró email o nombre del acueducto");
            System.err.println("   Email encontrado: " + (email != null ? email : "NULL"));
            System.err.println("   Acueducto encontrado: " + (nombreAcueducto != null ? nombreAcueducto : "NULL"));
            System.exit(1);
        }
        
        
        // Crear link de pago
        String resultado = WompiClient.crearLinkPago(privateKey, email, monto, nroFra, nombreAcueducto , idLocal);
        
        if (resultado != null) {
            System.out.println("✅ Link generado: " + resultado);
            
            // Registrar en BD con los tipos correctos
            DBLink.registrarLink(idCliente, idLocal,nroFra ,resultado);
            
            System.exit(0);
        } else {
            System.err.println("❌ Error generando link de pago");
            System.exit(1);
        }
    }
}