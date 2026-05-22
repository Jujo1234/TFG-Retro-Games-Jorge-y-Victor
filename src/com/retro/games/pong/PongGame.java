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

    public PongGame() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        // Usamos un diseño nulo (Absolute Positioning) para poder ubicar de forma exacta
        // los botones del menú final encima de los gráficos del juego sin alterar vuestro flujo.
        this.setLayout(null);

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

        // Inicialización y diseño de la botonera final (oculta por defecto)
        inicializarBotoneraFinal();

        timer = new Timer(10, this); 
        timer.start();
    }

    private void inicializarBotoneraFinal() {
        panelBotonesFinal = new JPanel(new GridLayout(1, 2, 20, 0));
        panelBotonesFinal.setOpaque(false);
        
        // El panel se posiciona matemáticamente en la zona baja interior de la tarjeta de resultados
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
        panelBotonesFinal.setVisible(false); // Invisible hasta que termine el juego
        this.add(panelBotonesFinal);
    }

    private void estilizarBotonInterface(JButton b, Color accentColor) {
        b.setBackground(new Color(40, 40, 50));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Consolas", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 120), 1));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto dinámico de iluminación al pasar el ratón (Hover)
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
                // Al finalizar el tiempo, activamos el contenedor de los botones interactivos
                panelBotonesFinal.setVisible(true);
            }
        }

        ballX += ballXSpeed;
        ballY += ballYSpeed;

        if (ballY <= 0 || ballY >= HEIGHT - BALL_SIZE) {
            ballYSpeed *= -1;
            playSound("choque.wav"); 
        }

        // --- CORRECCIÓN COLISIÓN JUGADOR 1 ---
        if (ballX >= 20 && ballX <= 35 && ballY + BALL_SIZE >= p1Y && ballY <= p1Y + PADDLE_HEIGHT && ballXSpeed < 0) {
            ballXSpeed = Math.abs(ballXSpeed);
            ballX = 36; 
            playSound("choque.wav"); 
        }

        // --- COLISIÓN JUGADOR 2 ---
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

        // Renderizado del campo intermedio (Línea discontinua)
        g2d.setColor(Color.DARK_GRAY);
        for(int i=0; i<HEIGHT; i+=20) g2d.drawLine(WIDTH/2, i, WIDTH/2, i+10);

        // Renderizado de las palas de juego
        g2d.setColor(Color.WHITE);
        g2d.fillRect(20, p1Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        g2d.fillRect(WIDTH - 35, p2Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        
        // Renderizado de la pelota si el juego está activo
        if (!gameFinished) {
            g2d.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);
        }

        // Interfaz de puntuación superior estandarizada
        g2d.setFont(new Font("Consolas", Font.BOLD, 40));
        g2d.drawString(score1 + " - " + score2, WIDTH/2 - 60, 50);
        
        g2d.setFont(new Font("Consolas", Font.PLAIN, 18));
        g2d.setColor(Color.GRAY);
        g2d.drawString("Tiempo: " + timeLeft + "s", WIDTH/2 - 55, 80);

        // Feedback visual de gol / saque inminente
        if (waiting && !gameFinished) {
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2d.setColor(Color.CYAN);
            FontMetrics fm = g2d.getFontMetrics();
            String textGol = "¡GOL! PREPARANDO SAQUE...";
            g2d.drawString(textGol, (WIDTH - fm.stringWidth(textGol)) / 2, HEIGHT - 80);
        }

        // PANTALLA FINAL DE RESULTADOS PROFESIONAL (DASHBOARD)
        if (gameFinished) {
            // Fondo traslúcido para oscurecer el campo
            g2d.setColor(new Color(10, 10, 15, 230));
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            // Caja central adaptativa aumentada ligeramente para albergar con soltura los botones reales
            int panelW = 560;
            int panelH = 320;
            int panelX = (WIDTH - panelW) / 2;
            int panelY = (HEIGHT - panelH) / 2 - 10;

            // Efecto sombreado
            g2d.setColor(new Color(0, 0, 0, 160));
            g2d.fillRoundRect(panelX + 6, panelY + 6, panelW, panelH, 18, 18);

            // Fondo del contenedor principal
            GradientPaint panelGrad = new GradientPaint(panelX, panelY, new Color(25, 25, 35), panelX, panelY + panelH, new Color(15, 15, 20));
            g2d.setPaint(panelGrad);
            g2d.fillRoundRect(panelX, panelY, panelW, panelH, 18, 18);

            // Selección de color y texto dinámico según el resultado final
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

            // Borde perimetral estilo neón
            g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 180));
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawRoundRect(panelX, panelY, panelW, panelH, 18, 18);
            g2d.setStroke(new BasicStroke(1f));

            FontMetrics fm;

            // TÍTULO: Efecto de texto iluminado (Glow)
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

            // Separador de diseño
            g2d.setColor(new Color(0, 255, 255, 80));
            g2d.drawLine(panelX + 40, panelY + 85, panelX + panelW - 40, panelY + 85);

            // CONTENEDOR DE ESTADÍSTICAS DEL MARCADOR
            g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
            fm = g2d.getFontMetrics();

            // Marcador Jugador 1
            g2d.setColor(Color.CYAN);
            g2d.drawString("PUNTUACIÓN JUGADOR 1:", panelX + 50, panelY + 135);
            g2d.setColor(Color.WHITE);
            String pts1 = score1 + " GOLES";
            g2d.drawString(pts1, panelX + panelW - 50 - fm.stringWidth(pts1), panelY + 135);

            // Marcador Jugador 2
            g2d.setColor(Color.CYAN);
            g2d.drawString("PUNTUACIÓN JUGADOR 2:", panelX + 50, panelY + 180);
            g2d.setColor(Color.WHITE);
            String pts2 = score2 + " GOLES";
            g2d.drawString(pts2, panelX + panelW - 50 - fm.stringWidth(pts2), panelY + 180);

            // Separador de diseño inferior sutil
            g2d.setColor(new Color(255, 255, 255, 30));
            g2d.drawLine(panelX + 40, panelY + 215, panelX + panelW - 40, panelY + 215);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }
}