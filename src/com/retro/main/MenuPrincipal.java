package com.retro.main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.retro.main.repository.UsuarioRepository;
import com.retro.main.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;

import com.retro.games.snake.SnakeGame;
import com.retro.games.pong.PongGame;
import com.retro.games.puzzle2048.Game2048;
import com.retro.games.tetris.TetrisGame;

//Define que esta es la clase que arranca todo el sistema Spring.
@SpringBootApplication
//Le dice a Spring que busque componentes en todo el proyecto.
@ComponentScan(basePackages = "com.retro") 
//Indica dónde está la clase "Usuario" (la tabla de la BD).
@EntityScan("com.retro.main.model")
//Indica dónde está el "UsuarioRepository" para las consultas SQL.
@EnableJpaRepositories("com.retro.main.repository")
//Marca esta clase como un objeto que Spring debe gestionar.
@Component

//"extends JFrame" significa que esta clase es una ventana.
public class MenuPrincipal extends JFrame {
	// Identificador único para la versión de la clase.
    private static final long serialVersionUID = 1L;
    // La etiqueta donde se verá la hora.
    private JLabel lblReloj;
    
 // "Inyecta" el acceso a la base de datos automáticamente.
    @Autowired
    private UsuarioRepository usuarioRepo;
 // Variable para recordar qué usuario ha iniciado sesión.
    private Usuario usuarioSesion;
    // El panel principal donde pondremos todo.
    private JPanel panelFondo;
    // Un panel pequeño para los iconos de perfil y ajustes.
    private JPanel panelIconos; 
 // Herramienta para cifrar contraseñas.
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

 // Este método se ejecuta al crear la ventana.
    public MenuPrincipal() {
    	// Pone el nombre en la barra superior.
        setTitle("ARCADE OS v2.0 - TFG EDITION");
        // Al cerrar la ventana, el programa se apaga del todo.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     // Define el tamaño (ancho x alto).
        setSize(480, 850);
     // Centra la ventana en la pantalla.
        setLocationRelativeTo(null);
        
     // Creas un panel de dibujo personalizado.
        panelFondo = new JPanel() {
            @Override
         // Método para "pintar" dentro del panel.
            protected void paintComponent(Graphics g) {
            	// Comprueba si el usuario prefiere colores oscuros o claros.
                boolean modoOscuro = (usuarioSesion == null) ? true : usuarioSesion.isDarkMode();
                // Define los dos colores para el degradado.
                Color c1 = modoOscuro ? new Color(10, 10, 15) : new Color(240, 240, 240);
                Color c2 = modoOscuro ? new Color(30, 35, 45) : new Color(190, 190, 190);
                
             // Herramienta de dibujo avanzada.
                Graphics2D g2d = (Graphics2D) g;
             // Define el degradado de color.
                GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
             // Selecciona ese degradado.
                g2d.setPaint(gp);
             // Pinta todo el fondo.
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        /*
         Resumen de Arquitectura de la Interfaz para tu Defensa:
         
"El constructor de la interfaz utiliza un patrón estructural basado en la composición de layouts anidados. 
El contenedor raíz emplea un BoxLayout en el eje vertical para organizar de forma secuencial las secciones primarias de la aplicación. 
Para romper la rigidez de esta distribución vertical y situar elementos en los extremos laterales o en cuadrículas exactas, incrusto subcontenedores regulados por BorderLayout, 
FlowLayout y GridLayout. El control exacto de las distancias se ha delegado a objetos estructurales transparentes Box.createRigidArea, 
evitando el uso de coordenadas fijas (Absolute Positioning) y cumpliendo con las buenas prácticas de diseño de interfaces gráficas adaptables."
         */
        
        // Asigna un gestor de diseño de tipo BoxLayout configurado en el eje vertical (Y_AXIS).
        // Esto obliga a que cada componente que añadas al panel se coloque justo debajo del anterior, 
        // como una torre de bloques.
        panelFondo.setLayout(new BoxLayout(panelFondo, BoxLayout.Y_AXIS));
     // Aplica un borde invisible para dejar márgenes de separación respecto a los bordes de la ventana: 
        // 10 píxeles arriba, 
        // 30 a la izquierda, 15 abajo y 30 a la derecha.
        panelFondo.setBorder(new EmptyBorder(10, 30, 15, 30));

        // Crea un subpanel para la zona superior que utiliza un BorderLayout, ideal para colocar elementos en los extremos (este, oeste).
        JPanel barraEstado = new JPanel(new BorderLayout());
        // Hace que el subpanel sea transparente para que no tape el degradado de colores del fondo principal.
        barraEstado.setOpaque(false);
        // Limita el tamaño máximo de esta barra para que no se estire verticalmente de manera desproporcionada.
        barraEstado.setMaximumSize(new Dimension(500, 25));
        // Crea el texto que indica el estado del sistema.
        JLabel lblStatus = new JLabel(" SYSTEM STATUS: ONLINE");
        // Pone el texto del estado en un color gris apagado.
        lblStatus.setForeground(new Color(100, 100, 100));
        // Aplica una fuente monoespaciada (tipo consola) en negrita con tamaño 11.
        lblStatus.setFont(new Font("Monospaced", Font.BOLD, 11));
        // Instancia el espacio donde irá la hora.
        lblReloj = new JLabel();
        // Pone el texto del reloj en color cian brillante.
        lblReloj.setForeground(Color.CYAN);
        // Aplica la misma fuente de tipo consola pero un punto más grande (12).
        lblReloj.setFont(new Font("Monospaced", Font.BOLD, 12));
        // Llama al método encargado de arrancar el temporizador que actualiza la hora segundo a segundo.
        iniciarReloj();
        // Coloca el texto del estado del sistema en el extremo izquierdo (Oeste).
        barraEstado.add(lblStatus, BorderLayout.WEST);
        // Coloca el reloj en el extremo derecho (Este).
        barraEstado.add(lblReloj, BorderLayout.EAST);
        // Introduce la barra de estado completa al panel principal.
        panelFondo.add(barraEstado);

        // Crea un panel que organiza sus elementos en línea horizontal (FlowLayout), 
        // alineándolos a la derecha (RIGHT) con una separación de 5 píxeles entre ellos.
        panelIconos = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        // Establece la transparencia de este panel.
        panelIconos.setOpaque(false);
        // Fija la altura máxima en 40 píxeles para controlar su espacio vertical.
        panelIconos.setMaximumSize(new Dimension(500, 40));
        
        // Usa la fábrica de botones cuadrados para crear el botón de perfil. 
        // El código interno es una expresión lambda que evalúa: si hay un usuario en sesión, 
        //ejecuta mostrarPerfil(); si es null, 
        // despliega un mensaje de advertencia.
        JButton btnPerfil = crearBotonCuadrado("res/perfil_icon.png", e -> { if(usuarioSesion != null) mostrarPerfil(); else JOptionPane.showMessageDialog(this, "Inicia sesión primero."); });
        // Hace exactamente lo mismo para el botón de configuración, llamando a mostrarAjustes() si el usuario está validado.
        JButton btnAjustes = crearBotonCuadrado("res/ajustes_icon.png", e -> { if(usuarioSesion != null) mostrarAjustes(); else JOptionPane.showMessageDialog(this, "Inicia sesión primero."); });
        
        // Inserta el botón de perfil en el panel horizontal.
        panelIconos.add(btnPerfil);
        // Inserta el botón de ajustes a la derecha del perfil.
        panelIconos.add(btnAjustes);
        // Añade el bloque completo de iconos debajo de la barra de estado.
        panelFondo.add(panelIconos);

        // Inserta un separador invisible de 15 píxeles de alto. Al usar BoxLayout, esto es clave para controlar el espacio entre bloques.
        panelFondo.add(Box.createRigidArea(new Dimension(0, 15)));

        // Crea el texto del título principal de la aplicación.
        JLabel titulo = new JLabel("ARCADE MULTIGAME");
        // Le otorga el color cian característico de tu diseño.
        titulo.setForeground(Color.CYAN);
        // plica una tipografía moderna (Segoe UI) muy grande (34 puntos) y en negrita.
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 34));
        // Centra el título horizontalmente respecto al eje del panel.
        titulo.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        // Inserta el título en la estructura.
        panelFondo.add(titulo);
        // Añade otro separador vertical, esta vez de 25 píxeles, antes de empezar el listado de botones.
        panelFondo.add(Box.createRigidArea(new Dimension(0, 25)));

        // Crea el botón de acceso llamando al método mostrarAutenticacion(). Al pasar null en el segundo parámetro, se genera sin icono lateral.
        panelFondo.add(crearBotonPro("NUEVO JUGADOR / SESIÓN", null, e -> mostrarAutenticacion()));
        // Separador de 12 píxeles para mantener la misma distancia entre todos los botones interactivos.
        panelFondo.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Genera el botón del Snake con su icono. La lambda comprueba si existe una sesión activa. 
        // De ser así, instancia e inicia SnakeGame pasándole la sesión y el repositorio para guardar puntuaciones.
        panelFondo.add(crearBotonPro("SNAKE ARCADE", "res/snake_icon.png", e -> {
            if (usuarioSesion == null) JOptionPane.showMessageDialog(this, "¡Registra un jugador!");
            else lanzarJuego(new SnakeGame(usuarioSesion, usuarioRepo), "Snake Arcade");
        }));
        panelFondo.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Lanza el panel del juego Pong de manera directa, ya que este juego no requiere obligatoriamente control de usuarios o persistencia.
        panelFondo.add(crearBotonPro("PONG RETRO", "res/pong_icon.png", e -> lanzarJuego(new PongGame(), "Pong Retro")));
        panelFondo.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Crea el botón para el juego 2048 con el mismo control de inicio de sesión obligatorio.
        panelFondo.add(crearBotonPro("2048 PUZZLE", "res/2048_icon.png", e -> {
            if (usuarioSesion == null) JOptionPane.showMessageDialog(this, "¡Registra un jugador!");
            else lanzarJuego(new Game2048(usuarioSesion, usuarioRepo), "2048 Puzzle");
        }));
        // Separador de 20 píxeles antes de colocar los elementos informativos del pie.
        panelFondo.add(Box.createRigidArea(new Dimension(0, 12)));
        
        panelFondo.add(crearBotonPro("TETRIS CLASSIC", "res/tetris_icon.png", e -> {
            if (usuarioSesion == null) JOptionPane.showMessageDialog(this, "¡Registra un jugador!");
            else lanzarJuego(new TetrisGame(usuarioSesion, usuarioRepo), "Tetris Classic");
        }));
        panelFondo.add(Box.createRigidArea(new Dimension(0, 12)));
        
        panelFondo.add(crearBotonPro(" VER RANKING DE MÉRITOS", "res/trofeo_neon.png", e -> mostrarRanking()));
        panelFondo.add(Box.createRigidArea(new Dimension(0, 20)));

        // Llama a la función encargada de cargar y redimensionar la imagen de collage publicitaria.
        panelFondo.add(cargarBannerJuegos());
        // Separador vertical de 20 píxeles.
        panelFondo.add(Box.createRigidArea(new Dimension(0, 20)));

        // Crea un contenedor para los créditos de los autores estructurado en una sola columna.
        JPanel panelInfo = new JPanel(new GridLayout(0, 1));
        // Hace transparente el contenedor de información.
        panelInfo.setOpaque(false);
        // Dibuja un borde fino de 1 píxel de grosor de color cian con una opacidad muy baja (30 de 255).
        panelInfo.setBorder(new LineBorder(new Color(0, 255, 255, 30), 1));
        // Restringe el tamaño del cuadro informativo a 380 píxeles de ancho y 85 de alto.
        panelInfo.setMaximumSize(new Dimension(380, 85));

        /*
         Define una cadena de texto formateada en HTML. 
         Permite usar etiquetas como <font> para pintar de cian el título, 
         <font color='#888888'> para poner gris los detalles del TFG,
          e incluir vuestros nombres: Jorge & Víctor.
         */
        String infoTexto = "<html><center><font color='cyan'><b>SISTEMA ARCADE v2.0</b></font><br>"
                + "<font color='#888888'>PROYECTO TFG - BASE DE DATOS ACTIVA<br>"
                + "AUTORES: JORGE & VÍCTOR | ESTADO: CONECTADO</font></center></html>";

        // Mete el texto renderizado en HTML dentro de una etiqueta Swing.
        JLabel lblInfo = new JLabel(infoTexto);
        // Centra el contenido del texto dentro de los límites de la etiqueta.
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        // Añade la etiqueta de créditos dentro del panel de información.
        panelInfo.add(lblInfo);
        // Centra el panel de información horizontalmente respecto al eje principal.
        panelInfo.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        // Añade el cuadro de información a la parte baja de la interfaz.
        panelFondo.add(panelInfo);

        // Último separador vertical de 20 píxeles previo al botón de cierre.
        panelFondo.add(Box.createRigidArea(new Dimension(0, 20)));

        // Crea el botón de apagado. La instrucción System.exit(0) detiene de forma fulminante la Máquina Virtual de Java (JVM) devolviendo un código de estado de finalización normal (0).
        JButton btnSalir = crearBotonPro("SALIR DE LA APP", null, e -> System.exit(0));
        // Reemplaza el color del tema por un fondo personalizado de tono rojo oscuro/granate.
        btnSalir.setBackground(new Color(50, 15, 15));
        // Asigna un color de texto rojo brillante para denotar peligro o acción destructiva.
        btnSalir.setForeground(new Color(255, 80, 80));
        // Establece un borde sólido de color rojo puro con opacidad media para que resalte frente a los bordes cian del resto del programa.
        btnSalir.setBorder(new LineBorder(new Color(255, 0, 0, 100), 1));
        // Coloca el botón de salida al final de la pila vertical.
        panelFondo.add(btnSalir);
        // Añade todo el panel contenedor principal (panelFondo) ya ensamblado directamente al panel raíz (ContentPane) de la ventana JFrame.
        add(panelFondo);
    }
    
    /*
     Resumen para el tribunal:
"He implementado un sistema de gestión de temas dinámico mediante la iteración de componentes en tiempo de ejecución. 
El método detecta el estado de la sesión y recorre los contenedores de la UI aplicando polimorfismo para modificar las propiedades de los objetos JButton. 
Esto permite una experiencia de usuario personalizada y persistente, ya que el estado del tema se recupera directamente de la configuración del usuario en la base de datos."
     */
    
    
    /*
     Pregunta trampa del tribunal:
Pregunta: "¿Por qué usas un bucle para cambiar los colores en lugar de hacerlo directamente?"

Respuesta: "Por mantenibilidad. 
Si en el futuro añado 10 juegos nuevos al menú,
 no tendré que modificar este método; el bucle detectará automáticamente los nuevos botones 
 y les aplicará el tema correcto, siguiendo el principio de diseño de código limpio."
     */
    
    
    private void aplicarTema() {
    	// Fuerza a la ventana principal a redibujarse. 
    	// Esto asegura que el fondo degradado del panel principal se actualice inmediatamente si el modo de color ha cambiado.
        this.getContentPane().repaint();
        // Determina qué tema aplicar.
        // Si no hay nadie logueado (usuarioSesion == null), por defecto se pone el Modo Oscuro.
        // Si hay alguien, mira el valor booleano isDarkMode() guardado en su perfil de la base de datos.
        boolean modoOscuro = (usuarioSesion == null || usuarioSesion.isDarkMode());
        // Usa un operador ternario para elegir el color de fondo de los botones.
        // Si es oscuro: Gris muy oscuro. Si es claro: Gris claro.
        Color btnBg = modoOscuro ? new Color(40, 40, 45) : new Color(200, 200, 200);
        // Elige el color del texto de forma opuesta al fondo para garantizar el contraste y la legibilidad.
        Color txt = modoOscuro ? Color.WHITE : Color.BLACK;

        // Inicia un bucle que recorre todos los elementos que hay dentro del panel principal (panelFondo).
        for (java.awt.Component comp : panelFondo.getComponents()) {
        	// Filtra los elementos. Solo nos interesan los botones; ignoramos etiquetas (JLabel) o espacios en blanco.
            if (comp instanceof JButton) {
            	// Hace un casting. Como ya sabemos que el componente es un botón, lo tratamos como tal para acceder a sus funciones de color.
                JButton btn = (JButton) comp;
                // Excepción de diseño: Comprueba que el botón no sea el de "SALIR". Esto se hace porque el botón de salir tiene un diseño rojo especial que no queremos que cambie con el tema.
                if (!btn.getText().equals("SALIR DE LA APP")) {
                	// Aplica los colores calculados al botón actual del bucle.
                    btn.setBackground(btnBg);
                    btn.setForeground(txt);
                }
            }
        }
        // Repite exactamente el mismo proceso para los botones pequeños de la esquina superior derecha (Perfil y Ajustes), asegurando que toda la pantalla sea coherente.
        for (java.awt.Component comp : panelIconos.getComponents()) {
            if (comp instanceof JButton) {
                ((JButton) comp).setBackground(btnBg);
                ((JButton) comp).setForeground(txt);
            }
        }
    }

    /*
     Este método es otra fábrica de componentes,
      pero especializada en los botones pequeños y cuadrados que usas en la parte superior derecha de tu aplicación (los iconos de Perfil y Ajustes).
       Su objetivo es crear botones compactos, 
     minimalistas y centrados exclusivamente en el icono.
     */
    
    // rutaIcono: La dirección del archivo de imagen (ej: "res/perfil_icon.png").
    // accion: La lógica que se ejecuta al hacer clic (abrir perfil o ajustes).
    private JButton crearBotonCuadrado(String rutaIcono, java.awt.event.ActionListener accion) {
        // Crea una nueva instancia de un botón de Swing, esta vez sin texto inicial.
    	JButton boton = new JButton();
    	// Establece un tamaño fijo y pequeño de 35x35 píxeles. Esto garantiza que el botón sea perfectamente cuadrado.
        boton.setPreferredSize(new Dimension(35, 35));
        // Desactiva el dibujo del foco (ese cerco que sale al pinchar), manteniendo la estética limpia.
        boton.setFocusPainted(false);
        // Cambia el puntero del ratón a la "manita" cuando pasamos por encima, indicando interacción.
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Aplica un borde de color Cian con una opacidad muy baja (50 de 255) y un grosor de 1 píxel para un efecto de neón sutil.
        boton.setBorder(new LineBorder(new Color(0, 255, 255, 50), 1));
        // Permite que el botón sea opaco para que se aplique correctamente el color de fondo definido en el tema (Oscuro/Claro).
        boton.setOpaque(true);
        // Elimina los márgenes internos predeterminados de Java. Esto es vital en botones tan pequeños para que el icono ocupe todo el espacio disponible.
        boton.setMargin(new Insets(0, 0, 0, 0));
        // Carga la imagen desde la ruta proporcionada.
        ImageIcon icon = new ImageIcon(rutaIcono);
        // Comprueba si la imagen se cargó correctamente (si el ancho es mayor a 0).
        if (icon.getIconWidth() > 0) {
        	// Redimensiona la imagen a 33x33 píxeles (dejando 1 píxel de margen para el borde) usando un escalado suave.
            Image img = icon.getImage().getScaledInstance(33, 33, Image.SCALE_SMOOTH);
            // Asigna la imagen reescalada como icono del botón.
            boton.setIcon(new ImageIcon(img));
            // Lógica de seguridad (fallback): Si por algún motivo la imagen no carga (archivo borrado o ruta mal escrita),
            // el botón mostrará un signo de interrogación "?" para que la interfaz no quede vacía.
        } else { boton.setText("?"); }
        // Enlaza el botón con la acción recibida por parámetro.
        boton.addActionListener(accion);
        // Devuelve el botón ya configurado para ser añadido al panelIconos.
        return boton;
    }

    // texto: El nombre del juego (ej: "SNAKE ARCADE").
    // rutaIcono: La dirección de la imagen (ej: "res/snake_icon.png").
    // accion: El código que se ejecutará al pulsar el botón (el lanzarJuego).
    private JButton crearBotonPro(String texto, String rutaIcono, java.awt.event.ActionListener accion) {
        // Crea el objeto botón con el texto del juego.
    	JButton boton = new JButton(texto);
    	// Establece el tamaño ideal del botón.
        boton.setPreferredSize(new Dimension(380, 50));
        // Evita que el botón se encoja si la ventana cambia.
        boton.setMinimumSize(new Dimension(380, 50));
        // Evita que el botón se estire más de lo debido.
        boton.setMaximumSize(new Dimension(380, 50));
        // Centra el botón horizontalmente dentro del panel vertical.
        boton.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        // Centra el contenido (icono + texto) dentro del botón.
        boton.setHorizontalAlignment(SwingConstants.CENTER); 
        // Ordena los elementos para que el icono salga a la izquierda y el texto a la derecha.
        boton.setHorizontalTextPosition(SwingConstants.RIGHT); 
        // Permite que se vea el color de fondo que vamos a asignar.
        boton.setOpaque(true);
        // Aplica un gris muy oscuro (casi negro) como fondo.
        boton.setBackground(new Color(40, 40, 45));
        // Pone el texto en color blanco.
        boton.setForeground(Color.WHITE);
        // Usa la fuente Segoe UI en negrita y tamaño 14.
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        // Quita el borde de puntos feo que sale al hacer clic.
        boton.setFocusPainted(false);
        // Cambia el ratón a una "manita" al pasar por encima.
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Crea un borde fino de color cian semitransparente (estética neón).
        boton.setBorder(new LineBorder(new Color(0, 255, 255, 50), 1));
        // Solo intenta poner icono si has pasado una ruta.
        if (rutaIcono != null) {
        	// Intenta cargar la imagen desde la carpeta res.
            try {
                ImageIcon icon = new ImageIcon(rutaIcono);
                // Comprueba que la imagen existe y se ha cargado bien (que no sea un archivo corrupto o inexistente).
                if (icon.getIconWidth() > 0) {
                	// Redimensiona la imagen a 25x25 píxeles con un filtro de suavizado para que no se vea pixelada.
                    Image img = icon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
                    boton.setIcon(new ImageIcon(img));
                    boton.setIconTextGap(20);
                }
            } catch (Exception e) {}
        }
        boton.addActionListener(accion);
        return boton;
    }

    private void iniciarReloj() { new Timer(1000, e -> lblReloj.setText(new SimpleDateFormat("HH:mm:ss  ").format(new Date()))).start(); }
    private JLabel cargarBannerJuegos() { JLabel banner = new JLabel(); try { ImageIcon icono = new ImageIcon("res/collage_juegos.png"); if (icono.getIconWidth() > 0) { Image img = icono.getImage().getScaledInstance(400, 110, Image.SCALE_SMOOTH); banner.setIcon(new ImageIcon(img)); } } catch (Exception e) { banner.setText("[ IMAGE ]"); banner.setForeground(Color.GRAY); } banner.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT); return banner; }
    
    // // 231: Recibe el panel del juego y su nombre.
    private void lanzarJuego(JPanel panelJuego, String tituloVentana) {
    	// 232: Registra el milisegundo exacto de inicio.
        long startTime = System.currentTimeMillis(); 
     // 233: Crea una ventana nueva para el juego.
        JFrame v = new JFrame(tituloVentana);
     // 234: Solo cierra esta ventana, no todo el programa.
        v.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
     // 235: Evita que el usuario cambie el tamaño y rompa el juego.
        v.setResizable(false); 
     // 236: Mete el juego dentro de la ventana.
        v.add(panelJuego); 
     // 237: Ajusta el tamaño de la ventana al tamaño del juego.
        v.pack(); 
     // 238: Centra el juego.
        v.setLocationRelativeTo(null); 
     // 239: Escuchamos eventos de la ventana.
        v.addWindowListener(new WindowAdapter() {
            @Override
         // 241: Se ejecuta al pulsar la X del juego.
            public void windowClosing(WindowEvent e) {
            	// 242: Calcula el tiempo total jugado.
                long duration = System.currentTimeMillis() - startTime;
             // 243: Solo guarda puntos si hay alguien logueado.
                if (usuarioSesion != null) {
                	// 244: Convertimos milisegundos a segundos.
                    int seconds = (int)(duration / 1000);
                 // 245: Lógica de puntos basada en velocidad.
                    int ptsPartida = Math.max(0, 5000 - seconds);
                 // 247-253: Identifica qué juego era y actualiza los puntos del objeto usuario.
                    if (panelJuego instanceof SnakeGame) {
                        usuarioSesion.setPuntos_snake(ptsPartida);
                    } else if (panelJuego instanceof Game2048) {
                        usuarioSesion.setPuntos_2048(ptsPartida);
                    } else if (panelJuego instanceof TetrisGame) {
                        usuarioSesion.setPuntos_tetris(ptsPartida);
                    }
                 // 254: PERSISTENCIA: Guarda los cambios en la base de datos SQL.
                    usuarioRepo.save(usuarioSesion);
                }
                
                //257-259: Detiene hilos y música para no dejar procesos basura en segundo plano.
                if (panelJuego instanceof PongGame) ((PongGame) panelJuego).detenerJuego();
                if (panelJuego instanceof SnakeGame) ((SnakeGame) panelJuego).pararMusica();
                if (panelJuego instanceof TetrisGame) ((TetrisGame) panelJuego).detenerJuego();
            }
        });
     // 260: Muestra la ventana del juego.
        v.setVisible(true);
     // 261: Pone el foco del teclado en el juego para poder jugar ya.
        panelJuego.requestFocusInWindow();
    }

    // Verifica si ya hay un usuario dentro. Si es así, lanza un aviso y detiene el método para no abrir el formulario dos veces.
    private void mostrarAutenticacion() {
        if (usuarioSesion != null) {
        	// Si hay sesión, muestra un mensaje emergente avisando al usuario.
            JOptionPane.showMessageDialog(this, "Ya tienes una sesión iniciada: " + usuarioSesion.getUsername());
            return;
        }

        // Crea una ventana modal. El parámetro true es vital: bloquea el menú principal hasta que el usuario se identifique o cierre esta ventana.
        JDialog dialog = new JDialog(this, "Autenticación", true);
        // Define el tamaño de la ventana (400px ancho, 250px alto).
        dialog.setSize(400, 250);
        // Centra la ventanita justo encima del menú principal.
        dialog.setLocationRelativeTo(this);
        // Crea el componente de pestañas para alternar entre "Iniciar Sesión" y "Crear Jugador".
        JTabbedPane tabbedPane = new JTabbedPane();

        // Crea el panel para el login usando una rejilla flexible (GridBagLayout).
        JPanel panelLogin = new JPanel(new GridBagLayout());
        // Crea el panel para el registro con la misma rejilla.
        JPanel panelRegistro = new JPanel(new GridBagLayout());
        // Crea el objeto que dicta las reglas de cómo se colocan los elementos en la rejilla.
        GridBagConstraints gbc = new GridBagConstraints();
        // Define un margen de 5 píxeles alrededor de cada cuadro de texto o botón.
        // gbc.fill = GridBagConstraints.HORIZONTAL;: Indica que los elementos deben estirarse horizontalmente para rellenar su hueco.
        gbc.insets = new Insets(5, 5, 5, 5); gbc.fill = GridBagConstraints.HORIZONTAL;

        // Crea el campo para escribir el nombre de usuario.
        // userLogin.setPreferredSize(new Dimension(180, 25));: Le da un tamaño estándar al campo de texto.
        JTextField userLogin = new JTextField(); userLogin.setPreferredSize(new Dimension(180, 25));
        // Crea el campo para la contraseña (oculta lo que escribes).
        // Le da el mismo tamaño que al de usuario.
        JPasswordField passLogin = new JPasswordField(); passLogin.setPreferredSize(new Dimension(180, 25));
        // Crea el botón con el texto "Entrar".
        JButton btnLogin = new JButton("Entrar");

        // Pone la etiqueta "Usuario:" en la columna 0, fila 0.
        gbc.gridx = 0; gbc.gridy = 0; panelLogin.add(new JLabel("Usuario:"), gbc);
        // Pone el campo de texto en la columna 1, fila 0.
        gbc.gridx = 1; panelLogin.add(userLogin, gbc);
        // Pone la etiqueta "Contraseña:" en columna 0, fila 1.
        gbc.gridx = 0; gbc.gridy = 1; panelLogin.add(new JLabel("Contraseña:"), gbc);
        // Pone el campo de contraseña en columna 1, fila 1.
        gbc.gridx = 1; panelLogin.add(passLogin, gbc);
        // Pone el botón "Entrar" en columna 1, fila 2.
        gbc.gridx = 1; gbc.gridy = 2; panelLogin.add(btnLogin, gbc);

        // Define qué pasa cuando haces clic en el botón.
        btnLogin.addActionListener(e -> {
        	// Lee el nombre escrito y borra espacios accidentales al principio o final.
            String username = userLogin.getText().trim();
            // Lee la contraseña escrita (la convierte de array de caracteres a texto).
            String password = new String(passLogin.getPassword());

            // Comprueba si has metido las claves maestras.
            if (username.equalsIgnoreCase("admin") && password.equals("admin1234")) {
            	// Busca si ya existe un usuario "admin" en la base de datos.
                Usuario admin = usuarioRepo.findAll().stream().filter(u -> u.getUsername().equalsIgnoreCase("admin")).findFirst().orElse(null);
                // Si no existe el admin todavía (primer arranque)...
                if (admin == null) {
                	// Crea un objeto usuario nuevo.
                    admin = new Usuario();
                    // Le pone de nombre "admin".
                    admin.setUsername("admin");
                    // Encripta la contraseña antes de guardarla.
                    admin.setPassword(encoder.encode("admin1234"));
                    // Le activa el modo oscuro por defecto.
                    admin.setDarkMode(true);
                    // Guarda el admin creado en la base de datos SQL.
                    usuarioRepo.save(admin);
                }
                // Inicia la sesión como administrador.
                usuarioSesion = admin;
                // Refresca los colores del menú según la preferencia del admin.
                aplicarTema();
                // Muestra el aviso de éxito.
                JOptionPane.showMessageDialog(dialog, "Acceso Admin concedido.");
                // Cierra la ventana de autenticación.
                dialog.dispose();
                // Sale de la función para no seguir comprobando otros usuarios.
                return;
            }

            // Busca en la BD al usuario que has escrito.
            Usuario u = usuarioRepo.findAll().stream()
                    .filter(user -> user.getUsername().equalsIgnoreCase(username))
                    .findFirst().orElse(null);

            // Si existe el usuario y la contraseña (encriptada) coincide con la que has escrito...
            if (u != null && encoder.matches(password, u.getPassword())) {
            	// Guarda al usuario en la sesión actual.
                usuarioSesion = u;
                // Cambia los colores del menú a los de ese usuario.
                aplicarTema(); 
                //  Avisa del éxito.
                JOptionPane.showMessageDialog(dialog, "¡Bienvenido, " + username + "!");
                // Cierra la ventanita
                dialog.dispose();
            } else {
            	// Avisa del error.
                JOptionPane.showMessageDialog(dialog, "Usuario o contraseña incorrectos.");
            }
        });

        // Crea el campo de usuario para registro.
        JTextField userReg = new JTextField(); userReg.setPreferredSize(new Dimension(180, 25));
        // Crea el campo de contraseña para registro.
        JPasswordField passReg = new JPasswordField(); passReg.setPreferredSize(new Dimension(180, 25));
        // Crea el campo para repetir la contraseña.
        JPasswordField passRegConfirm = new JPasswordField(); passRegConfirm.setPreferredSize(new Dimension(180, 25));
        // Crea el botón de registro.
        JButton btnReg = new JButton("Registrarse");

        gbc.gridx = 0; gbc.gridy = 0; panelRegistro.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1; panelRegistro.add(userReg, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panelRegistro.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; panelRegistro.add(passReg, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panelRegistro.add(new JLabel("Confirmar:"), gbc);
        gbc.gridx = 1; panelRegistro.add(passRegConfirm, gbc);
        gbc.gridx = 1; gbc.gridy = 3; panelRegistro.add(btnReg, gbc);

        btnReg.addActionListener(e -> {
        	// Lee el nombre deseado.
            String username = userReg.getText().trim();
            // Lee la primera contraseña.
            String password = new String(passReg.getPassword());
            // Lee la confirmación.
            String confirm = new String(passRegConfirm.getPassword());

            // Si hay campos vacíos, no hace nada.
            if (username.isEmpty() || password.isEmpty()) return;
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "Las contraseñas no coinciden.");
                return;
            }

            // Comprueba en la BD si ese nombre ya está pillado.
            boolean existe = usuarioRepo.findAll().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));

            // Si el nombre ya está en uso...
            if (existe) {
            	// Avisa del error.
                JOptionPane.showMessageDialog(dialog, "Ese nombre ya existe.");
            } else {
            	// Crea una nueva entidad Usuario.
                Usuario nuevo = new Usuario();
                // Le asigna el nombre.
                nuevo.setUsername(username);
                // Encripta la contraseña para cumplir con la seguridad.
                nuevo.setPassword(encoder.encode(password));
                // Le pone el modo oscuro por defecto.
                nuevo.setDarkMode(true);
                // Guarda el nuevo usuario en SQL e inicia sesión con él.
                usuarioSesion = usuarioRepo.save(nuevo);
                // Pone los colores en modo oscuro.
                aplicarTema(); 
                // Avisa del éxito.
                JOptionPane.showMessageDialog(dialog, "Usuario creado exitosamente.");
                // Cierra la ventana.
                dialog.dispose();
            }
        });

        // Crea la pestaña 1 con el panel de login.
        tabbedPane.addTab("Iniciar Sesión", panelLogin);
        // Crea la pestaña 2 con el panel de registro.
        tabbedPane.addTab("Crear Jugador", panelRegistro);
        // Mete el conjunto de pestañas en la ventana emergente.
        dialog.add(tabbedPane);
        // Muestra la ventana finalmente.
        dialog.setVisible(true);
    }
    
    /*
     "Este método gestiona la visualización de datos del perfil mediante un GridLayout para garantizar la alineación de las puntuaciones. 
     Además, implementa la actualización de credenciales en caliente, 
     asegurando que la nueva contraseña se procese mediante un hash de BCrypt antes de ser persistida en la base de datos a través del repositorio de Spring Data JPA."
     */
    private void mostrarPerfil() {
    	// Crea la ventana emergente con un tamaño de 400x350. Al ser un JDialog, bloquea la interacción con la ventana principal hasta que se cierre.
        JDialog d = createStyledDialog("PERFIL DEL JUGADOR", 400, 350);
        // Establece un diseño de "puntos cardinales". El 10, 10 indica la separación (gap) entre las zonas (Norte, Sur, Centro, etc.).
        d.setLayout(new BorderLayout(10, 10));
        // Crea un panel que organiza los elementos en una cuadrícula. El 0 significa "tantas filas como necesites" y el 2 indica exactamente dos columnas (Etiqueta y Valor).
        JPanel panelDatos = new JPanel(new GridLayout(0, 2, 10, 10));
        // Hace que el panel sea transparente para que se vea el fondo degradado que definiste en el método anterior.
        panelDatos.setOpaque(false);
        // Añade un margen de 20 píxeles por cada lado para que el texto no toque los bordes de la ventana.
        panelDatos.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Define el estilo (fuente Monospaced, negrita, tamaño 12)
        Font fontLabel = new Font("Monospaced", Font.BOLD, 12);
        // y el color (Cian) que se usará para los títulos de las estadísticas
        Color colLabel = Color.CYAN;
        
        // addStatRow(...): Llama al método auxiliar que explicamos antes para añadir cuatro filas: el nombre del jugador y sus puntos en Snake, 2048 y Tetris.
        addStatRow(panelDatos, "JUGADOR:", usuarioSesion.getUsername().toUpperCase(), fontLabel, colLabel);
        addStatRow(panelDatos, "SNAKE:", usuarioSesion.getPuntos_snake() + " pts", fontLabel, colLabel);
        addStatRow(panelDatos, "2048:", usuarioSesion.getPuntos_2048() + " pts", fontLabel, colLabel);
        addStatRow(panelDatos, "TETRIS:", usuarioSesion.getPuntos_tetris() + " pts", fontLabel, colLabel);

        // Crea un segundo panel en la parte inferior para los controles de actualización.
        JPanel panelAcciones = new JPanel(new GridBagLayout());
        panelAcciones.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Crea el cuadro de texto para la contraseña. Al ser un JPasswordField, oculta los caracteres con puntos por seguridad.
        JPasswordField pass = new JPasswordField(15);
        // Personaliza el estilo del cuadro de texto para que encaje con la estética oscura y cian del arcade.
        pass.setBackground(new Color(25, 25, 35)); pass.setForeground(Color.WHITE);
        pass.setCaretColor(Color.CYAN); pass.setBorder(new LineBorder(Color.CYAN, 1));
        
        // Coloca el texto indicativo en la rejilla del panel de acciones.
        gbc.gridx = 0; gbc.gridy = 0; panelAcciones.add(new JLabel("NUEVA CONTRASEÑA:"), gbc);
        gbc.gridy = 1; panelAcciones.add(pass, gbc);
        
        // Crea el botón para confirmar el cambio.
        JButton btnGuardar = new JButton("ACTUALIZAR");
        // Aplica el estilo visual (bordes, colores, efectos) que definiste en el método de estilizado.
        estilizarBotonPopup(btnGuardar);
        btnGuardar.addActionListener(e -> {
        	// Línea crítica. Toma la nueva contraseña, la encripta usando el encoder (BCrypt) y la guarda en el objeto en memoria.
            usuarioSesion.setPassword(encoder.encode(new String(pass.getPassword())));
            // Acceso a BD. Actualiza la fila del usuario en la base de datos SQL con la nueva contraseña ya cifrada.
            usuarioRepo.save(usuarioSesion);
            // Cierra la ventana de perfil.
            d.dispose();
            // Confirma al usuario que el cambio ha sido exitoso.
            JOptionPane.showMessageDialog(this, "Datos actualizados.");
        });
        gbc.gridy = 2; gbc.insets = new Insets(10,0,0,0); panelAcciones.add(btnGuardar, gbc);

        // Pone las estadísticas en el centro de la ventana.
        d.add(panelDatos, BorderLayout.CENTER);
        // Pone la zona de cambio de contraseña en la parte de abajo.
        d.add(panelAcciones, BorderLayout.SOUTH);
        // Hace que la ventana aparezca en pantalla.
        d.setVisible(true);
    }

    /*
     RESUMEN:
     "Este método implementa la gestión de preferencias del usuario y el control de sesión. 
     Destaca el uso de GridBagLayout para un diseño responsivo dentro del diálogo y la integración con Spring Data JPA mediante el método save, 
     lo que permite que la personalización del 'Modo Oscuro' persista de forma permanente en la base de datos relacional."
     */
    private void mostrarAjustes() {
    	// Crea una ventana emergente (hija de la principal) con el título "CONFIGURACIÓN" y un tamaño de 350x300 píxeles, usando tu método de estilo personalizado.
        JDialog d = createStyledDialog("CONFIGURACIÓN", 350, 300);
        // Establece el gestor de diseño más potente de Java Swing, que permite alinear componentes en una rejilla flexible.
        d.setLayout(new GridBagLayout());
        // Crea el objeto de "restricciones" que le dirá al GridBagLayout dónde y cómo colocar cada botón o check.
        GridBagConstraints gbc = new GridBagConstraints();
        // Define un margen de 10 píxeles alrededor de cada elemento para que no estén pegados entre sí.
        gbc.insets = new Insets(10, 10, 10, 10);

        // Crea una casilla de verificación. El segundo parámetro hace que aparezca marcada o no dependiendo de lo que el usuario tenga guardado en su perfil actualmente.
        JCheckBox checkDark = new JCheckBox("  MODO OSCURO", usuarioSesion.isDarkMode());
        // Hace que el fondo del checkbox sea transparente para que se vea el degradado del panel de fondo.
        checkDark.setOpaque(false);
        // Pone el texto del checkbox en color blanco.
        checkDark.setForeground(Color.WHITE);
        // Aplica una fuente de estilo "consola de programador" para mantener la estética retro.
        checkDark.setFont(new Font("Monospaced", Font.BOLD, 14));
        // Coloca el checkbox en la primera fila y primera columna (0,0) y lo añade a la ventana.
        gbc.gridx = 0; gbc.gridy = 0; d.add(checkDark, gbc);

        // Crea el botón de desconexión.
        JButton btnLogout = new JButton("CERRAR SESIÓN");
        // Llama a tu método (el que explicamos antes) para ponerle el borde cian y el efecto de ratón.
        estilizarBotonPopup(btnLogout);
        btnLogout.addActionListener(e -> {
        	// Línea crítica. Borra al usuario de la memoria del programa. Al ser null, el menú principal sabrá que no hay nadie dentro.
            usuarioSesion = null;
            // Refresca los colores del menú (volverá al tema oscuro por defecto).
            aplicarTema();
            // Cierra la ventana de ajustes.
            d.dispose();
            // Muestra un mensaje confirmando que ha salido.
            JOptionPane.showMessageDialog(this, "Sesión finalizada.");
        });
        // Lo coloca en la segunda fila (y=1).
        gbc.gridy = 1; d.add(btnLogout, gbc);

        // Crea el botón para confirmar ajustes.
        JButton btnGuardar = new JButton("GUARDAR CAMBIOS");
        estilizarBotonPopup(btnGuardar);
        btnGuardar.addActionListener(e -> {
        	// Actualiza el objeto Usuario en memoria con lo que el usuario haya marcado en el checkbox.
            usuarioSesion.setDarkMode(checkDark.isSelected());
            // Conexión con BD. Envía el cambio a la base de datos SQL para que la próxima vez que entre, se acuerde de su preferencia.
            usuarioRepo.save(usuarioSesion);
            // Actualiza los colores de la interfaz al instante.
            aplicarTema();
            // Cierra la ventanita.
            d.dispose();
        });
        // Lo coloca en la tercera fila (y=2).
        gbc.gridy = 2; d.add(btnGuardar, gbc);
        // Hace que la ventana de ajustes aparezca finalmente ante el usuario.
        d.setVisible(true);
    }
    
    /*
     Este método es el encargado de unificar los méritos del jugador. 
     Su lógica es interesante porque no todos tus juegos puntúan igual:
      mientras que en unos sumas puntos (2048, Tetris), en el Snake lo que guardas es el tiempo, 
      por lo que necesitas una fórmula para convertir ese tiempo en una puntuación que se pueda sumar a las demás.
     */

    //RESUMEN DEL METODO:
    /*
     "He implementado este método para normalizar las puntuaciones de los distintos juegos. 
     Dado que el juego Snake se basa en el tiempo de resolución, 
     utilizo una fórmula de inversión (restando el tiempo de una constante de 5000 puntos) para que los mejores tiempos se traduzcan en mayores puntuaciones. 
     Esto permite obtener un Ranking Global equitativo que suma el desempeño del usuario en todas las áreas del Arcade."
     */
    // Usuario u: Recibe el objeto del usuario al que le queremos calcular la nota global.
    private int calcularTotal(Usuario u) {
    	// Aquí usas un operador ternario (el ? y el :), que es como un if en una sola línea.
        // La condición: (u.getPuntos_snake() > 0). Comprueba si el usuario ha jugado alguna vez al Snake.
    	
    	/*
    	 // Si ha jugado: Calcula los puntos restando su tiempo de un "techo" de 5000. Usas Math.max(0, ...) para asegurar que, 
    	// si el usuario tarda más de 5000 segundos, la puntuación sea 0 y nunca un número negativo.
    	 */
    	
    	// Si NO ha jugado: Le asigna directamente un 0.
    	int ptsSnake = (u.getPuntos_snake() > 0) ? Math.max(0, 5000 - u.getPuntos_snake()) : 0;
        // Suma la puntuación calculada del Snake con los puntos directos que el usuario tiene guardados de los otros dos juegos.
    	return ptsSnake + u.getPuntos_2048() + u.getPuntos_tetris();
    }

    private void mostrarRanking() { // 403: Abre la ventana de méritos.
        JDialog ventanaRanking = new JDialog(this, "RANKING GLOBAL", true);
        ventanaRanking.setSize(650, 550);
        ventanaRanking.setLocationRelativeTo(this);
        ventanaRanking.setLayout(new BorderLayout());
        
     // 408: Trae TODOS los jugadores de la base de datos.
        List<Usuario> usuarios = usuarioRepo.findAll();
     // 409: Filtra para no mostrar al administrador.
        usuarios.removeIf(u -> "admin".equalsIgnoreCase(u.getUsername()));
     // 410: ORDENA de mayor a menor puntuación.
        usuarios.sort((u1, u2) -> Integer.compare(calcularTotal(u2), calcularTotal(u1)));
        
     // 412-421: Crea el modelo de la tabla y añade filas con los datos (Posición, Nombre, Puntos de cada juego).
        String[] columnas = {"POS", "JUGADOR", "TIME SNAKE", "MAX 2048", "PUNTOS TETRIS", "TOTAL"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) { 
            @Override public boolean isCellEditable(int r, int c) { return false; } 
        };
        
        int pos = 1;
        for (Usuario u : usuarios) {
            String labelPos = (pos==1) ? "🏆 1º" : (pos==2) ? "🥈 2º" : (pos==3) ? "🥉 3º" : String.valueOf(pos);
            modelo.addRow(new Object[]{ labelPos, u.getUsername(), u.getPuntos_snake() + "s", u.getPuntos_2048(), u.getPuntos_tetris(), calcularTotal(u) });
            pos++;
        }
        
        JTable tabla = new JTable(modelo);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(80);
        ventanaRanking.add(new JScrollPane(tabla), BorderLayout.CENTER);
        
        boolean esAdmin = usuarioSesion != null && "admin".equalsIgnoreCase(usuarioSesion.getUsername());
        
     // 432: Si el usuario actual es "admin", activa herramientas especiales.
        // Botón Borrar: llama a usuarioRepo.delete(u) para eliminar un registro de la BD.
        // Botón Editar: abre un formulario para modificar puntos a mano.
        if (esAdmin) {
            JPanel panelAdmin = new JPanel();
            JButton btnBorrar = new JButton("Borrar");
            JButton btnEditar = new JButton("Editar");
            
            /*
             Este bloque implementa las funciones de gestión del administrador. 
             He utilizado JOptionPane para crear formularios dinámicos y cuadros de confirmación, 
             integrándolos con los métodos delete y save de Spring Data JPA para asegurar que cualquier cambio administrativo se refleje inmediatamente en la persistencia de la base de datos."
             */
            
            // Define la acción que ocurrirá cuando hagas clic en el botón Borrar.
            btnBorrar.addActionListener(e -> {
            	// Pregunta a la tabla: "¿Qué fila tiene seleccionada el usuario ahora mismo?". Guarda el número de fila.
                int fila = tabla.getSelectedRow();
                // Comprueba si realmente hay una fila seleccionada. Si no hay nada seleccionado, el valor es -1 y no hace nada.
                if (fila != -1) {
                	// Busca en tu lista de objetos usuarios aquel que corresponde a la fila que el admin ha pinchado.
                    Usuario u = usuarios.get(fila);
                    // Lanza una ventana de confirmación. Solo si el admin pulsa "SÍ", se ejecuta lo siguiente. 
                    // Esto es una medida de seguridad para no borrar por error.
                    if (JOptionPane.showConfirmDialog(ventanaRanking, "¿Borrar a " + u.getUsername() + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        // Línea clave de Base de Datos. 
                    	// Llama al repositorio de Spring para ejecutar el comando SQL DELETE y borrar ese usuario de la base de datos.
                    	usuarioRepo.delete(u);
                    	// Cierra la ventana actual del ranking.
                        ventanaRanking.dispose();
                        // Vuelve a llamar al método para abrir el ranking. Esto hace que la tabla se refresque y el usuario borrado ya no aparezca.
                        mostrarRanking();
                    }
                }
            });
            
            // Define la acción para el botón Editar.
            btnEditar.addActionListener(e -> {
            	// Igual que antes, detecta qué fila de la tabla se ha seleccionado.
                int fila = tabla.getSelectedRow();
                // Verifica que haya un usuario seleccionado.
                if (fila != -1) {
                	// Obtiene el objeto Usuario de la lista.
                    Usuario u = usuarios.get(fila);
                    // Crea una cajita de texto y escribe dentro los puntos actuales de Tetris de ese usuario.
                    JTextField txtTetris = new JTextField(String.valueOf(u.getPuntos_tetris()));
                   // Crea una cajita de texto y escribe dentro los puntos actuales del 2048 de ese usuario.
                    JTextField txt2048 = new JTextField(String.valueOf(u.getPuntos_2048()));
                    // Crea una cajita de texto y escribe dentro los puntos actuales del snake de ese usuario.
                    JTextField txtSnake = new JTextField(String.valueOf(u.getPuntos_snake()));
                    // Crea un panel invisible con una rejilla de 3 filas y 2 columnas para organizar los textos y las cajitas.
                    JPanel panelForm = new JPanel(new GridLayout(3, 2, 5, 5));
                    // Añade la etiqueta "Tetris" y su caja de texto al panel. Repite esto para los otros juegos.
                    panelForm.add(new JLabel("Tetris:")); panelForm.add(txtTetris);
                    panelForm.add(new JLabel("2048:")); panelForm.add(txt2048);
                    panelForm.add(new JLabel("Snake:")); panelForm.add(txtSnake);
                    // Muestra una ventana emergente que contiene el panel con las cajitas que acabamos de crear.
                    int res = JOptionPane.showConfirmDialog(ventanaRanking, panelForm, "Editar " + u.getUsername(), JOptionPane.OK_CANCEL_OPTION);
                    // Si el administrador pulsa "Aceptar" después de cambiar los números...
                    if (res == JOptionPane.OK_OPTION) {
                    	// Abre un bloque de seguridad por si el admin escribe letras en lugar de números por error.
                        try {
                        	// Lee lo que hay en la cajita, lo convierte de "Texto" a "Número Entero" y se lo asigna al objeto usuario. Repite para los demás juegos.
                            u.setPuntos_tetris(Integer.parseInt(txtTetris.getText()));
                            u.setPuntos_2048(Integer.parseInt(txt2048.getText()));
                            u.setPuntos_snake(Integer.parseInt(txtSnake.getText()));
                            // Línea clave de Base de Datos. Llama al repositorio para ejecutar el comando SQL UPDATE y guardar los nuevos puntos en la base de datos.
                            usuarioRepo.save(u);
                            // Cierra y reabre el ranking para que se vean los puntos actualizados.
                            ventanaRanking.dispose();
                            mostrarRanking();
                            // Si el admin escribió algo que no es un número, muestra un aviso de error en lugar de que el programa se rompa.
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(ventanaRanking, "Error numérico");
                        }
                    }
                }
            });
            // Añade los dos botones al panel especial para el administrador.
            panelAdmin.add(btnEditar);
            panelAdmin.add(btnBorrar);
            // Coloca ese panel de botones en la parte de abajo de la ventana de ranking.
            ventanaRanking.add(panelAdmin, BorderLayout.SOUTH);
        }
        // Hace que la ventana de ranking aparezca en pantalla.
        ventanaRanking.setVisible(true);
    }
    
    private JDialog createStyledDialog(String title, int w, int h) {
        JDialog d = new JDialog(this, title, true);
        d.setSize(w, h);
        d.setLocationRelativeTo(this);
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0,0, new Color(20,20,30), 0, getHeight(), new Color(10,10,15)));
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        d.setContentPane(p);
        d.getRootPane().setBorder(new LineBorder(Color.CYAN, 2));
        return d;
    }

    /*
     * Este metodo es un configurador de estilo visual y comportamiento para los botones que aparecen en las ventanas emergentes
     * Su funcion es asegurar que todos los botones de dialogo tengan un aspecto moderno
     * y que reaccionen cuando el usuario pasa el raton por encima
     
     
     RESUMEN DEL METODO:
     "Este método encapsula toda la lógica de diseño de los botones de los diálogos.
      Utiliza un MouseListener para implementar un efecto de hover (cambio de color al pasar el ratón) 
      y define una identidad visual basada en colores oscuros y bordes de neón cian,
      mejorando la experiencia de usuario (UX) mediante feedback visual inmediato."
     */
    
    // JButton b: Recibe como parámetro el botón que quieres "tunear". 
    // Al pasarle el objeto, el método modifica directamente sus propiedades.
    private void estilizarBotonPopup(JButton b) {
    	// Define el tamaño ideal del boton: 220 pixeles de ancho por 40 de alto
        b.setPreferredSize(new Dimension(220, 40));
        // Establece el color de fondo. Es un tono gris muy oscuro con un matiz azulado
        b.setBackground(new Color(40, 40, 50));
        // Cambia el color del texto a blanco para que resalte sobre el fondo oscuro
        b.setForeground(Color.WHITE);
        // Aplica la fuente Segoe UI en negrita con un tamaño de 12 puntos. Es una fuente moderna y legible
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        // Elimina el recuadro punteado que suele aparecer alrededor del texto cuando haces clic en un boton, esto hace el diseño mas limpio
        b.setFocusPainted(false);
        // dibuja un borde solido de color cian con un grosor de 1 pixel
        b.setBorder(new LineBorder(Color.CYAN, 1));
        // cambia el icono del raton a una "manita" cuando el cursor pasa sobre el boton, indicando claramente que es un elemento clicable
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
        	// añade un escuchador de eventos del raton para crear un efecto de iluminacion
        	
        	// Cuando el ratón entra en el área del botón, el fondo se aclara ligeramente (pasa a un gris más claro). 
        	//Esto da feedback visual al usuario.
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(60, 60, 70)); }
            
            // Cuando el ratón sale del botón, el color vuelve a su estado original oscuro.
            public void mouseExited(MouseEvent e) { b.setBackground(new Color(40, 40, 50)); }
        });
    }

    
    
    // Este método es una función auxiliar de diseño.
    // Su objetivo es automatizar la creación de filas de estadísticas en la interfaz del perfil, 
    // evitando repetir el código cada vez que quieres mostrar un dato (como "Jugador", "Puntos Snake", etc.).
   
    /*
     "Este método es un helper de interfaz que permite añadir de forma dinámica y elegante filas de información al perfil del usuario, 
     garantizando que todos los datos tengan el mismo formato visual y facilitando el mantenimiento del código".
     */
    
    
    // JPanel p: El panel donde se van a meter los textos.
    // String label: El nombre del dato (ej: "SNAKE:").
    // String value: El valor del dato (ej: "500 pts").
    // Font f: El tipo de letra que se va a usar.
    // Color c: El color para el nombre del dato.
    private void addStatRow(JPanel p, String label, String value, Font f, Color c) {
    	// Crea una etiqueta de texto (l1) con el nombre del dato.
    	// Le aplica el color (c) que pasaste por parámetro (que suele ser cian).
    	// Le asigna la fuente (f) para que el tamaño y estilo sean correctos.
        JLabel l1 = new JLabel(label); l1.setForeground(c); l1.setFont(f);
        // Crea una segunda etiqueta (l2) con el valor o puntuación.
        // A diferencia de la anterior, este texto siempre se pone en Color Blanco (Color.WHITE) para que resalte.
        // Usa la misma fuente (f) para mantener la coherencia visual.
        JLabel l2 = new JLabel(value); l2.setForeground(Color.WHITE); l2.setFont(f);
        // Añade ambas etiquetas al panel (p).
        // Como el panel del perfil usa un GridLayout(0, 2), al añadir estas dos piezas, 
        // Java las coloca automáticamente una al lado de la otra, formando una fila perfecta.
        p.add(l1); p.add(l2);
    }

    public static void main(String[] args) {
    	// 514: Intenta que la app use el estilo visual del sistema.
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception e) {}
     // 515: Inicia Spring Boot pero desactiva el modo "headless" porque necesitamos monitor/ventana.
        ConfigurableApplicationContext context = new SpringApplicationBuilder(MenuPrincipal.class).headless(false).run(args);
     // 516: Ejecuta el código visual en el hilo de eventos de Swing (Hilo seguro).
        SwingUtilities.invokeLater(() -> {
        	// 517: Pide a Spring que le dé el objeto MenuPrincipal ya configurado.
            MenuPrincipal frame = context.getBean(MenuPrincipal.class);
         // 518: Hace aparecer la ventana en pantalla.
            frame.setVisible(true);
        });
    }
}