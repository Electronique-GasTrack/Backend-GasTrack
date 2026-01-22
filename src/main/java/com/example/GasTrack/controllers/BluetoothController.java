package com.example.GasTrack.controllers;

import com.example.GasTrack.models.Bluetooth;
import com.example.GasTrack.services.BluetoothService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bluetooth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BluetoothController {
    private final BluetoothService bluetoothService;

    @PostMapping
    public ResponseEntity<Bluetooth> createBluetooth(@RequestBody com.example.GasTrack.dto.BluetoothRequest request) {
        return new ResponseEntity<>(bluetoothService.createBluetooth(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bluetooth> getBluetooth(@PathVariable Integer id) {
        return ResponseEntity.ok(bluetoothService.getBluetoothById(id));
    }

    @GetMapping("/mac/{mac}")
    public ResponseEntity<Bluetooth> getBluetoothByMac(@PathVariable String mac) {
        return ResponseEntity.ok(bluetoothService.getBluetoothByMac(mac));
    }

    @GetMapping("/connexion/{connexion}")
    public ResponseEntity<List<Bluetooth>> getBluetoothByConnexion(@PathVariable Boolean connexion) {
        return ResponseEntity.ok(bluetoothService.getBluetoothByConnexion(connexion));
    }

    @GetMapping
    public ResponseEntity<List<Bluetooth>> getAllBluetooth() {
        return ResponseEntity.ok(bluetoothService.getAllBluetooth());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bluetooth> updateBluetooth(@PathVariable Integer id, @RequestBody Bluetooth bluetooth) {
        return ResponseEntity.ok(bluetoothService.updateBluetooth(id, bluetooth));
    }

    @PatchMapping("/{id}/connexion")
    public ResponseEntity<Bluetooth> updateConnexion(@PathVariable Integer id, @RequestParam Boolean connexion) {
        return ResponseEntity.ok(bluetoothService.updateConnexion(id, connexion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBluetooth(@PathVariable Integer id) {
        bluetoothService.deleteBluetooth(id);
        return ResponseEntity.noContent().build();
    }
}