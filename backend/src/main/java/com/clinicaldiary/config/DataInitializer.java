package com.clinicaldiary.config;

import com.clinicaldiary.entity.*;
import com.clinicaldiary.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final HealthRecordRepository recordRepo;
    private final DoctorPatientRepository doctorPatientRepo;
    private final Icd10CodeRepository icd10Repo;
    private final RecordConfirmationRepository confirmationRepo;
    private final RecordIcd10Repository recordIcd10Repo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PatientRepository patientRepo, DoctorRepository doctorRepo,
                           HealthRecordRepository recordRepo, DoctorPatientRepository doctorPatientRepo,
                           Icd10CodeRepository icd10Repo, RecordConfirmationRepository confirmationRepo,
                           RecordIcd10Repository recordIcd10Repo, PasswordEncoder passwordEncoder) {
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.recordRepo = recordRepo;
        this.doctorPatientRepo = doctorPatientRepo;
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
        patient1.setDateOfBirth("1990-05-15");
        patientRepo.save(patient1);

        Patient patient2 = new Patient();
        patient2.setEmail("max.muster@email.at");
        patient2.setPassword(passwordEncoder.encode("password123"));
        patient2.setSsn("0987654321");
        patient2.setFirstName("Max");
        patient2.setLastName("Muster");
        patient2.setDateOfBirth("1985-08-22");
        patientRepo.save(patient2);

        Patient patient3 = new Patient();
        patient3.setEmail("maria.weber@email.at");
        patient3.setPassword(passwordEncoder.encode("password123"));
        patient3.setSsn("1122334455");
        patient3.setFirstName("Maria");
        patient3.setLastName("Weber");
        patient3.setDateOfBirth("1978-12-03");
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

        // Assign patients to doctors
        DoctorPatient dp1 = new DoctorPatient();
        dp1.setDoctorId(doctor1.getId());
        dp1.setPatientId(patient1.getId());
        dp1.setAssignedAt("2025-01-15T10:00:00");
        doctorPatientRepo.save(dp1);

        DoctorPatient dp2 = new DoctorPatient();
        dp2.setDoctorId(doctor1.getId());
        dp2.setPatientId(patient2.getId());
        dp2.setAssignedAt("2025-02-01T10:00:00");
        doctorPatientRepo.save(dp2);

        DoctorPatient dp3 = new DoctorPatient();
        dp3.setDoctorId(doctor2.getId());
        dp3.setPatientId(patient1.getId());
        dp3.setAssignedAt("2025-03-10T10:00:00");
        doctorPatientRepo.save(dp3);

        // Create health records for patient1
        HealthRecord record1 = new HealthRecord();
        record1.setPatientId(patient1.getId());
        record1.setCategory("ILLNESS");
        record1.setTitle("Common Cold");
        record1.setDescription("Runny nose, sore throat, mild fever");
        record1.setDateFrom("2025-06-01");
        record1.setDateTo("2025-06-10");
        record1.setSeverity("Mild");
        record1.setNotes("Over-the-counter medication helped");
        record1.setCreatedAt("2025-06-01T08:30:00");
        record1.setUpdatedAt("2025-06-10T14:00:00");
        recordRepo.save(record1);

        HealthRecord record2 = new HealthRecord();
        record2.setPatientId(patient1.getId());
        record2.setCategory("VACCINATION");
        record2.setTitle("COVID-19 Booster");
        record2.setVaccineName("Comirnaty");
        record2.setDoseNumber(4);
        record2.setBatchNumber("BATCH-2025-A");
        record2.setInstitution("Vienna General Hospital");
        record2.setDateFrom("2025-03-15");
        record2.setCreatedAt("2025-03-15T09:00:00");
        record2.setUpdatedAt("2025-03-15T09:00:00");
        recordRepo.save(record2);

        HealthRecord record3 = new HealthRecord();
        record3.setPatientId(patient1.getId());
        record3.setCategory("MEDICATION");
        record3.setTitle("Ibuprofen");
        record3.setDescription("For occasional headaches");
        record3.setMedicationName("Ibuprofen");
        record3.setDosage("400mg");
        record3.setFrequency("As needed");
        record3.setPrescribingDoctor("Dr. Thomas Tandler");
        record3.setDateFrom("2025-01-01");
        record3.setCreatedAt("2025-01-01T10:00:00");
        record3.setUpdatedAt("2025-01-01T10:00:00");
        recordRepo.save(record3);

        HealthRecord record4 = new HealthRecord();
        record4.setPatientId(patient1.getId());
        record4.setCategory("ILLNESS");
        record4.setTitle("Back Pain");
        record4.setDescription("Lower back pain after heavy lifting");
        record4.setDateFrom("2025-04-20");
        record4.setSeverity("Moderate");
        record4.setNotes("Physical therapy recommended");
        record4.setCreatedAt("2025-04-20T11:00:00");
        record4.setUpdatedAt("2025-04-20T11:00:00");
        recordRepo.save(record4);

        // Records for patient2
        HealthRecord record5 = new HealthRecord();
        record5.setPatientId(patient2.getId());
        record5.setCategory("ILLNESS");
        record5.setTitle("Seasonal Allergy");
        record5.setDescription("Pollen allergy, sneezing, itchy eyes");
        record5.setDateFrom("2025-04-01");
        record5.setDateTo("2025-06-30");
        record5.setSeverity("Moderate");
        record5.setNotes("Antihistamines prescribed");
        record5.setCreatedAt("2025-04-01T08:00:00");
        record5.setUpdatedAt("2025-04-01T08:00:00");
        recordRepo.save(record5);

        // Confirm record1 by doctor1
        RecordConfirmation conf = new RecordConfirmation();
        conf.setRecordId(record1.getId());
        conf.setDoctorId(doctor1.getId());
        conf.setDoctorName("Thomas Tandler");
        conf.setDoctorLicenseNumber("L-12345");
        conf.setComment("Confirmed. Recovery looks good.");
        conf.setConfirmedAt("2025-06-12T10:30:00");
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
        ri.setRecordId(record1.getId());
        ri.setIcd10CodeId(j00.getId());
        ri.setIcd10Code("J00");
        ri.setIcd10Description("Acute nasopharyngitis [common cold]");
        ri.setDoctorId(doctor1.getId());
        ri.setAssignedAt("2025-06-12T10:30:00");
        recordIcd10Repo.save(ri);
    }
}
