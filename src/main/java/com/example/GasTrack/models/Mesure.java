package com.example.GasTrack.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "mesure")
@Data
public class Mesure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMesure;

    private Integer batteriePourcentage;

    @Column(name = "date_mesure")
    private LocalDateTime dateMesure;

    private Integer gazPourcentage;

    @ManyToOne
    @JoinColumn(name = "bouteille_id")
    @JsonIgnore
    private Bouteille bouteille;

}