package com.clinicaldiary.repository;

import com.clinicaldiary.entity.DoctorPatient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface DoctorPatientRepository extends JpaRepository<DoctorPatient, Long> {

    List<DoctorPatient> findByPatientId(Long patientId);
    List<DoctorPatient> findByDoctorId(Long doctorId);

    @Query("SELECT dp.patientId FROM DoctorPatient dp WHERE dp.doctorId = :doctorId")
    List<Long> findPatientIdsByDoctorId(Long doctorId);

    @Query("SELECT dp.doctorId FROM DoctorPatient dp WHERE dp.patientId = :patientId")
    List<Long> findDoctorIdsByPatientId(Long patientId);

    Optional<DoctorPatient> findByDoctorIdAndPatientId(Long doctorId, Long patientId);
    boolean existsByDoctorIdAndPatientId(Long doctorId, Long patientId);
}
