package com.example.GasTrack.dto;

import lombok.Data;

@Data
public class BluetoothRequest {
    private String nomBluetooth;
    private String addresseMac;
    private Boolean connexion;
    private Integer bouteilleId; // Optional: Link this bluetooth to an existing bouteille
}
