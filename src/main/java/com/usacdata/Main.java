package com.usacdata;

import com.usacdata.view.MainView;

public class Main {
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new MainView().setVisible(true);
        });
    }
}