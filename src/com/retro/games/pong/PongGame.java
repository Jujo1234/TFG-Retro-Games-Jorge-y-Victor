package com.retro.games.pong;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class PongGame extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    private final int WIDTH = 800, HEIGHT = 500;
    private final int PADDLE_WIDTH = 15, PADDLE_HEIGHT = 80;
    private final int BALL_SIZE = 15;

    private int p1Y = 210, p2Y = 210;
    private int ballX = 400, ballY = 250;
    private int ballXSpeed = -4, ballYSpeed = 4;

    private int score1 = 0, score2 = 0;
    
    private int timeLeft = 60; 
    private int frameCounter = 0; 
    private boolean gameFinished = false;

    private int waitFrames = 0; 
    private boolean waiting = false; 

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

        timer = new Timer(10, this); 
        timer.start();
    }

    public void detenerJuego() {
        if (timer != null) timer.stop();
    }

    private void playSound(String fileName) {
        try {
            File soundPath = new File("res/" + fileName);
            if (soundPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
            }
        } catch (Exception e) {
            System.err.println("Error sonido: " + fileName);
        }
    }

    private void update() {
        if (gameFinished) return; 

        // 1. Movimiento de palas (SIEMPRE activo)
        if (wPressed && p1Y > 0) p1Y -= 5;
        if (sPressed && p1Y < HEIGHT - PADDLE_HEIGHT) p1Y += 5;
        if (upPressed && p2Y > 0) p2Y -= 5;
        if (downPressed && p2Y < HEIGHT - PADDLE_HEIGHT) p2Y += 5;

        // 2. Si estamos esperando, no hacemos nada más
        if (waiting) {
            waitFrames++;
            if (waitFrames >= 200) { 
                waiting = false;
                waitFrames = 0;
            }
            return; 
        }

        // 3. Cronómetro
        frameCounter++;
        if (frameCounter >= 100) { 
            timeLeft--;
            frameCounter = 0;
            if (timeLeft <= 0) {
                gameFinished = true;
                timer.stop();
            }
        }

        // 4. Movimiento de pelota
        ballX += ballXSpeed;
        ballY += ballYSpeed;

        // Rebotes en paredes
        if (ballY <= 0 || ballY >= HEIGHT - BALL_SIZE) {
            ballYSpeed *= -1;
            playSound("choque.wav"); 
        }

        // Colisiones con palas (Ajustadas para que no "tiemblen")
        if (ballX <= 35 && ballY + BALL_SIZE >= p1Y && ballY <= p1Y + PADDLE_HEIGHT && ballXSpeed < 0) {
            ballXSpeed = Math.abs(ballXSpeed);
            playSound("choque.wav"); 
        }

        if (ballX >= WIDTH - 50 && ballX <= WIDTH - 35 && ballY + BALL_SIZE >= p2Y && ballY <= p2Y + PADDLE_HEIGHT && ballXSpeed > 0) {
            ballXSpeed = -Math.abs(ballXSpeed);
            playSound("choque.wav"); 
        }

        // Goles (El resetBall ahora es fulminante)
        if (ballX < -BALL_SIZE) { 
            score2++; 
            playSound("gol.wav"); 
            resetBall(); 
        }
        else if (ballX > WIDTH) { 
            score1++; 
            playSound("gol.wav"); 
            resetBall(); 
        }
    }

    private void resetBall() {
        // La movemos al centro EXACTO antes de pintar el siguiente frame
        ballX = WIDTH / 2 - BALL_SIZE / 2;
        ballY = HEIGHT / 2 - BALL_SIZE / 2;
        
        // Invertimos dirección pero la dejamos quieta activando 'waiting'
        ballXSpeed *= -1;
        waiting = true;
        waitFrames = 0;
        
        // Forzamos un repintado para que el usuario vea la pelota en el centro YA
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);

        g.fillRect(20, p1Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        g.fillRect(WIDTH - 35, p2Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        
        if (!gameFinished) {
            g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);
        }

        g.setFont(new Font("Consolas", Font.BOLD, 40));
        g.drawString(score1 + " - " + score2, WIDTH/2 - 60, 50);
        
        g.setFont(new Font("Consolas", Font.PLAIN, 20));
        g.drawString("Tiempo: " + timeLeft + "s", WIDTH/2 - 50, 80);

        if (waiting && !gameFinished) {
            g.setFont(new Font("Consolas", Font.BOLD, 20));
            g.setColor(Color.CYAN);
            g.drawString("¡GOL! PREPARADOS...", WIDTH/2 - 100, HEIGHT/2 + 100);
        }

        if (gameFinished) {
            g.setFont(new Font("Arial", Font.BOLD, 50));
            String mensaje = (score1 > score2) ? "¡GANA JUGADOR 1!" : (score2 > score1) ? "¡GANA JUGADOR 2!" : "¡EMPATE!";
            g.setColor(Color.YELLOW);
            g.drawString(mensaje, WIDTH/2 - 200, HEIGHT/2);
        }
        
        g.setColor(Color.GRAY);
        for(int i=0; i<HEIGHT; i+=20) g.drawLine(WIDTH/2, i, WIDTH/2, i+10);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }
}