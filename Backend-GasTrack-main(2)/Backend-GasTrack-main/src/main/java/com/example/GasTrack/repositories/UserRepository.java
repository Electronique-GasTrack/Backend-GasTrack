package com.example.GasTrack.repositories;

import com.example.GasTrack.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findByUtilisation(String utilisation);
}