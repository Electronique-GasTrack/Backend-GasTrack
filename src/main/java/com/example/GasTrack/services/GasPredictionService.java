package com.example.GasTrack.services;

import com.example.GasTrack.models.Mesure;
import com.example.GasTrack.repositories.BouteilleRepository;
import com.example.GasTrack.repositories.MesureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GasPredictionService {

    private final MesureRepository mesureRepository;
    private final BouteilleRepository bouteilleRepository;

    /**
     * Prédit l'épuisement pour la première bouteille trouvée (cas par défaut sans paramètre)
     */
    public FullPredictionResponse predictDefault() {
        return bouteilleRepository.findAll().stream()
                .findFirst()
                .map(b -> predictWithHistory(b.getIdBouteille()))
                .orElseThrow(() -> new RuntimeException("Aucune bouteille trouvée dans le système"));
    }

    /**
     * Retourne les mesures et la prédiction pour une bouteille
     */
    public FullPredictionResponse predictWithHistory(Integer bouteilleId) {
        List<Mesure> mesures = mesureRepository.findByBouteilleIdBouteilleOrderByDateMesureDesc(bouteilleId);
        
        if (mesures.isEmpty()) {
            return new FullPredictionResponse(new ArrayList<>(), 0.0, new PredictionResult(null, 0.0, "Aucune mesure disponible"));
        }

        double currentLevel = mesures.get(0).getGazPourcentage();
        
        List<GasMeasurement> history = mesures.stream()
                .limit(20) // On limite aux 20 dernières mesures pour le front
                .map(m -> new GasMeasurement(m.getDateMesure(), m.getGazPourcentage()))
                .collect(Collectors.toList());

        PredictionResult prediction = predictGasDepletion(
                mesures.stream().map(m -> new GasMeasurement(m.getDateMesure(), m.getGazPourcentage())).collect(Collectors.toList()), 
                currentLevel
        );

        return new FullPredictionResponse(history, currentLevel, prediction);
    }

    /**
     * Prédit la date d'épuisement du gaz basé sur l'historique de consommation
     * Utilise plusieurs algorithmes pour plus de précision
     */
    public PredictionResult predictGasDepletion(List<GasMeasurement> measurements, double currentLevel) {
        if (measurements == null || measurements.isEmpty() || currentLevel <= 0) {
            return new PredictionResult(null, 0.0, "Données insuffisantes");
        }

        // Trier les mesures par date (ascendant pour les calculs)
        List<GasMeasurement> sortedMeasures = measurements.stream()
                .sorted(Comparator.comparing(GasMeasurement::getTimestamp))
                .collect(Collectors.toList());

        // Algorithme 1: Régression linéaire simple
        LinearRegressionResult linearResult = calculateLinearRegression(sortedMeasures);

        // Algorithme 2: Moyenne mobile pondérée (plus de poids aux mesures récentes)
        double weightedAvgConsumption = calculateWeightedAverageConsumption(sortedMeasures);

        // Algorithme 3: Consommation médiane (résistant aux valeurs aberrantes)
        double medianConsumption = calculateMedianConsumption(sortedMeasures);

        // Combinaison des algorithmes avec pondération
        double finalConsumptionRate = (linearResult.slope * 0.5) +
                (weightedAvgConsumption * 0.3) +
                (medianConsumption * 0.2);

        // Calcul de la durée restante
        if (finalConsumptionRate <= 0.001) { // Tolérance pour éviter division par zéro ou infini
             // Si la consommation est nulle ou on a rechargé, on ne peut pas prédire
             // Ou alors on retourne une valeur très grande
            return new PredictionResult(null, 0.0, "Consommation stable ou remplissage détecté");
        }

        double hoursRemaining = currentLevel / finalConsumptionRate;
        LocalDateTime depletionDate = LocalDateTime.now().plusHours((long) hoursRemaining);

        // Calcul de la confiance basé sur la cohérence des données
        double confidence = calculateConfidence(sortedMeasures, linearResult.rSquared);

        return new PredictionResult(
                depletionDate,
                confidence,
                String.format("Consommation estimée: %.2f%% /heure", finalConsumptionRate)
        );
    }

    /**
     * Régression linéaire pour détecter la tendance de consommation
     */
    private LinearRegressionResult calculateLinearRegression(List<GasMeasurement> measurements) {
        int n = measurements.size();
        if (n < 2) {
            return new LinearRegressionResult(0.0, 0.0);
        }

        LocalDateTime baseTime = measurements.get(0).getTimestamp();

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (GasMeasurement m : measurements) {
            double x = ChronoUnit.HOURS.between(baseTime, m.getTimestamp());
            double y = m.getLevel();

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        
        double denominator = n * sumX2 - sumX * sumX;
        if (Math.abs(denominator) < 0.0001) return new LinearRegressionResult(0.0, 0.0);

        double slope = (n * sumXY - sumX * sumY) / denominator;
        double intercept = (sumY - slope * sumX) / n;

        // Calcul du R² pour évaluer la qualité de la régression
        double meanY = sumY / n;
        double ssTotal = 0, ssResidual = 0;

        for (GasMeasurement m : measurements) {
            double x = ChronoUnit.HOURS.between(baseTime, m.getTimestamp());
            double predicted = slope * x + intercept;
            ssTotal += Math.pow(m.getLevel() - meanY, 2);
            ssResidual += Math.pow(m.getLevel() - predicted, 2);
        }

        double rSquared = (ssTotal == 0) ? 0 : 1 - (ssResidual / ssTotal);

        return new LinearRegressionResult(Math.abs(slope), rSquared);
    }

    /**
     * Moyenne pondérée - donne plus d'importance aux mesures récentes
     */
    private double calculateWeightedAverageConsumption(List<GasMeasurement> measurements) {
        if (measurements.size() < 2) return 0.0;

        double totalWeight = 0;
        double weightedSum = 0;

        for (int i = 1; i < measurements.size(); i++) {
            GasMeasurement current = measurements.get(i);
            GasMeasurement previous = measurements.get(i - 1);

            double hoursDiff = ChronoUnit.HOURS.between(previous.getTimestamp(), current.getTimestamp());
            if (hoursDiff > 0.5) { // Ignorer les mesures trop rapprochées (< 30 min)
                double levelDiff = previous.getLevel() - current.getLevel();
                
                // Si le niveau a augmenté, c'est un remplissage, on ignore pour la consommation moyenne
                if (levelDiff < 0) continue; 
                
                double consumption = levelDiff / hoursDiff;

                // Poids exponentiel: les mesures récentes comptent plus
                double weight = Math.exp(i / (double) measurements.size());

                weightedSum += consumption * weight;
                totalWeight += weight;
            }
        }

        return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
    }

    /**
     * Consommation médiane - robuste aux valeurs aberrantes
     */
    private double calculateMedianConsumption(List<GasMeasurement> measurements) {
        if (measurements.size() < 2) return 0.0;

        List<Double> consumptionRates = new ArrayList<>();

        for (int i = 1; i < measurements.size(); i++) {
            GasMeasurement current = measurements.get(i);
            GasMeasurement previous = measurements.get(i - 1);

            double hoursDiff = ChronoUnit.HOURS.between(previous.getTimestamp(), current.getTimestamp());
            if (hoursDiff > 0.5) {
                double levelDiff = previous.getLevel() - current.getLevel();
                 // Ignorer remplissages
                if (levelDiff < 0) continue;
                
                double consumption = levelDiff / hoursDiff;
                consumptionRates.add(consumption);
            }
        }

        if (consumptionRates.isEmpty()) return 0.0;

        Collections.sort(consumptionRates);
        int middle = consumptionRates.size() / 2;

        if (consumptionRates.size() % 2 == 0) {
            return (consumptionRates.get(middle - 1) + consumptionRates.get(middle)) / 2.0;
        } else {
            return consumptionRates.get(middle);
        }
    }

    /**
     * Calcule un indice de confiance basé sur la cohérence des données
     */
    private double calculateConfidence(List<GasMeasurement> measurements, double rSquared) {
        if (measurements.size() < 5) {
            return 0.4; // Confiance faible pour peu de données
        }

        // Facteurs de confiance
        double dataPointsFactor = Math.min(measurements.size() / 50.0, 1.0); // Max à 50 points
        double rSquaredFactor = Math.max(rSquared, 0.0); // Qualité de la régression
        double timeCoverageFactor = calculateTimeCoverage(measurements);

        // Combinaison pondérée
        double confidence = (dataPointsFactor * 0.3) +
                (rSquaredFactor * 0.5) +
                (timeCoverageFactor * 0.2);

        return Math.min(Math.max(confidence, 0.0), 1.0);
    }

    /**
     * Évalue la couverture temporelle des données
     */
    private double calculateTimeCoverage(List<GasMeasurement> measurements) {
        if (measurements.size() < 2) return 0.0;

        LocalDateTime first = measurements.get(0).getTimestamp();
        LocalDateTime last = measurements.get(measurements.size() - 1).getTimestamp();

        long daysCovered = ChronoUnit.DAYS.between(first, last);

        // Idéalement, on veut au moins 7 jours de données
        return Math.min(daysCovered / 7.0, 1.0);
    }

    // Classes internes
    private static class LinearRegressionResult {
        double slope;
        double rSquared;

        LinearRegressionResult(double slope, double rSquared) {
            this.slope = slope;
            this.rSquared = rSquared;
        }
    }

    // Classes publiques pour l'API
    public static class GasMeasurement {
        private LocalDateTime timestamp;
        private double level; // Pourcentage (0-100)

        public GasMeasurement(LocalDateTime timestamp, double level) {
            this.timestamp = timestamp;
            this.level = level;
        }

        public LocalDateTime getTimestamp() { return timestamp; }
        public double getLevel() { return level; }
    }

    public static class PredictionResult {
        private LocalDateTime depletionDate;
        private double confidence; // 0.0 à 1.0
        private String details;

        public PredictionResult(LocalDateTime depletionDate, double confidence, String details) {
            this.depletionDate = depletionDate;
            this.confidence = confidence;
            this.details = details;
        }

        public LocalDateTime getDepletionDate() { return depletionDate; }
        public double getConfidence() { return confidence; }
        public String getDetails() { return details; }

        public long getDaysRemaining() {
            return depletionDate != null ?
                    ChronoUnit.DAYS.between(LocalDateTime.now(), depletionDate) : 0;
        }

        public long getHoursRemaining() {
            return depletionDate != null ?
                    ChronoUnit.HOURS.between(LocalDateTime.now(), depletionDate) : 0;
        }
    }

    public static class FullPredictionResponse {
        private List<GasMeasurement> measurements;
        private double currentLevel;
        private PredictionResult prediction;

        public FullPredictionResponse(List<GasMeasurement> measurements, double currentLevel, PredictionResult prediction) {
            this.measurements = measurements;
            this.currentLevel = currentLevel;
            this.prediction = prediction;
        }

        public List<GasMeasurement> getMeasurements() { return measurements; }
        public double getCurrentLevel() { return currentLevel; }
        public PredictionResult getPrediction() { return prediction; }
    }
}