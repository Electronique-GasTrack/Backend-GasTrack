package com.example.GasTrack.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Bouteille {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idBouteille;

    private Float poids;
    private String frequenceCuisine;
    private Float niveauInitial;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "bouteille", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Mesure> mesures;

    @OneToOne
    @JoinColumn(name = "bluetooth_id")
    @JsonIgnore
    private Bluetooth bluetooth;

    public Integer estimerJoursRestants() {
        if (mesures == null || mesures.isEmpty()) return 0;

        Mesure derniere = mesures.get(mesures.size() - 1);
        if (derniere.getGazPourcentage() <= 0) return 0;

        // Calcul simple basé sur la consommation moyenne
        long joursDepuisDebut = mesures.size();
        float consommationMoyenne = (niveauInitial - derniere.getGazPourcentage()) / joursDepuisDebut;

        if (consommationMoyenne <= 0) return 999;
        return (int) (derniere.getGazPourcentage() / consommationMoyenne);
    }

    public void declencherAlerte() {
        if (mesures != null && !mesures.isEmpty()) {
            Mesure derniere = mesures.get(mesures.size() - 1);
            if (derniere.getGazPourcentage() < 20) {
                // Logique d'alerte
                System.out.println("ALERTE: Niveau de gaz critique (" + derniere.getGazPourcentage() + "%)");
            }
        }
    }
}