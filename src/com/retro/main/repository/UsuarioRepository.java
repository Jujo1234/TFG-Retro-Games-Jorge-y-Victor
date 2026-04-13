package com.retro.main.repository;

import com.retro.main.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // FÓRMULA GLOBAL:
    // Si ha jugado al Snake: (5000 - segundos) + puntos_2048 + puntos_tetris
    // Si no ha jugado al Snake (segundos = 0): puntos_2048 + puntos_tetris
    @Query("SELECT u FROM Usuario u ORDER BY " +
           "((CASE WHEN u.puntos_snake > 0 THEN (5000 - u.puntos_snake) ELSE 0 END) + " +
           "u.puntos_2048 + u.puntos_tetris) DESC")
    List<Usuario> findRankingGlobal();
}