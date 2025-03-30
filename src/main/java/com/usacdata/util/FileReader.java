package com.usacdata.util;

import com.usacdata.model.DataPoint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class FileReader {
    private String xAxisLabel = "Categoría";
    private String yAxisLabel = "Contador";
    private String errorMessage = null;

    public FileReaderResult readDataFromFile(File file) {
        try {
            String fileName = file.getName().toLowerCase();

            // Validar extensión de archivo
            if (!fileName.endsWith(".csv") && !fileName.endsWith(".ibpc1")) {
                errorMessage = "Formato de archivo no soportado. Use archivos .csv o .ibpc1";
                return null;
            }

            // Detectar separador basado en el contenido
            String separator = detectSeparator(file);
            if (separator == null) {
                errorMessage = "No se pudo detectar el formato del archivo. Asegúrese de que sea un archivo CSV válido.";
                return null;
            }

            // Primero contamos las líneas para dimensionar el array
            int validLines = 0;
            BufferedReader lineCounter = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file)));

            // Saltamos la línea de cabecera
            lineCounter.readLine();

            String line;
            while ((line = lineCounter.readLine()) != null) {
                String[] parts = line.split(separator);
                if (parts.length >= 2) {
                    try {
                        Integer.parseInt(parts[1].trim());
                        validLines++; // Solo contamos líneas con valores numéricos válidos
                    } catch (NumberFormatException e) {
                        // No contamos esta línea
                    }
                }
            }
            lineCounter.close();

            if (validLines == 0) {
                errorMessage = "No se encontraron datos válidos en el archivo";
                return null;
            }

            // Ahora leemos los datos
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file)));

            // Leer cabecera
            String headerLine = reader.readLine();
            if (headerLine == null) {
                errorMessage = "No se pudo leer la cabecera del archivo";
                reader.close();
                return null;
            }

            parseAxisLabels(headerLine, separator);

            // Crear el array con tamaño exacto
            DataPoint[] dataPoints = new DataPoint[validLines];
            int currentIndex = 0;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] parts = line.split(separator);
                if (parts.length >= 2) {
                    String category = parts[0].trim();
                    try {
                        int count = Integer.parseInt(parts[1].trim());
                        dataPoints[currentIndex++] = new DataPoint(category, count);
                    } catch (NumberFormatException e) {
                        System.out.println("Advertencia: Error al parsear valor numérico en línea " + lineNumber + ": "
                                + parts[1]);
                    }
                }
            }

            reader.close();

            return new FileReaderResult(dataPoints, xAxisLabel, yAxisLabel);

        } catch (Exception e) {
            errorMessage = "Error al leer el archivo: " + e.getMessage();
            e.printStackTrace();
            return null;
        }
    }

    // Método para detectar automáticamente el separador
    private String detectSeparator(File file) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file)));

            // Leer la primera línea
            String headerLine = reader.readLine();
            reader.close();

            if (headerLine == null) {
                return null;
            }

            // Verificar si contiene comas
            if (headerLine.contains(",")) {
                return ",";
            }

            // Verificar si contiene dos puntos
            if (headerLine.contains(":")) {
                return ":";
            }

            // Si no se detecta ningún separador conocido
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void parseAxisLabels(String headerLine, String separator) {
        if (headerLine != null && !headerLine.isEmpty()) {
            String[] parts = headerLine.split(separator);
            if (parts.length >= 2) {
                xAxisLabel = parts[0].trim();
                yAxisLabel = parts[1].trim();
            }
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static class FileReaderResult {
        private DataPoint[] dataPoints;
        private String xAxisLabel;
        private String yAxisLabel;

        public FileReaderResult(DataPoint[] dataPoints, String xAxisLabel, String yAxisLabel) {
            this.dataPoints = dataPoints;
            this.xAxisLabel = xAxisLabel;
            this.yAxisLabel = yAxisLabel;
        }

        public DataPoint[] getDataPoints() {
            return dataPoints;
        }

        public String getXAxisLabel() {
            return xAxisLabel;
        }

        public String getYAxisLabel() {
            return yAxisLabel;
        }
    }
}
