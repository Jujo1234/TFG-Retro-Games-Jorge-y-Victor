package com.retro.games.pong;

import javax.swing.*;
import javax.swing.border.LineBorder;
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

    // Componentes para la botonera final interactiva
    private JPanel panelBotonesFinal;
    private JButton btnReiniciar;
    private JButton btnSalir;

    // COMPONENTE GRÁFICO NUEVO: BOTÓN DE AUDIO MUTE
    private JButton btnSonido;
    private boolean sonidoActivado = true; // Controla si se reproducen los efectos y choques

    // VARIABLES EXCLUSIVAS PARA LA PANTALLA DE CONTROLES PREVIA
    private boolean mostrarControles = true;
    private Image imgControles;
    private Timer timerParpadeo;
    private boolean textoVisible = true;

    public PongGame() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.setLayout(null);

        // --- DOBLE SISTEMA DE SEGURIDAD PARA CARGA DE IMAGEN ---
        File fileImgPng = new File("res/pongControles.png");
        File fileImgJpg = new File("res/pongControles.jpg");
        
        if (fileImgPng.exists()) {
            imgControles = new ImageIcon(fileImgPng.getAbsolutePath()).getImage();
        } else if (fileImgJpg.exists()) {
            imgControles = new ImageIcon(fileImgJpg.getAbsolutePath()).getImage();
        }

        // Temporizador para el efecto parpadeante retro del texto de inicio
        timerParpadeo = new Timer(500, e -> {
            textoVisible = !textoVisible;
            repaint();
        });
        timerParpadeo.start();

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Si la pantalla de guía está activa, cualquier tecla la apaga e inicia el juego
                if (mostrarControles) {
                    mostrarControles = false;
                    if (timerParpadeo != null) timerParpadeo.stop();
                    btnSonido.setVisible(true);
                    
                    ballX = WIDTH / 2 - BALL_SIZE / 2;
                    ballY = HEIGHT / 2 - BALL_SIZE / 2;
                    waiting = true;
                    waitFrames = 0;
                    
                    timer.start(); 
                    repaint();
                    return;
                }

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

        // Inicializamos los botones de la interfaz
        inicializarBotoneraFinal();
        inicializarBotónSonido();

        // El Timer se declara pero se inicia al quitar los controles para que no congele el renderizado
        timer = new Timer(10, this); 
        
        // Forzamos la carga asíncrona de la interfaz de usuario
        SwingUtilities.invokeLater(() -> repaint());
    }

    private void inicializarBotónSonido() {
        btnSonido = new JButton("AUDIO: ON");
        btnSonido.setBounds((WIDTH - 120) / 2, HEIGHT - 45, 120, 30);
        btnSonido.setFont(new Font("Consolas", Font.BOLD, 12));
        btnSonido.setBackground(new Color(25, 25, 30));
        btnSonido.setForeground(Color.CYAN);
        btnSonido.setBorder(javax.swing.BorderFactory.createLineBorder(Color.CYAN, 1));
        btnSonido.setFocusable(false); 
        btnSonido.setVisible(false); 

        btnSonido.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sonidoActivado = !sonidoActivado;
                if (sonidoActivado) {
                    btnSonido.setText("AUDIO: ON");
                    btnSonido.setForeground(Color.CYAN);
                    btnSonido.setBorder(javax.swing.BorderFactory.createLineBorder(Color.CYAN, 1));
                } else {
                    btnSonido.setText("AUDIO: OFF");
                    btnSonido.setForeground(Color.LIGHT_GRAY);
                    btnSonido.setBorder(javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                }
                repaint();
                requestFocusInWindow(); 
            }
        });

        this.add(btnSonido);
    }

    private void inicializarBotoneraFinal() {
        panelBotonesFinal = new JPanel(new GridLayout(1, 2, 20, 0));
        panelBotonesFinal.setOpaque(false);
        
        int panelW = 460;
        int panelH = 42;
        int panelX = (WIDTH - panelW) / 2;
        int panelY = ((HEIGHT - 320) / 2) + 245; 
        panelBotonesFinal.setBounds(panelX, panelY, panelW, panelH);

        btnReiniciar = new JButton("NUEVA PARTIDA");
        estilizarBotonInterface(btnReiniciar, Color.GREEN);
        btnReiniciar.addActionListener(e -> reiniciarPartidaCompleta());

        btnSalir = new JButton("VOLVER AL MENÚ");
        estilizarBotonInterface(btnSalir, new Color(255, 80, 80));
        btnSalir.addActionListener(e -> salirAlMenuPrincipal());

        panelBotonesFinal.add(btnReiniciar);
        panelBotonesFinal.add(btnSalir);
        panelBotonesFinal.setVisible(false); 
        this.add(panelBotonesFinal);
    }

    private void estilizarBotonInterface(JButton b, Color accentColor) {
        b.setBackground(new Color(40, 40, 50));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Consolas", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 120), 1));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                b.setBackground(new Color(55, 55, 68)); 
                b.setBorder(new LineBorder(accentColor, 1));
            }
            public void mouseExited(MouseEvent e) { 
                b.setBackground(new Color(40, 40, 50)); 
                b.setBorder(new LineBorder(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 120), 1));
            }
        });
    }

    private void reiniciarPartidaCompleta() {
        p1Y = 210; p2Y = 210;
        score1 = 0; score2 = 0;
        timeLeft = 60;
        frameCounter = 0;
        waiting = false;
        waitFrames = 0;
        gameFinished = false;
        panelBotonesFinal.setVisible(false);
        resetBall();
        timer.start();
        this.requestFocusInWindow();
    }

    private void salirAlMenuPrincipal() {
        detenerJuego();
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (topFrame != null) {
            topFrame.dispose();
        }
    }

    public void detenerJuego() {
        if (timerParpadeo != null) timerParpadeo.stop();
        if (timer != null) timer.stop();
    }

    private void playSound(String fileName) {
        if (!sonidoActivado || mostrarControles) return; 
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
        if (mostrarControles || gameFinished) return; 

        if (wPressed && p1Y > 0) p1Y -= 5;
        if (sPressed && p1Y < HEIGHT - PADDLE_HEIGHT) p1Y += 5;
        if (upPressed && p2Y > 0) p2Y -= 5;
        if (downPressed && p2Y < HEIGHT - PADDLE_HEIGHT) p2Y += 5;

        if (waiting) {
            waitFrames++;
            if (waitFrames >= 200) { 
                waiting = false;
                waitFrames = 0;
            }
            return; 
        }

        frameCounter++;
        if (frameCounter >= 100) { 
            timeLeft--;
            frameCounter = 0;
            if (timeLeft <= 0) {
                gameFinished = true;
                timer.stop();
                panelBotonesFinal.setVisible(true);
            }
        }

        ballX += ballXSpeed;
        ballY += ballYSpeed;

        if (ballY <= 0 || ballY >= HEIGHT - BALL_SIZE) {
            ballYSpeed *= -1;
            playSound("choque.wav"); 
        }

        if (ballX >= 20 && ballX <= 35 && ballY + BALL_SIZE >= p1Y && ballY <= p1Y + PADDLE_HEIGHT && ballXSpeed < 0) {
            ballXSpeed = Math.abs(ballXSpeed);
            ballX = 36; 
            playSound("choque.wav"); 
        }

        if (ballX >= WIDTH - 50 && ballX <= WIDTH - 35 && ballY + BALL_SIZE >= p2Y && ballY <= p2Y + PADDLE_HEIGHT && ballXSpeed > 0) {
            ballXSpeed = -Math.abs(ballXSpeed);
            ballX = WIDTH - 51; 
            playSound("choque.wav"); 
        }

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
        if (mostrarControles) return;
        ballX = WIDTH / 2 - BALL_SIZE / 2;
        ballY = HEIGHT / 2 - BALL_SIZE / 2;
        ballXSpeed *= -1;
        waiting = true;
        waitFrames = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- ESCALADO PROPORCIONAL SEGURO ---
        if (mostrarControles) {
            int panelW = getWidth();
            int panelH = getHeight();
            
            g2d.setColor(new Color(15, 15, 18));
            g2d.fillRect(0, 0, panelW, panelH);

            if (imgControles != null) {
                int imgW = imgControles.getWidth(this);
                int imgH = imgControles.getHeight(this);
                
                int maxW = panelW - 80;
                int maxH = panelH - 120;
                
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
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
                g2d.drawString("GUÍA DE CONTROLES", 50, 150);
            }

            if (textoVisible) {
                g2d.setFont(new Font("Consolas", Font.BOLD, 14));
                g2d.setColor(Color.GREEN);
                FontMetrics fm = g2d.getFontMetrics();
                String msgInicio = "PULSA CUALQUIER TECLA PARA EMPEZAR";
                int xMsg = (panelW - fm.stringWidth(msgInicio)) / 2;
                g2d.drawString(msgInicio, xMsg, panelH - 50);
            }
            return; 
        }

        // Campo intermedio (Línea discontinua)
        g2d.setColor(Color.DARK_GRAY);
        for(int i=0; i<HEIGHT; i+=20) g2d.drawLine(WIDTH/2, i, WIDTH/2, i+10);

        // Palas de juego
        g2d.setColor(Color.WHITE);
        g2d.fillRect(20, p1Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        g2d.fillRect(WIDTH - 35, p2Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        
        // Pelota activa
        if (!gameFinished) {
            g2d.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);
        }

        // Puntuación superior
        g2d.setFont(new Font("Consolas", Font.BOLD, 40));
        g2d.drawString(score1 + " - " + score2, WIDTH/2 - 60, 50);
        
        g2d.setFont(new Font("Consolas", Font.PLAIN, 18));
        g2d.setColor(Color.GRAY);
        g2d.drawString("Tiempo: " + timeLeft + "s", WIDTH/2 - 55, 80);

        // Saque inminente
        if (waiting && !gameFinished) {
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2d.setColor(Color.CYAN);
            FontMetrics fm = g2d.getFontMetrics();
            String textGol = " PREPARANDO SAQUE...";
            g2d.drawString(textGol, (WIDTH - fm.stringWidth(textGol)) / 2, HEIGHT - 80);
        }

        // Tarjeta de resultados final
        if (gameFinished) {
            btnSonido.setVisible(false);

            g2d.setColor(new Color(10, 10, 15, 230));
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            int panelW = 560;
            int panelH = 320;
            int panelX = (WIDTH - panelW) / 2;
            int panelY = (HEIGHT - panelH) / 2 - 10;

            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRoundRect(panelX + 6, panelY + 6, panelW, panelH, 18, 18);

            GradientPaint panelGrad = new GradientPaint(panelX, panelY, new Color(25, 25, 35), panelX, panelY + panelH, new Color(15, 15, 20));
            g2d.setPaint(panelGrad);
            g2d.fillRoundRect(panelX, panelY, panelW, panelH, 18, 18);

            String mensaje;
            Color accentColor;
            if (score1 > score2) {
                mensaje = "¡VICTORIA JUGADOR 1!";
                accentColor = Color.GREEN;
            } else if (score2 > score1) {
                mensaje = "¡VICTORIA JUGADOR 2!";
                accentColor = Color.GREEN;
            } else {
                mensaje = "¡EMPATE TÉCNICO!";
                accentColor = Color.YELLOW;
            }

            g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 180));
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawRoundRect(panelX, panelY, panelW, panelH, 18, 18);
            g2d.setStroke(new BasicStroke(1f));

            FontMetrics fm;

            g2d.setFont(new Font("Segoe UI", Font.BOLD, 36));
            fm = g2d.getFontMetrics();
            int msgX = panelX + (panelW - fm.stringWidth(mensaje)) / 2;
            
            g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 50));
            g2d.drawString(mensaje, msgX - 2, panelY + 57);
            g2d.drawString(mensaje, msgX + 2, panelY + 57);
            g2d.drawString(mensaje, msgX, panelY + 55 - 2);
            g2d.drawString(mensaje, msgX, panelY + 55 + 2);
            
            g2d.setColor(accentColor);
            g2d.drawString(mensaje, msgX, panelY + 55);

            g2d.setColor(new Color(0, 255, 255, 80));
            g2d.drawLine(panelX + 40, panelY + 85, panelX + panelW - 40, panelY + 85);

            g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
            fm = g2d.getFontMetrics();

            g2d.setColor(Color.CYAN);
            g2d.drawString("PUNTUACIÓN JUGADOR 1:", panelX + 50, panelY + 135);
            g2d.setColor(Color.WHITE);
            String pts1 = score1 + " GOLES";
            g2d.drawString(pts1, panelX + panelW - 50 - fm.stringWidth(pts1), panelY + 135);

            g2d.setColor(Color.CYAN);
            g2d.drawString("PUNTUACIÓN JUGADOR 2:", panelX + 50, panelY + 180);
            g2d.setColor(Color.WHITE);
            String pts2 = score2 + " GOLES";
            g2d.drawString(pts2, panelX + panelW - 50 - fm.stringWidth(pts2), panelY + 180);

            g2d.setColor(new Color(255, 255, 255, 30));
            g2d.drawLine(panelX + 40, panelY + 215, panelX + panelW - 40, panelY + 215);
        } else {
            if (!mostrarControles) btnSonido.setVisible(true); 
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }
}