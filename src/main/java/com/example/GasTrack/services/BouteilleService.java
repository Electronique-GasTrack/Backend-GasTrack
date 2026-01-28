package com.example.GasTrack.services;

import com.example.GasTrack.models.Bouteille;
import com.example.GasTrack.models.Mesure;
import com.example.GasTrack.repositories.BouteilleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BouteilleService {
    private final BouteilleRepository bouteilleRepository;
    private final GasPredictionService gasPredictionService;

    private final com.example.GasTrack.repositories.UserRepository userRepository;
    private final com.example.GasTrack.repositories.BluetoothRepository bluetoothRepository;

    public Bouteille createBouteille(com.example.GasTrack.dto.BouteilleRequest request) {
        Bouteille bouteille = new Bouteille();
        bouteille.setPoids(request.getPoids());
        bouteille.setFrequenceCuisine(request.getFrequenceCuisine());
        bouteille.setNiveauInitial(request.getNiveauInitial() != null ? request.getNiveauInitial() : 100f);

        // Link User
        if (request.getUserId() != null) {
            com.example.GasTrack.models.User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
            bouteille.setUser(user);
        } else {
             throw new RuntimeException("User ID is required to create a Bouteille");
        }

        // Link Bluetooth (Optional)
        if (request.getBluetoothId() != null) {
            com.example.GasTrack.models.Bluetooth bluetooth = bluetoothRepository.findById(request.getBluetoothId())
                    .orElseThrow(() -> new RuntimeException("Bluetooth not found with ID: " + request.getBluetoothId()));
            bouteille.setBluetooth(bluetooth);
        }

        return bouteilleRepository.save(bouteille);
    }

    public Bouteille createBouteille(Bouteille bouteille) {
        return bouteilleRepository.save(bouteille);
    }

    public Bouteille getBouteilleById(Integer id) {
        return bouteilleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bouteille not found"));
    }

    public List<Bouteille> getBouteillesByUser(Integer userId) {
        return bouteilleRepository.findByUserIdUser(userId);
    }

    public Bouteille mettreAJourNiveau(Integer bouteilleId, Mesure mesure) {
        Bouteille bouteille = getBouteilleById(bouteilleId);
        mesure.setBouteille(bouteille);
        bouteille.getMesures().add(mesure);
        bouteille.declencherAlerte();
        return bouteilleRepository.save(bouteille);
    }

    public Integer estimerJoursRestants(Integer bouteilleId) {
        // Use the sophisticated service for estimation too if possible, or keep simple fallback
        // For consistency, let's try to align them, but estimerJoursRestants returns Integer
        GasPredictionService.FullPredictionResponse fullResult = gasPredictionService.predictWithHistory(bouteilleId);
        return (int) fullResult.getPrediction().getDaysRemaining();
    }

    public Map<String, Object> getDashboardData(Integer bouteilleId) {
        Bouteille bouteille = getBouteilleById(bouteilleId);
        List<Mesure> mesures = bouteille.getMesures();
        Map<String, Object> dashboard = new HashMap<>();

        if (mesures != null && !mesures.isEmpty()) {
            Mesure derniere = mesures.get(mesures.size() - 1);
            dashboard.put("niveauGaz", derniere.getGazPourcentage());
            dashboard.put("batterie", derniere.getBatteriePourcentage());
            dashboard.put("joursRestants", estimerJoursRestants(bouteilleId));
            dashboard.put("consommation", getConsommationStatus(mesures));
            dashboard.put("statut", getStatut(derniere.getGazPourcentage()));
            dashboard.put("nomAppareil", bouteille.getBluetooth().getNomBluetooth());
            dashboard.put("connexion", bouteille.getBluetooth().getConnexion());
        }
        return dashboard;
    }

    public Map<String, Object> getPredictions(Integer bouteilleId) {
        Bouteille bouteille = getBouteilleById(bouteilleId);
        GasPredictionService.FullPredictionResponse fullResult = gasPredictionService.predictWithHistory(bouteilleId);
        GasPredictionService.PredictionResult result = fullResult.getPrediction();
        
        Map<String, Object> predictions = new HashMap<>();
        predictions.put("joursRestants", result.getDaysRemaining());
        predictions.put("vitesseConsommation", getVitesse(bouteille));
        
        // Use prediction date or fallback to now if null
        String dateRecharge = result.getDepletionDate() != null ? 
                result.getDepletionDate().toLocalDate().toString() : 
                LocalDate.now().toString();
                
        predictions.put("dateRecharge", dateRecharge);
        predictions.put("confiance", result.getConfidence());
        predictions.put("details", result.getDetails());
        predictions.put("conseils", List.of("Couvrez vos casseroles", "Utilisez ustensiles adaptés", "Éteignez avant fin cuisson"));
        return predictions;
    }

    private String getConsommationStatus(List<Mesure> mesures) {
        if (mesures.size() < 2) return "Normale";
        float vitesse = (mesures.get(0).getGazPourcentage() - mesures.get(mesures.size()-1).getGazPourcentage()) / (float) mesures.size();
        return vitesse < 3 ? "Faible" : vitesse < 6 ? "Normale" : "Élevée";
    }

    private String getStatut(Integer niveau) {
        return niveau > 60 ? "Niveau optimal" : niveau > 20 ? "Niveau modéré" : "Niveau critique";
    }

    private String getVitesse(Bouteille b) {
        return getConsommationStatus(b.getMesures());
    }
}