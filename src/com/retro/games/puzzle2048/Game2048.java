package com.retro.games.puzzle2048;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
// --- IMPORTACIONES PARA AUDIO ---
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

// --- IMPORTACIONES PARA DB ---
import com.retro.main.model.Usuario;
import com.retro.main.repository.UsuarioRepository;

public class Game2048 extends JPanel {
    private static final long serialVersionUID = 1L;
    private int[][] board = new int[4][4];
    private int score = 0;
    private boolean gameOver = false;
    private boolean win = false;

    private Usuario jugadorActual;
    private UsuarioRepository repo;

    public Game2048(Usuario jugador, UsuarioRepository repo) {
        this.jugadorActual = jugador;
        this.repo = repo;
        setPreferredSize(new Dimension(400, 500));
        setBackground(new Color(187, 173, 160));
        setFocusable(true);
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameOver || win) return;
                
                boolean moved = false;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:    moved = moveUp(); break;
                    case KeyEvent.VK_DOWN:  moved = moveDown(); break;
                    case KeyEvent.VK_LEFT:  moved = moveLeft(); break;
                    case KeyEvent.VK_RIGHT: moved = moveRight(); break;
                }
                
                if (moved) {
                    spawnRandom();
                    repaint();
                    checkGameState(); 
                }
            }
        });
        
        reiniciarJuego();
    }

    // --- SISTEMA DE AUDIO PARA EFECTOS ---
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
            System.err.println("Error al reproducir: " + archivo);
        }
    }

    private void reiniciarJuego() {
        board = new int[4][4];
        score = 0;
        gameOver = false;
        win = false;
        spawnRandom();
        spawnRandom();
        repaint();
    }

    private void spawnRandom() {
        ArrayList<Integer> emptySpaces = new ArrayList<>();
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (board[r][c] == 0) emptySpaces.add(r * 4 + c);
            }
        }
        if (!emptySpaces.isEmpty()) {
            int pos = emptySpaces.get(new Random().nextInt(emptySpaces.size()));
            board[pos / 4][pos % 4] = (Math.random() < 0.9) ? 2 : 4;
        }
    }

    private boolean moveLeft() {
        boolean moved = false;
        for (int r = 0; r < 4; r++) {
            int[] row = board[r];
            int[] newRow = new int[4];
            int pos = 0;
            for (int c = 0; c < 4; c++) {
                if (row[c] != 0) newRow[pos++] = row[c];
            }
            for (int c = 0; c < 3; c++) {
                if (newRow[c] != 0 && newRow[c] == newRow[c+1]) {
                    newRow[c] *= 2;
                    score += newRow[c];
                    newRow[c+1] = 0;
                }
            }
            int[] finalRow = new int[4];
            pos = 0;
            for (int c = 0; c < 4; c++) {
                if (newRow[c] != 0) finalRow[pos++] = newRow[c];
            }
            if (!java.util.Arrays.equals(board[r], finalRow)) moved = true;
            board[r] = finalRow;
        }
        return moved;
    }

    private boolean moveRight() { reverseBoard(); boolean m = moveLeft(); reverseBoard(); return m; }
    private boolean moveUp() { transpose(); boolean m = moveLeft(); transpose(); return m; }
    private boolean moveDown() { transpose(); boolean m = moveRight(); transpose(); return m; }

    private void reverseBoard() {
        for(int r=0; r<4; r++) {
            for(int c=0; c<2; c++) {
                int temp = board[r][c];
                board[r][c] = board[r][3-c];
                board[r][3-c] = temp;
            }
        }
    }

    private void transpose() {
        for(int r=0; r<4; r++) {
            for(int c=r; c<4; c++) {
                int temp = board[r][c];
                board[r][c] = board[c][r];
                board[c][r] = temp;
            }
        }
    }

    private void checkGameState() {
        // Victoria
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (board[r][c] == 2048) {
                    win = true;
                    guardarPuntosBaseDatos();
                    playEfecto("victoria.wav"); // <--- SUENA ANTES
                    mostrarMenuFin("¡BRUTAL! Has llegado al 2048.");
                    return;
                }
            }
        }

        // ¿Quedan movimientos?
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (board[r][c] == 0) return;
                if (c < 3 && board[r][c] == board[r][c+1]) return;
                if (r < 3 && board[r][c] == board[r+1][c]) return;
            }
        }

        // Derrota
        gameOver = true;
        guardarPuntosBaseDatos();
        playEfecto("derrota.wav"); // <--- SUENA ANTES
        mostrarMenuFin("Game Over. No hay más movimientos.");
    }

    private void guardarPuntosBaseDatos() {
        if (jugadorActual != null && score > jugadorActual.getPuntos_2048()) {
            jugadorActual.setPuntos_2048(score);
            repo.save(jugadorActual);
        }
    }

    private void mostrarMenuFin(String mensaje) {
        Object[] opciones = {"Jugar otra vez", "Volver al Menú"};
        
        // Aumentamos un poco el tiempo (800ms) para que el sonido empiece a sonar antes de la ventana
        Timer timerMenu = new Timer(800, e -> {
            int seleccion = JOptionPane.showOptionDialog(
                this,
                mensaje + "\n¿Qué quieres hacer?",
                "Fin de la partida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, 
                opciones, 
                opciones[0]
            );

            if (seleccion == JOptionPane.YES_OPTION) {
                reiniciarJuego();
            } else {
                Window win = SwingUtilities.getWindowAncestor(this);
                if (win != null) win.dispose();
            }
        });
        timerMenu.setRepeats(false);
        timerMenu.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(119, 110, 101));
        g.setFont(new Font("Arial", Font.BOLD, 25));
        g.drawString("Puntos: " + score, 20, 40);

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                drawTile(g2, board[r][c], 20 + c * 90, 70 + r * 90);
            }
        }
    }

    private void drawTile(Graphics2D g, int value, int x, int y) {
        g.setColor(getTileColor(value));
        g.fillRoundRect(x, y, 80, 80, 15, 15);
        
        if (value != 0) {
            g.setColor(value < 8 ? new Color(119, 110, 101) : Color.WHITE);
            String s = String.valueOf(value);
            g.setFont(new Font("Arial", Font.BOLD, value < 100 ? 35 : value < 1000 ? 30 : 25));
            FontMetrics fm = g.getFontMetrics();
            int tx = x + (80 - fm.stringWidth(s)) / 2;
            int ty = y + (80 - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(s, tx, ty);
        }
    }

    private Color getTileColor(int value) {
        switch (value) {
            case 2:    return new Color(238, 228, 218);
            case 4:    return new Color(237, 224, 200);
            case 8:    return new Color(242, 177, 121);
            case 16:   return new Color(245, 149, 99);
            case 32:   return new Color(246, 124, 95);
            case 64:   return new Color(246, 94, 59);
            case 128:  return new Color(237, 207, 114);
            case 256:  return new Color(237, 204, 97);
            case 512:  return new Color(237, 200, 80);
            case 1024: return new Color(237, 197, 63);
            case 2048: return new Color(237, 194, 46);
            default:   return new Color(205, 193, 180);
        }
    }
}