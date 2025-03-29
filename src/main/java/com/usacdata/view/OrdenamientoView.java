package com.usacdata.view;

import com.usacdata.components.BarChartPanel;
import com.usacdata.model.DataPoint;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;

// Imports para iText 5
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class OrdenamientoView extends JDialog {

    private JPanel panelPrincipal;
    private JComboBox<String> comboAlgoritmo;
    private JComboBox<String> comboVelocidad;
    private ButtonGroup grupoDireccion;
    private JRadioButton radioAscendente;
    private JRadioButton radioDescendente;
    private JButton btnOrdenar;
    private JButton btnCancelar;
    private JLabel lblTiempo;
    private JLabel lblPasos;
    private JProgressBar progressBar;
    private BarChartPanel panelVisualizacion;

    private boolean ordenando = false;
    private boolean cancelado = false;
    private Thread hiloOrdenamiento = null;
    private DataPoint[] datosOriginales = null;
    private DataPoint[] datosActuales = null;
    private long tiempoInicio = 0;
    private int contadorPasos = 0;
    private int totalPasosEstimados = 0; // Nueva variable para estimar pasos totales
    private Timer timerActualizacion;
    private String xAxisLabel;
    private String yAxisLabel;
    private String chartTitle;

    // Variables para guardar la imagen inicial
    private BufferedImage imagenInicial = null;
    private DataPoint[] datosOriginalesNoOrdenados = null;

    public OrdenamientoView(Frame owner, DataPoint[] data, String xAxisLabel, String yAxisLabel, String title) {
        super(owner, "Métodos de Ordenamiento", true);
        setSize(800, 600);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Guardamos los datos reales
        datosOriginales = data;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.chartTitle = title;

        initComponents();

        // Clonar los datos para trabajar con ellos
        clonarDatos();

        // Mostrar datos iniciales
        actualizarVisualizacion();
    }

    private void initComponents() {
        panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel de configuración (Norte)
        JPanel panelConfiguracion = new JPanel(new GridLayout(3, 1, 5, 5));
        panelConfiguracion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                        "Configuración de Ordenamiento",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12),
                        new Color(70, 70, 70)),
                new EmptyBorder(10, 10, 10, 10)));

        // Fila 1: Algoritmo de ordenamiento
        JPanel panelAlgoritmo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblAlgoritmo = new JLabel("Algoritmo de ordenamiento:");
        comboAlgoritmo = new JComboBox<>(new String[] {
                "Bubble Sort", "Insert Sort", "Select Sort",
                "Merge Sort", "Quick Sort", "Shell Sort"
        });
        panelAlgoritmo.add(lblAlgoritmo);
        panelAlgoritmo.add(comboAlgoritmo);
        panelConfiguracion.add(panelAlgoritmo);

        // Fila 2: Velocidad de ordenamiento
        JPanel panelVelocidad = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblVelocidad = new JLabel("Velocidad de ordenamiento:");
        comboVelocidad = new JComboBox<>(new String[] { "Alta", "Media", "Baja" });
        panelVelocidad.add(lblVelocidad);
        panelVelocidad.add(comboVelocidad);
        panelConfiguracion.add(panelVelocidad);

        // Fila 3: Dirección de ordenamiento
        JPanel panelDireccion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblDireccion = new JLabel("Dirección de ordenamiento:");
        grupoDireccion = new ButtonGroup();
        radioAscendente = new JRadioButton("Ascendente", true);
        radioDescendente = new JRadioButton("Descendente");
        grupoDireccion.add(radioAscendente);
        grupoDireccion.add(radioDescendente);
        panelDireccion.add(lblDireccion);
        panelDireccion.add(radioAscendente);
        panelDireccion.add(radioDescendente);
        panelConfiguracion.add(panelDireccion);

        // Panel de visualización (Centro)
        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                        "Visualización del Ordenamiento",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12),
                        new Color(70, 70, 70)),
                new EmptyBorder(10, 10, 10, 10)));

        // Panel de estadísticas
        JPanel panelEstadisticas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblTiempo = new JLabel("Tiempo: 00:00:000");
        lblPasos = new JLabel("Pasos: 0");
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        panelEstadisticas.add(lblTiempo);
        panelEstadisticas.add(Box.createHorizontalStrut(15));
        panelEstadisticas.add(lblPasos);
        panelEstadisticas.add(Box.createHorizontalStrut(15));
        panelEstadisticas.add(progressBar);

        // Panel de gráfica
        panelVisualizacion = new BarChartPanel();
        panelCentro.add(panelEstadisticas, BorderLayout.NORTH);
        panelCentro.add(panelVisualizacion, BorderLayout.CENTER);

        // Panel de botones (Sur)
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnOrdenar = new JButton("Iniciar Ordenamiento");
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setEnabled(false);
        panelBotones.add(btnOrdenar);
        panelBotones.add(btnCancelar);

        // Añadir los paneles al panel principal
        panelPrincipal.add(panelConfiguracion, BorderLayout.NORTH);
        panelPrincipal.add(panelCentro, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        // Añadir el panel principal al contenedor
        getContentPane().add(panelPrincipal);

        // Configurar acciones de los botones
        setupActions();

        // Configurar timer para actualizar la UI
        timerActualizacion = new Timer(100, e -> actualizarInterfaz());
    }

    private void setupActions() {
        btnOrdenar.addActionListener(e -> {
            if (!ordenando) {
                iniciarOrdenamiento();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Ya hay un ordenamiento en progreso",
                        "Ordenamiento en progreso",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnCancelar.addActionListener(e -> {
            cancelarOrdenamiento();
        });
    }

    private void clonarDatos() {
        // Clonar para ordenamiento
        datosActuales = new DataPoint[datosOriginales.length];
        for (int i = 0; i < datosOriginales.length; i++) {
            datosActuales[i] = new DataPoint(
                    datosOriginales[i].getCategory(),
                    datosOriginales[i].getCount());
        }

        // Guardar una copia de los datos originales para el reporte
        datosOriginalesNoOrdenados = new DataPoint[datosOriginales.length];
        for (int i = 0; i < datosOriginales.length; i++) {
            datosOriginalesNoOrdenados[i] = new DataPoint(
                    datosOriginales[i].getCategory(),
                    datosOriginales[i].getCount());
        }
    }

    private void iniciarOrdenamiento() {
        // Reiniciar datos
        clonarDatos();
        contadorPasos = 0;
        cancelado = false;
        ordenando = true;

        // Estimar el número total de pasos basado en el algoritmo seleccionado
        String algoritmo = (String) comboAlgoritmo.getSelectedItem();
        int n = datosActuales.length;

        // Estimar pasos según algoritmo
        switch (algoritmo) {
            case "Bubble Sort":
                totalPasosEstimados = (n * n) / 2; // O(n²)
                break;
            case "Insert Sort":
                totalPasosEstimados = (n * n) / 4; // O(n²) pero generalmente más eficiente
                break;
            case "Select Sort":
                totalPasosEstimados = (n * n) / 2; // O(n²)
                break;
            case "Merge Sort":
            case "Quick Sort":
            case "Shell Sort":
                totalPasosEstimados = (int) (n * Math.log(n)); // O(n log n)
                break;
            default:
                totalPasosEstimados = n * n;
        }

        // Configurar la barra de progreso
        progressBar.setMinimum(0);
        progressBar.setMaximum(totalPasosEstimados);
        progressBar.setValue(0);

        // Capturar la imagen inicial antes de ordenar
        imagenInicial = capturarGrafica(panelVisualizacion);

        // Configurar UI
        btnOrdenar.setEnabled(false);
        btnCancelar.setEnabled(true);
        comboAlgoritmo.setEnabled(false);
        comboVelocidad.setEnabled(false);
        radioAscendente.setEnabled(false);
        radioDescendente.setEnabled(false);

        // Iniciar temporizador
        tiempoInicio = System.currentTimeMillis();
        timerActualizacion.start();

        // Iniciar hilo de ordenamiento
        hiloOrdenamiento = new Thread(() -> {
            try {
                String algoritmoSeleccionado = (String) comboAlgoritmo.getSelectedItem();
                boolean ascendente = radioAscendente.isSelected();

                switch (algoritmoSeleccionado) {
                    case "Bubble Sort":
                        bubbleSort(ascendente);
                        break;
                    case "Insert Sort":
                        insertSort(ascendente);
                        break;
                    case "Select Sort":
                        selectSort(ascendente);
                        break;
                    case "Merge Sort":
                        // El merge sort requiere un array auxiliar
                        DataPoint[] aux = new DataPoint[datosActuales.length];
                        mergeSort(0, datosActuales.length - 1, aux, ascendente);
                        break;
                    case "Quick Sort":
                        quickSort(0, datosActuales.length - 1, ascendente);
                        break;
                    case "Shell Sort":
                        shellSort(ascendente);
                        break;
                }

                // Actualizar UI una última vez después de completar
                SwingUtilities.invokeLater(() -> {
                    actualizarVisualizacion();
                    finalizarOrdenamiento(true);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    finalizarOrdenamiento(false);
                    JOptionPane.showMessageDialog(this,
                            "Error durante el ordenamiento: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        });

        hiloOrdenamiento.start();
    }

    private void cancelarOrdenamiento() {
        if (ordenando) {
            cancelado = true;
            finalizarOrdenamiento(false);
        }
    }

    private void finalizarOrdenamiento(boolean completado) {
        ordenando = false;
        timerActualizacion.stop();

        // Asegurar que la barra esté completa si terminó exitosamente
        if (completado && !cancelado) {
            progressBar.setValue(progressBar.getMaximum());
            progressBar.setString("100%");
        }

        // Restaurar UI
        btnOrdenar.setEnabled(true);
        btnCancelar.setEnabled(false);
        comboAlgoritmo.setEnabled(true);
        comboVelocidad.setEnabled(true);
        radioAscendente.setEnabled(true);
        radioDescendente.setEnabled(true);

        if (completado && !cancelado) {
            // Generar reporte
            generarReporte();

            JOptionPane.showMessageDialog(this,
                    "Ordenamiento completado en " + obtenerTiempoFormateado() +
                            " con " + contadorPasos + " pasos.",
                    "Ordenamiento Completado",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void actualizarInterfaz() {
        // Actualizar contador de tiempo
        lblTiempo.setText("Tiempo: " + obtenerTiempoFormateado());

        // Actualizar contador de pasos
        lblPasos.setText("Pasos: " + contadorPasos);

        // Actualizar barra de progreso
        if (totalPasosEstimados > 0) {
            int valorProgreso = Math.min(contadorPasos, totalPasosEstimados);
            progressBar.setValue(valorProgreso);
            progressBar.setString(String.format("%.1f%%", (valorProgreso * 100.0 / totalPasosEstimados)));
        }
    }

    private String obtenerTiempoFormateado() {
        long tiempoActual = System.currentTimeMillis();
        long tiempoTranscurrido = tiempoActual - tiempoInicio;

        // Formato mm:ss:ms
        long minutos = (tiempoTranscurrido / 60000) % 60;
        long segundos = (tiempoTranscurrido / 1000) % 60;
        long milisegundos = tiempoTranscurrido % 1000;

        return String.format("%02d:%02d:%03d", minutos, segundos, milisegundos);
    }

    private void actualizarVisualizacion() {
        // Actualizar gráfico con datos actuales
        panelVisualizacion.setData(
                datosActuales,
                xAxisLabel,
                yAxisLabel,
                "Ordenamiento: " + comboAlgoritmo.getSelectedItem() + " - " + chartTitle);
    }

    private void pausar() {
        try {
            String velocidad = (String) comboVelocidad.getSelectedItem();
            int pausa;

            // Definir pausas según velocidad
            switch (velocidad) {
                case "Alta":
                    pausa = 50;
                    break;
                case "Media":
                    pausa = 200;
                    break;
                case "Baja":
                    pausa = 500;
                    break;
                default:
                    pausa = 200;
                    break;
            }

            Thread.sleep(pausa);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void generarReporte() {
        try {
            // Crear directorio principal de reportes si no existe
            File dirReportes = new File("Reportes");
            if (!dirReportes.exists()) {
                dirReportes.mkdirs();
            }

            // Obtener nombre del algoritmo y crear subcarpeta
            String algoritmo = (String) comboAlgoritmo.getSelectedItem();
            File dirAlgoritmo = new File(dirReportes, algoritmo);
            if (!dirAlgoritmo.exists()) {
                dirAlgoritmo.mkdirs();
            }

            // Nombre del archivo
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fechaHora = sdf.format(new Date());
            String direccion = radioAscendente.isSelected() ? "Asc" : "Desc";
            String nombreArchivo = "Reporte_" + algoritmo.replace(" ", "") + "_" + direccion + "_" + fechaHora + ".pdf";

            File archivoReporte = new File(dirAlgoritmo, nombreArchivo);

            // Crear documento PDF (iText 5)
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(archivoReporte));
            document.open();

            // Establecer márgenes
            document.setMargins(36, 36, 36, 36);

            // Estilo de fuentes
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font fontSubtitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12);

            // Título del reporte
            Paragraph title = new Paragraph("REPORTE DE ORDENAMIENTO", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Información del estudiante
            document.add(new Paragraph("\nNombre: Christian Javier Rivas Arreaga", fontNormal));
            document.add(new Paragraph("Carné: 202303204", fontNormal));

            // Fecha y hora
            document.add(new Paragraph("Fecha y hora: " +
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()), fontNormal));

            // Separador
            document.add(new Paragraph("\n"));

            // Información del ordenamiento (tabla)
            PdfPTable tableInfo = new PdfPTable(2);
            tableInfo.setWidthPercentage(100);
            try {
                tableInfo.setWidths(new float[] { 1f, 2f });
            } catch (DocumentException e) {
                e.printStackTrace();
            }

            // Agregar filas a la tabla de información
            addRowToTable(tableInfo, "Algoritmo:", algoritmo, fontNormal);
            addRowToTable(tableInfo, "Dirección:", radioAscendente.isSelected() ? "Ascendente" : "Descendente",
                    fontNormal);
            addRowToTable(tableInfo, "Velocidad:", (String) comboVelocidad.getSelectedItem(), fontNormal);
            addRowToTable(tableInfo, "Tiempo total:", obtenerTiempoFormateado(), fontNormal);
            addRowToTable(tableInfo, "Total de pasos:", String.valueOf(contadorPasos), fontNormal);

            document.add(tableInfo);

            // Datos mínimo y máximo
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            String catMin = "";
            String catMax = "";

            for (DataPoint dp : datosActuales) {
                if (dp.getCount() < min) {
                    min = dp.getCount();
                    catMin = dp.getCategory();
                }
                if (dp.getCount() > max) {
                    max = dp.getCount();
                    catMax = dp.getCategory();
                }
            }

            document.add(new Paragraph("\nDato mínimo: " + catMin + " (" + min + ")", fontNormal));
            document.add(new Paragraph("Dato máximo: " + catMax + " (" + max + ")", fontNormal));

            // Título para los datos originales
            Paragraph originalTitle = new Paragraph("DATOS ORIGINALES", fontSubtitle);
            originalTitle.setAlignment(Element.ALIGN_CENTER);
            originalTitle.setSpacingBefore(20);
            originalTitle.setSpacingAfter(15);
            document.add(originalTitle);

            // Tabla con datos originales
            PdfPTable tableOriginal = new PdfPTable(2);
            tableOriginal.setWidthPercentage(80);
            tableOriginal.setHorizontalAlignment(Element.ALIGN_CENTER);

            // Encabezados de tabla
            PdfPCell headerCell1 = new PdfPCell(new Phrase("Categoría", fontNormal));
            headerCell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            tableOriginal.addCell(headerCell1);

            PdfPCell headerCell2 = new PdfPCell(new Phrase("Valor", fontNormal));
            headerCell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            tableOriginal.addCell(headerCell2);

            // Datos originales
            for (DataPoint dp : datosOriginales) {
                PdfPCell cell1 = new PdfPCell(new Phrase(dp.getCategory(), fontNormal));
                tableOriginal.addCell(cell1);

                PdfPCell cell2 = new PdfPCell(new Phrase(String.valueOf(dp.getCount()), fontNormal));
                cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tableOriginal.addCell(cell2);
            }

            document.add(tableOriginal);

            // Agregar la imagen original
            try {
                if (imagenInicial != null) {
                    document.add(new Paragraph("\n"));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(imagenInicial, "png", baos);
                    Image img = Image.getInstance(baos.toByteArray());

                    // Escalar imagen para que se ajuste a la página
                    float width = document.getPageSize().getWidth() * 0.8f;
                    img.scaleToFit(width, 300);
                    img.setAlignment(Element.ALIGN_CENTER);

                    document.add(img);
                }
            } catch (Exception e) {
                document.add(new Paragraph("\nNo se pudo incluir la gráfica original: " + e.getMessage(), fontNormal));
                e.printStackTrace();
            }

            // Título para los datos ordenados
            Paragraph orderedTitle = new Paragraph("DATOS ORDENADOS", fontSubtitle);
            orderedTitle.setAlignment(Element.ALIGN_CENTER);
            orderedTitle.setSpacingBefore(30);
            orderedTitle.setSpacingAfter(15);
            document.add(orderedTitle);

            // Tabla con datos ordenados
            PdfPTable tableOrdered = new PdfPTable(2);
            tableOrdered.setWidthPercentage(80);
            tableOrdered.setHorizontalAlignment(Element.ALIGN_CENTER);

            // Encabezados de tabla
            PdfPCell headerCell1Copy = new PdfPCell(new Phrase("Categoría", fontNormal));
            headerCell1Copy.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell headerCell2Copy = new PdfPCell(new Phrase("Valor", fontNormal));
            headerCell2Copy.setHorizontalAlignment(Element.ALIGN_CENTER);

            tableOrdered.addCell(headerCell1Copy);
            tableOrdered.addCell(headerCell2Copy);

            // Datos ordenados
            for (DataPoint dp : datosActuales) {
                PdfPCell cell1 = new PdfPCell(new Phrase(dp.getCategory(), fontNormal));
                tableOrdered.addCell(cell1);

                PdfPCell cell2 = new PdfPCell(new Phrase(String.valueOf(dp.getCount()), fontNormal));
                cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tableOrdered.addCell(cell2);
            }

            document.add(tableOrdered);

            // Agregar la imagen final (actual)
            try {
                document.add(new Paragraph("\n"));
                BufferedImage imagenFinal = capturarGrafica(panelVisualizacion);
                if (imagenFinal != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(imagenFinal, "png", baos);
                    Image img = Image.getInstance(baos.toByteArray());

                    // Escalar imagen para que se ajuste a la página
                    float width = document.getPageSize().getWidth() * 0.8f;
                    img.scaleToFit(width, 300);
                    img.setAlignment(Element.ALIGN_CENTER);

                    document.add(img);
                }
            } catch (Exception e) {
                document.add(new Paragraph("\nNo se pudo incluir la gráfica ordenada: " + e.getMessage(), fontNormal));
                e.printStackTrace();
            }

            // Cerrar el documento
            document.close();

            // Mostrar mensaje de éxito
            JOptionPane.showMessageDialog(this,
                    "Reporte generado exitosamente en:\n" + archivoReporte.getAbsolutePath(),
                    "Reporte Generado",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al generar reporte: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método auxiliar para añadir filas a la tabla de información
    private void addRowToTable(PdfPTable table, String label, String value, Font font) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, font));
        cell1.setBorder(PdfPCell.NO_BORDER);
        table.addCell(cell1);

        PdfPCell cell2 = new PdfPCell(new Phrase(value, font));
        cell2.setBorder(PdfPCell.NO_BORDER);
        table.addCell(cell2);
    }

    /* ALGORITMOS DE ORDENAMIENTO */

    // Bubble Sort
    private void bubbleSort(boolean ascendente) {
        int n = datosActuales.length;

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                // Verificar si debemos continuar
                if (cancelado)
                    return;

                boolean debeIntercambiar;
                if (ascendente) {
                    debeIntercambiar = datosActuales[j].getCount() > datosActuales[j + 1].getCount();
                } else {
                    debeIntercambiar = datosActuales[j].getCount() < datosActuales[j + 1].getCount();
                }

                if (debeIntercambiar) {
                    // Intercambiar elementos
                    DataPoint temp = datosActuales[j];
                    datosActuales[j] = datosActuales[j + 1];
                    datosActuales[j + 1] = temp;

                    // Actualizar contador y visualización
                    contadorPasos++;
                    SwingUtilities.invokeLater(this::actualizarVisualizacion);

                    // Pausar para mostrar el progreso
                    pausar();
                }
            }
        }
    }

    // Insert Sort
    private void insertSort(boolean ascendente) {
        int n = datosActuales.length;

        for (int i = 1; i < n; i++) {
            DataPoint key = datosActuales[i];
            int j = i - 1;

            if (ascendente) {
                while (j >= 0 && datosActuales[j].getCount() > key.getCount()) {
                    // Verificar si debemos continuar
                    if (cancelado)
                        return;

                    datosActuales[j + 1] = datosActuales[j];
                    j = j - 1;

                    // Actualizar contador y visualización
                    contadorPasos++;
                    SwingUtilities.invokeLater(this::actualizarVisualizacion);

                    // Pausar para mostrar el progreso
                    pausar();
                }
            } else {
                while (j >= 0 && datosActuales[j].getCount() < key.getCount()) {
                    // Verificar si debemos continuar
                    if (cancelado)
                        return;

                    datosActuales[j + 1] = datosActuales[j];
                    j = j - 1;

                    // Actualizar contador y visualización
                    contadorPasos++;
                    SwingUtilities.invokeLater(this::actualizarVisualizacion);

                    // Pausar para mostrar el progreso
                    pausar();
                }
            }

            datosActuales[j + 1] = key;

            // Actualizar visualización una vez más después de insertar la clave
            contadorPasos++;
            SwingUtilities.invokeLater(this::actualizarVisualizacion);
            pausar();
        }
    }

    // Select Sort
    private void selectSort(boolean ascendente) {
        int n = datosActuales.length;

        for (int i = 0; i < n - 1; i++) {
            int indice = i;

            for (int j = i + 1; j < n; j++) {
                // Verificar si debemos continuar
                if (cancelado)
                    return;

                boolean comparacion;
                if (ascendente) {
                    comparacion = datosActuales[j].getCount() < datosActuales[indice].getCount();
                } else {
                    comparacion = datosActuales[j].getCount() > datosActuales[indice].getCount();
                }

                if (comparacion) {
                    indice = j;
                }
            }

            // Intercambiar el elemento mínimo/máximo encontrado con el primero
            if (indice != i) {
                DataPoint temp = datosActuales[indice];
                datosActuales[indice] = datosActuales[i];
                datosActuales[i] = temp;

                // Actualizar contador y visualización
                contadorPasos++;
                SwingUtilities.invokeLater(this::actualizarVisualizacion);

                // Pausar para mostrar el progreso
                pausar();
            }
        }
    }

    // Merge Sort
    private void mergeSort(int izq, int der, DataPoint[] aux, boolean ascendente) {
        // Verificar si debemos continuar
        if (cancelado)
            return;

        if (izq < der) {
            // Encontrar el punto medio
            int medio = izq + (der - izq) / 2;

            // Ordenar primera y segunda mitad
            mergeSort(izq, medio, aux, ascendente);
            mergeSort(medio + 1, der, aux, ascendente);

            // Combinar las mitades ordenadas
            merge(izq, medio, der, aux, ascendente);
        }
    }

    private void merge(int izq, int medio, int der, DataPoint[] aux, boolean ascendente) {
        // Verificar si debemos continuar
        if (cancelado)
            return;

        // Copiar los datos a un array auxiliar
        for (int i = izq; i <= der; i++) {
            aux[i] = datosActuales[i];
        }

        int i = izq;
        int j = medio + 1;
        int k = izq;

        // Combinar los arrays
        while (i <= medio && j <= der) {
            boolean comparacion;
            if (ascendente) {
                comparacion = aux[i].getCount() <= aux[j].getCount();
            } else {
                comparacion = aux[i].getCount() >= aux[j].getCount();
            }

            if (comparacion) {
                datosActuales[k] = aux[i];
                i++;
            } else {
                datosActuales[k] = aux[j];
                j++;
            }
            k++;

            // Actualizar contador y visualización
            contadorPasos++;
            SwingUtilities.invokeLater(this::actualizarVisualizacion);

            // Pausar para mostrar el progreso
            pausar();
        }

        // Copiar los elementos restantes del primer subarray
        while (i <= medio) {
            datosActuales[k] = aux[i];
            i++;
            k++;

            // Actualizar contador y visualización
            contadorPasos++;
            SwingUtilities.invokeLater(this::actualizarVisualizacion);
            pausar();
        }

        // Copiar los elementos restantes del segundo subarray
        while (j <= der) {
            datosActuales[k] = aux[j];
            j++;
            k++;

            // Actualizar contador y visualización
            contadorPasos++;
            SwingUtilities.invokeLater(this::actualizarVisualizacion);
            pausar();
        }
    }

    // Quick Sort
    private void quickSort(int bajo, int alto, boolean ascendente) {
        // Verificar si debemos continuar
        if (cancelado)
            return;

        if (bajo < alto) {
            // Encontrar el índice de partición
            int pi = partition(bajo, alto, ascendente);

            // Ordenar elementos antes y después de la partición
            quickSort(bajo, pi - 1, ascendente);
            quickSort(pi + 1, alto, ascendente);
        }
    }

    private int partition(int bajo, int alto, boolean ascendente) {
        // Verificar si debemos continuar
        if (cancelado)
            return bajo;

        // Elemento pivote
        DataPoint pivote = datosActuales[alto];
        int i = (bajo - 1);

        for (int j = bajo; j < alto; j++) {
            boolean comparacion;
            if (ascendente) {
                comparacion = datosActuales[j].getCount() <= pivote.getCount();
            } else {
                comparacion = datosActuales[j].getCount() >= pivote.getCount();
            }

            if (comparacion) {
                i++;

                // Intercambiar elementos
                DataPoint temp = datosActuales[i];
                datosActuales[i] = datosActuales[j];
                datosActuales[j] = temp;

                // Actualizar contador y visualización
                contadorPasos++;
                SwingUtilities.invokeLater(this::actualizarVisualizacion);

                // Pausar para mostrar el progreso
                pausar();
            }
        }

        // Intercambiar el pivote con el elemento en (i + 1)
        DataPoint temp = datosActuales[i + 1];
        datosActuales[i + 1] = datosActuales[alto];
        datosActuales[alto] = temp;

        // Actualizar contador y visualización
        contadorPasos++;
        SwingUtilities.invokeLater(this::actualizarVisualizacion);
        pausar();

        return i + 1;
    }

    // Shell Sort
    private void shellSort(boolean ascendente) {
        int n = datosActuales.length;

        // Iniciar con un gap grande y reducirlo
        for (int gap = n / 2; gap > 0; gap /= 2) {
            // Verificar si debemos continuar
            if (cancelado)
                return;

            // Realizar insertion sort para este tamaño de gap
            for (int i = gap; i < n; i++) {
                DataPoint temp = datosActuales[i];

                int j;
                if (ascendente) {
                    for (j = i; j >= gap && datosActuales[j - gap].getCount() > temp.getCount(); j -= gap) {
                        // Verificar si debemos continuar
                        if (cancelado)
                            return;

                        datosActuales[j] = datosActuales[j - gap];

                        // Actualizar contador y visualización
                        contadorPasos++;
                        SwingUtilities.invokeLater(this::actualizarVisualizacion);

                        // Pausar para mostrar el progreso
                        pausar();
                    }
                } else {
                    for (j = i; j >= gap && datosActuales[j - gap].getCount() < temp.getCount(); j -= gap) {
                        // Verificar si debemos continuar
                        if (cancelado)
                            return;

                        datosActuales[j] = datosActuales[j - gap];

                        // Actualizar contador y visualización
                        contadorPasos++;
                        SwingUtilities.invokeLater(this::actualizarVisualizacion);

                        // Pausar para mostrar el progreso
                        pausar();
                    }
                }

                datosActuales[j] = temp;

                // Actualizar visualización después de insertar temp
                contadorPasos++;
                SwingUtilities.invokeLater(this::actualizarVisualizacion);
                pausar();
            }
        }
    }

    private BufferedImage capturarGrafica(BarChartPanel panel) {
        BufferedImage image = new BufferedImage(
                panel.getWidth(),
                panel.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        panel.paint(g2d);
        g2d.dispose();
        return image;
    }
}
