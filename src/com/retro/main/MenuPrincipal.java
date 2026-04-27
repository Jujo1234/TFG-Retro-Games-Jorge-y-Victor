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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private JPanel panelFondo;
    private JPanel panelIconos; 
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public MenuPrincipal() {
        setTitle("ARCADE OS v2.0 - TFG EDITION");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 850);
        setLocationRelativeTo(null);
        
        panelFondo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                boolean modoOscuro = (usuarioSesion == null) ? true : usuarioSesion.isDarkMode();
                Color c1 = modoOscuro ? new Color(10, 10, 15) : new Color(240, 240, 240);
                Color c2 = modoOscuro ? new Color(30, 35, 45) : new Color(190, 190, 190);
                
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panelFondo.setLayout(new BoxLayout(panelFondo, BoxLayout.Y_AXIS));
        panelFondo.setBorder(new EmptyBorder(10, 30, 15, 30));

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

        panelIconos = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelIconos.setOpaque(false);
        panelIconos.setMaximumSize(new Dimension(500, 40));
        
        JButton btnPerfil = crearBotonCuadrado("res/perfil_icon.png", e -> { if(usuarioSesion != null) mostrarPerfil(); else JOptionPane.showMessageDialog(this, "Inicia sesión primero."); });
        JButton btnAjustes = crearBotonCuadrado("res/ajustes_icon.png", e -> { if(usuarioSesion != null) mostrarAjustes(); else JOptionPane.showMessageDialog(this, "Inicia sesión primero."); });
        
        panelIconos.add(btnPerfil);
        panelIconos.add(btnAjustes);
        panelFondo.add(panelIconos);

        panelFondo.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel titulo = new JLabel("ARCADE MULTIGAME");
        titulo.setForeground(Color.CYAN);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 34));
        titulo.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        panelFondo.add(titulo);
        panelFondo.add(Box.createRigidArea(new Dimension(0, 25)));

        panelFondo.add(crearBotonPro("NUEVO JUGADOR / SESIÓN", null, e -> mostrarAutenticacion()));
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
        panelFondo.add(Box.createRigidArea(new Dimension(0, 20)));

        panelFondo.add(cargarBannerJuegos());
        panelFondo.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel panelInfo = new JPanel(new GridLayout(0, 1));
        panelInfo.setOpaque(false);
        panelInfo.setBorder(new LineBorder(new Color(0, 255, 255, 30), 1));
        panelInfo.setMaximumSize(new Dimension(380, 85));

        String infoTexto = "<html><center><font color='cyan'><b>SISTEMA ARCADE v2.0</b></font><br>"
                + "<font color='#888888'>PROYECTO TFG - BASE DE DATOS ACTIVA<br>"
                + "AUTORES: JORGE & VÍCTOR | ESTADO: CONECTADO</font></center></html>";

        JLabel lblInfo = new JLabel(infoTexto);
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        panelInfo.add(lblInfo);
        panelInfo.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        panelFondo.add(panelInfo);

        panelFondo.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton btnSalir = crearBotonPro("SALIR DE LA APP", null, e -> System.exit(0));
        btnSalir.setBackground(new Color(50, 15, 15));
        btnSalir.setForeground(new Color(255, 80, 80));
        btnSalir.setBorder(new LineBorder(new Color(255, 0, 0, 100), 1));
        panelFondo.add(btnSalir);

        add(panelFondo);
    }
    
    private void aplicarTema() {
        this.getContentPane().repaint();
        boolean modoOscuro = (usuarioSesion == null || usuarioSesion.isDarkMode());
        Color btnBg = modoOscuro ? new Color(40, 40, 45) : new Color(200, 200, 200);
        Color txt = modoOscuro ? Color.WHITE : Color.BLACK;

        for (java.awt.Component comp : panelFondo.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if (!btn.getText().equals("SALIR DE LA APP")) {
                    btn.setBackground(btnBg);
                    btn.setForeground(txt);
                }
            }
        }
        for (java.awt.Component comp : panelIconos.getComponents()) {
            if (comp instanceof JButton) {
                ((JButton) comp).setBackground(btnBg);
                ((JButton) comp).setForeground(txt);
            }
        }
    }

    private JButton crearBotonCuadrado(String rutaIcono, java.awt.event.ActionListener accion) {
        JButton boton = new JButton();
        boton.setPreferredSize(new Dimension(35, 35));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(new LineBorder(new Color(0, 255, 255, 50), 1));
        boton.setOpaque(true);
        boton.setMargin(new Insets(0, 0, 0, 0));
        ImageIcon icon = new ImageIcon(rutaIcono);
        if (icon.getIconWidth() > 0) {
            Image img = icon.getImage().getScaledInstance(33, 33, Image.SCALE_SMOOTH);
            boton.setIcon(new ImageIcon(img));
        } else { boton.setText("?"); }
        boton.addActionListener(accion);
        return boton;
    }

    private JButton crearBotonPro(String texto, String rutaIcono, java.awt.event.ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setPreferredSize(new Dimension(380, 50));
        boton.setMinimumSize(new Dimension(380, 50));
        boton.setMaximumSize(new Dimension(380, 50));
        boton.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
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
                    boton.setIconTextGap(20);
                }
            } catch (Exception e) {}
        }
        boton.addActionListener(accion);
        return boton;
    }

    private void iniciarReloj() { new Timer(1000, e -> lblReloj.setText(new SimpleDateFormat("HH:mm:ss  ").format(new Date()))).start(); }
    private JLabel cargarBannerJuegos() { JLabel banner = new JLabel(); try { ImageIcon icono = new ImageIcon("res/collage_juegos.png"); if (icono.getIconWidth() > 0) { Image img = icono.getImage().getScaledInstance(400, 110, Image.SCALE_SMOOTH); banner.setIcon(new ImageIcon(img)); } } catch (Exception e) { banner.setText("[ IMAGE ]"); banner.setForeground(Color.GRAY); } banner.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT); return banner; }
    
    private void lanzarJuego(JPanel panelJuego, String tituloVentana) {
        long startTime = System.currentTimeMillis(); 
        JFrame v = new JFrame(tituloVentana);
        v.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        v.setResizable(false); 
        v.add(panelJuego); 
        v.pack(); 
        v.setLocationRelativeTo(null); 
        v.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                long duration = System.currentTimeMillis() - startTime;
                if (usuarioSesion != null) {
                    int seconds = (int)(duration / 1000);
                    int ptsPartida = Math.max(0, 5000 - seconds);
                    
                    if (panelJuego instanceof SnakeGame) {
                        usuarioSesion.setPuntos_snake(ptsPartida);
                    } else if (panelJuego instanceof Game2048) {
                        usuarioSesion.setPuntos_2048(ptsPartida);
                    } else if (panelJuego instanceof TetrisGame) {
                        usuarioSesion.setPuntos_tetris(ptsPartida);
                    }
                    usuarioRepo.save(usuarioSesion);
                }
                if (panelJuego instanceof PongGame) ((PongGame) panelJuego).detenerJuego();
                if (panelJuego instanceof SnakeGame) ((SnakeGame) panelJuego).pararMusica();
                if (panelJuego instanceof TetrisGame) ((TetrisGame) panelJuego).detenerJuego();
            }
        });
        v.setVisible(true);
        panelJuego.requestFocusInWindow();
    }

    private void mostrarAutenticacion() {
        if (usuarioSesion != null) {
            JOptionPane.showMessageDialog(this, "Ya tienes una sesión iniciada: " + usuarioSesion.getUsername());
            return;
        }

        JDialog dialog = new JDialog(this, "Autenticación", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel panelLogin = new JPanel(new GridBagLayout());
        JPanel panelRegistro = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField userLogin = new JTextField(); userLogin.setPreferredSize(new Dimension(180, 25));
        JPasswordField passLogin = new JPasswordField(); passLogin.setPreferredSize(new Dimension(180, 25));
        JButton btnLogin = new JButton("Entrar");

        gbc.gridx = 0; gbc.gridy = 0; panelLogin.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1; panelLogin.add(userLogin, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panelLogin.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; panelLogin.add(passLogin, gbc);
        gbc.gridx = 1; gbc.gridy = 2; panelLogin.add(btnLogin, gbc);

        btnLogin.addActionListener(e -> {
            String username = userLogin.getText().trim();
            String password = new String(passLogin.getPassword());

            if (username.equalsIgnoreCase("admin") && password.equals("admin1234")) {
                Usuario admin = usuarioRepo.findAll().stream().filter(u -> u.getUsername().equalsIgnoreCase("admin")).findFirst().orElse(null);
                if (admin == null) {
                    admin = new Usuario();
                    admin.setUsername("admin");
                    admin.setPassword(encoder.encode("admin1234"));
                    admin.setDarkMode(true);
                    usuarioRepo.save(admin);
                }
                usuarioSesion = admin;
                aplicarTema();
                JOptionPane.showMessageDialog(dialog, "Acceso Admin concedido.");
                dialog.dispose();
                return;
            }

            Usuario u = usuarioRepo.findAll().stream()
                    .filter(user -> user.getUsername().equalsIgnoreCase(username))
                    .findFirst().orElse(null);

            if (u != null && encoder.matches(password, u.getPassword())) {
                usuarioSesion = u;
                aplicarTema(); 
                JOptionPane.showMessageDialog(dialog, "¡Bienvenido, " + username + "!");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Usuario o contraseña incorrectos.");
            }
        });

        JTextField userReg = new JTextField(); userReg.setPreferredSize(new Dimension(180, 25));
        JPasswordField passReg = new JPasswordField(); passReg.setPreferredSize(new Dimension(180, 25));
        JPasswordField passRegConfirm = new JPasswordField(); passRegConfirm.setPreferredSize(new Dimension(180, 25));
        JButton btnReg = new JButton("Registrarse");

        gbc.gridx = 0; gbc.gridy = 0; panelRegistro.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1; panelRegistro.add(userReg, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panelRegistro.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; panelRegistro.add(passReg, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panelRegistro.add(new JLabel("Confirmar:"), gbc);
        gbc.gridx = 1; panelRegistro.add(passRegConfirm, gbc);
        gbc.gridx = 1; gbc.gridy = 3; panelRegistro.add(btnReg, gbc);

        btnReg.addActionListener(e -> {
            String username = userReg.getText().trim();
            String password = new String(passReg.getPassword());
            String confirm = new String(passRegConfirm.getPassword());

            if (username.isEmpty() || password.isEmpty()) return;
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "Las contraseñas no coinciden.");
                return;
            }

            boolean existe = usuarioRepo.findAll().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));

            if (existe) {
                JOptionPane.showMessageDialog(dialog, "Ese nombre ya existe.");
            } else {
                Usuario nuevo = new Usuario();
                nuevo.setUsername(username);
                nuevo.setPassword(encoder.encode(password));
                nuevo.setDarkMode(true);
                usuarioSesion = usuarioRepo.save(nuevo);
                aplicarTema(); 
                JOptionPane.showMessageDialog(dialog, "Usuario creado exitosamente.");
                dialog.dispose();
            }
        });

        tabbedPane.addTab("Iniciar Sesión", panelLogin);
        tabbedPane.addTab("Crear Jugador", panelRegistro);
        dialog.add(tabbedPane);
        dialog.setVisible(true);
    }
    
    private void mostrarPerfil() {
        JDialog d = createStyledDialog("PERFIL DEL JUGADOR", 400, 350);
        d.setLayout(new BorderLayout(10, 10));
        
        JPanel panelDatos = new JPanel(new GridLayout(0, 2, 10, 10));
        panelDatos.setOpaque(false);
        panelDatos.setBorder(new EmptyBorder(20, 20, 20, 20));

        Font fontLabel = new Font("Monospaced", Font.BOLD, 12);
        Color colLabel = Color.CYAN;
        
        addStatRow(panelDatos, "JUGADOR:", usuarioSesion.getUsername().toUpperCase(), fontLabel, colLabel);
        addStatRow(panelDatos, "SNAKE:", usuarioSesion.getPuntos_snake() + " pts", fontLabel, colLabel);
        addStatRow(panelDatos, "2048:", usuarioSesion.getPuntos_2048() + " pts", fontLabel, colLabel);
        addStatRow(panelDatos, "TETRIS:", usuarioSesion.getPuntos_tetris() + " pts", fontLabel, colLabel);

        JPanel panelAcciones = new JPanel(new GridBagLayout());
        panelAcciones.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        
        JPasswordField pass = new JPasswordField(15);
        pass.setBackground(new Color(25, 25, 35)); pass.setForeground(Color.WHITE);
        pass.setCaretColor(Color.CYAN); pass.setBorder(new LineBorder(Color.CYAN, 1));
        
        gbc.gridx = 0; gbc.gridy = 0; panelAcciones.add(new JLabel("NUEVA CONTRASEÑA:"), gbc);
        gbc.gridy = 1; panelAcciones.add(pass, gbc);
        
        JButton btnGuardar = new JButton("ACTUALIZAR");
        estilizarBotonPopup(btnGuardar);
        btnGuardar.addActionListener(e -> {
            usuarioSesion.setPassword(encoder.encode(new String(pass.getPassword())));
            usuarioRepo.save(usuarioSesion);
            d.dispose();
            JOptionPane.showMessageDialog(this, "Datos actualizados.");
        });
        gbc.gridy = 2; gbc.insets = new Insets(10,0,0,0); panelAcciones.add(btnGuardar, gbc);

        d.add(panelDatos, BorderLayout.CENTER);
        d.add(panelAcciones, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void mostrarAjustes() {
        JDialog d = createStyledDialog("CONFIGURACIÓN", 350, 300);
        d.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JCheckBox checkDark = new JCheckBox("  MODO OSCURO", usuarioSesion.isDarkMode());
        checkDark.setOpaque(false);
        checkDark.setForeground(Color.WHITE);
        checkDark.setFont(new Font("Monospaced", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; d.add(checkDark, gbc);

        JButton btnLogout = new JButton("CERRAR SESIÓN");
        estilizarBotonPopup(btnLogout);
        btnLogout.addActionListener(e -> {
            usuarioSesion = null;
            aplicarTema();
            d.dispose();
            JOptionPane.showMessageDialog(this, "Sesión finalizada.");
        });
        gbc.gridy = 1; d.add(btnLogout, gbc);

        JButton btnGuardar = new JButton("GUARDAR CAMBIOS");
        estilizarBotonPopup(btnGuardar);
        btnGuardar.addActionListener(e -> {
            usuarioSesion.setDarkMode(checkDark.isSelected());
            usuarioRepo.save(usuarioSesion);
            aplicarTema();
            d.dispose();
        });
        gbc.gridy = 2; d.add(btnGuardar, gbc);
        d.setVisible(true);
    }

    private int calcularTotal(Usuario u) {
        int ptsSnake = (u.getPuntos_snake() > 0) ? Math.max(0, 5000 - u.getPuntos_snake()) : 0;
        return ptsSnake + u.getPuntos_2048() + u.getPuntos_tetris();
    }

    private void mostrarRanking() {
        JDialog ventanaRanking = new JDialog(this, "RANKING GLOBAL", true);
        ventanaRanking.setSize(650, 550);
        ventanaRanking.setLocationRelativeTo(this);
        ventanaRanking.setLayout(new BorderLayout());
        
        List<Usuario> usuarios = usuarioRepo.findAll();
        usuarios.removeIf(u -> "admin".equalsIgnoreCase(u.getUsername()));
        usuarios.sort((u1, u2) -> Integer.compare(calcularTotal(u2), calcularTotal(u1)));
        
        String[] columnas = {"POS", "JUGADOR", "TIME SNAKE", "MAX 2048", "PUNTOS TETRIS", "TOTAL"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) { 
            @Override public boolean isCellEditable(int r, int c) { return false; } 
        };
        
        int pos = 1;
        for (Usuario u : usuarios) {
            String labelPos = (pos==1) ? "🏆 1º" : (pos==2) ? "🥈 2º" : (pos==3) ? "🥉 3º" : String.valueOf(pos);
            modelo.addRow(new Object[]{ labelPos, u.getUsername(), u.getPuntos_snake() + "s", u.getPuntos_2048(), u.getPuntos_tetris(), calcularTotal(u) });
            pos++;
        }
        
        JTable tabla = new JTable(modelo);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(80);
        ventanaRanking.add(new JScrollPane(tabla), BorderLayout.CENTER);
        
        boolean esAdmin = usuarioSesion != null && "admin".equalsIgnoreCase(usuarioSesion.getUsername());
        
        if (esAdmin) {
            JPanel panelAdmin = new JPanel();
            JButton btnBorrar = new JButton("Borrar");
            JButton btnEditar = new JButton("Editar");
            
            btnBorrar.addActionListener(e -> {
                int fila = tabla.getSelectedRow();
                if (fila != -1) {
                    Usuario u = usuarios.get(fila);
                    if (JOptionPane.showConfirmDialog(ventanaRanking, "¿Borrar a " + u.getUsername() + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        usuarioRepo.delete(u);
                        ventanaRanking.dispose();
                        mostrarRanking();
                    }
                }
            });
            
            btnEditar.addActionListener(e -> {
                int fila = tabla.getSelectedRow();
                if (fila != -1) {
                    Usuario u = usuarios.get(fila);
                    JTextField txtTetris = new JTextField(String.valueOf(u.getPuntos_tetris()));
                    JTextField txt2048 = new JTextField(String.valueOf(u.getPuntos_2048()));
                    JTextField txtSnake = new JTextField(String.valueOf(u.getPuntos_snake()));
                    JPanel panelForm = new JPanel(new GridLayout(3, 2, 5, 5));
                    panelForm.add(new JLabel("Tetris:")); panelForm.add(txtTetris);
                    panelForm.add(new JLabel("2048:")); panelForm.add(txt2048);
                    panelForm.add(new JLabel("Snake:")); panelForm.add(txtSnake);
                    int res = JOptionPane.showConfirmDialog(ventanaRanking, panelForm, "Editar " + u.getUsername(), JOptionPane.OK_CANCEL_OPTION);
                    if (res == JOptionPane.OK_OPTION) {
                        try {
                            u.setPuntos_tetris(Integer.parseInt(txtTetris.getText()));
                            u.setPuntos_2048(Integer.parseInt(txt2048.getText()));
                            u.setPuntos_snake(Integer.parseInt(txtSnake.getText()));
                            usuarioRepo.save(u);
                            ventanaRanking.dispose();
                            mostrarRanking();
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(ventanaRanking, "Error numérico");
                        }
                    }
                }
            });
            panelAdmin.add(btnEditar);
            panelAdmin.add(btnBorrar);
            ventanaRanking.add(panelAdmin, BorderLayout.SOUTH);
        }
        ventanaRanking.setVisible(true);
    }
    
    private JDialog createStyledDialog(String title, int w, int h) {
        JDialog d = new JDialog(this, title, true);
        d.setSize(w, h);
        d.setLocationRelativeTo(this);
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0,0, new Color(20,20,30), 0, getHeight(), new Color(10,10,15)));
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        d.setContentPane(p);
        d.getRootPane().setBorder(new LineBorder(Color.CYAN, 2));
        return d;
    }

    private void estilizarBotonPopup(JButton b) {
        b.setPreferredSize(new Dimension(220, 40));
        b.setBackground(new Color(40, 40, 50));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(Color.CYAN, 1));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(60, 60, 70)); }
            public void mouseExited(MouseEvent e) { b.setBackground(new Color(40, 40, 50)); }
        });
    }

    private void addStatRow(JPanel p, String label, String value, Font f, Color c) {
        JLabel l1 = new JLabel(label); l1.setForeground(c); l1.setFont(f);
        JLabel l2 = new JLabel(value); l2.setForeground(Color.WHITE); l2.setFont(f);
        p.add(l1); p.add(l2);
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