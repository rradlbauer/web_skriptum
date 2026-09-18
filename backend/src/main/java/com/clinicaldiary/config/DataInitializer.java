package com.clinicaldiary.config;

import com.clinicaldiary.entity.*;
import com.clinicaldiary.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final HealthRecordRepository recordRepo;
    private final Icd10CodeRepository icd10Repo;
    private final RecordConfirmationRepository confirmationRepo;
    private final RecordIcd10Repository recordIcd10Repo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PatientRepository patientRepo, DoctorRepository doctorRepo,
                           HealthRecordRepository recordRepo,
                           Icd10CodeRepository icd10Repo, RecordConfirmationRepository confirmationRepo,
                           RecordIcd10Repository recordIcd10Repo, PasswordEncoder passwordEncoder) {
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.recordRepo = recordRepo;
        this.icd10Repo = icd10Repo;
        this.confirmationRepo = confirmationRepo;
        this.recordIcd10Repo = recordIcd10Repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create patients
        Patient patient1 = new Patient();
        patient1.setEmail("anna.schmidt@email.at");
        patient1.setPassword(passwordEncoder.encode("password123"));
        patient1.setSsn("1234567890");
        patient1.setFirstName("Anna");
        patient1.setLastName("Schmidt");
        patient1.setDateOfBirth(LocalDate.of(1990, 5, 15));
        patientRepo.save(patient1);

        Patient patient2 = new Patient();
        patient2.setEmail("max.muster@email.at");
        patient2.setPassword(passwordEncoder.encode("password123"));
        patient2.setSsn("0987654321");
        patient2.setFirstName("Max");
        patient2.setLastName("Muster");
        patient2.setDateOfBirth(LocalDate.of(1985, 8, 22));
        patientRepo.save(patient2);

        Patient patient3 = new Patient();
        patient3.setEmail("maria.weber@email.at");
        patient3.setPassword(passwordEncoder.encode("password123"));
        patient3.setSsn("1122334455");
        patient3.setFirstName("Maria");
        patient3.setLastName("Weber");
        patient3.setDateOfBirth(LocalDate.of(1978, 12, 3));
        patientRepo.save(patient3);

        // Create doctors
        Doctor doctor1 = new Doctor();
        doctor1.setEmail("dr.tandler@klinik.at");
        doctor1.setPassword(passwordEncoder.encode("doctor123"));
        doctor1.setMedicalLicenseNumber("L-12345");
        doctor1.setFirstName("Thomas");
        doctor1.setLastName("Tandler");
        doctor1.setSpecialization("General Practice");
        doctor1.setActive(true);
        doctorRepo.save(doctor1);

        Doctor doctor2 = new Doctor();
        doctor2.setEmail("dr.müller@klinik.at");
        doctor2.setPassword(passwordEncoder.encode("doctor123"));
        doctor2.setMedicalLicenseNumber("L-67890");
        doctor2.setFirstName("Sabine");
        doctor2.setLastName("Müller");
        doctor2.setSpecialization("Cardiology");
        doctor2.setActive(true);
        doctorRepo.save(doctor2);

        Doctor doctor3 = new Doctor();
        doctor3.setEmail("dr.bauer@klinik.at");
        doctor3.setPassword(passwordEncoder.encode("doctor123"));
        doctor3.setMedicalLicenseNumber("L-11111");
        doctor3.setFirstName("Michael");
        doctor3.setLastName("Bauer");
        doctor3.setSpecialization("Dermatology");
        doctor3.setActive(true);
        doctorRepo.save(doctor3);

        // Assign patients to doctors via ManyToMany
        doctor1.getPatients().add(patient1);
        doctor1.getPatients().add(patient2);
        doctor2.getPatients().add(patient1);
        doctorRepo.save(doctor1);
        doctorRepo.save(doctor2);

        // Create health records for patient1
        IllnessRecord record1 = new IllnessRecord();
        record1.setPatient(patient1);
        record1.setTitle("Common Cold");
        record1.setDescription("Runny nose, sore throat, mild fever");
        record1.setDateFrom(LocalDate.of(2025, 6, 1));
        record1.setDateTo(LocalDate.of(2025, 6, 10));
        record1.setSeverity("Mild");
        record1.setNotes("Over-the-counter medication helped");
        record1.setCreatedAt(LocalDateTime.of(2025, 6, 1, 8, 30));
        record1.setUpdatedAt(LocalDateTime.of(2025, 6, 10, 14, 0));
        recordRepo.save(record1);

        VaccinationRecord record2 = new VaccinationRecord();
        record2.setPatient(patient1);
        record2.setTitle("COVID-19 Booster");
        record2.setVaccineName("Comirnaty");
        record2.setDoseNumber(4);
        record2.setBatchNumber("BATCH-2025-A");
        record2.setInstitution("Vienna General Hospital");
        record2.setDateFrom(LocalDate.of(2025, 3, 15));
        record2.setCreatedAt(LocalDateTime.of(2025, 3, 15, 9, 0));
        record2.setUpdatedAt(LocalDateTime.of(2025, 3, 15, 9, 0));
        recordRepo.save(record2);

        MedicationRecord record3 = new MedicationRecord();
        record3.setPatient(patient1);
        record3.setTitle("Ibuprofen");
        record3.setDescription("For occasional headaches");
        record3.setMedicationName("Ibuprofen");
        record3.setDosage("400mg");
        record3.setFrequency("As needed");
        record3.setPrescribingDoctor("Dr. Thomas Tandler");
        record3.setDateFrom(LocalDate.of(2025, 1, 1));
        record3.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        record3.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        recordRepo.save(record3);

        IllnessRecord record4 = new IllnessRecord();
        record4.setPatient(patient1);
        record4.setTitle("Back Pain");
        record4.setDescription("Lower back pain after heavy lifting");
        record4.setDateFrom(LocalDate.of(2025, 4, 20));
        record4.setSeverity("Moderate");
        record4.setNotes("Physical therapy recommended");
        record4.setCreatedAt(LocalDateTime.of(2025, 4, 20, 11, 0));
        record4.setUpdatedAt(LocalDateTime.of(2025, 4, 20, 11, 0));
        recordRepo.save(record4);

        // Records for patient2
        IllnessRecord record5 = new IllnessRecord();
        record5.setPatient(patient2);
        record5.setTitle("Seasonal Allergy");
        record5.setDescription("Pollen allergy, sneezing, itchy eyes");
        record5.setDateFrom(LocalDate.of(2025, 4, 1));
        record5.setDateTo(LocalDate.of(2025, 6, 30));
        record5.setSeverity("Moderate");
        record5.setNotes("Antihistamines prescribed");
        record5.setCreatedAt(LocalDateTime.of(2025, 4, 1, 8, 0));
        record5.setUpdatedAt(LocalDateTime.of(2025, 4, 1, 8, 0));
        recordRepo.save(record5);

        // Confirm record1 by doctor1
        RecordConfirmation conf = new RecordConfirmation();
        conf.setRecord(record1);
        conf.setDoctor(doctor1);
        conf.setComment("Confirmed. Recovery looks good.");
        conf.setConfirmedAt(LocalDateTime.of(2025, 6, 12, 10, 30));
        confirmationRepo.save(conf);

        // ICD-10 codes
        Icd10Code j00 = new Icd10Code();
        j00.setCode("J00");
        j00.setDescription("Acute nasopharyngitis [common cold]");
        icd10Repo.save(j00);

        Icd10Code j06 = new Icd10Code();
        j06.setCode("J06");
        j06.setDescription("Acute upper respiratory infections");
        icd10Repo.save(j06);

        Icd10Code m54 = new Icd10Code();
        m54.setCode("M54");
        m54.setDescription("Dorsalgia [back pain]");
        icd10Repo.save(m54);

        Icd10Code m545 = new Icd10Code();
        m545.setCode("M54.5");
        m545.setDescription("Low back pain");
        icd10Repo.save(m545);

        Icd10Code j30 = new Icd10Code();
        j30.setCode("J30");
        j30.setDescription("Vasomotor and allergic rhinitis");
        icd10Repo.save(j30);

        Icd10Code z23 = new Icd10Code();
        z23.setCode("Z23");
        z23.setDescription("Encounter for immunization");
        icd10Repo.save(z23);

        Icd10Code k08 = new Icd10Code();
        k08.setCode("K08");
        k08.setDescription("Other disorders of tooth and supporting structures");
        icd10Repo.save(k08);

        Icd10Code e11 = new Icd10Code();
        e11.setCode("E11");
        e11.setDescription("Type 2 diabetes mellitus");
        icd10Repo.save(e11);

        Icd10Code i10 = new Icd10Code();
        i10.setCode("I10");
        i10.setDescription("Essential (primary) hypertension");
        icd10Repo.save(i10);

        Icd10Code j45 = new Icd10Code();
        j45.setCode("J45");
        j45.setDescription("Asthma");
        icd10Repo.save(j45);

        // Assign ICD-10 to confirmed record
        RecordIcd10 ri = new RecordIcd10();
        ri.setRecord(record1);
        ri.setIcd10Code(j00);
        ri.setDoctor(doctor1);
        ri.setAssignedAt(LocalDateTime.of(2025, 6, 12, 10, 30));
        recordIcd10Repo.save(ri);
    }
}
