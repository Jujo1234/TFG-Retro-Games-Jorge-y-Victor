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

public class SnakeGame extends JPanel {
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

    public SnakeGame(Usuario jugador, UsuarioRepository repo) {
        this.jugadorActual = jugador;
        this.repo = repo;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(new Color(15, 15, 20)); 
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        
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
        g2d.drawString("LEVEL: " + currentLevel, 30, 608);
        g2d.drawString("LEFT: " + applesInLevel.size(), 180, 608);

        if (gameFinished) drawFinalOverlay(g2d);
        else if (levelCleared) drawOverlay(g2d, "LEVEL " + currentLevel + " CLEARED!", "Press 'N' for next level", Color.GREEN);
        else if (!running) drawOverlay(g2d, "GAME OVER", "Press 'R' to try again", Color.RED);
    }

    private void drawOverlay(Graphics2D g2d, String t1, String t2, Color c) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(c);
        g2d.setFont(new Font("Consolas", Font.BOLD, 50));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(t1, (WIDTH - fm.stringWidth(t1)) / 2, HEIGHT / 2);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Consolas", Font.PLAIN, 20));
        fm = g2d.getFontMetrics();
        g2d.drawString(t2, (WIDTH - fm.stringWidth(t2)) / 2, HEIGHT / 2 + 60);
    }

    private void drawFinalOverlay(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 210));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Consolas", Font.BOLD, 50));
        String text = "MISSION ACCOMPLISHED!";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(text, (WIDTH - fm.stringWidth(text)) / 2, HEIGHT / 2 - 40);
        
        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Consolas", Font.BOLD, 25));
        String timeText = "TOTAL TIME: " + tiempoFinalSegundos + " SECONDS";
        fm = g2d.getFontMetrics();
        g2d.drawString(timeText, (WIDTH - fm.stringWidth(timeText)) / 2, HEIGHT / 2 + 20);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Consolas", Font.PLAIN, 18));
        g2d.drawString("M - Return to Menu | R - Restart | V - Level 1", WIDTH/2 - 200, HEIGHT/2 + 80);
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