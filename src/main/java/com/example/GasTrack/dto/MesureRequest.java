package com.example.GasTrack.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MesureRequest {
    private Integer batteriePourcentage;
    private Integer gazPourcentage;
    private LocalDateTime dateMesure; // Optional, defaults to now if null
    private Integer bouteilleId;
}
