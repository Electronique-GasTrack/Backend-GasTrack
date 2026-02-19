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
        Bouteille bouteille = getBouteilleById(bouteilleId);
        return bouteille.estimerJoursRestants();
    }

    public Map<String, Object> getDashboardData(Integer bouteilleId) {
        Bouteille bouteille = getBouteilleById(bouteilleId);
        List<Mesure> mesures = bouteille.getMesures();
        Map<String, Object> dashboard = new HashMap<>();

        if (mesures != null && !mesures.isEmpty()) {
            Mesure derniere = mesures.get(mesures.size() - 1);
            dashboard.put("niveauGaz", derniere.getGazPourcentage());
            dashboard.put("batterie", derniere.getBatteriePourcentage());
            dashboard.put("joursRestants", bouteille.estimerJoursRestants());
            dashboard.put("consommation", getConsommationStatus(mesures));
            dashboard.put("statut", getStatut(derniere.getGazPourcentage()));
            // ✅ LIGNES BLUETOOTH SUPPRIMÉES !
        }
        return dashboard;
    }

    public Map<String, Object> getPredictions(Integer bouteilleId) {
        Bouteille bouteille = getBouteilleById(bouteilleId);
        Map<String, Object> predictions = new HashMap<>();
        predictions.put("joursRestants", bouteille.estimerJoursRestants());
        predictions.put("vitesseConsommation", getVitesse(bouteille));
        predictions.put("dateRecharge", LocalDate.now().plusDays(bouteille.estimerJoursRestants()).toString());
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