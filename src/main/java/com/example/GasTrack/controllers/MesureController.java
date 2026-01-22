package com.example.GasTrack.controllers;

import com.example.GasTrack.models.Mesure;
import com.example.GasTrack.services.MesureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mesures")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MesureController {
    private final MesureService mesureService;

    @PostMapping
    public ResponseEntity<Mesure> createMesure(@RequestBody com.example.GasTrack.dto.MesureRequest request) {
        return new ResponseEntity<>(mesureService.createMesure(request), org.springframework.http.HttpStatus.CREATED);
    }

    @GetMapping("/bouteille/{bouteilleId}")
    public ResponseEntity<List<Mesure>> getHistorique(@PathVariable Integer bouteilleId) {
        return ResponseEntity.ok(mesureService.getHistoriqueMesures(bouteilleId));
    }

    @GetMapping("/bouteille/{bouteilleId}/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @PathVariable Integer bouteilleId,
            @RequestParam(defaultValue = "jour") String periode) {
        return ResponseEntity.ok(mesureService.getStatistiques(bouteilleId, periode));
    }

    @GetMapping("/bouteille/{bouteilleId}/periode")
    public ResponseEntity<List<Mesure>> getMesuresByPeriode(
            @PathVariable Integer bouteilleId,
            @RequestParam String debut,
            @RequestParam String fin) {
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime dateDebut;
        LocalDateTime dateFin;
        
        try {
            dateDebut = LocalDateTime.parse(debut, formatter);
        } catch (java.time.format.DateTimeParseException e) {
             // Fallback to date only
             dateDebut = LocalDate.parse(debut, java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")).atStartOfDay();
        }
        
        try {
            dateFin = LocalDateTime.parse(fin, formatter);
        } catch (java.time.format.DateTimeParseException e) {
             // Fallback to date only (end of day)
             dateFin = LocalDate.parse(fin, java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")).atTime(23, 59, 59);
        }

        return ResponseEntity.ok(mesureService.getMesuresByPeriode(bouteilleId, dateDebut, dateFin));
    }
}
