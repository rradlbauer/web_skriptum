package com.clinicaldiary.repository;

import com.clinicaldiary.entity.Icd10Code;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface Icd10CodeRepository extends JpaRepository<Icd10Code, Long> {

    @Query("SELECT i FROM Icd10Code i WHERE LOWER(i.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Icd10Code> search(String query);
}
