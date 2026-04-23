package com.retro.main.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private int puntos_snake = 0;
    private int puntos_2048 = 0;
    private int puntos_tetris = 0;

    // Getters y Setters
    public Long getId() { return id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public int getPuntos_snake() { return puntos_snake; }
    public void setPuntos_snake(int puntos_snake) { this.puntos_snake = puntos_snake; }
    
    public int getPuntos_2048() { return puntos_2048; }
    public void setPuntos_2048(int puntos_2048) { this.puntos_2048 = puntos_2048; }
    
    public int getPuntos_tetris() { return puntos_tetris; }
    public void setPuntos_tetris(int puntos_tetris) { this.puntos_tetris = puntos_tetris; }
}