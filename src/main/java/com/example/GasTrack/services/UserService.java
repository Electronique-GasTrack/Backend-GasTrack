package com.example.GasTrack.services;

import com.example.GasTrack.models.Bluetooth;
import com.example.GasTrack.models.Bouteille;
import com.example.GasTrack.models.User;
import com.example.GasTrack.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BluetoothService bluetoothService;
    private final BouteilleService bouteilleService;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public Map<String, Object> completeOnboarding(Map<String, Object> data) {
        // Créer User
        User user = new User();
        user.setNombreOccupant((Integer) data.get("nombreOccupant"));
        user.setUtilisation(data.get("utilisation").toString());
        user = userRepository.save(user);

        // Récupérer/Créer Bluetooth
        String macAddress = data.get("macAddress").toString();
        Bluetooth bluetooth = bluetoothService.getBluetoothByMac(macAddress);
        bluetooth.setConnexion(true);
        bluetoothService.updateBluetooth(bluetooth.getIdBluetooth(), bluetooth);

        // Créer Bouteille
        Bouteille bouteille = new Bouteille();
        bouteille.setPoids(Float.parseFloat(data.get("poids").toString()));
        bouteille.setFrequenceCuisine(data.get("frequenceCuisine").toString());
        bouteille.setNiveauInitial(100.0f);
        bouteille.setUser(user);
        bouteille.setBluetooth(bluetooth);
        bouteille = bouteilleService.createBouteille(bouteille);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getIdUser());
        response.put("bouteilleId", bouteille.getIdBouteille());
        response.put("message", "Configuration terminée");
        return response;
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getUsersByUtilisation(String utilisation) {
        return userRepository.findByUtilisation(utilisation);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Integer id, User user) {
        User existing = getUserById(id);
        existing.setNombreOccupant(user.getNombreOccupant());
        existing.setUtilisation(user.getUtilisation());
        return userRepository.save(existing);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}