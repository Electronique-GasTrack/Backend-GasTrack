package com.example.GasTrack.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "bluetooth")
public class Bluetooth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bluetooth")
    private Integer idBluetooth;

    @Column(name = "nom_bluetooth")
    private String nomBluetooth;

    @Column(name = "addresse_mac", nullable = false)
    private String addresseMac;

    @Column(name = "connexion")
    private Boolean connexion = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "bluetooth")
    @JsonIgnore
    private Bouteille bouteille;
}