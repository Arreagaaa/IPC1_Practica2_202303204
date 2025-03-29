package com.usacdata.view;

import com.usacdata.components.BarChartPanel;
import com.usacdata.model.DataPoint;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OrdenamientoView extends JDialog {

    private JPanel panelPrincipal;
    private JComboBox<String> comboAlgoritmo;
    private JComboBox<String> comboVelocidad;
    private ButtonGroup grupoDireccion;
    private JRadioButton radioAscendente;
    private JRadioButton radioDescendente;
    private JTextArea areaResultados;
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
    private Timer timerActualizacion;
    private String xAxisLabel;
    private String yAxisLabel;
    private String chartTitle;

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
                        new Font("Segoe UI", Font.BOLD, 12),
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
                        new Font("Segoe UI", Font.BOLD, 12),
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

    private void generarDatosDePrueba() {
        // Aquí deberíamos obtener los datos del gráfico principal
        // Por ahora generamos datos de prueba
        datosOriginales = new DataPoint[10];
        datosOriginales[0] = new DataPoint("A", 45);
        datosOriginales[1] = new DataPoint("B", 23);
        datosOriginales[2] = new DataPoint("C", 78);
        datosOriginales[3] = new DataPoint("D", 12);
        datosOriginales[4] = new DataPoint("E", 90);
        datosOriginales[5] = new DataPoint("F", 34);
        datosOriginales[6] = new DataPoint("G", 56);
        datosOriginales[7] = new DataPoint("H", 8);
        datosOriginales[8] = new DataPoint("I", 67);
        datosOriginales[9] = new DataPoint("J", 25);

        // Clonar los datos para trabajar con ellos
        clonarDatos();

        // Mostrar datos iniciales
        actualizarVisualizacion();
    }

    private void clonarDatos() {
        datosActuales = new DataPoint[datosOriginales.length];
        for (int i = 0; i < datosOriginales.length; i++) {
            datosActuales[i] = new DataPoint(
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
                String algoritmo = (String) comboAlgoritmo.getSelectedItem();
                boolean ascendente = radioAscendente.isSelected();

                switch (algoritmo) {
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
                    pausa = 50; // 50 ms (más rápido)
                    break;
                case "Media":
                    pausa = 200; // 200 ms
                    break;
                case "Baja":
                    pausa = 500; // 500 ms (más lento)
                    break;
                default:
                    pausa = 200;
            }

            Thread.sleep(pausa);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void generarReporte() {
        // Este método será reemplazado con la generación de PDF
        JOptionPane.showMessageDialog(this,
                "Reporte pendiente de implementar con iText",
                "Información",
                JOptionPane.INFORMATION_MESSAGE);
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
}
