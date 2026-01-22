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

    @PostMapping("/predict")
    public ResponseEntity<PredictionResponse> predictDepletion(@RequestBody PredictionRequest request) {
        try {
            PredictionResult result = predictionService.predictGasDepletion(
                    request.getMeasurements(),
                    request.getCurrentLevel()
            );

            return ResponseEntity.ok(new PredictionResponse(
                    result.getDepletionDate() != null ? result.getDepletionDate().toString() : null,
                    result.getDaysRemaining(),
                    result.getHoursRemaining(),
                    result.getConfidence(),
                    result.getDetails(),
                    "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new PredictionResponse(null, 0, 0, 0.0, e.getMessage(), "error")
            );
        }
    }

    @GetMapping("/predict/{bouteilleId}")
    public ResponseEntity<PredictionResponse> predictForBouteille(@PathVariable Integer bouteilleId) {
        try {
            PredictionResult result = predictionService.predictForBouteille(bouteilleId);

            return ResponseEntity.ok(new PredictionResponse(
                    result.getDepletionDate() != null ? result.getDepletionDate().toString() : null,
                    result.getDaysRemaining(),
                    result.getHoursRemaining(),
                    result.getConfidence(),
                    result.getDetails(),
                    "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new PredictionResponse(null, 0, 0, 0.0, e.getMessage(), "error")
            );
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service de prédiction opérationnel");
    }

    // DTOs
    public static class PredictionRequest {
        private List<GasMeasurement> measurements;
        private double currentLevel;

        public List<GasMeasurement> getMeasurements() { return measurements; }
        public void setMeasurements(List<GasMeasurement> measurements) { this.measurements = measurements; }
        public double getCurrentLevel() { return currentLevel; }
        public void setCurrentLevel(double currentLevel) { this.currentLevel = currentLevel; }
    }

    public static class PredictionResponse {
        private String depletionDate;
        private long daysRemaining;
        private long hoursRemaining;
        private double confidence;
        private String details;
        private String status;

        public PredictionResponse(String depletionDate, long daysRemaining, long hoursRemaining,
                                  double confidence, String details, String status) {
            this.depletionDate = depletionDate;
            this.daysRemaining = daysRemaining;
            this.hoursRemaining = hoursRemaining;
            this.confidence = confidence;
            this.details = details;
            this.status = status;
        }

        public String getDepletionDate() { return depletionDate; }
        public long getDaysRemaining() { return daysRemaining; }
        public long getHoursRemaining() { return hoursRemaining; }
        public double getConfidence() { return confidence; }
        public String getDetails() { return details; }
        public String getStatus() { return status; }
    }
}