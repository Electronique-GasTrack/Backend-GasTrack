package com.example.GasTrack.repositories;

import com.example.GasTrack.models.Bluetooth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface BluetoothRepository extends JpaRepository<Bluetooth, Integer> {
    Optional<Bluetooth> findByAddresseMac(String adresseMac);
    List<Bluetooth> findByConnexion(Boolean connexion);
}