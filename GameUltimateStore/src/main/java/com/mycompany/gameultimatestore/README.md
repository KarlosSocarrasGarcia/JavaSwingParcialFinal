# GameStoreUltimate

Aplicación de escritorio desarrollada en Java Swing para la gestión de catálogo, selección de plataformas interactivas y facturación de videojuegos.

## Estructura del proyecto

El flujo de navegación se distribuye en cuatro ventanas independientes:

- **FrameBienvenida**: Pantalla de inicio con presentación, logotipo y acceso principal a la tienda.
- **FrameTienda**: Catálogo con visualización de videojuegos en cuadrícula, precios en pesos colombianos (COP), portadas escaladas y acceso directo a la compra.
- **FrameSeleccion**: Vista detallada donde se selecciona la plataforma mediante imágenes interactivas (PlayStation, Xbox o PC) y la cantidad deseada.
- **FrameFacturacion**: Resumen detallado de productos añadidos, formulario de captura de datos del cliente, cálculo del total y generación del comprobante con opciones de impresión y exportación en archivo TXT.

## Requisitos y ejecución

- Java Development Kit (JDK) 8 o superior.
- Apache Maven o cualquier entorno compatible (NetBeans, IntelliJ IDEA, Eclipse).
- Recursos gráficos ubicados en la ruta interna del proyecto: src/main/resources/img/.
- Archivo de inicio del programa: FrameBienvenida.java.

## Pilares de Programación Orientada a Objetos

- **Abstracción**: Definición de contratos y plantillas base mediante la interfaz Facturable y las clases abstractas Producto, VentanaBase, Comprobante y RenderBase.
- **Encapsulamiento**: Atributos con modificador de visibilidad privado en clases de modelo (Producto, Juego, Linea, Compra, SeleccionActual) con acceso mediante métodos consultores y mutadores controlados.
- **Herencia**: Jerarquías de clases gráficas basadas en Swing (VentanaBase extiende JFrame, componentes especializados de JPanel, JComponent y JButton) y de dominio (Juego hereda de Producto, ComprobanteDemostracion hereda de Comprobante).
- **Polimorfismo**: Sobrescritura de comportamiento visual mediante @Override paintComponent, cálculo dinámico de subtotales a través de la interfaz Facturable, y tratamiento uniforme de componentes seleccionables con Seleccionable.
