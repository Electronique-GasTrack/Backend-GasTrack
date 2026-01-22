package com.example.GasTrack.dto;

import lombok.Data;

@Data
public class BouteilleRequest {
    private Float poids;
    private String frequenceCuisine;
    private Float niveauInitial = 100.0f;
    private Integer userId;
    private Integer bluetoothId; // Optional
}
