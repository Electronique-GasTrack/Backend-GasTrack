package com.example.GasTrack.controllers;

import com.example.GasTrack.models.Bouteille;
import com.example.GasTrack.models.Mesure;
import com.example.GasTrack.services.BouteilleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bouteilles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BouteilleController {
    private final BouteilleService bouteilleService;

    @PostMapping
    public ResponseEntity<Bouteille> createBouteille(@RequestBody Bouteille bouteille) {
        return new ResponseEntity<>(bouteilleService.createBouteille(bouteille), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bouteille> getBouteille(@PathVariable Integer id) {
        return ResponseEntity.ok(bouteilleService.getBouteilleById(id));
    }

    @GetMapping("/{id}/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(@PathVariable Integer id) {
        return ResponseEntity.ok(bouteilleService.getDashboardData(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Bouteille>> getBouteillesByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(bouteilleService.getBouteillesByUser(userId));
    }

    @PostMapping("/{id}/mesures")
    public ResponseEntity<Bouteille> addMesure(@PathVariable Integer id, @RequestBody Mesure mesure) {
        return ResponseEntity.ok(bouteilleService.mettreAJourNiveau(id, mesure));
    }

    @GetMapping("/{id}/jours-restants")
    public ResponseEntity<Integer> getJoursRestants(@PathVariable Integer id) {
        return ResponseEntity.ok(bouteilleService.estimerJoursRestants(id));
    }

    @GetMapping("/{id}/predictions")
    public ResponseEntity<Map<String, Object>> getPredictions(@PathVariable Integer id) {
        return ResponseEntity.ok(bouteilleService.getPredictions(id));
    }
}