package com.retro.main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
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
import com.retro.main.repository.UsuarioRepository;
import com.retro.main.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;

import com.retro.games.snake.SnakeGame;
import com.retro.games.pong.PongGame;
import com.retro.games.puzzle2048.Game2048;
import com.retro.games.tetris.TetrisGame;

@SpringBootApplication
@ComponentScan(basePackages = "com.retro") 
@EntityScan("com.retro.main.model")
@EnableJpaRepositories("com.retro.main.repository")
@Component
public class MenuPrincipal extends JFrame {
    private static final long serialVersionUID = 1L;
    private JLabel lblReloj;
    
    @Autowired
    private UsuarioRepository usuarioRepo;
    private Usuario usuarioSesion;

    public MenuPrincipal() {
        setTitle("ARCADE OS v2.0 - TFG EDITION");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 850);
        setLocationRelativeTo(null);
        
        JPanel panelFondo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(10, 10, 15), 
                                                     0, getHeight(), new Color(30, 35, 45));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panelFondo.setLayout(new BoxLayout(panelFondo, BoxLayout.Y_AXIS));
        panelFondo.setBorder(new EmptyBorder(10, 30, 15, 30));

        // 1. BARRA SUPERIOR
        JPanel barraEstado = new JPanel(new BorderLayout());
        barraEstado.setOpaque(false);
        barraEstado.setMaximumSize(new Dimension(500, 25));
        JLabel lblStatus = new JLabel(" SYSTEM STATUS: ONLINE");
        lblStatus.setForeground(new Color(100, 100, 100));
        lblStatus.setFont(new Font("Monospaced", Font.BOLD, 11));
        lblReloj = new JLabel();
        lblReloj.setForeground(Color.CYAN);
        lblReloj.setFont(new Font("Monospaced", Font.BOLD, 12));
        iniciarReloj();
        barraEstado.add(lblStatus, BorderLayout.WEST);
        barraEstado.add(lblReloj, BorderLayout.EAST);
        panelFondo.add(barraEstado);
        panelFondo.add(Box.createRigidArea(new Dimension(0, 15)));

        // 2. TÍTULO
        JLabel titulo = new JLabel("ARCADE MULTIGAME");
        titulo.setForeground(Color.CYAN);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 34));
        titulo.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        panelFondo.add(titulo);
        panelFondo.add(Box.createRigidArea(new Dimension(0, 25)));

        // 3. BOTONES (Centrados perfectamente)
        panelFondo.add(crearBotonPro("NUEVO JUGADOR", null, e -> mostrarRegistro()));
        panelFondo.add(Box.createRigidArea(new Dimension(0, 12)));
        
        panelFondo.add(crearBotonPro("SNAKE ARCADE", "res/snake_icon.png", e -> {
            if (usuarioSesion == null) JOptionPane.showMessageDialog(this, "¡Registra un jugador!");
            else lanzarJuego(new SnakeGame(usuarioSesion, usuarioRepo), "Snake Arcade");
        }));
        panelFondo.add(Box.createRigidArea(new Dimension(0, 12)));
        
        panelFondo.add(crearBotonPro("PONG RETRO", "res/pong_icon.png", e -> lanzarJuego(new PongGame(), "Pong Retro")));
        panelFondo.add(Box.createRigidArea(new Dimension(0, 12)));
        
        panelFondo.add(crearBotonPro("2048 PUZZLE", "res/2048_icon.png", e -> {
            if (usuarioSesion == null) JOptionPane.showMessageDialog(this, "¡Registra un jugador!");
            else lanzarJuego(new Game2048(usuarioSesion, usuarioRepo), "2048 Puzzle");
        }));
        panelFondo.add(Box.createRigidArea(new Dimension(0, 12)));
        
        panelFondo.add(crearBotonPro("TETRIS CLASSIC", "res/tetris_icon.png", e -> {
            if (usuarioSesion == null) JOptionPane.showMessageDialog(this, "¡Registra un jugador!");
            else lanzarJuego(new TetrisGame(usuarioSesion, usuarioRepo), "Tetris Classic");
        }));
        panelFondo.add(Box.createRigidArea(new Dimension(0, 12)));
        
        panelFondo.add(crearBotonPro(" VER RANKING DE MÉRITOS", "res/trofeo_neon.png", e -> mostrarRanking()));
        
        panelFondo.add(Box.createRigidArea(new Dimension(0, 25)));

        // 4. BANNER
        panelFondo.add(cargarBannerJuegos());
        panelFondo.add(Box.createRigidArea(new Dimension(0, 20)));

     // INFO BOX
        JPanel panelInfo = new JPanel(new GridLayout(0, 1));
        panelInfo.setOpaque(false);
        panelInfo.setBorder(new LineBorder(new Color(0, 255, 255, 30), 1));
        panelInfo.setMaximumSize(new Dimension(380, 85));

        // AQUÍ ESTÁ EL CAMBIO: Texto traducido y limpio
        String infoTexto = "<html><center><font color='cyan'><b>SISTEMA ARCADE v2.0</b></font><br>"
                + "<font color='#888888'>PROYECTO TFG - BASE DE DATOS ACTIVA<br>"
                + "AUTORES: JORGE & VÍCTOR | ESTADO: CONECTADO</font></center></html>";

        JLabel lblInfo = new JLabel(infoTexto);
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        panelInfo.add(lblInfo);
        panelInfo.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        panelFondo.add(panelInfo);

        panelFondo.add(Box.createRigidArea(new Dimension(0, 20)));

        // BOTÓN SALIR
        JButton btnSalir = crearBotonPro("SALIR DE LA APP", null, e -> System.exit(0));
        btnSalir.setBackground(new Color(50, 15, 15));
        btnSalir.setForeground(new Color(255, 80, 80));
        btnSalir.setBorder(new LineBorder(new Color(255, 0, 0, 100), 1));
        panelFondo.add(btnSalir);

        add(panelFondo);
    }

    private void mostrarRegistro() {
        while (true) {
            String nombre = JOptionPane.showInputDialog(this, "Nombre del nuevo jugador:");
            if (nombre == null) break;
            if (nombre.trim().isEmpty()) continue;
            boolean existe = usuarioRepo.findAll().stream()
                             .anyMatch(u -> u.getUsername().equalsIgnoreCase(nombre.trim()));
            if (existe) {
                JOptionPane.showMessageDialog(this, "Ese nombre ya existe.", "Error", JOptionPane.WARNING_MESSAGE);
            } else {
                Usuario user = new Usuario();
                user.setUsername(nombre.trim());
                usuarioSesion = usuarioRepo.save(user);
                JOptionPane.showMessageDialog(this, "¡Bienvenido " + nombre + "!");
                break;
            }
        }
    }

    private void mostrarRanking() {
        JDialog ventanaRanking = new JDialog(this, "RANKING GLOBAL", true);
        ventanaRanking.setSize(600, 500);
        ventanaRanking.setLocationRelativeTo(this);
        ventanaRanking.getContentPane().setBackground(new Color(15, 15, 25));
        List<Usuario> usuarios = usuarioRepo.findRankingGlobal();
        String[] columnas = {"JUGADOR", "TIME SNAKE", "MAX 2048", "PUNTOS TETRIS", "TOTAL"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Usuario u : usuarios) {
            int ptsSnake = (u.getPuntos_snake() > 0) ? (5000 - u.getPuntos_snake()) : 0;
            int total = ptsSnake + u.getPuntos_2048() + u.getPuntos_tetris();
            String tiempoStr = (u.getPuntos_snake() > 0) ? u.getPuntos_snake() + "s" : "---";
            modelo.addRow(new Object[]{ u.getUsername(), tiempoStr, u.getPuntos_2048(), u.getPuntos_tetris(), total });
        }
        JTable tabla = new JTable(modelo);
        tabla.setBackground(new Color(30, 30, 40));
        tabla.setForeground(Color.CYAN);
        tabla.setRowHeight(35);
        ventanaRanking.add(new JScrollPane(tabla), BorderLayout.CENTER);
        ventanaRanking.setVisible(true);
    }

    private JButton crearBotonPro(String texto, String rutaIcono, java.awt.event.ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setPreferredSize(new Dimension(380, 50));
        boton.setMinimumSize(new Dimension(380, 50));
        boton.setMaximumSize(new Dimension(380, 50));
        boton.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        
        // CORRECCIÓN: Alineación centrada para el conjunto icono + texto
        boton.setHorizontalAlignment(SwingConstants.CENTER); 
        boton.setHorizontalTextPosition(SwingConstants.RIGHT); 

        boton.setOpaque(true);
        boton.setBackground(new Color(40, 40, 45));
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(new LineBorder(new Color(0, 255, 255, 50), 1));
        
        if (rutaIcono != null) {
            try {
                ImageIcon icon = new ImageIcon(rutaIcono);
                if (icon.getIconWidth() > 0) {
                    Image img = icon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
                    boton.setIcon(new ImageIcon(img));
                    boton.setIconTextGap(20); // Espacio entre icono y texto
                }
            } catch (Exception e) {}
        }
        
        boton.addActionListener(accion);
        return boton;
    }

    private void iniciarReloj() {
        new Timer(1000, e -> lblReloj.setText(new SimpleDateFormat("HH:mm:ss  ").format(new Date()))).start();
    }

    private JLabel cargarBannerJuegos() {
        JLabel banner = new JLabel();
        try {
            ImageIcon icono = new ImageIcon("res/collage_juegos.png");
            if (icono.getIconWidth() > 0) {
                Image img = icono.getImage().getScaledInstance(400, 110, Image.SCALE_SMOOTH);
                banner.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) { banner.setText("[ IMAGE ]"); banner.setForeground(Color.GRAY); }
        banner.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        return banner;
    }

    private void lanzarJuego(JPanel panelJuego, String tituloVentana) {
        JFrame v = new JFrame(tituloVentana);
        v.add(panelJuego); v.pack(); v.setLocationRelativeTo(null); v.setVisible(true);
        panelJuego.requestFocusInWindow();
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