package com.retro.main;

import com.retro.main.model.Usuario;
import com.retro.main.repository.UsuarioRepository;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaRanking extends JDialog {
    public VentanaRanking(JFrame padre, UsuarioRepository repo) {
        super(padre, "RANKING DE JUGADORES", true);
        setSize(500, 400);
        setLocationRelativeTo(padre);

        // 1. Crear las columnas de la tabla
        String[] columnas = {"NOMBRE", "SNAKE", "PONG", "2048", "TETRIS"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);
        JTable tabla = new JTable(modelo);

        // 2. Pedir los datos a MySQL usando el repositorio
        List<Usuario> lista = repo.findAll(); // Esto trae a TODOS de la base de datos
        
        for (Usuario u : lista) {
            Object[] fila = {
                u.getUsername(), 
                
            };
            modelo.addRow(fila);
        }

        // 3. Diseño de la ventana
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        JButton btnCerrar = new JButton("VOLVER");
        btnCerrar.addActionListener(e -> dispose());
        add(btnCerrar, BorderLayout.SOUTH);
    }
}