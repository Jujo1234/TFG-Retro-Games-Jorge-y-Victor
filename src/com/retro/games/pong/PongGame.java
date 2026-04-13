package com.retro.games.pong;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PongGame extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    private final int WIDTH = 800, HEIGHT = 500;
    private final int PADDLE_WIDTH = 15, PADDLE_HEIGHT = 80;
    private final int BALL_SIZE = 15;

    private int p1Y = 210, p2Y = 210;
    private int ballX = 400, ballY = 250;
    private int ballXSpeed = -4, ballYSpeed = 4;

    private int score1 = 0, score2 = 0;
    
    // VARIABLES PARA EL TIEMPO
    private int timeLeft = 60; // 60 segundos = 1 minuto
    private int frameCounter = 0; // Para contar los ms y restar un segundo
    private boolean gameFinished = false;

    private boolean wPressed, sPressed, upPressed, downPressed;
    private Timer timer;

    public PongGame() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_W) wPressed = true;
                if (e.getKeyCode() == KeyEvent.VK_S) sPressed = true;
                if (e.getKeyCode() == KeyEvent.VK_UP) upPressed = true;
                if (e.getKeyCode() == KeyEvent.VK_DOWN) downPressed = true;
            }
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_W) wPressed = false;
                if (e.getKeyCode() == KeyEvent.VK_S) sPressed = false;
                if (e.getKeyCode() == KeyEvent.VK_UP) upPressed = false;
                if (e.getKeyCode() == KeyEvent.VK_DOWN) downPressed = false;
            }
        });

        timer = new Timer(10, this); // Se ejecuta cada 10ms
        timer.start();
    }

    private void update() {
        if (gameFinished) return; // Si el tiempo acabó, no movemos nada

        // Lógica del Cronómetro
        frameCounter++;
        if (frameCounter >= 100) { // Como el timer es de 10ms, 100 veces = 1 segundo
            timeLeft--;
            frameCounter = 0;
            if (timeLeft <= 0) {
                timeLeft = 0;
                gameFinished = true;
                timer.stop();
            }
        }

        // Movimiento de palas y pelota (igual que antes)
        if (wPressed && p1Y > 0) p1Y -= 5;
        if (sPressed && p1Y < HEIGHT - PADDLE_HEIGHT) p1Y += 5;
        if (upPressed && p2Y > 0) p2Y -= 5;
        if (downPressed && p2Y < HEIGHT - PADDLE_HEIGHT) p2Y += 5;

        ballX += ballXSpeed;
        ballY += ballYSpeed;

        if (ballY <= 0 || ballY >= HEIGHT - BALL_SIZE) ballYSpeed *= -1;

        if (ballX <= 35 && ballY + BALL_SIZE >= p1Y && ballY <= p1Y + PADDLE_HEIGHT) {
            ballXSpeed = Math.abs(ballXSpeed);
        }
     // Colisión Jugador 2 (Pala derecha)
     // Solo rebota si toca el frente de la pala (entre 750 y 765 de X)
     if (ballX >= WIDTH - 50 && ballX <= WIDTH - 35 && ballY + BALL_SIZE >= p2Y && ballY <= p2Y + PADDLE_HEIGHT) {
         ballXSpeed = -Math.abs(ballXSpeed);
     }

        if (ballX < 0) { score2++; resetBall(); }
        if (ballX > WIDTH) { score1++; resetBall(); }
    }

    private void resetBall() {
        ballX = WIDTH / 2;
        ballY = HEIGHT / 2;
        ballXSpeed *= -1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);

        // Dibujar palas y pelota
        g.fillRect(20, p1Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        g.fillRect(WIDTH - 35, p2Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        if (!gameFinished) {
            g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);
        }

        // Dibujar Marcador
        g.setFont(new Font("Consolas", Font.BOLD, 40));
        g.drawString(score1 + " - " + score2, WIDTH/2 - 60, 50);
        
        // Dibujar Cronómetro
        g.setFont(new Font("Consolas", Font.PLAIN, 20));
        g.drawString("Tiempo: " + timeLeft + "s", WIDTH/2 - 50, 80);

        // Pantalla de Ganador
        if (gameFinished) {
            g.setFont(new Font("Arial", Font.BOLD, 50));
            String mensaje = "";
            if (score1 > score2) mensaje = "¡GANA JUGADOR 1!";
            else if (score2 > score1) mensaje = "¡GANA JUGADOR 2!";
            else mensaje = "¡EMPATE!";
            
            g.setColor(Color.YELLOW);
            g.drawString(mensaje, WIDTH/2 - 200, HEIGHT/2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Cierra esta ventana para volver al menú", WIDTH/2 - 180, HEIGHT/2 + 50);
        }
        
        // Línea central decorativa
        g.setColor(Color.GRAY);
        for(int i=0; i<HEIGHT; i+=20) g.drawLine(WIDTH/2, i, WIDTH/2, i+10);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }
}