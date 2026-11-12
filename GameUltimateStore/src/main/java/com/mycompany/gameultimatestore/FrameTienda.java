package com.mycompany.gameultimatestore;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FrameTienda extends JFrame {

    final Compra compra = new Compra();
    private final JLabel resumen = new JLabel();

    public FrameTienda() {
        super("GameStoreUltimate - Catálogo");

        Estilo.ventana(this);
        setSize(1150, 800);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        FondoLabel fondo = new FondoLabel(Estilo.imagen(Estilo.FONDO));
        fondo.setLayout(new BorderLayout());

        JPanel base = Estilo.transparente(new BorderLayout(0, 16));

        JLabel titulo = new JLabel("GameStoreUltimate");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 36));

        JLabel subtitulo = new JLabel("Catálogo de juegos  |  Elige tu próxima aventura");
        subtitulo.setForeground(new Color(205, 175, 255));
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 16));

        JPanel encabezado = Estilo.transparente(new GridLayout(0, 1, 0, 2));
        encabezado.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        encabezado.add(titulo);
        encabezado.add(subtitulo);
        base.add(encabezado, BorderLayout.NORTH);

        Juego[] datos = {
            new Juego("GTA VI", "350000", "GTAVI.jpg",
                "PlayStation", "Xbox"),
            new Juego("Minecraft", "100000", "Minecraft.jpg",
                "PC", "PlayStation", "Xbox"),
            new Juego("ARK: Survival Ascended", "240000", "ARKSA.jpg",
                "PC", "PlayStation", "Xbox"),
            new Juego("EA FC 27", "270000", "FC27.jpg",
                "PC", "PlayStation", "Xbox"),
            new Juego("Call of Duty: Modern Warfare 4", "310000", "CODMW4.jpg",
                "PC", "PlayStation", "Xbox"),
            new Juego("Planet Coaster 2", "250000", "PlanetCoaster2.jpg",
                "PC", "PlayStation", "Xbox")
        };

        JPanel cuadricula = Estilo.transparente(new GridLayout(2, 3, 18, 18));
        cuadricula.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        for (Juego juego : datos) {
            cuadricula.add(new TarjetaJuego(juego, () -> abrirSeleccion(juego)));
        }
        base.add(cuadricula, BorderLayout.CENTER);

        JPanel pie = Estilo.vidrio(new BorderLayout(10, 10));
        resumen.setForeground(Color.WHITE);
        resumen.setFont(new Font("Arial", Font.BOLD, 17));
        pie.add(resumen, BorderLayout.WEST);

        JButton factura = Estilo.boton("Ir a facturación");
        factura.addActionListener(e -> facturar());
        pie.add(factura, BorderLayout.EAST);
        base.add(pie, BorderLayout.SOUTH);

        fondo.add(base, BorderLayout.CENTER);
        setContentPane(fondo);
        actualizar();
    }

    private void abrirSeleccion(Juego juego) {
        FrameSeleccion seleccion = new FrameSeleccion(this, juego);
        seleccion.setVisible(true);
        setVisible(false);
    }

    void actualizar() {
        resumen.setText(
            "Productos: " + compra.unidades()
            + "   |   Total: " + Estilo.precio(compra.total())
        );
    }

    void volver() {
        actualizar();
        setVisible(true);
    }

    void facturar() {
        if (compra.estaVacia()) {
            JOptionPane.showMessageDialog(this, "Primero agrega un juego.");
            return;
        }
        FrameFacturacion facturacion = new FrameFacturacion(this);
        facturacion.setVisible(true);
        setVisible(false);
    }
}

class FondoLabel extends JLabel {

    private final BufferedImage imagen;

    FondoLabel(BufferedImage imagen) {
        this.imagen = imagen;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (imagen != null) {
            Estilo.cubrir(g2, imagen, 0, 0, getWidth(), getHeight());
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else {
            g2.setPaint(new GradientPaint(0, 0, new Color(10, 20, 45),
                getWidth(), getHeight(), new Color(45, 15, 75)));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.dispose();
    }
}

class TarjetaJuego extends JPanel {

    TarjetaJuego(Juego juego, Runnable accion) {
        super(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(new ImagenJuego(juego, accion), BorderLayout.CENTER);

        JPanel precioYBoton = new JPanel(new BorderLayout(8, 0));
        precioYBoton.setOpaque(false);
        precioYBoton.add(new TextoAjustado(Estilo.precio(juego.getPrecio()), 17, true,
            new Color(205, 175, 255)), BorderLayout.CENTER);

        JButton elegir = Estilo.boton("Seleccionar");
        elegir.addActionListener(e -> accion.run());
        precioYBoton.add(elegir, BorderLayout.EAST);

        JPanel info = new JPanel(new BorderLayout(0, 4));
        info.setOpaque(false);
        info.add(new TextoAjustado(juego.getNombre(), 19, true, Color.WHITE), BorderLayout.NORTH);
        info.add(precioYBoton, BorderLayout.CENTER);

        add(info, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(19, 18, 35, 210));
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
        g2.setColor(new Color(130, 80, 210, 170));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
        g2.dispose();
    }
}

class ImagenJuego extends JComponent {

    private final Juego juego;
    private final BufferedImage imagen;

    ImagenJuego(Juego juego, Runnable accion) {
        this.juego = juego;
        this.imagen = Estilo.imagen(Estilo.IMG + juego.getImagen());
        setPreferredSize(new Dimension(100, 100));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                accion.run();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();
        g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 18, 18));

        if (imagen != null) {
            Estilo.cubrir(g2, imagen, 0, 0, w, h);
        } else {
            g2.setPaint(new GradientPaint(0, 0, new Color(91, 25, 170),
                w, h, new Color(40, 20, 80)));
            g2.fillRect(0, 0, w, h);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(juego.getNombre(),
                Math.max(8, (w - fm.stringWidth(juego.getNombre())) / 2), h / 2);
        }
        g2.dispose();
    }
}

class TextoAjustado extends JComponent {

    private final String texto;
    private final int tamano;
    private final boolean negrita;
    private final Color color;

    TextoAjustado(String texto, int tamano, boolean negrita, Color color) {
        this.texto = texto;
        this.tamano = tamano;
        this.negrita = negrita;
        this.color = color;
    }

    private Font fuente(int size) {
        return new Font("Arial", negrita ? Font.BOLD : Font.PLAIN, size);
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(fuente(tamano));
        return new Dimension(fm.stringWidth(texto) + 4, tamano + 12);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(10, tamano + 12);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int size = tamano;
        FontMetrics fm = g2.getFontMetrics(fuente(size));
        while (size > 10 && fm.stringWidth(texto) > getWidth() - 4) {
            size--;
            fm = g2.getFontMetrics(fuente(size));
        }

        g2.setFont(fuente(size));
        g2.setColor(color);
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(texto, 2, y);
        g2.dispose();
    }
}

class BotonRedondo extends JButton {

    private boolean encima;
    private boolean presionado;

    BotonRedondo(String texto) {
        super(texto);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(new Font("Arial", Font.BOLD, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                encima = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                encima = false;
                presionado = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                presionado = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                presionado = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color color = new Color(91, 25, 170);
        if (presionado) {
            color = new Color(60, 15, 120);
        } else if (encima) {
            color = new Color(125, 55, 215);
        }
        g2.setColor(color);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
        g2.dispose();
        super.paintComponent(g);
    }
}

/* ===== Abstracción: contratos ===== */

interface Facturable {

    String getDescripcion();

    BigDecimal subtotal();
}

abstract class Producto {

    private final String nombre;
    private final BigDecimal precio;
    private final String imagen;

    Producto(String nombre, BigDecimal precio, String imagen) {
        this.nombre = nombre;
        this.precio = precio;
        this.imagen = imagen;
    }

    String getNombre() {
        return nombre;
    }

    BigDecimal getPrecio() {
        return precio;
    }

    String getImagen() {
        return imagen;
    }

    abstract String getTipo();

    @Override
    public String toString() {
        return getTipo() + ": " + nombre;
    }
}

/* ===== Herencia: Juego extiende Producto ===== */

class Juego extends Producto {

    private final String[] plataformas;

    Juego(String nombre, String precio, String imagen, String... plataformas) {
        super(nombre, new BigDecimal(precio), imagen);
        this.plataformas = plataformas.clone();
    }

    String[] getPlataformas() {
        return plataformas.clone();
    }

    boolean ofrece(String plataforma) {
        for (String disponible : plataformas) {
            if (disponible.equals(plataforma)) {
                return true;
            }
        }
        return false;
    }

    @Override
    String getTipo() {
        return "Videojuego";
    }
}

/* ===== Polimorfismo: Linea se usa como Facturable ===== */

class Linea implements Facturable {

    private final Juego juego;
    private final String plataforma;
    private int cantidad;

    Linea(Juego juego, String plataforma, int cantidad) {
        this.juego = juego;
        this.plataforma = plataforma;
        this.cantidad = cantidad;
    }

    Juego getJuego() {
        return juego;
    }

    String getPlataforma() {
        return plataforma;
    }

    int getCantidad() {
        return cantidad;
    }

    void aumentar(int unidades) {
        cantidad += unidades;
    }

    @Override
    public String getDescripcion() {
        return juego.getNombre() + " - " + plataforma;
    }

    @Override
    public BigDecimal subtotal() {
        return juego.getPrecio().multiply(BigDecimal.valueOf(cantidad));
    }
}

/* ===== Encapsulamiento: datos privados y acceso controlado ===== */

class Compra {

    private final ArrayList<Linea> lineas = new ArrayList<>();
    private String nombre = "";
    private String documento = "";
    private String correo = "";
    private String direccion = "";
    private int numero = 1;

    void agregar(Juego juego, String plataforma, int cantidad) {
        for (Linea linea : lineas) {
            if (linea.getJuego() == juego && linea.getPlataforma().equals(plataforma)) {
                linea.aumentar(cantidad);
                return;
            }
        }
        lineas.add(new Linea(juego, plataforma, cantidad));
    }

    void eliminar(int indice) {
        lineas.remove(indice);
    }

    boolean estaVacia() {
        return lineas.isEmpty();
    }

    List<Linea> getLineas() {
        return Collections.unmodifiableList(lineas);
    }

    int siguienteNumero() {
        return numero++;
    }

    int unidades() {
        int unidades = 0;
        for (Linea linea : lineas) {
            unidades += linea.getCantidad();
        }
        return unidades;
    }

    BigDecimal total() {
        BigDecimal total = BigDecimal.ZERO;
        for (Facturable item : lineas) {
            total = total.add(item.subtotal());
        }
        return total;
    }

    String getNombre() {
        return nombre;
    }

    String getDocumento() {
        return documento;
    }

    String getCorreo() {
        return correo;
    }

    String getDireccion() {
        return direccion;
    }

    void setCliente(String nombre, String documento, String correo, String direccion) {
        this.nombre = nombre;
        this.documento = documento;
        this.correo = correo;
        this.direccion = direccion;
    }
}

class Estilo {

    static final String IMG = "/img/";
    static final String FONDO = IMG + "FondoDePantalla.jpeg";

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();
    private static final DecimalFormat PESOS;

    static {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setGroupingSeparator('.');
        PESOS = new DecimalFormat("#,##0", simbolos);
    }

    static void ventana(JFrame ventana) {
        ventana.setSize(1050, 740);
        ventana.setMinimumSize(new Dimension(800, 600));
        ventana.setLocationRelativeTo(null);
    }

    static BufferedImage imagen(String ruta) {
        if (CACHE.containsKey(ruta)) {
            return CACHE.get(ruta);
        }
        BufferedImage resultado = null;
        try (java.io.InputStream entrada = Estilo.class.getResourceAsStream(ruta)) {
            if (entrada != null) {
                resultado = ImageIO.read(entrada);
            }
        } catch (java.io.IOException ex) {
            resultado = null;
        }
        CACHE.put(ruta, resultado);
        return resultado;
    }

    static void cubrir(Graphics2D g2, BufferedImage img, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        double escala = Math.max((double) w / img.getWidth(), (double) h / img.getHeight());
        int dw = (int) Math.ceil(img.getWidth() * escala);
        int dh = (int) Math.ceil(img.getHeight() * escala);
        g2.drawImage(img, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, null);
    }

    static JPanel panel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(new Color(19, 18, 35));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return panel;
    }

    static JPanel transparente(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        return panel;
    }

    static JPanel vidrio(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(19, 18, 35, 210));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        return panel;
    }

    static JLabel texto(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(Color.WHITE);
        etiqueta.setFont(new Font("Arial", Font.PLAIN, 16));
        return etiqueta;
    }

    static JLabel titulo(String texto) {
        JLabel etiqueta = texto(texto);
        etiqueta.setFont(new Font("Arial", Font.BOLD, 26));
        return etiqueta;
    }

    static JButton boton(String texto) {
        return new BotonRedondo(texto);
    }

    static String precio(BigDecimal cantidad) {
        return "$" + PESOS.format(cantidad) + " COP";
    }
}