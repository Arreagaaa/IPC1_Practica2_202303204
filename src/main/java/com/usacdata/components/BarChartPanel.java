package com.usacdata.components;

import com.usacdata.model.DataPoint;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class BarChartPanel extends JPanel {
    private DataPoint[] dataPoints;
    private String xAxisLabel = "Categoría";
    private String yAxisLabel = "Contador";
    private String title = "";

    // Constantes para el dibujo
    private final int PADDING = 30;
    private final int LABEL_PADDING = 20;
    private final Color BAR_COLOR = new Color(79, 129, 189);
    private final Color AXIS_COLOR = new Color(80, 80, 80);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 11);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);

    public BarChartPanel() {
        setBackground(Color.WHITE);
    }

    public void setData(DataPoint[] dataPoints, String xAxisLabel, String yAxisLabel, String title) {
        this.dataPoints = dataPoints;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.title = title;

        // Depuración
        System.out.println("Configurando gráfico con etiquetas - X: " + xAxisLabel + ", Y: " + yAxisLabel);

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (dataPoints == null || dataPoints.length == 0) {
            drawNoDataMessage(g);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dimensiones del área de dibujo
        int width = getWidth();
        int height = getHeight();

        // Área para el gráfico
        int graphWidth = width - 2 * PADDING - LABEL_PADDING;
        int graphHeight = height - 2 * PADDING - LABEL_PADDING;

        // Encontrar el valor máximo para escalar el gráfico
        int maxValue = 0;
        for (DataPoint dp : dataPoints) {
            maxValue = Math.max(maxValue, dp.getCount());
        }
        maxValue = (maxValue <= 0) ? 10 : maxValue; // Asegurar un valor máximo positivo

        // Dibujar el título
        g2d.setColor(Color.BLACK);
        g2d.setFont(TITLE_FONT);
        FontMetrics titleMetrics = g2d.getFontMetrics();
        g2d.drawString(title, (width - titleMetrics.stringWidth(title)) / 2, PADDING);

        // Dibujar los ejes
        g2d.setColor(AXIS_COLOR);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(PADDING + LABEL_PADDING, height - PADDING - LABEL_PADDING,
                PADDING + LABEL_PADDING, PADDING);
        g2d.drawLine(PADDING + LABEL_PADDING, height - PADDING - LABEL_PADDING,
                width - PADDING, height - PADDING - LABEL_PADDING);

        // Dibujar etiquetas de ejes
        g2d.setFont(LABEL_FONT);
        FontMetrics metrics = g2d.getFontMetrics();

        // Etiqueta del eje X
        g2d.drawString(xAxisLabel,
                (width - metrics.stringWidth(xAxisLabel)) / 2,
                height - metrics.getHeight() / 2);

        // Etiqueta del eje Y (girada)
        // Aumentar el padding para dar más espacio a la etiqueta
        int yLabelPadding = PADDING + 5;

        // Guardar el estado de transformación actual
        AffineTransform originalTransform = g2d.getTransform();

        // Rotación y dibujo de la etiqueta
        g2d.rotate(-Math.PI / 2);
        g2d.drawString(yAxisLabel,
                -height / 2 - metrics.stringWidth(yAxisLabel) / 2,
                yLabelPadding);

        // Restaurar la transformación original
        g2d.setTransform(originalTransform);

        // Agregar verificación de debug
        System.out.println("Dibujando etiqueta Y: " + yAxisLabel + " en posición: " +
                (-height / 2 - metrics.stringWidth(yAxisLabel) / 2) + ", " + yLabelPadding);

        // Calcular ancho de barras
        int barWidth = graphWidth / dataPoints.length;
        int barPadding = barWidth / 4;

        // Dibujar barras
        for (int i = 0; i < dataPoints.length; i++) {
            DataPoint dp = dataPoints[i];
            int barHeight = (int) ((dp.getCount() * 1.0 / maxValue) * graphHeight);
            int barX = PADDING + LABEL_PADDING + i * barWidth + barPadding;
            int barY = height - PADDING - LABEL_PADDING - barHeight;

            // Dibujar la barra
            g2d.setColor(BAR_COLOR);
            g2d.fillRect(barX, barY, barWidth - 2 * barPadding, barHeight);
            g2d.setColor(new Color(50, 50, 50));
            g2d.drawRect(barX, barY, barWidth - 2 * barPadding, barHeight);

            // Dibujar etiqueta de categoría
            String label = dp.getCategory();
            int labelWidth = metrics.stringWidth(label);
            if (labelWidth > barWidth - 2 * barPadding) {
                label = label.substring(0, 3) + "...";
                labelWidth = metrics.stringWidth(label);
            }

            g2d.setColor(Color.BLACK);
            g2d.drawString(label,
                    barX + (barWidth - 2 * barPadding - labelWidth) / 2,
                    height - PADDING - LABEL_PADDING + 15);

            // Dibujar valor encima de la barra
            String valueStr = String.valueOf(dp.getCount());
            g2d.drawString(valueStr,
                    barX + (barWidth - 2 * barPadding - metrics.stringWidth(valueStr)) / 2,
                    barY - 5);
        }
    }

    private void drawNoDataMessage(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        String message = "No hay datos para mostrar";
        g2d.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        FontMetrics metrics = g2d.getFontMetrics();
        int x = (getWidth() - metrics.stringWidth(message)) / 2;
        int y = getHeight() / 2;

        g2d.setColor(new Color(150, 150, 150));
        g2d.drawString(message, x, y);
    }
}
