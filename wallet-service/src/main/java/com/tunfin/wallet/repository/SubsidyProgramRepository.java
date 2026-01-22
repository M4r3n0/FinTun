package com.tunfin.wallet.repository;

import com.tunfin.wallet.model.SubsidyProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubsidyProgramRepository extends JpaRepository<SubsidyProgram, UUID> {
    List<SubsidyProgram> findByIsActiveTrue();
}
