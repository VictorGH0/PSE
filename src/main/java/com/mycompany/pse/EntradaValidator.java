package com.mycompany.pse;

import java.util.regex.Pattern;

public class EntradaValidator {
    
    // Patrones de validación
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern FACTURA_PATTERN = Pattern.compile("^[A-Za-z0-9-_]{1,50}$");
    
    /**
     * Valida todos los parámetros de entrada
     */
    public static boolean validarEntrada(String privateKey, String email, int monto, 
                                        String numeroFactura, String acueducto) {
        boolean valido = true;
        
        // Validar clave privada
        if (privateKey == null || privateKey.trim().isEmpty()) {
            System.err.println("❌ Error: La clave privada es requerida");
            valido = false;
        } else if (!privateKey.startsWith("prv_")) {
            System.err.println("❌ Error: La clave privada debe comenzar con 'prv_'");
            valido = false;
        }
        
        // Validar email
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            System.err.println("❌ Error: Email inválido: " + email);
            valido = false;
        }
        
        // Validar monto
        if (monto <= 0) {
            System.err.println("❌ Error: El monto debe ser mayor a 0");
            valido = false;
        }
        if (monto > 20000000) { // Límite de Wompi
            System.err.println("❌ Error: El monto no puede superar $20,000,000");
            valido = false;
        }
        
        // Validar número de factura
        if (numeroFactura == null || !FACTURA_PATTERN.matcher(numeroFactura).matches()) {
            System.err.println("❌ Error: Número de factura inválido (solo alfanumérico, guiones y guiones bajos, max 50 caracteres)");
            valido = false;
        }
        
        // Validar acueducto
        if (acueducto == null || acueducto.trim().isEmpty() || acueducto.length() > 100) {
            System.err.println("❌ Error: Nombre del acueducto inválido (máximo 100 caracteres)");
            valido = false;
        }
        
        return valido;
    }
}