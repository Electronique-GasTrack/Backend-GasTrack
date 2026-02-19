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
        LocalDateTime dateDebut = LocalDateTime.parse(debut);
        LocalDateTime dateFin = LocalDateTime.parse(fin);
        return ResponseEntity.ok(mesureService.getMesuresByPeriode(bouteilleId, dateDebut, dateFin));
    }
}
