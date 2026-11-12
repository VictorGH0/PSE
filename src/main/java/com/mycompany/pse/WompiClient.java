package com.mycompany.pse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WompiClient {
    
    //link produccion
    private static final String API_URL = "https://production.wompi.co/v1/payment_links";
    //link modo pruebas
    //private static final String API_URL = "https://sandbox.wompi.co/v1/payment_links";

    
    private static final Logger LOGGER = Logger.getLogger(WompiClient.class.getName());
    
    /**
     * Crea el link de pago con todas las validaciones y manejo de errores
     */
    public static String crearLinkPago(String privateKey, String email, int montoNetoSinComision, 
                                     String numeroFactura, String acueducto, int  idLocal) {
        HttpURLConnection con = null;
        try {
            int montoFinal = ComisionCalculator.calcularMontoConComisionDesdeJson(montoNetoSinComision, idLocal);
            
            System.out.println("ENTRO A CREAR LINK");
            String xIdPeriodo = DBLink.obtenerPeriodo(idLocal, numeroFactura);
            String fechaPagoRecargo = DBLink.obtenerFechaPagoRecargo(idLocal, xIdPeriodo);
            
            // Calcular fecha de vencimiento (30 días desde ahora)
            LocalDateTime fechaVencimiento = LocalDateTime.now();
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
            LocalDateTime fechaRecargo = LocalDateTime.parse(fechaPagoRecargo, formatter);
            
            long dias = (Duration.between(fechaVencimiento, fechaRecargo).toDays()) + 1;
            
            System.out.println("dias a sumaaarrrr ====== " + dias);
            LocalDateTime fechaVencimientoDias = fechaVencimiento.plusDays(dias);
            
            String fechaVencimientoISO = fechaVencimientoDias.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")); 

            URL url = new URL(API_URL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + privateKey);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("User-Agent", "GeneradorLinkPago/1.0");
            con.setDoOutput(true);
            con.setConnectTimeout(30000); // 30 segundos
            con.setReadTimeout(30000);    // 30 segundos
            
            // Crear descripción que incluya el número de factura
            String description = String.format("Pago de factura Fra# %s - %s",numeroFactura, acueducto);
            String name = String.format("%s-Pago Factura %s", idLocal,numeroFactura);
            
            // Construir JSON con fecha de vencimiento
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            jsonBuilder.append("\"name\": \"").append(JsonUtils.escapeJson(name)).append("\",");
            jsonBuilder.append("\"description\": \"").append(JsonUtils.escapeJson(description)).append("\",");
            jsonBuilder.append("\"single_use\": true,");
            jsonBuilder.append("\"collect_shipping\": false,");
            jsonBuilder.append("\"currency\": \"COP\",");
            jsonBuilder.append("\"amount_in_cents\": ").append(montoFinal * 100).append(",");
            jsonBuilder.append("\"customer_email\": \"").append(JsonUtils.escapeJson(email)).append("\",");
            jsonBuilder.append("\"expires_at\": \"").append(fechaVencimientoISO).append("\"");
            jsonBuilder.append("}");

            String json = jsonBuilder.toString();
            
            System.out.println("📋 Datos del pago:");
            System.out.println("   Número de Factura: " + numeroFactura);
            System.out.println("   Acueducto: " + acueducto);
            System.out.println("   Email: " + email);
            System.out.println("   Monto solicitado: $" + String.format("%,d", montoNetoSinComision));
            System.out.println("   Monto cobrado (con comisión): $" + String.format("%,d", montoFinal));
            System.out.println("   Fecha de vencimiento: " + fechaVencimiento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            System.out.println("   Descripción: " + description);
            System.out.println();
            
            LOGGER.info("Creando link de pago para factura: " + numeroFactura);
            
            // Enviar request
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = json.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int code = con.getResponseCode();
            System.out.println("📊 Código de respuesta: " + code);
            
            InputStream responseStream = (code >= 200 && code < 300) ? 
                con.getInputStream() : con.getErrorStream();
                
            String response = new BufferedReader(new InputStreamReader(responseStream, "UTF-8"))
                .lines()
                .reduce("", (acc, line) -> acc + line + "\n");
            
            if (code >= 200 && code < 300) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response);
                
                if (root.has("data") && root.get("data").has("id")) {
                    String id = root.get("data").get("id").asText();
                    String link = "https://checkout.wompi.co/l/" + id;
                    
                    System.out.println("✅ Link generado exitosamente:");
                    System.out.println("   ID: " + id);
                    System.out.println("   Link: " + link);
                    System.out.println("   Factura: " + numeroFactura);
                    System.out.println("   Vence: " + fechaVencimiento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    
                    
                    LOGGER.info("Link de pago creado exitosamente: " + id);
                    return link;
                } else {
                    System.err.println("❌ Error: No se encontró el ID en la respuesta");
                    System.err.println("Respuesta completa: " + response);
                    LOGGER.severe("Respuesta inválida de la API: " + response);
                    return null;
                }
            } else {
                System.err.println("❌ Error HTTP " + code + ": " + response);
                LOGGER.severe("Error HTTP " + code + ": " + response);
                
                // Manejo específico de errores comunes
                if (code == 401) {
                    System.err.println("💡 Sugerencia: Verificar que la clave privada sea correcta y válida");
                } else if (code == 400) {
                    System.err.println("💡 Sugerencia: Verificar que todos los datos enviados sean válidos");
                } else if (code >= 500) {
                    System.err.println("💡 Sugerencia: Error del servidor, intentar nuevamente más tarde");
                }
                
                return null;
            }
            
        } catch (IOException e) {
            System.err.println("❌ Error de conexión: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error de conexión", e);
            System.err.println("💡 Sugerencia: Verificar conexión a internet y que la URL de la API sea correcta");
            return null;
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error inesperado", e);
            return null;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }
}