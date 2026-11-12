package com.mycompany.gameultimatestore;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class FrameBienvenida extends JFrame {

    private final Color MORADO = new Color(91, 25, 170);
    private final Color OSCURO = new Color(25, 25, 40);

    private static final String RECURSO_FONDO = "/img/FondoDePantalla.jpeg";

    public FrameBienvenida() {
        setTitle("GameStoreUltimate");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JLabel fondo = new JLabel();
        fondo.setLayout(new GridBagLayout());
        fondo.setOpaque(true);
        fondo.setBackground(new Color(25, 15, 50));

        URL recurso = FrameBienvenida.class.getResource(RECURSO_FONDO);
        if (recurso != null) {
            ImageIcon original = new ImageIcon(recurso);
            Image escalada = original.getImage()
                .getScaledInstance(1000, 650, Image.SCALE_SMOOTH);
            fondo.setIcon(new ImageIcon(escalada));
        }

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("🎮");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 85));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nombre = new JLabel("GameStoreUltimate");
        nombre.setForeground(Color.WHITE);
        nombre.setFont(new Font("Arial", Font.BOLD, 43));
        nombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel eslogan = new JLabel("DONDE COMIENZA TU PRÓXIMA AVENTURA");
        eslogan.setForeground(new Color(205, 175, 255));
        eslogan.setFont(new Font("Arial", Font.BOLD, 16));
        eslogan.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descripcion = new JLabel("Descubre. Juega. Supera tus límites.");
        descripcion.setForeground(Color.WHITE);
        descripcion.setFont(new Font("Arial", Font.PLAIN, 15));
        descripcion.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton entrar = crearBoton("ENTRAR A LA TIENDA", MORADO);
        entrar.addActionListener(e -> {
            FrameTienda tienda = new FrameTienda();
            tienda.setVisible(true);
            this.dispose();
        });

        JButton salir = crearBoton("SALIR", OSCURO);
        salir.addActionListener(e -> {
            int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas salir de GameStoreUltimate?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION
            );
            if (respuesta == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        contenido.add(logo);
        contenido.add(Box.createVerticalStrut(5));
        contenido.add(nombre);
        contenido.add(Box.createVerticalStrut(12));
        contenido.add(eslogan);
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(descripcion);
        contenido.add(Box.createVerticalStrut(35));
        contenido.add(entrar);
        contenido.add(Box.createVerticalStrut(12));
        contenido.add(salir);

        fondo.add(contenido);
        setContentPane(fondo);
    }

    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setPreferredSize(new Dimension(270, 48));
        boton.setMaximumSize(new Dimension(270, 48));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Arial", Font.BOLD, 16));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        return boton;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrameBienvenida().setVisible(true));
    }
}