package com.example.GasTrack.services;

import com.example.GasTrack.models.Bluetooth;
import com.example.GasTrack.repositories.BluetoothRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BluetoothService {
    private final BluetoothRepository bluetoothRepository;
    private final com.example.GasTrack.repositories.BouteilleRepository bouteilleRepository;

    public Bluetooth createBluetooth(com.example.GasTrack.dto.BluetoothRequest request) {
        Bluetooth bluetooth = new Bluetooth();
        bluetooth.setNomBluetooth(request.getNomBluetooth());
        bluetooth.setAddresseMac(request.getAddresseMac());
        bluetooth.setConnexion(request.getConnexion() != null ? request.getConnexion() : false);
        
        Bluetooth saved = bluetoothRepository.save(bluetooth);
        
        if (request.getBouteilleId() != null) {
            com.example.GasTrack.models.Bouteille bouteille = bouteilleRepository.findById(request.getBouteilleId())
                    .orElseThrow(() -> new RuntimeException("Bouteille not found with ID: " + request.getBouteilleId()));
            bouteille.setBluetooth(saved);
            bouteilleRepository.save(bouteille);
        }
        return saved;
    }

    public Bluetooth createBluetooth(Bluetooth bluetooth) {
        return bluetoothRepository.save(bluetooth);
    }

    public Bluetooth getBluetoothById(Integer id) {
        return bluetoothRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bluetooth not found"));
    }

    public Bluetooth getBluetoothByMac(String mac) {
        return bluetoothRepository.findByAddresseMac(mac)
                .orElseThrow(() -> new RuntimeException("Bluetooth not found"));
    }

    public List<Bluetooth> getBluetoothByConnexion(Boolean connexion) {
        return bluetoothRepository.findByConnexion(connexion);
    }

    public List<Bluetooth> getAllBluetooth() {
        return bluetoothRepository.findAll();
    }

    public Bluetooth updateBluetooth(Integer id, Bluetooth bluetooth) {
        Bluetooth existing = getBluetoothById(id);
        existing.setNomBluetooth(bluetooth.getNomBluetooth());
        existing.setAddresseMac(bluetooth.getAddresseMac());
        existing.setConnexion(bluetooth.getConnexion());
        return bluetoothRepository.save(existing);
    }

    public Bluetooth updateConnexion(Integer id, Boolean connexion) {
        Bluetooth bluetooth = getBluetoothById(id);
        bluetooth.setConnexion(connexion);
        return bluetoothRepository.save(bluetooth);
    }

    public void deleteBluetooth(Integer id) {
        bluetoothRepository.deleteById(id);
    }
}