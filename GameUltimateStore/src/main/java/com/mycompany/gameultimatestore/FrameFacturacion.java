package com.mycompany.gameultimatestore;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/* ===== Abstracción + método plantilla: cómo se arma un comprobante ===== */

abstract class Comprobante {

    private final Compra compra;

    Comprobante(Compra compra) {
        this.compra = compra;
    }

    protected abstract String getTitulo();

    protected abstract String getNota();

    final String generar() {
        StringBuilder texto = new StringBuilder("GAMESTOREULTIMATE\n");
        texto.append(getTitulo()).append('\n');
        texto.append("Número de sesión: ").append(compra.siguienteNumero())
            .append("\nFecha: ")
            .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        texto.append("\n\nCliente: ").append(compra.getNombre())
            .append("\nDocumento: ").append(compra.getDocumento())
            .append("\nCorreo: ").append(compra.getCorreo())
            .append("\nDirección: ").append(compra.getDireccion())
            .append("\n\n");

        for (Linea linea : compra.getLineas()) {
            texto.append(linea.getDescripcion())
                .append("\nCantidad: ").append(linea.getCantidad())
                .append(" | Precio: ").append(Estilo.precio(linea.getJuego().getPrecio()))
                .append(" | Subtotal: ").append(Estilo.precio(linea.subtotal()))
                .append("\n\n");
        }

        texto.append("TOTAL: ").append(Estilo.precio(compra.total()))
            .append("\n\n").append(getNota());
        return texto.toString();
    }
}

/* ===== Herencia + polimorfismo: una versión concreta ===== */

class ComprobanteDemostracion extends Comprobante {

    ComprobanteDemostracion(Compra compra) {
        super(compra);
    }

    @Override
    protected String getTitulo() {
        return "COMPROBANTE DE COMPRA";
    }

    @Override
    protected String getNota() {
        return "Valores en pesos colombianos (COP). Impuestos no incluidos."
            + "\nComprobante de demostración. No es factura fiscal ni acredita pago.";
    }
}

/* ===== Encapsulamiento: las reglas de validación en un solo lugar ===== */

class ValidadorCliente {

    private static final String PATRON_CORREO = "[^\\s@]+@[^\\s@]+\\.[^\\s@]+";

    private ValidadorCliente() {
    }

    static String validar(Compra compra) {
        if (compra.estaVacia()) {
            return "La compra está vacía.";
        }
        if (compra.getNombre().isEmpty() || compra.getDocumento().isEmpty()
                || compra.getCorreo().isEmpty() || compra.getDireccion().isEmpty()) {
            return "Completa todos los datos del cliente.";
        }
        if (!compra.getCorreo().matches(PATRON_CORREO)) {
            return "Revisa el correo electrónico.";
        }
        return null;
    }
}

/* ===== Herencia: FrameFacturacion extiende VentanaBase ===== */

public class FrameFacturacion extends VentanaBase {

    private static final Color PANEL_OSCURO = new Color(30, 26, 50);
    private static final Color PANEL_CLARO = new Color(38, 32, 62);
    private static final Color MORADO = new Color(91, 25, 170);
    private static final Color LILA = new Color(205, 175, 255);

    private final Compra compra;
    private final CampoRedondo nombre = new CampoRedondo();
    private final CampoRedondo documento = new CampoRedondo();
    private final CampoRedondo correo = new CampoRedondo();
    private final CampoRedondo direccion = new CampoRedondo();
    private final JLabel total = new JLabel();
    private final DefaultTableModel modelo = new DefaultTableModel(
        new String[]{"Juego", "Plataforma", "Cant.", "Precio", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    private final JTextArea comprobante = new JTextArea() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(150, 140, 180));
                g2.setFont(new Font("Arial", Font.ITALIC, 14));
                g2.drawString("Aquí aparecerá tu comprobante.", 14, 28);
                g2.drawString("Completa tus datos y pulsa", 14, 52);
                g2.drawString("\"Generar comprobante\".", 14, 72);
                g2.dispose();
            }
        }
    };

    public FrameFacturacion(FrameTienda catalogo) {
        super("GameStoreUltimate - Facturación", catalogo);
        this.compra = catalogo.compra;
        iniciar();
    }

    @Override
    protected JComponent crearContenido() {
        JPanel base = Estilo.transparente(new BorderLayout(0, 16));

        JLabel titulo = new JLabel("Facturación");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 36));

        JLabel subtitulo = new JLabel("Revisa tu compra y completa tus datos");
        subtitulo.setForeground(LILA);
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 16));

        JPanel encabezado = new JPanel(new GridLayout(0, 1, 0, 2));
        encabezado.setOpaque(false);
        encabezado.add(titulo);
        encabezado.add(subtitulo);
        base.add(encabezado, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridy = 0;
        c.weighty = 1;

        c.gridx = 0;
        c.weightx = 0.58;
        c.insets = new Insets(0, 0, 0, 18);
        centro.add(crearColumnaIzquierda(), c);

        c.gridx = 1;
        c.weightx = 0.42;
        c.insets = new Insets(0, 0, 0, 0);
        centro.add(crearTarjetaComprobante(), c);

        base.add(centro, BorderLayout.CENTER);
        base.add(crearBotones(), BorderLayout.SOUTH);

        nombre.setText(compra.getNombre());
        documento.setText(compra.getDocumento());
        correo.setText(compra.getCorreo());
        direccion.setText(compra.getDireccion());
        actualizar();

        DocumentListener invalidar = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { comprobante.setText(""); }
            public void removeUpdate(DocumentEvent e) { comprobante.setText(""); }
            public void changedUpdate(DocumentEvent e) { comprobante.setText(""); }
        };
        for (JTextField entrada : new JTextField[]{nombre, documento, correo, direccion}) {
            entrada.getDocument().addDocumentListener(invalidar);
        }
        return base;
    }

    private JPanel crearBotones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);

        JButton atras = Estilo.boton("Volver al catálogo");
        atras.addActionListener(e -> volver());

        JButton guardar = Estilo.boton("Guardar TXT");
        guardar.addActionListener(e -> guardar());

        JButton imprimir = Estilo.boton("Imprimir");
        imprimir.addActionListener(e -> imprimir());

        botones.add(atras);
        botones.add(guardar);
        botones.add(imprimir);
        return botones;
    }

    private JPanel crearColumnaIzquierda() {
        JPanel columna = new JPanel(new GridBagLayout());
        columna.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;

        c.gridy = 0;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 18, 0);
        columna.add(crearTarjetaCompra(), c);

        c.gridy = 1;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 0, 0);
        columna.add(crearTarjetaDatos(), c);
        return columna;
    }

    private JPanel crearTarjetaCompra() {
        JPanel tarjeta = Estilo.vidrio(new BorderLayout(0, 12));
        tarjeta.add(seccion("Tu compra"), BorderLayout.NORTH);

        tabla.setRowHeight(36);
        tabla.setBackground(PANEL_OSCURO);
        tabla.setForeground(Color.WHITE);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionBackground(MORADO);
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setDefaultRenderer(Object.class, new RenderCelda());

        int[] anchos = {220, 110, 50, 130, 130};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setReorderingAllowed(false);
        encabezado.setResizingAllowed(false);
        encabezado.setPreferredSize(new Dimension(0, 38));
        encabezado.setDefaultRenderer(new RenderEncabezado());

        JScrollPane desplazamiento = new JScrollPane(tabla);
        desplazamiento.getViewport().setBackground(PANEL_OSCURO);
        desplazamiento.setBorder(BorderFactory.createLineBorder(new Color(90, 70, 140)));
        tarjeta.add(desplazamiento, BorderLayout.CENTER);

        total.setForeground(Color.WHITE);
        total.setFont(new Font("Arial", Font.BOLD, 26));

        JButton eliminar = Estilo.boton("Eliminar seleccionado");
        eliminar.addActionListener(e -> eliminar());

        JPanel pie = new JPanel(new BorderLayout(10, 0));
        pie.setOpaque(false);
        pie.add(total, BorderLayout.WEST);
        pie.add(eliminar, BorderLayout.EAST);
        tarjeta.add(pie, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearTarjetaDatos() {
        JPanel tarjeta = Estilo.vidrio(new BorderLayout(0, 12));
        tarjeta.add(seccion("Datos del cliente"), BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridLayout(2, 2, 16, 12));
        formulario.setOpaque(false);
        formulario.add(bloque("Nombre / razón social", nombre));
        formulario.add(bloque("Documento / ID fiscal", documento));
        formulario.add(bloque("Correo electrónico", correo));
        formulario.add(bloque("Dirección", direccion));
        tarjeta.add(formulario, BorderLayout.CENTER);

        JLabel aviso = new JLabel("Demostración, no es factura fiscal");
        aviso.setForeground(new Color(170, 160, 200));
        aviso.setFont(new Font("Arial", Font.ITALIC, 13));

        JButton generar = Estilo.boton("Generar comprobante");
        generar.addActionListener(e -> generar());

        JPanel pie = new JPanel(new BorderLayout(10, 0));
        pie.setOpaque(false);
        pie.add(aviso, BorderLayout.WEST);
        pie.add(generar, BorderLayout.EAST);
        tarjeta.add(pie, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearTarjetaComprobante() {
        JPanel tarjeta = Estilo.vidrio(new BorderLayout(0, 12));
        tarjeta.add(seccion("Comprobante"), BorderLayout.NORTH);

        comprobante.setEditable(false);
        comprobante.setLineWrap(true);
        comprobante.setWrapStyleWord(true);
        comprobante.setBackground(PANEL_OSCURO);
        comprobante.setForeground(new Color(235, 230, 250));
        comprobante.setCaretColor(PANEL_OSCURO);
        comprobante.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        comprobante.setMargin(new Insets(12, 14, 12, 14));

        JScrollPane desplazamiento = new JScrollPane(comprobante);
        desplazamiento.getViewport().setBackground(PANEL_OSCURO);
        desplazamiento.setBorder(BorderFactory.createLineBorder(new Color(90, 70, 140)));
        tarjeta.add(desplazamiento, BorderLayout.CENTER);
        return tarjeta;
    }

    private JLabel seccion(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(Color.WHITE);
        etiqueta.setFont(new Font("Arial", Font.BOLD, 21));
        etiqueta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(160, 90, 240)),
            BorderFactory.createEmptyBorder(0, 10, 0, 0)));
        return etiqueta;
    }

    private JPanel bloque(String etiqueta, JTextField campo) {
        JLabel texto = new JLabel(etiqueta);
        texto.setForeground(LILA);
        texto.setFont(new Font("Arial", Font.BOLD, 13));

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.add(texto, BorderLayout.NORTH);
        panel.add(campo, BorderLayout.CENTER);
        return panel;
    }

    private void actualizar() {
        modelo.setRowCount(0);
        for (Linea linea : compra.getLineas()) {
            modelo.addRow(new Object[]{
                linea.getJuego().getNombre(),
                linea.getPlataforma(),
                linea.getCantidad(),
                Estilo.precio(linea.getJuego().getPrecio()),
                Estilo.precio(linea.subtotal())
            });
        }
        total.setText("Total: " + Estilo.precio(compra.total()));
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto.");
            return;
        }
        compra.eliminar(tabla.convertRowIndexToModel(fila));
        comprobante.setText("");
        actualizar();
    }

    private void recordar() {
        compra.setCliente(
            nombre.getText().trim(),
            documento.getText().trim(),
            correo.getText().trim(),
            direccion.getText().trim());
    }

    @Override
    protected void volver() {
        recordar();
        super.volver();
    }

    private void generar() {
        recordar();

        String error = ValidadorCliente.validar(compra);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error);
            return;
        }

        Comprobante generador = new ComprobanteDemostracion(compra);
        comprobante.setText(generador.generar());
        comprobante.setCaretPosition(0);
    }

    private boolean listo() {
        if (comprobante.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero genera el comprobante.");
            return false;
        }
        return true;
    }

    private void guardar() {
        if (!listo()) return;

        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new java.io.File("comprobante.txt"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File archivo = selector.getSelectedFile();
        if (archivo.exists() && JOptionPane.showConfirmDialog(this,
                "¿Reemplazar el archivo existente?", "Confirmar",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Files.write(archivo.toPath(), comprobante.getText().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Comprobante guardado.");
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
        }
    }

    private void imprimir() {
        if (!listo()) return;
        try {
            comprobante.print();
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Error al imprimir: " + ex.getMessage());
        }
    }

    /* ===== Abstracción + herencia: base común de los renderizadores ===== */

    private abstract static class RenderBase extends DefaultTableCellRenderer {

        protected void alinear(int columna) {
            if (columna == 2) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else if (columna >= 3) {
                setHorizontalAlignment(SwingConstants.RIGHT);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }
        }

        protected abstract void estilizar(boolean seleccionada, int fila);

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccionada, boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, seleccionada, false, fila, columna);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            estilizar(seleccionada, fila);
            alinear(columna);
            return this;
        }
    }

    private static class RenderCelda extends RenderBase {
        @Override
        protected void estilizar(boolean seleccionada, int fila) {
            setFont(new Font("Arial", Font.PLAIN, 14));
            setForeground(Color.WHITE);
            setBackground(seleccionada ? MORADO : (fila % 2 == 0 ? PANEL_OSCURO : PANEL_CLARO));
        }
    }

    private static class RenderEncabezado extends RenderBase {
        @Override
        protected void estilizar(boolean seleccionada, int fila) {
            setOpaque(true);
            setBackground(MORADO);
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 14));
        }
    }
}

class CampoRedondo extends JTextField {

    CampoRedondo() {
        setOpaque(false);
        setForeground(Color.WHITE);
        setCaretColor(Color.WHITE);
        setSelectionColor(new Color(125, 55, 215));
        setSelectedTextColor(Color.WHITE);
        setFont(new Font("Arial", Font.PLAIN, 15));
        setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(38, 32, 62));
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        g2.setStroke(new BasicStroke(hasFocus() ? 2f : 1.2f));
        g2.setColor(hasFocus() ? new Color(190, 120, 255) : new Color(95, 75, 145));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
        g2.dispose();
        super.paintComponent(g);
    }
}