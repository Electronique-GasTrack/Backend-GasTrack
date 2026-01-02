package com.example.GasTrack.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Bluetooth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idBluetooth;

    private String nomBluetooth;
    private String addresseMac;
    private Boolean connexion;

    @OneToOne(mappedBy = "bluetooth")
    private Bouteille bouteille;
}