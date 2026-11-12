package com.mycompany.pse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ComisionCalculator {

    /**
     * Calcula el monto bruto a partir del JSON de configuración
     */
    public static int calcularMontoConComisionDesdeJson(double montoNetoDeseado, int idLocal) {
        
        try {
            String jsonConfig = DBLink.obtenerConfiguracionComisionPorIdLocal(idLocal);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(jsonConfig);

            // Extraer valores desde el JSON
            double porcentaje = Double.parseDouble(json.get("porcentaje").asText()) / 100.0;
            double valorFijo = "si".equalsIgnoreCase(json.get("valor_fijo").asText()) ? 700 : 0;
            double iva = "si".equalsIgnoreCase(json.get("iva").asText()) ? 0.19 : 0;

            // Calcular el monto bruto
            double factor = 1 - (porcentaje * (1 + iva));
            double montoBruto = (montoNetoDeseado + valorFijo * (1 + iva)) / factor;

            return (int) Math.round(montoBruto);

        } catch (Exception e) {
            System.err.println("❌ Error al interpretar configuración JSON: " + e.getMessage());
            return -1; // o podrías usar un valor por defecto o lanzar excepción si prefieres
        }
    }
}