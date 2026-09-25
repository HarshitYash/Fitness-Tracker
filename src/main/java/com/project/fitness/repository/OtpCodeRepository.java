package com.project.fitness.repository;

import com.project.fitness.entity.OtpChannel;
import com.project.fitness.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, String> {
    Optional<OtpCode> findTopByDestinationAndChannelAndUsedFalseOrderByExpiresAtDesc(
            String destination, OtpChannel channel);

    void deleteByDestinationAndChannel(String destination, OtpChannel channel);
}
