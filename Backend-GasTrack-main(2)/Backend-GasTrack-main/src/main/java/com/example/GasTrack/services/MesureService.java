package com.example.GasTrack.services;

import com.example.GasTrack.models.Mesure;
import com.example.GasTrack.repositories.MesureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MesureService {
    private final MesureRepository mesureRepository;

    public Mesure createMesure(Mesure mesure) {
        mesure.setDateMesure(LocalDateTime.now());
        return mesureRepository.save(mesure);
    }

    public List<Mesure> getHistoriqueMesures(Integer bouteilleId) {
        return mesureRepository.findByBouteilleIdBouteilleOrderByDateMesureDesc(bouteilleId);
    }

    public List<Mesure> getMesuresByPeriode(Integer bouteilleId, LocalDateTime debut, LocalDateTime fin) {
        return mesureRepository.findByBouteilleIdBouteilleAndDateMesureBetween(bouteilleId, debut, fin);
    }

    public Map<String, Object> getStatistiques(Integer bouteilleId, String periode) {
        LocalDateTime fin = LocalDateTime.now();
        LocalDateTime debut = switch(periode) {
            case "semaine" -> fin.minusWeeks(1);
            case "mois" -> fin.minusMonths(1);
            default -> fin.minusDays(1);
        };

        List<Mesure> mesures = getMesuresByPeriode(bouteilleId, debut, fin);
        Map<String, Object> stats = new HashMap<>();

        if (!mesures.isEmpty()) {
            float consomme = mesures.get(0).getGazPourcentage() - mesures.get(mesures.size()-1).getGazPourcentage();
            stats.put("consomme", consomme);
            stats.put("moyenne", consomme / mesures.size());
            stats.put("niveauActuel", mesures.get(mesures.size()-1).getGazPourcentage());
            stats.put("picsDetectes", countPics(mesures));
        }
        return stats;
    }

    private int countPics(List<Mesure> mesures) {
        int pics = 0;
        for(int i=1; i<mesures.size()-1; i++) {
            if(mesures.get(i).getGazPourcentage() - mesures.get(i+1).getGazPourcentage() > 10) pics++;
        }
        return pics;
    }
}