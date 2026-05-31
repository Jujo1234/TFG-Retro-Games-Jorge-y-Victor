package com.retro.main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
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
          
"El constructor de la interfaz utiliza un patrón estructural basado en la composición de layouts aninados. 
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
        // aplica una tipografía moderna (Segoe UI) muy grande (34 puntos) y en negrita.
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
        
        // Lanza el panel del juego Pong de manera directa, ya que este juego no requiere obligatoriamente control de usuarios o presistencia.
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
Esto permite una experiencia de usuario personalizada y persistent, ya que el estado del tema se recupera directamente de la configuración del usuario en la base de datos."
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
        ImageIcon icon = new ImageIcon(rutaIcono);
        if (icon.getIconWidth() > 0) {
            Image img = icon.getImage().getScaledInstance(33, 33, Image.SCALE_SMOOTH);
            boton.setIcon(new ImageIcon(img));
        } else { boton.setText("?"); }
        boton.addActionListener(accion);
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
        if (rutaIcono != null) {
            try {
                ImageIcon icon = new ImageIcon(rutaIcono);
                if (icon.getIconWidth() > 0) {
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

    // Método de autenticación con estética oscura neón cian.
    private void mostrarAutenticacion() {
        if (usuarioSesion != null) {
            JOptionPane.showMessageDialog(this, "Ya tienes una sesión iniciada: " + usuarioSesion.getUsername());
            return;
        }

        JDialog dialog = createStyledDialog("AUTENTICACIÓN DE OPERADOR", 420, 320);
        dialog.setLayout(new BorderLayout());

        Color fondoArcadeOscuro = new Color(20, 20, 30);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(fondoArcadeOscuro); 
        tabbedPane.setForeground(Color.CYAN);
        tabbedPane.setBorder(null); 

        JPanel panelLogin = new JPanel(new GridBagLayout()); 
        panelLogin.setBackground(fondoArcadeOscuro); panelLogin.setOpaque(true);
        
        JPanel panelRegistro = new JPanel(new GridBagLayout()); 
        panelRegistro.setBackground(fondoArcadeOscuro); panelRegistro.setOpaque(true);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font fontLabels = new Font("Monospaced", Font.BOLD, 13);
        Color colorLabels = Color.WHITE;

        // Inputs Login
        JTextField userLogin = new JTextField(); 
        userLogin.setPreferredSize(new Dimension(190, 28));
        userLogin.setBackground(new Color(15, 15, 20)); userLogin.setForeground(Color.WHITE);
        userLogin.setCaretColor(Color.CYAN); userLogin.setBorder(new LineBorder(new Color(0, 255, 255, 60), 1));
        userLogin.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPasswordField passLogin = new JPasswordField(); 
        passLogin.setPreferredSize(new Dimension(190, 28));
        passLogin.setBackground(new Color(15, 15, 20)); passLogin.setForeground(Color.WHITE);
        passLogin.setCaretColor(Color.CYAN); passLogin.setBorder(new LineBorder(new Color(0, 255, 255, 60), 1));
        passLogin.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton btnLogin = new JButton("ENTRAR AL SISTEMA");
        estilizarBotonPopup(btnLogin);

        JLabel lblUserL = new JLabel("USUARIO:"); lblUserL.setForeground(colorLabels); lblUserL.setFont(fontLabels);
        JLabel lblPassL = new JLabel("PASS:"); lblPassL.setForeground(colorLabels); lblPassL.setFont(fontLabels);
        
        gbc.gridx = 0; gbc.gridy = 0; panelLogin.add(lblUserL, gbc);
        gbc.gridx = 1; panelLogin.add(userLogin, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panelLogin.add(lblPassL, gbc);
        gbc.gridx = 1; panelLogin.add(passLogin, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.insets = new Insets(15, 8, 8, 8);
        panelLogin.add(btnLogin, gbc);

        btnLogin.addActionListener(e -> {
            String username = userLogin.getText().trim();
            String password = new String(passLogin.getPassword());

            if (username.equalsIgnoreCase("admin") && password.equals("admin1234")) {
                Usuario admin = usuarioRepo.findAll().stream().filter(u -> u.getUsername().equalsIgnoreCase("admin")).findFirst().orElse(null);
                if (admin == null) {
                    admin = new Usuario();
                    admin.setUsername("admin");
                    admin.setPassword(encoder.encode("admin1234"));
                    admin.setDarkMode(true);
                    usuarioRepo.save(admin);
                }
                usuarioSesion = admin;
                aplicarTema();
                JOptionPane.showMessageDialog(dialog, "Acceso Admin concedido.");
                dialog.dispose();
                return;
            }

            Usuario u = usuarioRepo.findAll().stream()
                    .filter(user -> user.getUsername().equalsIgnoreCase(username))
                    .findFirst().orElse(null);

            if (u != null && encoder.matches(password, u.getPassword())) {
                usuarioSesion = u;
                aplicarTema(); 
                JOptionPane.showMessageDialog(dialog, "¡Bienvenido, " + username + "!");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Usuario o contraseña incorrectos.");
            }
        });

        // Inputs Registro
        gbc.gridwidth = 1; gbc.insets = new Insets(6, 6, 6, 6); 

        JTextField userReg = new JTextField(); 
        userReg.setPreferredSize(new Dimension(190, 28));
        userReg.setBackground(new Color(15, 15, 20)); userReg.setForeground(Color.WHITE);
        userReg.setCaretColor(Color.CYAN); userReg.setBorder(new LineBorder(new Color(0, 255, 255, 60), 1));
        userReg.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPasswordField passReg = new JPasswordField(); 
        passReg.setPreferredSize(new Dimension(190, 28));
        passReg.setBackground(new Color(15, 15, 20)); passReg.setForeground(Color.WHITE);
        passReg.setCaretColor(Color.CYAN); passReg.setBorder(new LineBorder(new Color(0, 255, 255, 60), 1));
        passReg.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPasswordField passRegConfirm = new JPasswordField(); 
        passRegConfirm.setPreferredSize(new Dimension(190, 28));
        passRegConfirm.setBackground(new Color(15, 15, 20)); passRegConfirm.setForeground(Color.WHITE);
        passRegConfirm.setCaretColor(Color.CYAN); passRegConfirm.setBorder(new LineBorder(new Color(0, 255, 255, 60), 1));
        passRegConfirm.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton btnReg = new JButton("CREAR NUEVA CUENTA");
        estilizarBotonPopup(btnReg);

        JLabel lblUserR = new JLabel("NUEVO USER:"); lblUserR.setForeground(colorLabels); lblUserR.setFont(fontLabels);
        JLabel lblPassR = new JLabel("PASSWORD:"); lblPassR.setForeground(colorLabels); lblPassR.setFont(fontLabels);
        JLabel lblConfR = new JLabel("CONFIRMAR:"); lblConfR.setForeground(colorLabels); lblConfR.setFont(fontLabels);

        gbc.gridx = 0; gbc.gridy = 0; panelRegistro.add(lblUserR, gbc);
        gbc.gridx = 1; panelRegistro.add(userReg, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panelRegistro.add(lblPassR, gbc);
        gbc.gridx = 1; panelRegistro.add(passReg, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panelRegistro.add(lblConfR, gbc);
        gbc.gridx = 1; panelRegistro.add(passRegConfirm, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.insets = new Insets(12, 6, 6, 6);
        panelRegistro.add(btnReg, gbc);

        btnReg.addActionListener(e -> {
            String username = userReg.getText().trim();
            String password = new String(passReg.getPassword());
            String confirm = new String(passRegConfirm.getPassword());

            if (username.isEmpty() || password.isEmpty()) return;
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "Las contraseñas no coinciden.");
                return;
            }

            boolean existe = usuarioRepo.findAll().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));

            if (existe) {
                JOptionPane.showMessageDialog(dialog, "Ese nombre ya existe.");
            } else {
                Usuario nuevo = new Usuario();
                nuevo.setUsername(username);
                nuevo.setPassword(encoder.encode(password));
                nuevo.setDarkMode(true);
                usuarioSesion = usuarioRepo.save(nuevo);
                aplicarTema(); 
                JOptionPane.showMessageDialog(dialog, "Usuario creado exitosamente.");
                dialog.dispose();
            }
        });

        tabbedPane.addTab(" INICIAR SESIÓN ", panelLogin);
        tabbedPane.addTab(" CREAR JUGADOR ", panelRegistro);
        dialog.add(tabbedPane, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    /*
     "Este método gestiona la visualización de datos del perfil mediante un GridLayout para garantizar la alineación de las puntuaciones. 
     Además, implementa la actualización de credenciales en caliente, 
     asegurando que la nueva contraseña se procese mediante un hash de BCrypt antes de ser persistida en la base de datos a través del repositorio de Spring Data JPA."
     */
    private void mostrarPerfil() {
        JDialog d = createStyledDialog("PERFIL DEL JUGADOR", 400, 350);
        d.setLayout(new BorderLayout(10, 10));
        JPanel panelDatos = new JPanel(new GridLayout(0, 2, 10, 10));
        panelDatos.setOpaque(false);
        panelDatos.setBorder(new EmptyBorder(20, 20, 20, 20));

        Font fontLabel = new Font("Monospaced", Font.BOLD, 12);
        Color colLabel = Color.CYAN;
        
        addStatRow(panelDatos, "JUGADOR:", usuarioSesion.getUsername().toUpperCase(), fontLabel, colLabel);
        addStatRow(panelDatos, "SNAKE:", usuarioSesion.getPuntos_snake() + " pts", fontLabel, colLabel);
        addStatRow(panelDatos, "2048:", usuarioSesion.getPuntos_2048() + " pts", fontLabel, colLabel);
        addStatRow(panelDatos, "TETRIS:", usuarioSesion.getPuntos_tetris() + " pts", fontLabel, colLabel);

        JPanel panelAcciones = new JPanel(new GridBagLayout());
        panelAcciones.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        
        JPasswordField pass = new JPasswordField(15);
        pass.setBackground(new Color(25, 25, 35)); pass.setForeground(Color.WHITE);
        pass.setCaretColor(Color.CYAN); pass.setBorder(new LineBorder(Color.CYAN, 1));
        
        gbc.gridx = 0; gbc.gridy = 0; panelAcciones.add(new JLabel("NUEVA CONTRASEÑA:"), gbc);
        gbc.gridy = 1; panelAcciones.add(pass, gbc);
        
        JButton btnGuardar = new JButton("ACTUALIZAR");
        estilizarBotonPopup(btnGuardar);
        btnGuardar.addActionListener(e -> {
            usuarioSesion.setPassword(encoder.encode(new String(pass.getPassword())));
            usuarioRepo.save(usuarioSesion);
            d.dispose();
            JOptionPane.showMessageDialog(this, "Datos actualizados.");
        });
        gbc.gridy = 2; gbc.insets = new Insets(10,0,0,0); panelAcciones.add(btnGuardar, gbc);

        d.add(panelDatos, BorderLayout.CENTER);
        d.add(panelAcciones, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    /*
      RESUMEN:
"Este método implementa la gestión de preferencias del usuario y el control de sesión. 
Destaca el uso de GridBagLayout para un diseño responsivo dentro del diálogo y la integración con Spring Data JPA mediante el método save, 
lo que permite que la personalización del 'Modo Oscuro' persista de forma permanente en la base de datos relacional."
     */
    private void mostrarAjustes() {
        JDialog d = createStyledDialog("CONFIGURACIÓN", 350, 300);
        d.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JCheckBox checkDark = new JCheckBox("  MODO OSCURO", usuarioSesion.isDarkMode());
        checkDark.setOpaque(false);
        checkDark.setForeground(Color.WHITE);
        checkDark.setFont(new Font("Monospaced", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; d.add(checkDark, gbc);

        JButton btnLogout = new JButton("CERRAR SESIÓN");
        estilizarBotonPopup(btnLogout);
        btnLogout.addActionListener(e -> {
            usuarioSesion = null;
            aplicarTema();
            d.dispose();
            JOptionPane.showMessageDialog(this, "Sesión finalizada.");
        });
        gbc.gridy = 1; d.add(btnLogout, gbc);

        JButton btnGuardar = new JButton("GUARDAR CAMBIOS");
        estilizarBotonPopup(btnGuardar);
        btnGuardar.addActionListener(e -> {
            usuarioSesion.setDarkMode(checkDark.isSelected());
            usuarioRepo.save(usuarioSesion);
            aplicarTema();
            d.dispose();
        });
        gbc.gridy = 2; d.add(btnGuardar, gbc);
        d.setVisible(true);
    }
    
    private int calcularTotal(Usuario u) {
    	int ptsSnake = (u.getPuntos_snake() > 0) ? Math.max(0, 5000 - u.getPuntos_snake()) : 0;
    	return ptsSnake + u.getPuntos_2048() + u.getPuntos_tetris();
    }

    // Método de Ranking modificado con Renderizador de Celdas seguro para aplicar colores de medallas reales
    private void mostrarRanking() {
        JDialog ventanaRanking = createStyledDialog("RANKING GLOBAL DE MÉRITOS", 650, 550);
        ventanaRanking.setLayout(new BorderLayout(10, 10));
        
        List<Usuario> usuarios = usuarioRepo.findAll();
        usuarios.removeIf(u -> "admin".equalsIgnoreCase(u.getUsername()));
        usuarios.sort((u1, u2) -> Integer.compare(calcularTotal(u2), calcularTotal(u1)));
        
        String[] columnas = {"POS", "JUGADOR", "TIME SNAKE", "MAX 2048", "PUNTOS TETRIS", "TOTAL"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) { 
            @Override public boolean isCellEditable(int r, int c) { return false; } 
        };
        
        int pos = 1;
        for (Usuario u : usuarios) {
            String labelPos = (pos==1) ? "[1st]" : (pos==2) ? "[2nd]" : (pos==3) ? "[3rd]" : String.valueOf(pos) + "º";
            modelo.addRow(new Object[]{ labelPos, u.getUsername(), u.getPuntos_snake() + "s", u.getPuntos_2048(), u.getPuntos_tetris(), calcularTotal(u) });
            pos++;
        }
        
        JTable tabla = new JTable(modelo);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setBackground(new Color(25, 25, 35));
        tabla.setForeground(Color.WHITE);
        tabla.setRowHeight(25);
        tabla.setSelectionBackground(new Color(0, 255, 255, 40));
        tabla.setSelectionForeground(Color.CYAN);
        tabla.setGridColor(new Color(0, 255, 255, 30));
        tabla.getColumnModel().getColumn(0).setPreferredWidth(80);
        
        // --- INYECCIÓN DEL FILTRADO DE COLOR EN CALIENTE ---
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable t, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                java.awt.Component c = super.getTableCellRendererComponent(t, val, isSelected, hasFocus, row, col);
                
                // Si la celda está seleccionada por el usuario, respetamos el color de selección cian
                if (isSelected) {
                    c.setForeground(Color.CYAN);
                } else {
                    // Evaluamos el texto de la primera columna (Posición) para inyectar los colores metalizados
                    String posText = t.getValueAt(row, 0).toString();
                    if ("[1st]".equals(posText)) {
                        c.setForeground(new Color(255, 215, 0)); // Dorado Brillante (Oro)
                    } else if ("[2nd]".equals(posText)) {
                        c.setForeground(new Color(192, 192, 192)); // Plateado Estilizado (Plata)
                    } else if ("[3rd]".equals(posText)) {
                        c.setForeground(new Color(205, 127, 50)); // Bronce Real
                    } else {
                        c.setForeground(Color.WHITE); // Texto estándar para el resto de posiciones
                    }
                }
                return c;
            }
        });
        
        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(40, 40, 50));
        header.setForeground(Color.CYAN);
        header.setBorder(new LineBorder(new Color(0, 255, 255, 50), 1));

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.getViewport().setBackground(new Color(20, 20, 30));
        scrollPane.setBorder(new LineBorder(new Color(0, 255, 255, 30), 1));
        
        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.setOpaque(false);
        panelContenedor.setBorder(new EmptyBorder(15, 15, 15, 15));
        panelContenedor.add(scrollPane, BorderLayout.CENTER);
        ventanaRanking.add(panelContenedor, BorderLayout.CENTER);
        
        boolean esAdmin = usuarioSesion != null && "admin".equalsIgnoreCase(usuarioSesion.getUsername());
        
        if (esAdmin) {
            JPanel panelAdmin = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            panelAdmin.setOpaque(false);
            
            JButton btnBorrar = new JButton("BORRAR REGISTRO");
            JButton btnEditar = new JButton("EDITAR OPERADOR");
            
            estilizarBotonPopup(btnEditar);
            estilizarBotonPopup(btnBorrar);
            
            btnBorrar.setBorder(new LineBorder(new Color(255, 0, 0, 100), 1));
            btnBorrar.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btnBorrar.setBackground(new Color(70, 20, 20)); }
                public void mouseExited(MouseEvent e) { btnBorrar.setBackground(new Color(40, 40, 50)); }
            });
            
            btnBorrar.addActionListener(e -> {
                int fila = tabla.getSelectedRow();
                if (fila != -1) {
                    Usuario u = usuarios.get(fila);
                    if (JOptionPane.showConfirmDialog(ventanaRanking, "¿Seguro que deseas eliminar permanentemente a " + u.getUsername() + "?", "CONFIRMAR BAJA", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    	usuarioRepo.delete(u);
                        ventanaRanking.dispose();
                        mostrarRanking();
                    }
                }
            });
            
            btnEditar.addActionListener(e -> {
                int fila = tabla.getSelectedRow();
                if (fila != -1) {
                    Usuario u = usuarios.get(fila);
                    JTextField txtTetris = new JTextField(String.valueOf(u.getPuntos_tetris()));
                    JTextField txt2048 = new JTextField(String.valueOf(u.getPuntos_2048()));
                    JTextField txtSnake = new JTextField(String.valueOf(u.getPuntos_snake()));
                    
                    JTextField[] inputs = {txtTetris, txt2048, txtSnake};
                    for(JTextField input : inputs) {
                        input.setBackground(new Color(15, 15, 20)); input.setForeground(Color.WHITE);
                        input.setCaretColor(Color.CYAN); input.setBorder(new LineBorder(Color.CYAN, 1));
                    }
                    
                    JPanel panelForm = new JPanel(new GridLayout(3, 2, 8, 8));
                    panelForm.setBackground(new Color(20, 20, 30));
                    
                    JLabel l1 = new JLabel("Tetris:"); l1.setForeground(Color.WHITE);
                    JLabel l2 = new JLabel("2048:"); l2.setForeground(Color.WHITE);
                    JLabel l3 = new JLabel("Snake:"); l3.setForeground(Color.WHITE);
                    
                    panelForm.add(l1); panelForm.add(txtTetris);
                    panelForm.add(l2); panelForm.add(txt2048);
                    panelForm.add(l3); panelForm.add(txtSnake);
                    
                    int res = JOptionPane.showConfirmDialog(ventanaRanking, panelForm, "MODIFICAR HISTORIAL DE: " + u.getUsername().toUpperCase(), JOptionPane.OK_CANCEL_OPTION);
                    if (res == JOptionPane.OK_OPTION) {
                        try {
                            u.setPuntos_tetris(Integer.parseInt(txtTetris.getText()));
                            u.setPuntos_2048(Integer.parseInt(txt2048.getText()));
                            u.setPuntos_snake(Integer.parseInt(txtSnake.getText()));
                            usuarioRepo.save(u);
                            ventanaRanking.dispose();
                            mostrarRanking();
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(ventanaRanking, "Error numérico");
                        }
                    }
                }
            });
            panelAdmin.add(btnEditar);
            panelAdmin.add(btnBorrar);
            ventanaRanking.add(panelAdmin, BorderLayout.SOUTH);
        }
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

    private void estilizarBotonPopup(JButton b) {
        b.setPreferredSize(new Dimension(220, 40));
        b.setBackground(new Color(40, 40, 50));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(Color.CYAN, 1));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(60, 60, 70)); }
            public void mouseExited(MouseEvent e) { b.setBackground(new Color(40, 40, 50)); }
        });
    }

    private void addStatRow(JPanel p, String label, String value, Font f, Color c) {
        JLabel l1 = new JLabel(label); l1.setForeground(c); l1.setFont(f);
        JLabel l2 = new JLabel(value); l2.setForeground(Color.WHITE); l2.setFont(f);
        p.add(l1); p.add(l2);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception e) {}
        ConfigurableApplicationContext context = new SpringApplicationBuilder(MenuPrincipal.class).headless(false).run(args);
        SwingUtilities.invokeLater(() -> {
            MenuPrincipal frame = context.getBean(MenuPrincipal.class);
            frame.setVisible(true);
        });
    }
}