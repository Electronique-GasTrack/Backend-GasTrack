package com.example.GasTrack.repositories;

import com.example.GasTrack.models.Bouteille;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BouteilleRepository extends JpaRepository<Bouteille, Integer> {
    List<Bouteille> findByUserIdUser(Integer userId);
}