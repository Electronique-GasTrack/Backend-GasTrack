package com.example.GasTrack.controllers;

import com.example.GasTrack.services.GasPredictionService;
import com.example.GasTrack.services.GasPredictionService.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gas")
@CrossOrigin(origins = "*")
public class GasPredictionController {

    @Autowired
    private GasPredictionService predictionService;

    @GetMapping("/predict")
    public ResponseEntity<PredictionResponse> predictDefault() {
        try {
            GasPredictionService.FullPredictionResponse result = predictionService.predictDefault();
            return ResponseEntity.ok(mapToResponse(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new PredictionResponse(null, 0.0, null, 0, 0, 0.0, e.getMessage(), "error")
            );
        }
    }


    @GetMapping("/predict/{bouteilleId}")
    public ResponseEntity<PredictionResponse> predictForBouteille(@PathVariable Integer bouteilleId) {
        try {
            GasPredictionService.FullPredictionResponse result = predictionService.predictWithHistory(bouteilleId);
            return ResponseEntity.ok(mapToResponse(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new PredictionResponse(null, 0.0, null, 0, 0, 0.0, e.getMessage(), "error")
            );
        }
    }

    private PredictionResponse mapToResponse(GasPredictionService.FullPredictionResponse result) {
        PredictionResult pred = result.getPrediction();
        return new PredictionResponse(
                result.getMeasurements(),
                result.getCurrentLevel(),
                pred.getDepletionDate() != null ? pred.getDepletionDate().toString() : null,
                pred.getDaysRemaining(),
                pred.getHoursRemaining(),
                pred.getConfidence(),
                pred.getDetails(),
                "success"
        );
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service de prédiction opérationnel");
    }


    public static class PredictionResponse {
        private List<GasMeasurement> measurements;
        private double currentLevel;
        private String depletionDate;
        private long daysRemaining;
        private long hoursRemaining;
        private double confidence;
        private String details;
        private String status;

        public PredictionResponse(List<GasMeasurement> measurements, double currentLevel, String depletionDate, 
                                  long daysRemaining, long hoursRemaining, double confidence, 
                                  String details, String status) {
            this.measurements = measurements;
            this.currentLevel = currentLevel;
            this.depletionDate = depletionDate;
            this.daysRemaining = daysRemaining;
            this.hoursRemaining = hoursRemaining;
            this.confidence = confidence;
            this.details = details;
            this.status = status;
        }

        public List<GasMeasurement> getMeasurements() { return measurements; }
        public double getCurrentLevel() { return currentLevel; }
        public String getDepletionDate() { return depletionDate; }
        public long getDaysRemaining() { return daysRemaining; }
        public long getHoursRemaining() { return hoursRemaining; }
        public double getConfidence() { return confidence; }
        public String getDetails() { return details; }
        public String getStatus() { return status; }
    }
}