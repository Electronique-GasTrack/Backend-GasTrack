package com.example.GasTrack.controllers;

import com.example.GasTrack.repositories.BouteilleRepository;
import com.example.GasTrack.repositories.MesureRepository;
import com.example.GasTrack.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final MesureRepository mesureRepository;
    private final BouteilleRepository bouteilleRepository;
    private final UserRepository userRepository;

    /**
     * Endpoint pour supprimer toutes les données de la base de données
     * ATTENTION: Cette action est irréversible!
     *
     * Pour plus de sécurité, vous pouvez ajouter une clé API ou un mot de passe
     */
    @DeleteMapping("/reset-database")
    @Transactional
    public ResponseEntity<Map<String, Object>> resetDatabase(
            @RequestParam(required = false) String confirmationKey) {

        // Sécurité: vérifier une clé de confirmation
        // Vous pouvez la définir dans application.properties
        // Pour l'instant, on demande "CONFIRM_RESET" comme paramètre
        if (!"CONFIRM_RESET".equals(confirmationKey)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Clé de confirmation invalide");
            error.put("message", "Utilisez ?confirmationKey=CONFIRM_RESET pour confirmer");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            // Ordre important: supprimer d'abord les tables avec clés étrangères
            long mesuresCount = mesureRepository.count();
            long bouteillesCount = bouteilleRepository.count();
            long usersCount = userRepository.count();

            // Suppression dans l'ordre
            mesureRepository.deleteAll();
            bouteilleRepository.deleteAll();
            userRepository.deleteAll();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Base de données réinitialisée avec succès");
            response.put("deletedRecords", Map.of(
                    "mesures", mesuresCount,
                    "bouteilles", bouteillesCount,
                    "users", usersCount
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Erreur lors de la réinitialisation");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Endpoint pour vérifier le nombre d'enregistrements dans chaque table
     */
    @GetMapping("/database-stats")
    public ResponseEntity<Map<String, Object>> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("mesures", mesureRepository.count());
        stats.put("bouteilles", bouteilleRepository.count());
        stats.put("users", userRepository.count());
        return ResponseEntity.ok(stats);
    }

    /**
     * Endpoint de santé pour vérifier que l'API fonctionne
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Admin API is running");
        return ResponseEntity.ok(response);
    }
}