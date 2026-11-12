package com.mycompany.pse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DBLink {

    private static final String JDBC_URL = "jdbc:jtds:sqlserver://localhost;DatabaseName=BDAquamovil";
    private static final String JDBC_USER = "usrbd";
    private static final String JDBC_PASSWORD = "LinuX16%";
    
    

    public static String obtenerEmailCliente(int idLocal, int idCliente) {
    	
        String sql = "SELECT email FROM tblTerceros WHERE idLocal = ? AND idCliente = ?";
        
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ No se pudo cargar el driver JTDS");
            e.printStackTrace();
        }
        
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
        	
        	stmt.setInt(1, idLocal);
            stmt.setInt(2, idCliente);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo email: " + e.getMessage());
        }
        return null;
    }

    public static String obtenerNombreAcueducto(int idLocal) {
        String sql = "SELECT nombreLocal FROM tblLocales WHERE idLocal = ?";
        
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ No se pudo cargar el driver JTDS");
            e.printStackTrace();
        }
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idLocal);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nombreLocal");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo acueducto: " + e.getMessage());
        }
        return null;
    }
    
    public static String obtenerConfiguracionComisionPorIdLocal(int idLocal) {
        String jsonConfig = null;

        String query = "SELECT configuracion_comision FROM tblLocales WHERE idLocal = ?";
        
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ No se pudo cargar el driver JTDS");
            e.printStackTrace();
        }

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idLocal);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                jsonConfig = rs.getString("configuracion_comision");
            } else {
                System.err.println("⚠️ No se encontró configuración para idLocal: " + idLocal);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error consultando la base de datos:");
            e.printStackTrace();
        }

        return jsonConfig;
    }
    
    // Función corregida para DBLink.java
    public static void registrarLink(int idCliente, int idLocal,String nroFra, String link) {
        String sql = "INSERT INTO tblLinks (idCliente, idLocal, idDcto, idPeriodo, link, fechaExpiracion) VALUES (?, ?,?, ?, ?, ?)";

        // Usar la misma fecha de expiración que WompiClient (30 días)
        LocalDateTime fechaExp = LocalDateTime.now().plusDays(30);
        String fechaExpStr = fechaExp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        LocalDateTime now = LocalDateTime.now();
        int idPeriodo = now.getYear() * 100 + now.getMonthValue();
        
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ No se pudo cargar el driver JTDS");
            e.printStackTrace();
        }

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCliente);    // Cambiado de setString a setInt
            stmt.setInt(2, idLocal);
            stmt.setInt(3, Integer.parseInt(nroFra));
            stmt.setInt(4, idPeriodo);
            stmt.setString(5, link);
            stmt.setString(6, fechaExpStr);

            stmt.executeUpdate();
            System.out.println("✅ Link registrado en base de datos:");
            System.out.println("   IdCliente: " + idCliente);
            System.out.println("   IdLocal: " + idLocal);
            System.out.println("   IdPeriodo: " + idPeriodo);
            System.out.println("   Expira: " + fechaExp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        } catch (SQLException e) {
            System.err.println("❌ Error registrando link en DB: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    public static String obtenerPeriodo(int idLocal, String nroFactura) {
        String sql = "SELECT idPeriodo FROM tblDctos WHERE idLocal = ? AND idDcto = ? AND IDTIPOORDEN = 9";
        
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ No se pudo cargar el driver JTDS");
            e.printStackTrace();
        }
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idLocal);
            stmt.setString(2, nroFactura);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("idPeriodo");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo idPeriodo: " + e.getMessage());
        }
        return null;
    }
    
    
    public static String obtenerFechaPagoRecargo(int idLocal, String idPeriodo) {
        String sql = "SELECT fechaConRecargo FROM tblDctosPeriodo WHERE idLocal = ? AND idPeriodo = ?";
        
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ No se pudo cargar el driver JTDS");
            e.printStackTrace();
        }
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idLocal);
            stmt.setString(2, idPeriodo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("fechaConRecargo");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo fechaConRecargo: " + e.getMessage());
        }
        return null;
    }
    

}
