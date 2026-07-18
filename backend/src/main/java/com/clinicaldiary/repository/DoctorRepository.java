package com.clinicaldiary.repository;

import com.clinicaldiary.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByMedicalLicenseNumber(String licenseNumber);
    List<Doctor> findByActiveTrue();

    @Query("SELECT d FROM Doctor d WHERE d.active = true AND " +
           "(LOWER(d.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.medicalLicenseNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Doctor> search(String query);
}
