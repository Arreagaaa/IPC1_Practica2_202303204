package com.usacdata.util;

import com.usacdata.model.DataPoint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class FileReader {
    private String xAxisLabel = "Categoría";
    private String yAxisLabel = "Contador";

    public FileReaderResult readDataFromFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file)));

            // Leer la primera línea para obtener las etiquetas de los ejes
            String headerLine = reader.readLine();
            parseAxisLabels(headerLine);

            // Contar el número de líneas para dimensionar el array
            int lineCount = 0;
            BufferedReader countReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file)));
            // Saltamos la línea de encabezado
            countReader.readLine();

            while (countReader.readLine() != null) {
                lineCount++;
            }
            countReader.close();

            // Crear el array con el tamaño exacto necesario
            DataPoint[] dataPoints = new DataPoint[lineCount];

            // Leer los datos
            String line;
            int index = 0;

            while ((line = reader.readLine()) != null) {
                // El formato .ibpc1 usa ":" como separador
                String[] parts = line.split(":");
                if (parts.length >= 2) {
                    String category = parts[0].trim();
                    try {
                        int count = Integer.parseInt(parts[1].trim());
                        dataPoints[index++] = new DataPoint(category, count);
                    } catch (NumberFormatException e) {
                        System.err.println("Error al convertir el valor: " + parts[1]);
                    }
                }
            }

            reader.close();

            // Asegurarnos de que se devuelvan las etiquetas correctas
            System.out.println("Etiqueta eje X: " + xAxisLabel);
            System.out.println("Etiqueta eje Y: " + yAxisLabel);

            return new FileReaderResult(dataPoints, xAxisLabel, yAxisLabel);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void parseAxisLabels(String headerLine) {
        if (headerLine != null && !headerLine.isEmpty()) {
            // Para formato .ibpc1 el separador es ":"
            String[] parts = headerLine.split(":");
            if (parts.length >= 2) {
                xAxisLabel = parts[0].trim();
                yAxisLabel = parts[1].trim();

                // Depuración para verificar que se leen correctamente
                System.out.println("Encabezados leídos - X: " + xAxisLabel + ", Y: " + yAxisLabel);
            }
        }
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
