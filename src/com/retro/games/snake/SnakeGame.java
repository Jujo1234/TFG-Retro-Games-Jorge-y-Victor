package com.retro.games.snake;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

import com.retro.main.model.Usuario;
import com.retro.main.repository.UsuarioRepository;

/*
 Declaras la clase SnakeGame. Al usar extends JPanel, 
 significa que esta clase es un panel de dibujo. No es una ventana entera, 
 sino el lienzo que se incrusta dentro del JFrame que lanza el menú principal.
 */
public class SnakeGame extends JPanel {
	// El identificador único de versión para la serialización de la clase, requerido por Java al heredar de un componente visual.
    private static final long serialVersionUID = 1L;

    // Define el tamaño en píxeles de cada "casilla" o cuadrado del juego (la cabeza, el cuerpo, las manzanas y las paredes miden $25 \times 25$ píxeles).
    private final int TILE_SIZE = 25;
    // El ancho total de la pantalla del juego en píxeles ($32 \text{ casillas} \times 25 \text{ píxeles} = 800$).
    private final int WIDTH = 800; 
    // El alto total de la pantalla en píxeles ($25 \text{ casillas} \times 25 \text{ píxeles} = 625$, aunque jugables son 23 filas).
    private final int HEIGHT = 625; 
    // El número de casillas horizontales que componen la rejilla del juego.
    private final int GRID_WIDTH = 32;
    // El número de casillas verticales que componen la rejilla del juego.
    private final int GRID_HEIGHT = 23;

    // Un ArrayList que almacena objetos Point (coordenadas $X, Y$). 
    // Cada punto es una parte del cuerpo de la serpiente. 
    // La posición 0 es la cabeza y las siguientes son la cola.
    private final ArrayList<Point> snake = new ArrayList<>();
    // Lista de coordenadas donde están las manzanas que la serpiente debe comer en el nivel actual.
    private final ArrayList<Point> applesInLevel = new ArrayList<>();
    // Lista de coordenadas de los enemigos o fantasmas que se mueven por el mapa para intentar matarte.
    private final ArrayList<Point> enemies = new ArrayList<>();
    // Línea clave (Matriz bidimensional). Es el mapa o laberinto del juego. Si maze[y][x] vale 1, 
    // significa que en esa casilla hay una pared indestructible; si vale 0, está vacía.
    private int[][] maze = new int[GRID_HEIGHT][GRID_WIDTH];
    
    // Variable entera que almacena el nivel actual en el que se encuentra el jugador.
    private int currentLevel = 1;
    // Bandera que indica si el bucle del juego está activo (movimiento de la serpiente, físicas). Si es false, el juego está en pausa o has muerto.
    private boolean running = false;
    // Se vuelve true cuando te comes todas las manzanas de la pantalla, indicando que has superado el nivel actual.
    private boolean levelCleared = false;
    // Se vuelve true cuando superas el último nivel del juego, activando la pantalla de victoria final.
    private boolean gameFinished = false;

    // Guarda el milisegundo exacto en el que el jugador realiza el primer movimiento de la partida.
    private long startTime;
    // Controla si el cronómetro ya está en marcha para evitar reiniciarlo cada vez que pulsas una tecla de dirección.
    private boolean timerStarted = false;
    // Almacena el tiempo total que ha tardado el jugador en pasarse el juego. Este valor es el que luego lee el menú principal para transformarlo en puntuación.
    private int tiempoFinalSegundos = 0;

    // El objeto que contiene la información del usuario de la sesión actual (se le pasa desde el menú principal).
    private Usuario jugadorActual;
    // El repositorio inyectado de Spring. Permite que el juego guarde datos en las tablas SQL directamente si fuera necesario.
    private UsuarioRepository repo;

    // Objeto de la librería de audio de Java (javax.sound.sampled). Almacena y reproduce el archivo de sonido de la música retro en bucle mientras juegas.
    private Clip musicaFondo;

    // Usuario jugador: Recibe como parámetro el objeto con los datos del jugador actual que tiene la sesión iniciada en el menú.
   // Recibe la referencia del repositorio de Spring Boot para poder interactuar con la base de datos SQL.
    public SnakeGame(Usuario jugador, UsuarioRepository repo) {
    	// Guarda el objeto del jugador recibido en la variable global jugadorActual de esta clase. De este modo, 
    	// el juego sabe en todo momento quién está jugando (para mirar si tiene el modo oscuro, su nombre, etc.).
        this.jugadorActual = jugador;
        // Almacena la referencia del repositorio de la base de datos en la variable local repo para poder usar las funciones de guardado más adelante.
        this.repo = repo;
        // Establece el tamaño preferido del panel de juego utilizando las constantes que vimos en la cabecera ($800 \times 625$ píxeles).
        // Cuando en el menú principal ejecutas la instrucción v.pack(), 
        // la ventana se encoge o estira hasta clavarse exactamente en estas dimensiones.
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        // Aplica un color de fondo al lienzo del juego. Es un tono azul/gris espacial muy oscuro (casi negro)
        this.setBackground(new Color(15, 15, 20)); 
        // Activa la capacidad de que este panel reciba el "foco". En Java, si un panel no es focusable, 
        // ignorará por completo todo lo que teclees, 
        // haciendo imposible controlar a la serpiente.
        this.setFocusable(true);
        // Enlaza el panel con un "escuchador" de teclado personalizado (MyKeyAdapter).
        // A partir de esta línea, cada vez que el usuario pulse una flecha de dirección o las teclas WASD, 
        // el juego capturará el evento para desviar la trayectoria de la serpiente.
        this.addKeyListener(new MyKeyAdapter());
        
        // Invoca al método interno encargado de construir el escenario del Nivel 1.
        // Esta función se ocupa de limpiar la pantalla, drawing las paredes del primer laberinto, 
        // posicionar a la serpiente en su casilla de salida y esparcir las manzanas correspondientes.
        loadLevel(1);
    }

    private void playMusicaFondo() {
        try {
            if (musicaFondo != null) {
                musicaFondo.stop();
                musicaFondo.setFramePosition(0); 
            } else {
                File musicPath = new File("res/musica_fondo.wav");
                if (musicPath.exists()) {
                    AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                    musicaFondo = AudioSystem.getClip();
                    musicaFondo.open(audioInput);
                    musicaFondo.loop(Clip.LOOP_CONTINUOUSLY);
                }
            }
            if (musicaFondo != null) musicaFondo.start();
        } catch (Exception e) {
            System.err.println("Error música: " + e.getMessage());
        }
    }

    private void stopMusicaFondo() {
        if (musicaFondo != null && musicaFondo.isRunning()) {
            musicaFondo.stop();
        }
    }

    // MÉTODO PARA DETENER TODO DESDE EL MENÚ
    public void pararMusica() {
        stopMusicaFondo();
        if (musicaFondo != null) musicaFondo.close();
    }

    private void playSonidoEfecto(String archivo) {
        try {
            File soundPath = new File("res/" + archivo);
            if (soundPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
            }
        } catch (Exception e) {
            System.err.println("Error efecto " + archivo + ": " + e.getMessage());
        }
    }

    private void playSonidoComida() {
        playSonidoEfecto("comida.wav");
    }

    public void loadLevel(int level) {
        this.currentLevel = level;
        this.running = true;
        this.levelCleared = false;
        this.gameFinished = false;
        
        playMusicaFondo();

        if (level == 1) {
            timerStarted = false;
            tiempoFinalSegundos = 0;
        }

        snake.clear();
        applesInLevel.clear();
        enemies.clear();
        snake.add(new Point(TILE_SIZE * 2, TILE_SIZE * 2)); 
        generateMapData(level);
        repaint();
    }

    private void setTile(int x, int y, int type) {
        if (x < 0 || x >= GRID_WIDTH || y < 0 || y >= GRID_HEIGHT) return;
        if (type == 1) maze[y][x] = 1; 
        if (type == 2) applesInLevel.add(new Point(x * TILE_SIZE, y * TILE_SIZE)); 
        if (type == 3) enemies.add(new Point(x * TILE_SIZE, y * TILE_SIZE)); 
    }

    private void generateMapData(int level) {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) maze[y][x] = 0;
        }
        for (int x = 0; x < 32; x++) { setTile(x, 0, 1); setTile(x, 22, 1); }
        for (int y = 0; y < 23; y++) { setTile(0, y, 1); setTile(31, y, 1); }

        if (level == 1) {
            for (int x = 8; x < 25; x += 4) setTile(x, 11, 2);
        } else if (level == 2) {
            for (int y = 5; y < 18; y++) { setTile(10, y, 1); setTile(21, y, 1); }
            setTile(5, 5, 2); setTile(26, 17, 2); setTile(15, 11, 2);
        } else if (level == 3) {
            for (int x = 4; x < 28; x += 4) {
                for (int y = 3; y < 20; y++) { if (y != 11) setTile(x, y, 1); }
            }
            for (int x = 6; x < 30; x += 8) setTile(x, 11, 2);
            setTile(16, 11, 3);
        } else if (level == 4) {
            for (int x = 6; x < 26; x += 4) {
                setTile(x, 5, 1); setTile(x, 6, 1); setTile(x, 16, 1); setTile(x, 17, 1);
                setTile(x, 11, 2);
            }
            setTile(16, 5, 3); setTile(16, 17, 3);
        } else if (level == 5) {
            for (int x = 5; x < 27; x += 5) {
                for (int y = 2; y < 21; y++) { if (y != 6 && y != 16) setTile(x, y, 1); }
            }
            setTile(2, 20, 2); setTile(29, 2, 2);
            for (int x = 7; x < 25; x += 5) {
                setTile(x, 6, 2); setTile(x, 16, 2); setTile(x, 11, 3);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(25, 25, 35));
        for(int i=0; i<WIDTH; i+=TILE_SIZE) g2d.drawLine(i, 0, i, 575);
        for(int i=0; i<575; i+=TILE_SIZE) g2d.drawLine(0, i, WIDTH, i);

        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (maze[y][x] == 1) {
                    g2d.setPaint(new GradientPaint(x*TILE_SIZE, y*TILE_SIZE, new Color(80, 80, 90), 
                                                   (x+1)*TILE_SIZE, (y+1)*TILE_SIZE, new Color(40, 40, 50)));
                    g2d.fillRoundRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE, 8, 8);
                    g2d.setColor(new Color(100, 100, 110));
                    g2d.drawRoundRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE-1, TILE_SIZE-1, 8, 8);
                }
            }
        }

        for (Point p : applesInLevel) {
            int x = p.x; int y = p.y;
            g2d.setColor(new Color(101, 67, 33)); g2d.fillRect(x + 11, y + 2, 3, 6);
            g2d.setColor(new Color(34, 177, 76)); g2d.fillOval(x + 13, y + 2, 8, 5);
            g2d.setPaint(new RadialGradientPaint(new Point(x + 12, y + 15), 10, new float[]{0f, 1f}, 
                        new Color[]{new Color(255, 60, 60), new Color(180, 0, 0)}));
            g2d.fillOval(x + 4, y + 7, 10, 14); g2d.fillOval(x + 11, y + 7, 10, 14);
            g2d.setColor(new Color(255, 255, 255, 130)); g2d.fillOval(x + 7, y + 10, 5, 3);
        }

        for (Point e : enemies) {
            g2d.setColor(new Color(255, 255, 0));
            g2d.fillArc(e.x + 3, e.y + 3, TILE_SIZE - 6, TILE_SIZE - 6, 0, 180);
            g2d.fillRect(e.x + 3, e.y + TILE_SIZE/2, TILE_SIZE - 6, TILE_SIZE/3);
            g2d.setColor(Color.BLACK);
            g2d.fillOval(e.x + 7, e.y + 8, 4, 4); g2d.fillOval(e.x + 14, e.y + 8, 4, 4);
        }

        for (int i = snake.size() - 1; i >= 0; i--) {
            Point p = snake.get(i);
            if (i == 0) { 
                g2d.setPaint(new GradientPaint(p.x, p.y, new Color(50, 255, 50), p.x + TILE_SIZE, p.y + TILE_SIZE, new Color(0, 150, 0)));
                g2d.fillRoundRect(p.x, p.y, TILE_SIZE, TILE_SIZE, 12, 12);
                g2d.setColor(Color.WHITE); g2d.fillOval(p.x + 4, p.y + 4, 6, 6); g2d.fillOval(p.x + 15, p.y + 4, 6, 6);
                g2d.setColor(Color.BLACK); g2d.fillOval(p.x + 6, p.y + 5, 3, 3); g2d.fillOval(p.x + 17, p.y + 5, 3, 3);
            } else { 
                float ratio = (float) i / snake.size();
                int greenValue = Math.max(100, 200 - (int)(ratio * 100));
                g2d.setColor(new Color(0, greenValue, 0));
                g2d.fillRoundRect(p.x + 1, p.y + 1, TILE_SIZE - 2, TILE_SIZE - 2, 8, 8);
            }
        }

        g2d.setPaint(new GradientPaint(0, 575, new Color(30, 30, 35), 0, HEIGHT, new Color(15, 15, 20)));
        g2d.fillRect(0, 575, WIDTH, 50);
        g2d.setColor(Color.CYAN); g2d.drawRect(0, 575, WIDTH-1, 49);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Consolas", Font.BOLD, 18));
        g2d.drawString("NIVEL: " + currentLevel, 30, 608);
        g2d.drawString("RESTAN: " + applesInLevel.size(), 180, 608);

        if (gameFinished) drawFinalOverlay(g2d);
        else if (levelCleared) drawOverlay(g2d, "¡NIVEL " + currentLevel + " COMPLETADO!", "Presiona 'N' para el siguiente nivel", Color.GREEN);
        else if (!running) drawOverlay(g2d, "FIN DEL JUEGO", "Presiona 'R' para reintentar", Color.RED);
    }

    private void drawOverlay(Graphics2D g2d, String title, String subtitle, Color mainColor) {
        // Fondo oscurecido total de ambiente
        g2d.setColor(new Color(10, 10, 15, 230));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Caja de interfaz central estilizada
        int panelW = 560;
        int panelH = 260;
        int panelX = (WIDTH - panelW) / 2;
        int panelY = (HEIGHT - panelH) / 2 - 20;

        // Sombra de la caja central
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(panelX + 8, panelY + 8, panelW, panelH, 20, 20);

        // Fondo degradado del panel
        GradientPaint panelGrad = new GradientPaint(panelX, panelY, new Color(25, 25, 35), panelX, panelY + panelH, new Color(15, 15, 20));
        g2d.setPaint(panelGrad);
        g2d.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);

        // Marco exterior brillante de estilo neón del color principal
        g2d.setColor(new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 180));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2d.setStroke(new BasicStroke(1f)); // Restaurar grosor de línea base

        FontMetrics fm;

        // TEXTO PRINCIPAL: EFECTO GLOW (Brillo Retro)
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 38));
        fm = g2d.getFontMetrics();
        int titleX = panelX + (panelW - fm.stringWidth(title)) / 2;
        
        // Dibujo de brillo difuminado detrás del texto
        g2d.setColor(new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 60));
        g2d.drawString(title, titleX - 2, panelY + 92);
        g2d.drawString(title, titleX + 2, panelY + 92);
        g2d.drawString(title, titleX, panelY + 90 - 2);
        g2d.drawString(title, titleX, panelY + 90 + 2);
        // Texto frontal definitivo
        g2d.setColor(mainColor);
        g2d.drawString(title, titleX, panelY + 90);

        // Separador horizontal neón sutil
        g2d.setColor(new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 100));
        g2d.drawLine(panelX + 40, panelY + 130, panelX + panelW - 40, panelY + 130);

        // Subtítulo centrado
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Consolas", Font.PLAIN, 20));
        fm = g2d.getFontMetrics();
        g2d.drawString(subtitle, panelX + (panelW - fm.stringWidth(subtitle)) / 2, panelY + 180);
    }

    private void drawFinalOverlay(Graphics2D g2d) {
        // Fondo oscurecido total de ambiente
        g2d.setColor(new Color(10, 10, 15, 230));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Caja de interfaz central estilizada (Dashboard de Victoria)
        int panelW = 560;
        int panelH = 360;
        int panelX = (WIDTH - panelW) / 2;
        int panelY = (HEIGHT - panelH) / 2 - 20;

        // Sombra de la caja central
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(panelX + 8, panelY + 8, panelW, panelH, 20, 20);

        // Fondo degradado del panel de estadísticas
        GradientPaint panelGrad = new GradientPaint(panelX, panelY, new Color(25, 25, 35), panelX, panelY + panelH, new Color(15, 15, 20));
        g2d.setPaint(panelGrad);
        g2d.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);

        // Marco exterior brillante de estilo neón dorado
        g2d.setColor(new Color(255, 215, 0, 180));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2d.setStroke(new BasicStroke(1f)); // Restaurar grosor de línea base

        FontMetrics fm;

        // TEXTO PRINCIPAL: EFECTO GLOW (Brillo Retro)
        String titleText = "MISIÓN CUMPLIDA";
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 38));
        fm = g2d.getFontMetrics();
        int titleX = panelX + (panelW - fm.stringWidth(titleText)) / 2;
        
        // Dibujo de brillo difuminado detrás del texto
        g2d.setColor(new Color(255, 215, 0, 60));
        g2d.drawString(titleText, titleX - 2, panelY + 62);
        g2d.drawString(titleText, titleX + 2, panelY + 62);
        g2d.drawString(titleText, titleX, panelY + 60 - 2);
        g2d.drawString(titleText, titleX, panelY + 60 + 2);
        // Texto frontal definitivo
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString(titleText, titleX, panelY + 60);

        // Separador horizontal neón dorado sutil
        g2d.setColor(new Color(255, 215, 0, 100));
        g2d.drawLine(panelX + 40, panelY + 90, panelX + panelW - 40, panelY + 90);

        // PANEL DE ESTADÍSTICAS DEL TFG
        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
        fm = g2d.getFontMetrics();
        
        // Fila 1: Jugador
        String userStr = jugadorActual != null ? jugadorActual.getUsername().toUpperCase() : "INVITADO";
        g2d.setColor(Color.CYAN);
        g2d.drawString("OPERADOR:", panelX + 60, panelY + 140);
        g2d.setColor(Color.WHITE);
        g2d.drawString(userStr, panelX + panelW - 60 - fm.stringWidth(userStr), panelY + 140);

        // Fila 2: Tiempo Record
        String timeStr = tiempoFinalSegundos + " SEGUNDOS";
        g2d.setColor(Color.CYAN);
        g2d.drawString("TIEMPO RÉCORD:", panelX + 60, panelY + 180);
        g2d.setColor(Color.WHITE);
        g2d.drawString(timeStr, panelX + panelW - 60 - fm.stringWidth(timeStr), panelY + 180);

        // Fila 3: Autores del Software
        String authStr = "JORGE & VICTOR";
        g2d.setColor(Color.CYAN);
        g2d.drawString("DESARROLLADORES:", panelX + 60, panelY + 220);
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawString(authStr, panelX + panelW - 60 - fm.stringWidth(authStr), panelY + 220);

        // Separador horizontal inferior sutil
        g2d.setColor(new Color(255, 215, 0, 40));
        g2d.drawLine(panelX + 40, panelY + 250, panelX + panelW - 40, panelY + 250);

        // BOTONES DE CONTROL DE INTERFAZ (Controles de consola)
        g2d.setFont(new Font("Consolas", Font.BOLD, 14));
        fm = g2d.getFontMetrics();
        
        String prompt1 = "[M] VOLVER AL MENÚ OS";
        String prompt2 = "[R] REINICIAR JUEGO";
        String prompt3 = "[V] REINICIAR A NIVEL 1";
        
        int spacing = panelW / 3;
        g2d.setColor(new Color(255, 80, 80)); // Rojo retro para salir
        g2d.drawString(prompt1, panelX + (spacing - fm.stringWidth(prompt1))/2 + 5, panelY + 295);
        
        g2d.setColor(Color.GREEN); // Verde para reintentar nivel
        g2d.drawString(prompt2, panelX + spacing + (spacing - fm.stringWidth(prompt2))/2, panelY + 295);
        
        g2d.setColor(Color.YELLOW); // Dorado/Amarillo para empezar de cero
        g2d.drawString(prompt3, panelX + spacing*2 + (spacing - fm.stringWidth(prompt3))/2 - 5, panelY + 295);
    }

    private void manualMove(int dx, int dy) {
        if (!running || levelCleared || gameFinished) return;

        if (!timerStarted && currentLevel == 1) {
            startTime = System.currentTimeMillis();
            timerStarted = true;
        }

        Point head = new Point(snake.get(0).x + dx, snake.get(0).y + dy);
        
        if (head.x < 0) head.x = WIDTH - TILE_SIZE;
        if (head.x >= WIDTH) head.x = 0;
        if (head.y < 0) head.y = 550;
        if (head.y > 550) head.y = 0;

        if (maze[head.y/TILE_SIZE][head.x/TILE_SIZE] == 1 || checkCuerpo(head)) {
            morir("choque.wav");
            return;
        } else {
            snake.add(0, head);
            Point eaten = null;
            for (Point p : applesInLevel) { if (p.equals(head)) { eaten = p; break; } }
            
            if (eaten != null) {
                playSonidoComida(); 
                applesInLevel.remove(eaten);
                if (applesInLevel.isEmpty()) {
                    stopMusicaFondo(); 
                    playSonidoEfecto("victoria.wav"); 
                    
                    if (currentLevel == 5) {
                        gameFinished = true;
                        long endTime = System.currentTimeMillis();
                        tiempoFinalSegundos = (int) ((endTime - startTime) / 1000);
                        if (jugadorActual != null) {
                            jugadorActual.setPuntos_snake(tiempoFinalSegundos);
                            repo.save(jugadorActual);
                        }
                    } else {
                        levelCleared = true;
                    }
                }
            } else { 
                snake.remove(snake.size() - 1); 
            }

            moveEnemies();

            if (checkFantasma(snake.get(0))) {
                morir("fantasma.wav");
                return;
            }
        }
        repaint();
    }
    
    private boolean checkCuerpo(Point head) {
        for (int i = 1; i < snake.size(); i++) if (snake.get(i).equals(head)) return true;
        return false;
    }

    private boolean checkFantasma(Point head) {
        for (Point e : enemies) if (e.equals(head)) return true;
        return false;
    }

    private void morir(String sonidoCausa) {
        running = false;
        stopMusicaFondo(); 
        playSonidoEfecto(sonidoCausa);   
        repaint();
    }

    private void moveEnemies() {
        for (Point e : enemies) {
            int dir = (int)(Math.random() * 4);
            int nx = e.x, ny = e.y;
            if (dir == 0) ny -= TILE_SIZE; else if (dir == 1) ny += TILE_SIZE;
            else if (dir == 2) nx -= TILE_SIZE; else nx += TILE_SIZE;
            if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < 575 && maze[ny/TILE_SIZE][nx/TILE_SIZE] == 0) {
                e.x = nx; e.y = ny;
            }
        }
    }

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (gameFinished) {
                if (key == KeyEvent.VK_M) {
                    stopMusicaFondo();
                    JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(SnakeGame.this);
                    if(topFrame != null) topFrame.dispose();
                }
                if (key == KeyEvent.VK_R) loadLevel(5);
                if (key == KeyEvent.VK_V) loadLevel(1);
            } else if (levelCleared) {
                if (key == KeyEvent.VK_N && currentLevel < 5) loadLevel(currentLevel + 1);
                if (key == KeyEvent.VK_R) loadLevel(currentLevel);
            } else if (!running) {
                if (key == KeyEvent.VK_R) loadLevel(currentLevel);
            } else {
                if (key == KeyEvent.VK_UP) manualMove(0, -TILE_SIZE);
                else if (key == KeyEvent.VK_DOWN) manualMove(0, TILE_SIZE);
                else if (key == KeyEvent.VK_LEFT) manualMove(-TILE_SIZE, 0);
                else if (key == KeyEvent.VK_RIGHT) manualMove(TILE_SIZE, 0);
            }
        }
    }
}