package com.mycompany.gameultimatestore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/* ===== Abstracción: plantilla común de las ventanas ===== */

abstract class VentanaBase extends JFrame {

    protected final FrameTienda catalogo;

    VentanaBase(String titulo, FrameTienda catalogo) {
        super(titulo);
        this.catalogo = catalogo;

        Estilo.ventana(this);
        setSize(1150, 800);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                alCerrar();
            }
        });
    }

    protected abstract JComponent crearContenido();

    protected final void iniciar() {
        FondoLabel fondo = new FondoLabel(Estilo.imagen(Estilo.FONDO));
        fondo.setLayout(new BorderLayout());
        fondo.add(crearContenido(), BorderLayout.CENTER);
        setContentPane(fondo);
    }

    protected void alCerrar() {
        volver();
    }

    protected void volver() {
        catalogo.volver();
        dispose();
    }
}

/* ===== Encapsulamiento: valores fijos con datos propios ===== */

enum Plataforma {

    PLAYSTATION("PlayStation", "PS.jpg"),
    XBOX("Xbox", "Xbox.jpg"),
    PC("PC", "PC.jpg");

    private final String nombre;
    private final String archivo;

    Plataforma(String nombre, String archivo) {
        this.nombre = nombre;
        this.archivo = archivo;
    }

    String getNombre() {
        return nombre;
    }

    String getArchivo() {
        return archivo;
    }
}

/* ===== Abstracción: contrato de lo que se puede marcar ===== */

interface Seleccionable {

    String getNombre();

    boolean isSeleccionada();

    void setSeleccionada(boolean seleccionada);
}

/* ===== Encapsulamiento + polimorfismo: modelo de la selección ===== */

class SeleccionActual implements Facturable {

    private final Juego juego;
    private Plataforma plataforma;
    private int cantidad = 1;

    SeleccionActual(Juego juego) {
        this.juego = juego;
    }

    void setPlataforma(Plataforma plataforma) {
        if (!juego.ofrece(plataforma.getNombre())) {
            throw new IllegalArgumentException(
                juego.getNombre() + " no está disponible en " + plataforma.getNombre());
        }
        this.plataforma = plataforma;
    }

    void setCantidad(int cantidad) {
        if (cantidad < 1 || cantidad > 99) {
            throw new IllegalArgumentException("La cantidad debe estar entre 1 y 99.");
        }
        this.cantidad = cantidad;
    }

    boolean estaCompleta() {
        return plataforma != null;
    }

    void confirmar(Compra compra) {
        if (!estaCompleta()) {
            throw new IllegalStateException("Falta elegir la plataforma.");
        }
        compra.agregar(juego, plataforma.getNombre(), cantidad);
    }

    @Override
    public String getDescripcion() {
        return estaCompleta()
            ? juego.getNombre() + " - " + plataforma.getNombre()
            : juego.getNombre() + " (sin plataforma)";
    }

    @Override
    public BigDecimal subtotal() {
        return juego.getPrecio().multiply(BigDecimal.valueOf(cantidad));
    }
}

/* ===== Herencia: FrameSeleccion extiende VentanaBase ===== */

public class FrameSeleccion extends VentanaBase {

    private final Juego juego;
    private final SeleccionActual seleccion;
    private final JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final JLabel estado = Estilo.texto("Plataforma elegida: ninguna");
    private final JLabel subtotal = new JLabel();
    private final List<Seleccionable> opciones = new ArrayList<>();

    public FrameSeleccion(FrameTienda catalogo, Juego juego) {
        super("GameStoreUltimate - Selección del juego", catalogo);
        this.juego = juego;
        this.seleccion = new SeleccionActual(juego);
        iniciar();
    }

    @Override
    protected JComponent crearContenido() {
        JPanel base = Estilo.transparente(new BorderLayout(0, 16));

        JLabel titulo = new JLabel("Selección del juego");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 32));
        base.add(titulo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridy = 0;

        JPanel marcoPortada = Estilo.vidrio(new BorderLayout());
        ImagenJuego portada = new ImagenJuego(juego, () -> { });
        portada.setCursor(Cursor.getDefaultCursor());
        marcoPortada.add(portada, BorderLayout.CENTER);
        c.gridx = 0;
        c.weightx = 0.4;
        c.insets = new Insets(0, 0, 0, 18);
        centro.add(marcoPortada, c);

        JPanel detalle = Estilo.vidrio(new GridBagLayout());
        GridBagConstraints d = new GridBagConstraints();
        d.gridx = 0;
        d.weightx = 1;
        d.fill = GridBagConstraints.HORIZONTAL;
        d.anchor = GridBagConstraints.WEST;
        d.insets = new Insets(0, 0, 12, 0);

        d.gridy = 0;
        detalle.add(new TextoAjustado(juego.getNombre(), 32, true, Color.WHITE), d);

        d.gridy = 1;
        detalle.add(new TextoAjustado("Precio unitario: " + Estilo.precio(juego.getPrecio()),
            20, false, new Color(205, 175, 255)), d);

        d.gridy = 2;
        detalle.add(Estilo.texto("Elige tu plataforma:"), d);

        d.gridy = 3;
        d.weighty = 0.6;
        d.fill = GridBagConstraints.BOTH;
        detalle.add(crearFilaPlataformas(), d);

        d.weighty = 0;
        d.fill = GridBagConstraints.HORIZONTAL;
        d.gridy = 4;
        detalle.add(estado, d);

        d.gridy = 5;
        detalle.add(crearFilaCantidad(), d);

        subtotal.setForeground(Color.WHITE);
        subtotal.setFont(new Font("Arial", Font.BOLD, 22));
        actualizarSubtotal();
        d.gridy = 6;
        detalle.add(subtotal, d);

        JPanel relleno = new JPanel();
        relleno.setOpaque(false);
        d.gridy = 7;
        d.weighty = 0.4;
        d.fill = GridBagConstraints.BOTH;
        d.insets = new Insets(0, 0, 0, 0);
        detalle.add(relleno, d);

        c.gridx = 1;
        c.weightx = 0.6;
        c.insets = new Insets(0, 0, 0, 0);
        centro.add(detalle, c);

        base.add(centro, BorderLayout.CENTER);
        base.add(crearBotones(), BorderLayout.SOUTH);
        return base;
    }

    private JPanel crearBotones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);

        JButton atras = Estilo.boton("Volver al catálogo");
        atras.addActionListener(e -> volver());

        JButton agregar = Estilo.boton("Agregar y seguir comprando");
        agregar.addActionListener(e -> {
            if (agregarSeleccion()) {
                volver();
            }
        });

        JButton facturar = Estilo.boton("Agregar e ir a facturación");
        facturar.addActionListener(e -> {
            if (agregarSeleccion()) {
                catalogo.facturar();
                dispose();
            }
        });

        botones.add(atras);
        botones.add(agregar);
        botones.add(facturar);
        return botones;
    }

    private JPanel crearFilaPlataformas() {
        JPanel fila = new JPanel(new GridLayout(1, 0, 14, 0));
        fila.setOpaque(false);

        for (Plataforma plataforma : Plataforma.values()) {
            if (!juego.ofrece(plataforma.getNombre())) {
                continue;
            }
            PlataformaOpcion opcion = new PlataformaOpcion(
                plataforma, elegida -> elegir(plataforma, elegida));
            opciones.add(opcion);
            fila.add(opcion);
        }
        return fila;
    }

    private void elegir(Plataforma plataforma, Seleccionable elegida) {
        seleccion.setPlataforma(plataforma);
        for (Seleccionable opcion : opciones) {
            opcion.setSeleccionada(opcion == elegida);
        }
        estado.setText("Plataforma elegida: " + plataforma.getNombre());
    }

    private JPanel crearFilaCantidad() {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        fila.setOpaque(false);

        JLabel etiqueta = Estilo.texto("Cantidad:");
        etiqueta.setFont(new Font("Arial", Font.PLAIN, 15));

        cantidad.setPreferredSize(new Dimension(60, 26));
        cantidad.setFont(new Font("Arial", Font.PLAIN, 13));
        cantidad.addChangeListener(e -> actualizarSubtotal());

        fila.add(etiqueta);
        fila.add(cantidad);
        return fila;
    }

    private void actualizarSubtotal() {
        seleccion.setCantidad((Integer) cantidad.getValue());
        subtotal.setText("Subtotal: " + Estilo.precio(seleccion.subtotal()));
    }

    private boolean agregarSeleccion() {
        if (!seleccion.estaCompleta()) {
            JOptionPane.showMessageDialog(this, "Selecciona una plataforma.");
            return false;
        }
        try {
            cantidad.commitEdit();
            seleccion.setCantidad((Integer) cantidad.getValue());
        } catch (ParseException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Introduce una cantidad entera entre 1 y 99.");
            return false;
        }
        seleccion.confirmar(catalogo.compra);
        return true;
    }
}

/* ===== Polimorfismo: PlataformaOpcion se usa como Seleccionable ===== */

class PlataformaOpcion extends JComponent implements Seleccionable {

    private final Plataforma plataforma;
    private final BufferedImage imagen;
    private boolean seleccionada;
    private boolean encima;

    PlataformaOpcion(Plataforma plataforma, Consumer<Seleccionable> alElegir) {
        this.plataforma = plataforma;
        this.imagen = Estilo.imagen(Estilo.IMG + plataforma.getArchivo());
        setToolTipText(plataforma.getNombre());
        setPreferredSize(new Dimension(100, 120));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                alElegir.accept(PlataformaOpcion.this);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                encima = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                encima = false;
                repaint();
            }
        });
    }

    @Override
    public String getNombre() {
        return plataforma.getNombre();
    }

    @Override
    public boolean isSeleccionada() {
        return seleccionada;
    }

    @Override
    public void setSeleccionada(boolean seleccionada) {
        this.seleccionada = seleccionada;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();
        g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 22, 22));

        if (imagen != null) {
            Estilo.cubrir(g2, imagen, 0, 0, w, h);
        } else {
            g2.setPaint(new GradientPaint(0, 0, new Color(91, 25, 170),
                w, h, new Color(40, 20, 80)));
            g2.fillRect(0, 0, w, h);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            FontMetrics fm = g2.getFontMetrics();
            String texto = plataforma.getNombre();
            g2.drawString(texto, Math.max(6, (w - fm.stringWidth(texto)) / 2), h / 2);
        }

        if (!seleccionada) {
            g2.setColor(new Color(0, 0, 0, encima ? 40 : 130));
            g2.fillRect(0, 0, w, h);
        }
        g2.setClip(null);

        if (seleccionada) {
            g2.setStroke(new BasicStroke(5f));
            g2.setColor(new Color(190, 120, 255));
            g2.drawRoundRect(2, 2, w - 5, h - 5, 22, 22);

            int d = 28;
            int x = w - d - 10;
            int y = 10;
            g2.setColor(new Color(91, 25, 170));
            g2.fillOval(x, y, d, d);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 7, y + 15, x + 12, y + 20);
            g2.drawLine(x + 12, y + 20, x + 21, y + 9);
        } else {
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(new Color(255, 255, 255, 90));
            g2.drawRoundRect(1, 1, w - 3, h - 3, 22, 22);
        }
        g2.dispose();
    }
}