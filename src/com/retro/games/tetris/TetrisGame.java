package com.retro.games.tetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
// --- IMPORTACIONES PARA AUDIO ---
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

// --- IMPORTACIONES PARA DB ---
import com.retro.main.model.Usuario;
import com.retro.main.repository.UsuarioRepository;

public class TetrisGame extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 20;
    private final int TILE_SIZE = 30;

    private Timer timer;
    private boolean isStarted = false;
    private int curX = 0;
    private int curY = 0;
    private int score = 0;
    
    private int[][] board;
    private Tetromino curPiece;

    // --- VARIABLES DE AUDIO ---
    private Clip musicaFondo;

    // --- VARIABLES DE SESIÓN ---
    private Usuario jugadorActual;
    private UsuarioRepository repo;

    public TetrisGame(Usuario jugador, UsuarioRepository repo) {
        this.jugadorActual = jugador;
        this.repo = repo;
        setPreferredSize(new Dimension(BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE + 50));
        setBackground(new Color(20, 20, 20));
        setFocusable(true);
        
        board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        curPiece = new Tetromino();
        timer = new Timer(400, this); 
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!isStarted) return;

                int key = e.getKeyCode();
                if (key == KeyEvent.VK_LEFT) tryMove(curPiece, curX - 1, curY);
                if (key == KeyEvent.VK_RIGHT) tryMove(curPiece, curX + 1, curY);
                if (key == KeyEvent.VK_DOWN) dropOneLine();
                if (key == KeyEvent.VK_UP) tryMove(curPiece.rotate(), curX, curY);
                if (key == KeyEvent.VK_SPACE) dropToBottom();
            }
        });

        // Iniciamos música y juego con delay
        Timer initDelay = new Timer(200, e -> {
            playMusicaFondo();
            start();
        });
        initDelay.setRepeats(false);
        initDelay.start();
    }

    // --- LÓGICA DE AUDIO ---
    private void playMusicaFondo() {
        try {
            File musicPath = new File("res/musica_tetris.wav");
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                musicaFondo = AudioSystem.getClip();
                musicaFondo.open(audioInput);
                musicaFondo.loop(Clip.LOOP_CONTINUOUSLY);
                musicaFondo.start();
            }
        } catch (Exception e) {
            System.err.println("Error música Tetris: " + e.getMessage());
        }
    }

    private void playEfecto(String archivo) {
        try {
            File soundPath = new File("res/" + archivo);
            if (soundPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
            }
        } catch (Exception e) {
            System.err.println("Error efecto: " + archivo);
        }
    }

    private void stopMusica() {
        if (musicaFondo != null && musicaFondo.isRunning()) {
            musicaFondo.stop();
        }
    }

    public void start() {
        clearBoard();
        score = 0;
        isStarted = true;
        newPiece();
        timer.start();
    }

    private void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT; i++)
            for (int j = 0; j < BOARD_WIDTH; j++) board[i][j] = 0;
    }

    private void newPiece() {
        curPiece.setRandomShape();
        curX = BOARD_WIDTH / 2;
        curY = 1;

        if (!tryMove(curPiece, curX, curY)) {
            isStarted = false;
            timer.stop();
            stopMusica(); // Parar música al perder
            playEfecto("derrota.wav"); // Sonido reutilizado

            if (jugadorActual != null) {
                jugadorActual.setPuntos_tetris(score);
                repo.save(jugadorActual);
            }
            mostrarMenuFin();
        }
    }

    private boolean tryMove(Tetromino piece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + piece.x(i);
            int y = newY + piece.y(i);
            
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) return false;
            if (board[y][x] != 0) return false;
        }
        
        curPiece = piece;
        curX = newX;
        curY = newY;
        repaint();
        return true;
    }

    private void dropOneLine() {
        if (!tryMove(curPiece, curX, curY + 1)) pieceDropped();
    }

    private void pieceDropped() {
        for (int i = 0; i < 4; i++) {
            int x = curX + curPiece.x(i);
            int y = curY + curPiece.y(i);
            board[y][x] = curPiece.getShape();
        }
        removeFullLines();
        newPiece();
    }

    private void removeFullLines() {
        int linesFilled = 0;
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean full = true;
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                linesFilled++;
                for (int k = i; k > 0; k--)
                    System.arraycopy(board[k - 1], 0, board[k], 0, BOARD_WIDTH);
                i++;
            }
        }
        if (linesFilled > 0) {
            score += linesFilled * 100;
            playEfecto("puntos.wav"); // Sonido al ganar puntos
            repaint();
        }
    }

    private void dropToBottom() {
        int newY = curY;
        while (newY < BOARD_HEIGHT - 1) {
            if (!tryMove(curPiece, curX, newY + 1)) break;
            newY++;
        }
        pieceDropped();
    }

    private void mostrarMenuFin() {
        Object[] options = {"Reintentar", "Cerrar"};
        int n = JOptionPane.showOptionDialog(this, "GAME OVER\nScore: " + score, "Tetris",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (n == JOptionPane.YES_OPTION) {
            playMusicaFondo();
            start();
        }
        else SwingUtilities.getWindowAncestor(this).dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        dropOneLine();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] != 0) drawSquare(g, j * TILE_SIZE, i * TILE_SIZE, board[i][j]);
            }
        }

        if (isStarted && curPiece.getShape() != 0) {
            for (int i = 0; i < 4; i++) {
                drawSquare(g, (curX + curPiece.x(i)) * TILE_SIZE, (curY + curPiece.y(i)) * TILE_SIZE, curPiece.getShape());
            }
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.drawString("PUNTOS: " + score, 10, BOARD_HEIGHT * TILE_SIZE + 30);
    }

    private void drawSquare(Graphics g, int x, int y, int shape) {
        Color colors[] = { new Color(0, 0, 0), new Color(204, 102, 102), 
            new Color(102, 204, 102), new Color(102, 102, 204), 
            new Color(204, 204, 102), new Color(204, 102, 204), 
            new Color(102, 204, 204), new Color(218, 170, 0) };
        Color color = colors[shape];
        g.setColor(color);
        g.fillRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2);
        
        // Bordes para efecto 3D
        g.setColor(color.brighter());
        g.drawLine(x, y + TILE_SIZE - 1, x, y);
        g.drawLine(x, y, x + TILE_SIZE - 1, y);
        g.setColor(color.darker());
        g.drawLine(x + 1, y + TILE_SIZE - 1, x + TILE_SIZE - 1, y + TILE_SIZE - 1);
        g.drawLine(x + TILE_SIZE - 1, y + TILE_SIZE - 1, x + TILE_SIZE - 1, y + 1);
    }

    class Tetromino {
        private int[][] coords;
        private int shape;
        private final int[][][] table = {
            {{0,0},{0,0},{0,0},{0,0}},
            {{-1,0},{0,0},{1,0},{0,1}}, 
            {{0,0},{1,0},{0,1},{1,1}}, 
            {{-1,-1},{0,-1},{0,0},{0,1}}, 
            {{1,-1},{0,-1},{0,0},{0,1}}, 
            {{0,-1},{0,0},{0,1},{0,2}}, 
            {{-1,0},{0,0},{0,1},{1,1}}, 
            {{-1,1},{0,1},{0,0},{1,0}}  
        };

        public Tetromino() { coords = new int[4][2]; }
        public void setShape(int s) {
            for(int i=0; i<4; i++) System.arraycopy(table[s][i], 0, coords[i], 0, 2);
            shape = s;
        }
        public void setRandomShape() { setShape(new Random().nextInt(7) + 1); }
        public int x(int i) { return coords[i][0]; }
        public int y(int i) { return coords[i][1]; }
        public int getShape() { return shape; }
        public Tetromino rotate() {
            if (shape == 2) return this;
            Tetromino res = new Tetromino();
            res.shape = shape;
            for(int i=0; i<4; i++) {
                res.coords[i][0] = -coords[i][1];
                res.coords[i][1] = coords[i][0];
            }
            return res;
        }
    }
}