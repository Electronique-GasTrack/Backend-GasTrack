package com.example.GasTrack.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MesureRequest {
    private Integer batteriePourcentage;
    private Integer gazPourcentage;
    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime dateMesure; // Doit être au format : dd-MM-yyyy HH:mm:ss (ex: 29-01-2026 14:00:00)
    private Integer bouteilleId;
}
