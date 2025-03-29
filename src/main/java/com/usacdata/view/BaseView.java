package com.usacdata.view;

import javax.swing.*;
import java.awt.*;

public abstract class BaseView extends JFrame {
    // Propiedades comunes para todas las vistas
    protected final int WIDTH = 900;
    protected final int HEIGHT = 650;
    protected final String APP_TITLE = "USAC Data Analyzer";

    public BaseView() {
        // Configuración básica del frame
        setTitle(APP_TITLE);
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en pantalla
        setMinimumSize(new Dimension(700, 500));

        // Intentar usar look and feel del sistema para un aspecto más moderno
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Error al establecer Look & Feel: " + e.getMessage());
        }

        ImageIcon icon = new ImageIcon("C:/JAVIER_USAC/IPC1_Practica2_202303204/src/resources/icon.png");
        setIconImage(icon.getImage());

        // Inicializar componentes (implementado por las subclases)
        initComponents();
    }

    // Método abstracto que deben implementar todas las subclases
    protected abstract void initComponents();
}