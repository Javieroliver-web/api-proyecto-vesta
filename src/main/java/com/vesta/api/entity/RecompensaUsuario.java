package com.vesta.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "recompensas_usuario")
@Data @NoArgsConstructor @AllArgsConstructor
public class RecompensaUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "usu_id")
    private Usuario usuario;
    
    @Column(name = "puntos")
    private Integer puntos = 0;
    
    @Column(name = "nivel")
    private String nivel = "BRONCE"; // BRONCE, PLATA, ORO, DIAMANTE
    
    @Column(name = "racha_dias")
    private Integer rachaDias = 0;
    
    @OneToMany(mappedBy = "recompensa")
    private List<Logro> logros;
}