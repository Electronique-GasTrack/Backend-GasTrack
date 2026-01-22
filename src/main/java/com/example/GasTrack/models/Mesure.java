package com.example.GasTrack.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mesure")
@Data
public class Mesure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mesure")
    private Integer idMesure;

    @Column(name = "batterie_pourcentage", nullable = false)
    private Integer batteriePourcentage;

    @Column(name = "gaz_pourcentage", nullable = false)
    private Integer gazPourcentage;

    @Column(name = "date_mesure", nullable = false)
    private LocalDateTime dateMesure;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bouteille_id", nullable = false)
    @JsonIgnore
    private Bouteille bouteille;
}