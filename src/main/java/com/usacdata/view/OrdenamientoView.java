package com.usacdata.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OrdenamientoView extends JDialog {

    private JPanel panelPrincipal;
    private JPanel panelSeleccion;
    private JPanel panelInsercion;
    private JPanel panelBurbuja;
    private JTabbedPane pestanas;

    private JComboBox<String> comboTiposDatos;
    private JTextArea areaResultados;
    private JButton btnGenerar;
    private JButton btnOrdenar;

    public OrdenamientoView(Frame owner) {
        super(owner, "Métodos de Ordenamiento", true);
        setSize(700, 500);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BorderLayout());

        // Panel superior con controles comunes
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new FlowLayout());

        JLabel lblTipoDatos = new JLabel("Tipo de datos:");
        comboTiposDatos = new JComboBox<>(new String[] { "Números", "Cadenas", "Objetos" });

        btnGenerar = new JButton("Generar Datos");
        btnGenerar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generarDatos();
            }
        });

        panelSuperior.add(lblTipoDatos);
        panelSuperior.add(comboTiposDatos);
        panelSuperior.add(btnGenerar);

        // Creación de pestañas para los diferentes métodos
        pestanas = new JTabbedPane();

        // Panel para Ordenamiento por Selección
        panelSeleccion = crearPanelMetodo("Ordenamiento por Selección",
                "Este método funciona seleccionando repetidamente el elemento más pequeño\n" +
                        "de la parte no ordenada y moviéndolo al principio.");
        pestanas.addTab("Selección", panelSeleccion);

        // Panel para Ordenamiento por Inserción
        panelInsercion = crearPanelMetodo("Ordenamiento por Inserción",
                "Este método funciona tomando elementos uno por uno e\n" +
                        "insertándolos en la posición correcta en una lista ordenada.");
        pestanas.addTab("Inserción", panelInsercion);

        // Panel para Ordenamiento Burbuja
        panelBurbuja = crearPanelMetodo("Ordenamiento Burbuja",
                "Este método funciona comparando elementos adyacentes repetidamente\n" +
                        "e intercambiándolos si están en el orden incorrecto.");
        pestanas.addTab("Burbuja", panelBurbuja);

        // Panel inferior con área de resultados
        JPanel panelInferior = new JPanel(new BorderLayout());
        areaResultados = new JTextArea(10, 40);
        areaResultados.setEditable(false);
        JScrollPane scrollResultados = new JScrollPane(areaResultados);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnOrdenar = new JButton("Ordenar");
        btnOrdenar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ordenarDatos();
            }
        });
        panelBotones.add(btnOrdenar);

        panelInferior.add(new JLabel("Resultados:"), BorderLayout.NORTH);
        panelInferior.add(scrollResultados, BorderLayout.CENTER);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);

        // Añadir todo al panel principal
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(pestanas, BorderLayout.CENTER);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        // Añadir panel principal al diálogo
        getContentPane().add(panelPrincipal);
    }

    private JPanel crearPanelMetodo(String titulo, String descripcion) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JTextArea txtDescripcion = new JTextArea(descripcion);
        txtDescripcion.setEditable(false);
        txtDescripcion.setBackground(panel.getBackground());
        txtDescripcion.setMargin(new Insets(10, 10, 10, 10));

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(txtDescripcion, BorderLayout.CENTER);

        return panel;
    }

    private void generarDatos() {
        // Aquí iría la lógica para generar datos según el tipo seleccionado
        String tipoDatos = (String) comboTiposDatos.getSelectedItem();
        areaResultados.setText("Datos generados para tipo: " + tipoDatos + "\n");

        // Ejemplo de datos generados
        if (tipoDatos.equals("Números")) {
            areaResultados.append("45, 23, 78, 12, 90, 34, 56, 8, 67, 25");
        } else if (tipoDatos.equals("Cadenas")) {
            areaResultados.append("manzana, naranja, pera, uva, kiwi, fresa, piña, melón, sandía, limón");
        } else {
            areaResultados.append("Objeto1[val=45], Objeto2[val=23], Objeto3[val=78], Objeto4[val=12]");
        }
    }

    private void ordenarDatos() {
        // Aquí iría la lógica para ordenar según el método seleccionado
        String metodo = pestanas.getTitleAt(pestanas.getSelectedIndex());
        String tipoDatos = (String) comboTiposDatos.getSelectedItem();

        areaResultados.append("\n\nOrdenando datos tipo " + tipoDatos + " con método " + metodo + "...\n");

        // Simulación de ordenamiento completado (lógica real a implementar después)
        if (tipoDatos.equals("Números")) {
            areaResultados.append("8, 12, 23, 25, 34, 45, 56, 67, 78, 90");
        } else if (tipoDatos.equals("Cadenas")) {
            areaResultados.append("fresa, kiwi, limón, manzana, melón, naranja, pera, piña, sandía, uva");
        } else {
            areaResultados.append("Objeto4[val=12], Objeto2[val=23], Objeto1[val=45], Objeto3[val=78]");
        }
    }
}
