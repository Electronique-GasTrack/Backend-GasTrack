package com.example.GasTrack.repositories;

import com.example.GasTrack.models.Mesure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MesureRepository extends JpaRepository<Mesure, Integer> {
    List<Mesure> findByBouteilleIdBouteilleOrderByDateMesureDesc(Integer bouteilleId);
    List<Mesure> findByBouteilleIdBouteilleAndDateMesureBetween(Integer bouteilleId, LocalDateTime debut, LocalDateTime fin);
}