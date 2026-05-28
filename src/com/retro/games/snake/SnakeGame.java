package com.retro.games.snake;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.JButton; 
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import javax.swing.ImageIcon; 

import com.retro.main.model.Usuario;
import com.retro.main.repository.UsuarioRepository;

public class SnakeGame extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    private final int TILE_SIZE = 25;
    private final int WIDTH = 800; 
    private final int HEIGHT = 625; 
    private final int GRID_WIDTH = 32;
    private final int GRID_HEIGHT = 23;

    private final ArrayList<Point> snake = new ArrayList<>();
    private final ArrayList<Point> applesInLevel = new ArrayList<>();
    private final ArrayList<Point> enemies = new ArrayList<>();
    private int[][] maze = new int[GRID_HEIGHT][GRID_WIDTH];
    
    private int currentLevel = 1;
    private boolean running = false;
    private boolean levelCleared = false;
    private boolean gameFinished = false;

    private long startTime;
    private boolean timerStarted = false;
    private int tiempoFinalSegundos = 0;

    private Usuario jugadorActual;
    private UsuarioRepository repo;

    private Clip musicaFondo;
    private boolean musicaActivada = true; 
    private boolean efectosActivados = true; 

    private JButton btnMusica;
    private JButton btnEfectos;

    private Timer gameLoopTimer;
    private int currentDirection = KeyEvent.VK_RIGHT;

    // VARIABLES EXCLUSIVAS PARA LA PANTALLA DE CONTROLES
    private boolean mostrarControles = true;
    private Image imgControles;
    private Timer timerParpadeo;
    private boolean textoVisible = true;

    public SnakeGame(Usuario jugador, UsuarioRepository repo) {
        this.jugadorActual = jugador;
        this.repo = repo;
        
        this.setLayout(null);
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(new Color(15, 15, 20)); 
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        
        // Cargar la guía de controles desde la carpeta res
        File fileImg = new File("res/snakeControles.png");
        if (!fileImg.exists()) {
            // Reintento alternativo si estuviera en .jpg
            fileImg = new File("res/snakeControles.jpg");
        }
        if (fileImg.exists()) {
            imgControles = new ImageIcon(fileImg.getAbsolutePath()).getImage();
        }

        // Timer para el parpadeo del mensaje inferior
        timerParpadeo = new Timer(500, e -> {
            textoVisible = !textoVisible;
            repaint();
        });
        timerParpadeo.start();

        // --- BOTÓN 1: MÚSICA DE FONDO ---
        btnMusica = new JButton("MÚSICA: ON");
        btnMusica.setBounds(530, 585, 110, 30);
        btnMusica.setFont(new Font("Consolas", Font.BOLD, 12));
        btnMusica.setBackground(new Color(30, 30, 45));
        btnMusica.setForeground(Color.CYAN);
        btnMusica.setBorder(javax.swing.BorderFactory.createLineBorder(Color.CYAN, 1));
        btnMusica.setFocusable(false); 
        btnMusica.setVisible(false); 
        
        btnMusica.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                musicaActivada = !musicaActivada;
                if (musicaActivada) {
                    btnMusica.setText("MÚSICA: ON");
                    btnMusica.setForeground(Color.CYAN);
                    btnMusica.setBorder(javax.swing.BorderFactory.createLineBorder(Color.CYAN, 1));
                    playMusicaFondo();
                } else {
                    btnMusica.setText("MÚSICA: OFF");
                    btnMusica.setForeground(Color.LIGHT_GRAY);
                    btnMusica.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                    stopMusicaFondo();
                }
                repaint();
                requestFocusInWindow();
            }
        });
        this.add(btnMusica);

        // --- BOTÓN 2: EFECTOS DE SONIDO ---
        btnEfectos = new JButton("EFECTOS: ON");
        btnEfectos.setBounds(650, 585, 110, 30);
        btnEfectos.setFont(new Font("Consolas", Font.BOLD, 12));
        btnEfectos.setBackground(new Color(30, 30, 45));
        btnEfectos.setForeground(Color.CYAN);
        btnEfectos.setBorder(javax.swing.BorderFactory.createLineBorder(Color.CYAN, 1));
        btnEfectos.setFocusable(false); 
        btnEfectos.setVisible(false); 
        
        btnEfectos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                efectosActivados = !efectosActivados;
                if (efectosActivados) {
                    btnEfectos.setText("EFECTOS: ON");
                    btnEfectos.setForeground(Color.CYAN);
                    btnEfectos.setBorder(javax.swing.BorderFactory.createLineBorder(Color.CYAN, 1));
                } else {
                    btnEfectos.setText("EFECTOS: OFF");
                    btnEfectos.setForeground(Color.LIGHT_GRAY);
                    btnEfectos.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                }
                repaint();
                requestFocusInWindow();
            }
        });
        this.add(btnEfectos);

        gameLoopTimer = new Timer(140, this);

        SwingUtilities.invokeLater(() -> repaint());
    }

    private void playMusicaFondo() {
        if (!musicaActivada || mostrarControles) return; 
        try {
            if (musicaFondo != null) {
                if (!musicaFondo.isRunning()) {
                    musicaFondo.start();
                }
            } else {
                File musicPath = new File("res/musica_fondo.wav");
                if (musicPath.exists()) {
                    AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                    musicaFondo = AudioSystem.getClip();
                    musicaFondo.open(audioInput);
                    musicaFondo.loop(Clip.LOOP_CONTINUOUSLY);
                    musicaFondo.start();
                }
            }
        } catch (Exception e) {
            System.err.println("Error música: " + e.getMessage());
        }
    }

    private void stopMusicaFondo() {
        if (musicaFondo != null && musicaFondo.isRunning()) {
            musicaFondo.stop();
        }
    }

    public void pararMusica() {
        stopMusicaFondo();
        if (musicaFondo != null) musicaFondo.close();
    }

    private void playSonidoEfecto(String archivo) {
        if (!efectosActivados || mostrarControles) return; 
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
        this.currentDirection = KeyEvent.VK_RIGHT; 
        
        if (musicaFondo != null) {
            musicaFondo.setFramePosition(0); 
        }
        playMusicaFondo();

        if (level == 1) {
            timerStarted = false;
            tiempoFinalSegundos = 0;
        }

        snake.clear();
        applesInLevel.clear();
        enemies.clear();
        
        snake.add(new Point(TILE_SIZE * 4, TILE_SIZE * 2)); 
        snake.add(new Point(TILE_SIZE * 3, TILE_SIZE * 2)); 
        snake.add(new Point(TILE_SIZE * 2, TILE_SIZE * 2)); 
        
        generateMapData(level);
        
        gameLoopTimer.start();
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
            for (int y = 4; y < 9; y++) { setTile(8, y, 1); setTile(23, y + 10, 1); }
        } else if (level == 2) {
            for (int y = 3; y < 12; y++) { setTile(8, y, 1); setTile(23, y + 7, 1); }
            for (int y = 10; y < 19; y++) { setTile(14, y, 1); setTile(17, y - 6, 1); }
        } else if (level == 3) {
            for (int x = 4; x < 28; x++) {
                if (x % 4 == 0) {
                    for (int y = 3; y < 14; y++) setTile(x, y, 1);
                } else if (x % 4 == 2) {
                    for (int y = 9; y < 20; y++) setTile(x, y, 1);
                }
            }
        } else if (level == 4) {
            for (int x = 5; x <= 12; x++) { setTile(x, 5, 1); setTile(x, 17, 1); setTile(x + 14, 5, 1); setTile(x + 14, 17, 1); }
            for (int y = 6; y <= 10; y++) { setTile(5, y, 1); setTile(12, y, 1); setTile(19, y, 1); setTile(26, y, 1); }
            for (int y = 12; y <= 16; y++) { setTile(5, y, 1); setTile(12, y, 1); setTile(19, y, 1); setTile(26, y, 1); }
        } else if (level == 5) {
            for (int x = 3; x < 29; x += 3) {
                for (int y = 2; y < 21; y += 3) {
                    setTile(x, y, 1); setTile(x + 1, y, 1);
                }
            }
            for (int x = 12; x < 20; x++) setTile(x, 11, 1);
        }

        int[][] manzanasProvisionales; 
        if (level == 1) {
            manzanasProvisionales = new int[][]{{5,5}, {10,18}, {15,11}, {20,4}, {25,15}, {12,7}, {18,14}, {7,12}, {27,8}, {14,19}};
        } else if (level == 2) {
            manzanasProvisionales = new int[][]{{3,4}, {5,17}, {11,6}, {12,15}, {15,3}, {16,18}, {19,7}, {20,14}, {26,5}, {28,16}, {9,20}, {22,2}};
        } else if (level == 3) {
            manzanasProvisionales = new int[][]{{2,5}, {6,5}, {10,5}, {14,5}, {18,5}, {22,5}, {26,5}, {30,5}, {2,17}, {6,17}, {10,17}, {14,17}, {18,17}, {26,17}};
        } else if (level == 4) {
            manzanasProvisionales = new int[][]{{2,2}, {3,20}, {7,2}, {9,20}, {15,2}, {16,20}, {21,2}, {23,20}, {29,2}, {30,20}, {1,11}, {14,11}, {17,11}, {30,11}, {15,8}, {16,14}};
        } else { 
            manzanasProvisionales = new int[][]{{1,3}, {2,3}, {5,3}, {8,3}, {11,3}, {14,3}, {17,3}, {20,3}, {23,3}, {26,3}, {29,3}, {30,3}, {5,9}, {11,9}, {17,9}, {23,9}, {29,9}, {2,15}, {8,15}, {20,15}};
        }

        for (int[] m : manzanasProvisionales) {
            int mx = m[0];
            int my = m[1];
            while (maze[my][mx] == 1) {
                mx = (mx + 1) % (GRID_WIDTH - 2) + 1; 
            }
            setTile(mx, my, 2);
        }

        if (level == 1) {
            setTile(16, 11, 3);
        } else if (level == 2) {
            setTile(11, 11, 3); setTile(20, 11, 3);
        } else if (level == 3) {
            setTile(2, 11, 3); setTile(15, 6, 3); setTile(29, 11, 3);
        } else if (level == 4) {
            setTile(2, 11, 3); setTile(14, 8, 3); setTile(17, 14, 3); setTile(29, 11, 3);
        } else if (level == 5) {
            setTile(2, 1, 3); setTile(29, 1, 3); setTile(14, 6, 3); setTile(17, 15, 3); setTile(29, 21, 3);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- PANTALLA DE CONTROLES PROPORCIONAL DE ALTA CALIDAD ---
        if (mostrarControles) {
            int panelW = getWidth();
            int panelH = getHeight();
            
            g2d.setColor(new Color(15, 15, 18));
            g2d.fillRect(0, 0, panelW, panelH);

            if (imgControles != null) {
                int imgW = imgControles.getWidth(this);
                int imgH = imgControles.getHeight(this);
                
                int maxW = panelW - 80;
                int maxH = panelH - 140;
                
                double scale = Math.min((double) maxW / imgW, (double) maxH / imgH);
                
                int targetW = (int) (imgW * scale);
                int targetH = (int) (imgH * scale);
                
                int renderX = (panelW - targetW) / 2;
                int renderY = (panelH - targetH) / 2 - 20;
                
                g2d.drawImage(imgControles, renderX, renderY, targetW, targetH, this);
                
                g2d.setColor(new Color(0, 255, 255, 80));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRect(renderX - 2, renderY - 2, targetW + 4, targetH + 4);
                g2d.setStroke(new BasicStroke(1f));
            } else {
                g2d.setColor(Color.CYAN);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 24));
                g2d.drawString("GUÍA DE CONTROLES", 50, 150);
            }

            if (textoVisible) {
                g2d.setFont(new Font("Consolas", Font.BOLD, 15));
                g2d.setColor(Color.GREEN);
                FontMetrics fm = g2d.getFontMetrics();
                String msgInicio = "PULSA CUALQUIER TECLA PARA EMPEZAR";
                int xMsg = (panelW - fm.stringWidth(msgInicio)) / 2;
                g2d.drawString(msgInicio, xMsg, panelH - 55);
            }
            return; 
        }

        // REJILLA DEL JUEGO
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

        if (gameFinished) {
            btnMusica.setVisible(false); 
            btnEfectos.setVisible(false); 
            drawFinalOverlay(g2d);
        } else if (levelCleared) {
            btnMusica.setVisible(false);
            btnEfectos.setVisible(false);
            drawOverlay(g2d, "¡NIVEL " + currentLevel + " COMPLETADO!", "Presiona 'N' para el siguiente nivel", Color.GREEN);
        } else if (!running) {
            btnMusica.setVisible(false);
            btnEfectos.setVisible(false);
            drawOverlay(g2d, "FIN DEL JUEGO", "Presiona 'R' para reintentar", Color.RED);
        } else {
            btnMusica.setVisible(true); 
            btnEfectos.setVisible(true); 
        }
    }

    private void drawOverlay(Graphics2D g2d, String title, String subtitle, Color mainColor) {
        g2d.setColor(new Color(10, 10, 15, 230));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        int panelW = 560;
        int panelH = 260;
        int panelX = (WIDTH - panelW) / 2;
        int panelY = (HEIGHT - panelH) / 2 - 20;

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(panelX + 8, panelY + 8, panelW, panelH, 20, 20);

        GradientPaint panelGrad = new GradientPaint(panelX, panelY, new Color(25, 25, 35), panelX, panelY + panelH, new Color(15, 15, 20));
        g2d.setPaint(panelGrad);
        g2d.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);

        g2d.setColor(new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 180));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2d.setStroke(new BasicStroke(1f));

        FontMetrics fm;
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 38));
        fm = g2d.getFontMetrics();
        int titleX = panelX + (panelW - fm.stringWidth(title)) / 2;
        
        g2d.setColor(new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 60));
        g2d.drawString(title, titleX - 2, panelY + 92);
        g2d.drawString(title, titleX + 2, panelY + 92);
        g2d.drawString(title, titleX, panelY + 90 - 2);
        g2d.drawString(title, titleX, panelY + 90 + 2);

        g2d.setColor(mainColor);
        g2d.drawString(title, titleX, panelY + 90);

        g2d.setColor(new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 100));
        g2d.drawLine(panelX + 40, panelY + 130, panelX + panelW - 40, panelY + 130);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Consolas", Font.PLAIN, 20));
        fm = g2d.getFontMetrics();
        g2d.drawString(subtitle, panelX + (panelW - fm.stringWidth(subtitle)) / 2, panelY + 180);
    }

    private void drawFinalOverlay(Graphics2D g2d) {
        g2d.setColor(new Color(10, 10, 15, 230));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        int panelW = 560;
        int panelH = 360;
        int panelX = (WIDTH - panelW) / 2;
        int panelY = (HEIGHT - panelH) / 2 - 20;

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(panelX + 8, panelY + 8, panelW, panelH, 20, 20);

        GradientPaint panelGrad = new GradientPaint(panelX, panelY, new Color(25, 25, 35), panelX, panelY + panelH, new Color(15, 15, 20));
        g2d.setPaint(panelGrad);
        g2d.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);

        g2d.setColor(new Color(255, 215, 0, 180));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2d.setStroke(new BasicStroke(1f));

        FontMetrics fm;
        String titleText = "MISIÓN CUMPLIDA";
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 38));
        fm = g2d.getFontMetrics();
        int titleX = panelX + (panelW - fm.stringWidth(titleText)) / 2;
        
        g2d.setColor(new Color(255, 215, 0, 60));
        g2d.drawString(titleText, titleX - 2, panelY + 62);
        g2d.drawString(titleText, titleX + 2, panelY + 62);
        g2d.drawString(titleText, titleX, panelY + 60 - 2);
        g2d.drawString(titleText, titleX, panelY + 60 + 2);

        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString(titleText, titleX, panelY + 60);

        g2d.setColor(new Color(255, 215, 0, 100));
        g2d.drawLine(panelX + 40, panelY + 90, panelX + panelW - 40, panelY + 90);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
        fm = g2d.getFontMetrics();
        
        String userStr = jugadorActual != null ? jugadorActual.getUsername().toUpperCase() : "INVITADO";
        g2d.setColor(Color.CYAN);
        g2d.drawString("OPERADOR:", panelX + 60, panelY + 140);
        g2d.setColor(Color.WHITE);
        g2d.drawString(userStr, panelX + panelW - 60 - fm.stringWidth(userStr), panelY + 140);

        String timeStr = tiempoFinalSegundos + " SEGUNDOS";
        g2d.setColor(Color.CYAN);
        g2d.drawString("TIEMPO RÉCORD:", panelX + 60, panelY + 180);
        g2d.setColor(Color.WHITE);
        g2d.drawString(timeStr, panelX + panelW - 60 - fm.stringWidth(timeStr), panelY + 180);

        String authStr = "JORGE & VICTOR";
        g2d.setColor(Color.CYAN);
        g2d.drawString("DESARROLLADORES:", panelX + 60, panelY + 220);
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawString(authStr, panelX + panelW - 60 - fm.stringWidth(authStr), panelY + 220);

        g2d.setColor(new Color(255, 215, 0, 40));
        g2d.drawLine(panelX + 40, panelY + 250, panelX + panelW - 40, panelY + 250);

        g2d.setFont(new Font("Consolas", Font.BOLD, 14));
        fm = g2d.getFontMetrics();
        
        String prompt1 = "[C] VOLVER AL MENÚ OS";
        String prompt2 = "[R] REINICIAR JUEGO";
        String prompt3 = "[V] REINICIAR A NIVEL 1";
        
        int spacing = panelW / 3;
        g2d.setColor(new Color(255, 80, 80)); 
        g2d.drawString(prompt1, panelX + (spacing - fm.stringWidth(prompt1))/2 + 5, panelY + 295);
        
        g2d.setColor(Color.GREEN); 
        g2d.drawString(prompt2, panelX + spacing + (spacing - fm.stringWidth(prompt2))/2, panelY + 295);
        
        g2d.setColor(Color.YELLOW); 
        g2d.drawString(prompt3, panelX + spacing*2 + (spacing - fm.stringWidth(prompt3))/2 - 5, panelY + 295);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running && !levelCleared && !gameFinished) {
            int dx = 0;
            int dy = 0;

            if (currentDirection == KeyEvent.VK_UP) dy = -TILE_SIZE;
            else if (currentDirection == KeyEvent.VK_DOWN) dy = TILE_SIZE;
            else if (currentDirection == KeyEvent.VK_LEFT) dx = -TILE_SIZE;
            else if (currentDirection == KeyEvent.VK_RIGHT) dx = TILE_SIZE;

            autoMove(dx, dy);
        }
    }

    private void autoMove(int dx, int dy) {
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
                        gameLoopTimer.stop();
                        long endTime = System.currentTimeMillis();
                        tiempoFinalSegundos = (int) ((endTime - startTime) / 1000);
                        if (jugadorActual != null) {
                            jugadorActual.setPuntos_snake(tiempoFinalSegundos);
                            repo.save(jugadorActual);
                        }
                    } else {
                        levelCleared = true;
                        gameLoopTimer.stop();
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
        gameLoopTimer.stop();
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
            if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < 575) {
                if (maze[ny/TILE_SIZE][nx/TILE_SIZE] == 0) {
                    e.x = nx; e.y = ny;
                }
            }
        }
    }

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            
            if (mostrarControles) {
                mostrarControles = false;
                timerParpadeo.stop();
                btnMusica.setVisible(true);
                btnEfectos.setVisible(true);
                loadLevel(1); 
                return;
            }

            if (gameFinished) {
                if (key == KeyEvent.VK_C) { 
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
                if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_W) && currentDirection != KeyEvent.VK_DOWN) {
                    currentDirection = KeyEvent.VK_UP;
                }
                else if ((key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) && currentDirection != KeyEvent.VK_UP) {
                    currentDirection = KeyEvent.VK_DOWN;
                }
                else if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) && currentDirection != KeyEvent.VK_RIGHT) {
                    currentDirection = KeyEvent.VK_LEFT;
                }
                else if ((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) && currentDirection != KeyEvent.VK_LEFT) {
                    currentDirection = KeyEvent.VK_RIGHT;
                }
            }
        }
    }
}