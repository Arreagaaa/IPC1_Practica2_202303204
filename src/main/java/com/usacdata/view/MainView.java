package com.usacdata.view;

import com.usacdata.components.BarChartPanel;
import com.usacdata.util.FileReader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainView extends BaseView {

    // Componentes de la interfaz
    private BarChartPanel graphPanel;
    private JTextField filePathField;
    private JTextField fileTitleField;
    private JButton browseButton;
    private JButton acceptButton;
    private JLabel statusLabel;
    private File selectedFile;
    private JPanel panelPrincipal;
    private JButton btnOrdenamientos;

    public MainView() {
        setTitle("Sistema de Gestión de Datos - USAC");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    @Override
    protected void initComponents() {
        panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(20, 20, 20, 20));
        panelPrincipal.setBackground(new Color(240, 240, 240));

        // Panel superior para selección de archivo y título
        JPanel topPanel = createTopPanel();

        // Panel central para la gráfica
        JPanel chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBackground(new Color(240, 240, 240));
        chartContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                        "Visualización de datos",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12),
                        new Color(70, 70, 70)),
                new EmptyBorder(10, 10, 10, 10)));

        graphPanel = new BarChartPanel();
        chartContainer.add(graphPanel, BorderLayout.CENTER);

        // Panel inferior para mensajes de estado
        JPanel statusPanel = createStatusPanel();

        // Agregar los paneles al panel principal
        panelPrincipal.add(topPanel, BorderLayout.NORTH);
        panelPrincipal.add(chartContainer, BorderLayout.CENTER);
        panelPrincipal.add(statusPanel, BorderLayout.SOUTH);

        // Panel inferior para contener el botón de ordenamientos
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new FlowLayout(FlowLayout.RIGHT));

        // Botón de ordenamientos
        btnOrdenamientos = new JButton("Ordenamientos");
        btnOrdenamientos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirVentanaOrdenamientos();
            }
        });

        panelInferior.add(btnOrdenamientos);

        // Añadir paneles al frame principal
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        // Añadir panel principal al frame
        getContentPane().add(panelPrincipal);

        // Configurar acciones de los botones
        setupActions();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                        "Configuración del archivo IBPC1",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12),
                        new Color(70, 70, 70)),
                new EmptyBorder(10, 10, 10, 10)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Etiqueta Archivo
        JLabel fileLabel = new JLabel("Archivo de datos:");
        fileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(fileLabel, gbc);

        // Campo de texto para la ruta del archivo
        filePathField = new JTextField();
        filePathField.setEditable(false);
        filePathField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filePathField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(filePathField, gbc);

        // Botón para buscar archivo
        browseButton = new JButton("Explorar...");
        browseButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        browseButton.setFocusPainted(false);
        browseButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(browseButton, gbc);

        // Etiqueta Título
        JLabel titleLabel = new JLabel("Título del gráfico:");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(titleLabel, gbc);

        // Campo de texto para el título
        fileTitleField = new JTextField();
        fileTitleField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fileTitleField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(fileTitleField, gbc);

        // Botón aceptar
        acceptButton = new JButton("Aceptar");
        acceptButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        acceptButton.setFocusPainted(false);
        acceptButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        acceptButton.setEnabled(false);
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(acceptButton, gbc);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(new EmptyBorder(5, 0, 0, 0));

        statusLabel = new JLabel("Listo");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(statusLabel, BorderLayout.WEST);

        return panel;
    }

    private void setupActions() {
        browseButton.addActionListener(e -> selectFile());
        acceptButton.addActionListener(e -> processFile());
    }

    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo de datos");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Configurar para aceptar solo archivos .ibpc1 (requisito sección B)
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de datos IBPC1 (*.ibpc1)", "ibpc1"));

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());

            // Extraer nombre del archivo como título sugerido (sin extensión)
            String fileName = selectedFile.getName();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                fileName = fileName.substring(0, dotIndex);
            }
            fileTitleField.setText(fileName);

            statusLabel.setText("Archivo seleccionado: " + selectedFile.getName());
            acceptButton.setEnabled(true);
        }
    }

    // En el método processFile(), asegurarse de pasar las etiquetas correctamente
    private void processFile() {
        if (selectedFile == null || !selectedFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor seleccione un archivo válido.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String title = fileTitleField.getText().trim();
        if (title.isEmpty()) {
            title = "Gráfico de datos";
        }

        try {
            statusLabel.setText("Procesando archivo...");

            FileReader fileReader = new FileReader();
            FileReader.FileReaderResult result = fileReader.readDataFromFile(selectedFile);

            if (result != null && result.getDataPoints() != null) {
                // Depuración
                System.out.println("Pasando etiquetas a la gráfica - X: " +
                        result.getXAxisLabel() + ", Y: " + result.getYAxisLabel());

                graphPanel.setData(
                        result.getDataPoints(),
                        result.getXAxisLabel(),
                        result.getYAxisLabel(),
                        title);

                statusLabel.setText("Archivo procesado correctamente: " +
                        result.getDataPoints().length + " registros.");
            } else {
                throw new Exception("No se pudieron leer datos del archivo");
            }
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error al procesar el archivo: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirVentanaOrdenamientos() {
        OrdenamientoView ventanaOrdenamientos = new OrdenamientoView(this);
        ventanaOrdenamientos.setVisible(true);
    }
}