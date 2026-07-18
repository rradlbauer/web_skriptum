"use strict";
// ============================================================
// Clinical Diary - Frontend Application (TypeScript)
// ============================================================
const state = {
    token: localStorage.getItem("token"),
    role: localStorage.getItem("role"),
    userId: parseInt(localStorage.getItem("userId") || "0"),
    name: localStorage.getItem("name"),
    currentView: "login",
};
function saveSession(data) {
    state.token = data.token;
    state.role = data.role;
    state.userId = data.userId;
    state.name = data.name;
    localStorage.setItem("token", data.token);
    localStorage.setItem("role", data.role);
    localStorage.setItem("userId", String(data.userId));
    localStorage.setItem("name", data.name);
}
function clearSession() {
    state.token = null;
    state.role = null;
    state.userId = null;
    state.name = null;
    localStorage.clear();
}
// --- API Client ---
async function api(method, url, body) {
    const headers = { "Content-Type": "application/json" };
    if (state.token) {
        headers["Authorization"] = "Bearer " + state.token;
    }
    const opts = { method, headers };
    if (body)
        opts.body = JSON.stringify(body);
    const response = await fetch(url, opts);
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.error || "Request failed");
    }
    return data;
}
// --- View Rendering ---
const mainContent = document.getElementById("main-content");
const navbar = document.getElementById("navbar");
const navUserName = document.getElementById("nav-user-name");
const navUserRole = document.getElementById("nav-user-role");
const btnLogout = document.getElementById("btn-logout");
function showNavbar() {
    navbar.classList.remove("hidden");
    navUserName.textContent = state.name || "";
    navUserRole.textContent = state.role || "";
    navUserRole.className = "badge badge-" + (state.role || "").toLowerCase();
}
function hideNavbar() {
    navbar.classList.add("hidden");
}
function navigate(view) {
    state.currentView = view;
    render();
}
function render() {
    if (!state.token) {
        hideNavbar();
        renderLogin();
        return;
    }
    showNavbar();
    switch (state.role) {
        case "PATIENT":
            navigatePatient();
            break;
        case "DOCTOR":
            navigateDoctor();
            break;
        case "SUPERUSER":
            navigateSuperuser();
            break;
        default:
            renderLogin();
    }
}
// --- Login / Register ---
function renderLogin() {
    mainContent.innerHTML = `
        <div class="login-container">
            <h1>Clinical Diary</h1>
            <p class="subtitle">Your personal health record</p>
            <div class="card">
                <div class="login-tabs">
                    <button class="login-tab active" data-tab="login">Login</button>
                    <button class="login-tab" data-tab="register">Register</button>
                </div>
                <div id="login-form">
                    <div class="form-group">
                        <label>Email</label>
                        <input type="email" id="login-email" placeholder="your@email.at">
                    </div>
                    <div class="form-group">
                        <label>Password</label>
                        <input type="password" id="login-password" placeholder="Password">
                    </div>
                    <div id="login-error"></div>
                    <button class="btn btn-primary btn-block" id="btn-login">Login</button>
                    <div style="margin-top:1rem; font-size:0.85rem; color:var(--gray-500);">
                        <strong>Demo Accounts:</strong><br>
                        Patient: anna.schmidt@email.at / password123<br>
                        Doctor: dr.tandler@klinik.at / doctor123<br>
                        Admin: admin@clinicaldiary.at / admin
                    </div>
                </div>
                <div id="register-form" class="hidden">
                    <div class="form-group">
                        <label>First Name</label>
                        <input type="text" id="reg-firstName" placeholder="First name">
                    </div>
                    <div class="form-group">
                        <label>Last Name</label>
                        <input type="text" id="reg-lastName" placeholder="Last name">
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>Email</label>
                            <input type="email" id="reg-email" placeholder="your@email.at">
                        </div>
                        <div class="form-group">
                            <label>Password</label>
                            <input type="password" id="reg-password" placeholder="Password">
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>Social Security Number</label>
                            <input type="text" id="reg-ssn" placeholder="SSN">
                        </div>
                        <div class="form-group">
                            <label>Date of Birth</label>
                            <input type="date" id="reg-dob">
                        </div>
                    </div>
                    <div id="register-error"></div>
                    <button class="btn btn-primary btn-block" id="btn-register">Register</button>
                </div>
            </div>
        </div>`;
    // Tab switching
    document.querySelectorAll(".login-tab").forEach(tab => {
        tab.addEventListener("click", () => {
            document.querySelectorAll(".login-tab").forEach(t => t.classList.remove("active"));
            tab.classList.add("active");
            const tabName = tab.getAttribute("data-tab");
            document.getElementById("login-form").classList.toggle("hidden", tabName !== "login");
            document.getElementById("register-form").classList.toggle("hidden", tabName !== "register");
        });
    });
    // Login
    document.getElementById("btn-login").addEventListener("click", async () => {
        const email = document.getElementById("login-email").value;
        const password = document.getElementById("login-password").value;
        try {
            const data = await api("POST", "/api/auth/login", { email, password });
            saveSession(data);
            render();
        }
        catch (e) {
            document.getElementById("login-error").innerHTML =
                `<div class="alert alert-error">${e.message}</div>`;
        }
    });
    // Register
    document.getElementById("btn-register").addEventListener("click", async () => {
        const body = {
            firstName: document.getElementById("reg-firstName").value,
            lastName: document.getElementById("reg-lastName").value,
            email: document.getElementById("reg-email").value,
            password: document.getElementById("reg-password").value,
            ssn: document.getElementById("reg-ssn").value,
            dateOfBirth: document.getElementById("reg-dob").value,
        };
        try {
            const data = await api("POST", "/api/auth/register/patient", body);
            saveSession(data);
            render();
        }
        catch (e) {
            document.getElementById("register-error").innerHTML =
                `<div class="alert alert-error">${e.message}</div>`;
        }
    });
    // Enter key
    document.getElementById("login-password").addEventListener("keydown", (e) => {
        if (e.key === "Enter")
            document.getElementById("btn-login").click();
    });
}
// --- Logout ---
btnLogout.addEventListener("click", () => {
    clearSession();
    render();
});
// ============================================================
// PATIENT VIEWS
// ============================================================
async function navigatePatient() {
    const tab = state.currentView;
    mainContent.innerHTML = `
        <div class="tabs">
            <button class="tab-btn ${tab === "patient-records" || tab === "login" ? "active" : ""}" data-view="patient-records">My Records</button>
            <button class="tab-btn ${tab === "patient-doctors" ? "active" : ""}" data-view="patient-doctors">My Doctors</button>
            <button class="tab-btn ${tab === "patient-search" ? "active" : ""}" data-view="patient-search">Find Doctor</button>
        </div>
        <div id="tab-content"><div class="loading">Loading...</div></div>`;
    document.querySelectorAll(".tab-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            navigate(btn.getAttribute("data-view"));
        });
    });
    switch (tab) {
        case "patient-doctors":
            await renderPatientDoctors();
            break;
        case "patient-search":
            await renderPatientSearch();
            break;
        default: await renderPatientRecords();
    }
}
async function renderPatientRecords() {
    const content = document.getElementById("tab-content");
    try {
        const records = await api("GET", "/api/patients/me/records");
        let html = `
            <div class="card-header">
                <h2>Health Records</h2>
                <button class="btn btn-primary" id="btn-new-record">+ New Record</button>
            </div>
            <div id="records-list">`;
        if (records.length === 0) {
            html += `<div class="empty-state"><p>No health records yet.</p></div>`;
        }
        else {
            for (const r of records) {
                html += renderRecordCard(r, true);
            }
        }
        html += `</div>`;
        content.innerHTML = html;
        document.getElementById("btn-new-record").addEventListener("click", () => showRecordModal(null));
        attachRecordEvents(true);
    }
    catch (e) {
        content.innerHTML = `<div class="alert alert-error">${e.message}</div>`;
    }
}
function renderRecordCard(record, isPatient) {
    const catClass = "cat-" + record.category.toLowerCase();
    const confirmBadge = record.confirmed
        ? `<span class="confirmed-badge">Confirmed by ${record.confirmedBy}</span>`
        : `<span class="unconfirmed-badge">Unconfirmed</span>`;
    let details = "";
    if (record.severity) {
        const sevClass = "severity-" + record.severity.toLowerCase();
        details += `<span class="detail-chip ${sevClass}">${record.severity}</span>`;
    }
    if (record.dateFrom)
        details += `<span class="detail-chip">${record.dateFrom}${record.dateTo ? " - " + record.dateTo : ""}</span>`;
    if (record.medicationName)
        details += `<span class="detail-chip">${record.medicationName} ${record.dosage || ""}</span>`;
    if (record.vaccineName)
        details += `<span class="detail-chip">${record.vaccineName} Dose ${record.doseNumber || ""}</span>`;
    if (record.institution)
        details += `<span class="detail-chip">${record.institution}</span>`;
    let icdHtml = "";
    if (record.icd10Codes && record.icd10Codes.length > 0) {
        icdHtml = `<div class="record-details" style="margin-top:0.5rem">` +
            record.icd10Codes.map((icd) => `<span class="icd-chip">${icd.code}: ${icd.description}</span>`).join("") + `</div>`;
    }
    let confirmInfo = "";
    if (record.confirmed && record.confirmationComment) {
        confirmInfo = `<div style="font-size:0.85rem; color:var(--gray-500); margin-top:0.3rem; font-style:italic;">"${record.confirmationComment}"</div>`;
    }
    let actions = "";
    if (isPatient && !record.confirmed) {
        actions = `
            <div style="margin-top:0.5rem">
                <button class="btn btn-outline btn-sm" onclick="editRecord(${record.id})">Edit</button>
                <button class="btn btn-danger btn-sm" onclick="deleteRecord(${record.id})">Delete</button>
            </div>`;
    }
    return `
        <div class="record-card" data-record-id="${record.id}">
            <div class="record-header">
                <div>
                    <span class="category-badge ${catClass}">${record.category}</span>
                    <span class="record-title">${record.title || "Untitled"}</span>
                </div>
                ${confirmBadge}
            </div>
            ${record.description ? `<div class="record-description">${record.description}</div>` : ""}
            <div class="record-details">${details}</div>
            ${record.notes ? `<div style="font-size:0.85rem; color:var(--gray-500); margin-top:0.3rem;">Notes: ${record.notes}</div>` : ""}
            ${icdHtml}
            ${confirmInfo}
            ${actions}
        </div>`;
}
// Make functions available globally for onclick handlers
window.editRecord = function (recordId) {
    api("GET", "/api/patients/me/records").then(records => {
        const record = records.find((r) => r.id === recordId);
        if (record)
            showRecordModal(record);
    });
};
window.deleteRecord = async function (recordId) {
    if (!confirm("Delete this record?"))
        return;
    try {
        await api("DELETE", `/api/patients/me/records/${recordId}`);
        renderPatientRecords();
    }
    catch (e) {
        alert(e.message);
    }
};
function showRecordModal(record) {
    const isEdit = record !== null;
    const modal = document.createElement("div");
    modal.className = "modal-overlay";
    modal.innerHTML = `
        <div class="modal">
            <h2>${isEdit ? "Edit Record" : "New Health Record"}</h2>
            <div class="form-group">
                <label>Category</label>
                <select id="m-category">
                    <option value="ILLNESS" ${record?.category === "ILLNESS" ? "selected" : ""}>Illness</option>
                    <option value="VACCINATION" ${record?.category === "VACCINATION" ? "selected" : ""}>Vaccination</option>
                    <option value="MEDICATION" ${record?.category === "MEDICATION" ? "selected" : ""}>Medication</option>
                    <option value="OTHER" ${record?.category === "OTHER" ? "selected" : ""}>Other</option>
                </select>
            </div>
            <div class="form-group">
                <label>Title</label>
                <input type="text" id="m-title" value="${record?.title || ""}" placeholder="Record title">
            </div>
            <div class="form-group">
                <label>Description</label>
                <textarea id="m-description" rows="2">${record?.description || ""}</textarea>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Start Date</label>
                    <input type="date" id="m-dateFrom" value="${record?.dateFrom || ""}">
                </div>
                <div class="form-group">
                    <label>End Date</label>
                    <input type="date" id="m-dateTo" value="${record?.dateTo || ""}">
                </div>
            </div>
            <div class="form-group">
                <label>Severity</label>
                <select id="m-severity">
                    <option value="">--</option>
                    <option value="Mild" ${record?.severity === "Mild" ? "selected" : ""}>Mild</option>
                    <option value="Moderate" ${record?.severity === "Moderate" ? "selected" : ""}>Moderate</option>
                    <option value="Severe" ${record?.severity === "Severe" ? "selected" : ""}>Severe</option>
                </select>
            </div>
            <div class="form-group">
                <label>Notes</label>
                <textarea id="m-notes" rows="2">${record?.notes || ""}</textarea>
            </div>
            <div id="m-vaccination-fields" class="${(record?.category || "ILLNESS") !== "VACCINATION" ? "hidden" : ""}">
                <hr style="margin:1rem 0; border-color:var(--gray-200)">
                <h3>Vaccination Details</h3>
                <div class="form-row">
                    <div class="form-group">
                        <label>Vaccine Name</label>
                        <input type="text" id="m-vaccineName" value="${record?.vaccineName || ""}">
                    </div>
                    <div class="form-group">
                        <label>Dose Number</label>
                        <input type="number" id="m-doseNumber" value="${record?.doseNumber || ""}">
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>Batch Number</label>
                        <input type="text" id="m-batchNumber" value="${record?.batchNumber || ""}">
                    </div>
                    <div class="form-group">
                        <label>Institution</label>
                        <input type="text" id="m-institution" value="${record?.institution || ""}">
                    </div>
                </div>
            </div>
            <div id="m-medication-fields" class="${(record?.category || "ILLNESS") !== "MEDICATION" ? "hidden" : ""}">
                <hr style="margin:1rem 0; border-color:var(--gray-200)">
                <h3>Medication Details</h3>
                <div class="form-row">
                    <div class="form-group">
                        <label>Medication Name</label>
                        <input type="text" id="m-medicationName" value="${record?.medicationName || ""}">
                    </div>
                    <div class="form-group">
                        <label>Dosage</label>
                        <input type="text" id="m-dosage" value="${record?.dosage || ""}">
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>Frequency</label>
                        <input type="text" id="m-frequency" value="${record?.frequency || ""}">
                    </div>
                    <div class="form-group">
                        <label>Prescribing Doctor</label>
                        <input type="text" id="m-prescribingDoctor" value="${record?.prescribingDoctor || ""}">
                    </div>
                </div>
            </div>
            <div class="modal-actions">
                <button class="btn btn-outline" id="m-cancel">Cancel</button>
                <button class="btn btn-primary" id="m-save">${isEdit ? "Save" : "Create"}</button>
            </div>
        </div>`;
    document.body.appendChild(modal);
    // Show/hide category-specific fields
    modal.querySelector("#m-category").addEventListener("change", (e) => {
        const val = e.target.value;
        modal.querySelector("#m-vaccination-fields").classList.toggle("hidden", val !== "VACCINATION");
        modal.querySelector("#m-medication-fields").classList.toggle("hidden", val !== "MEDICATION");
    });
    modal.querySelector("#m-cancel").addEventListener("click", () => modal.remove());
    modal.addEventListener("click", (e) => { if (e.target === modal)
        modal.remove(); });
    modal.querySelector("#m-save").addEventListener("click", async () => {
        const body = {
            category: modal.querySelector("#m-category").value,
            title: modal.querySelector("#m-title").value,
            description: modal.querySelector("#m-description").value,
            dateFrom: modal.querySelector("#m-dateFrom").value,
            dateTo: modal.querySelector("#m-dateTo").value,
            severity: modal.querySelector("#m-severity").value,
            notes: modal.querySelector("#m-notes").value,
            vaccineName: modal.querySelector("#m-vaccineName").value,
            doseNumber: modal.querySelector("#m-doseNumber").value || null,
            batchNumber: modal.querySelector("#m-batchNumber").value,
            institution: modal.querySelector("#m-institution").value,
            medicationName: modal.querySelector("#m-medicationName").value,
            dosage: modal.querySelector("#m-dosage").value,
            frequency: modal.querySelector("#m-frequency").value,
            prescribingDoctor: modal.querySelector("#m-prescribingDoctor").value,
        };
        try {
            if (isEdit) {
                await api("PUT", `/api/patients/me/records/${record.id}`, body);
            }
            else {
                await api("POST", "/api/patients/me/records", body);
            }
            modal.remove();
            renderPatientRecords();
        }
        catch (e) {
            alert(e.message);
        }
    });
}
async function renderPatientDoctors() {
    const content = document.getElementById("tab-content");
    try {
        const doctors = await api("GET", "/api/patients/me/doctors");
        let html = `<h2>My Doctors</h2>`;
        if (doctors.length === 0) {
            html += `<div class="empty-state"><p>No doctors assigned yet.</p><p>Go to "Find Doctor" to add one.</p></div>`;
        }
        else {
            for (const d of doctors) {
                html += `
                    <div class="doctor-card">
                        <div class="doctor-info">
                            <h4>Dr. ${d.firstName} ${d.lastName}</h4>
                            <p>${d.specialization} | License: ${d.medicalLicenseNumber}</p>
                        </div>
                        <button class="btn btn-danger btn-sm" onclick="removeDoctor(${d.id})">Remove</button>
                    </div>`;
            }
        }
        content.innerHTML = html;
    }
    catch (e) {
        content.innerHTML = `<div class="alert alert-error">${e.message}</div>`;
    }
}
window.removeDoctor = async function (doctorId) {
    if (!confirm("Remove this doctor?"))
        return;
    try {
        await api("DELETE", `/api/patients/me/doctors/${doctorId}`);
        renderPatientDoctors();
    }
    catch (e) {
        alert(e.message);
    }
};
async function renderPatientSearch() {
    const content = document.getElementById("tab-content");
    content.innerHTML = `
        <h2>Find a Doctor</h2>
        <div class="search-box">
            <input type="text" id="doctor-search-input" placeholder="Search by name, specialization, or license number...">
            <button class="btn btn-primary" id="btn-search-doctor">Search</button>
        </div>
        <div id="search-results"></div>`;
    const doSearch = async () => {
        const q = document.getElementById("doctor-search-input").value;
        if (!q)
            return;
        try {
            const doctors = await api("GET", `/api/patients/doctors/search?q=${encodeURIComponent(q)}`);
            const results = document.getElementById("search-results");
            if (doctors.length === 0) {
                results.innerHTML = `<div class="empty-state"><p>No doctors found.</p></div>`;
                return;
            }
            let html = "";
            for (const d of doctors) {
                html += `
                    <div class="doctor-card">
                        <div class="doctor-info">
                            <h4>Dr. ${d.firstName} ${d.lastName}</h4>
                            <p>${d.specialization} | License: ${d.medicalLicenseNumber}</p>
                        </div>
                        <button class="btn btn-success btn-sm" onclick="assignDoctor(${d.id})">Assign</button>
                    </div>`;
            }
            results.innerHTML = html;
        }
        catch (e) {
            document.getElementById("search-results").innerHTML =
                `<div class="alert alert-error">${e.message}</div>`;
        }
    };
    document.getElementById("btn-search-doctor").addEventListener("click", doSearch);
    document.getElementById("doctor-search-input").addEventListener("keydown", (e) => {
        if (e.key === "Enter")
            doSearch();
    });
}
window.assignDoctor = async function (doctorId) {
    try {
        await api("POST", `/api/patients/me/doctors/${doctorId}`);
        alert("Doctor assigned successfully!");
    }
    catch (e) {
        alert(e.message);
    }
};
function attachRecordEvents(isPatient) {
    // Events attached via global onclick handlers
}
// ============================================================
// DOCTOR VIEWS
// ============================================================
async function navigateDoctor() {
    const tab = state.currentView;
    mainContent.innerHTML = `
        <div class="tabs">
            <button class="tab-btn ${tab === "doctor-patients" || tab === "login" ? "active" : ""}" data-view="doctor-patients">My Patients</button>
            <button class="tab-btn ${tab === "doctor-icd10" ? "active" : ""}" data-view="doctor-icd10">ICD-10 Search</button>
        </div>
        <div id="tab-content"><div class="loading">Loading...</div></div>`;
    document.querySelectorAll(".tab-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            navigate(btn.getAttribute("data-view"));
        });
    });
    if (tab === "doctor-icd10") {
        await renderIcd10Search();
    }
    else {
        await renderDoctorPatients();
    }
}
async function renderDoctorPatients() {
    const content = document.getElementById("tab-content");
    try {
        const patients = await api("GET", "/api/doctors/me/patients");
        let html = `<h2>My Patients</h2>`;
        if (patients.length === 0) {
            html += `<div class="empty-state"><p>No patients assigned yet.</p></div>`;
        }
        else {
            for (const p of patients) {
                html += `
                    <div class="doctor-card">
                        <div class="doctor-info">
                            <h4>${p.firstName} ${p.lastName}</h4>
                            <p>SSN: ${p.ssn} | DOB: ${p.dateOfBirth} | ${p.email}</p>
                        </div>
                        <button class="btn btn-primary btn-sm" onclick="viewPatientRecords(${p.id}, '${p.firstName} ${p.lastName}')">View Records</button>
                    </div>`;
            }
        }
        content.innerHTML = html;
    }
    catch (e) {
        content.innerHTML = `<div class="alert alert-error">${e.message}</div>`;
    }
}
window.viewPatientRecords = async function (patientId, patientName) {
    const content = document.getElementById("tab-content");
    try {
        const records = await api("GET", `/api/doctors/me/patients/${patientId}/records`);
        let html = `
            <div class="card-header">
                <h2>Records of ${patientName}</h2>
                <button class="btn btn-outline" onclick="navigateDoctor()">Back</button>
            </div>`;
        if (records.length === 0) {
            html += `<div class="empty-state"><p>No records found.</p></div>`;
        }
        else {
            for (const r of records) {
                html += renderDoctorRecordCard(r, patientId);
            }
        }
        content.innerHTML = html;
    }
    catch (e) {
        content.innerHTML = `<div class="alert alert-error">${e.message}</div>`;
    }
};
function renderDoctorRecordCard(record, patientId) {
    const catClass = "cat-" + record.category.toLowerCase();
    const confirmBadge = record.confirmed
        ? `<span class="confirmed-badge">Confirmed</span>`
        : `<span class="unconfirmed-badge">Unconfirmed</span>`;
    let details = "";
    if (record.severity) {
        const sevClass = "severity-" + record.severity.toLowerCase();
        details += `<span class="detail-chip ${sevClass}">${record.severity}</span>`;
    }
    if (record.dateFrom)
        details += `<span class="detail-chip">${record.dateFrom}${record.dateTo ? " - " + record.dateTo : ""}</span>`;
    if (record.medicationName)
        details += `<span class="detail-chip">${record.medicationName} ${record.dosage || ""}</span>`;
    if (record.vaccineName)
        details += `<span class="detail-chip">${record.vaccineName} Dose ${record.doseNumber || ""}</span>`;
    let icdHtml = "";
    if (record.icd10Codes && record.icd10Codes.length > 0) {
        icdHtml = `<div class="record-details" style="margin-top:0.5rem">` +
            record.icd10Codes.map((icd) => `<span class="icd-chip">${icd.code}: ${icd.description} <button onclick="removeIcd10(${record.id}, ${icd.id})" title="Remove">&times;</button></span>`).join("") + `</div>`;
    }
    let actions = "";
    if (!record.confirmed) {
        actions = `
            <div style="margin-top:0.5rem">
                <button class="btn btn-success btn-sm" onclick="confirmRecord(${record.id})">Confirm</button>
            </div>`;
    }
    actions += `
        <div style="margin-top:0.5rem">
            <button class="btn btn-outline btn-sm" onclick="showIcd10Modal(${record.id})">+ ICD-10</button>
        </div>`;
    return `
        <div class="record-card">
            <div class="record-header">
                <div>
                    <span class="category-badge ${catClass}">${record.category}</span>
                    <span class="record-title">${record.title || "Untitled"}</span>
                </div>
                ${confirmBadge}
            </div>
            ${record.description ? `<div class="record-description">${record.description}</div>` : ""}
            <div class="record-details">${details}</div>
            ${record.notes ? `<div style="font-size:0.85rem;color:var(--gray-500);margin-top:0.3rem">Notes: ${record.notes}</div>` : ""}
            ${record.confirmedBy ? `<div style="font-size:0.85rem;color:var(--success);margin-top:0.3rem">Confirmed by Dr. ${record.confirmedBy} ${record.confirmedAt ? "on " + record.confirmedAt.split("T")[0] : ""}</div>` : ""}
            ${record.confirmationComment ? `<div style="font-size:0.85rem;color:var(--gray-500);margin-top:0.3rem;font-style:italic">"${record.confirmationComment}"</div>` : ""}
            ${icdHtml}
            ${actions}
        </div>`;
}
window.confirmRecord = async function (recordId) {
    const comment = prompt("Confirmation comment (optional):");
    try {
        await api("POST", `/api/doctors/records/${recordId}/confirm`, { comment: comment || "" });
        alert("Record confirmed!");
        // Reload current view
        const tabContent = document.getElementById("tab-content");
        if (tabContent) {
            const backBtn = tabContent.querySelector("[onclick*='navigateDoctor']");
            if (backBtn)
                backBtn.click();
        }
    }
    catch (e) {
        alert(e.message);
    }
};
window.showIcd10Modal = function (recordId) {
    const modal = document.createElement("div");
    modal.className = "modal-overlay";
    modal.innerHTML = `
        <div class="modal">
            <h2>Add ICD-10 Code</h2>
            <div class="search-box">
                <input type="text" id="icd10-search-input" placeholder="Search ICD-10 code or description...">
                <button class="btn btn-primary" id="btn-icd10-search">Search</button>
            </div>
            <div id="icd10-results"></div>
            <div class="modal-actions">
                <button class="btn btn-outline" id="icd10-cancel">Cancel</button>
            </div>
        </div>`;
    document.body.appendChild(modal);
    modal.addEventListener("click", (e) => { if (e.target === modal)
        modal.remove(); });
    modal.querySelector("#icd10-cancel").addEventListener("click", () => modal.remove());
    const doSearch = async () => {
        const q = modal.querySelector("#icd10-search-input").value;
        if (!q)
            return;
        try {
            const codes = await api("GET", `/api/doctors/icd10/search?q=${encodeURIComponent(q)}`);
            const results = modal.querySelector("#icd10-results");
            if (codes.length === 0) {
                results.innerHTML = `<p style="color:var(--gray-500)">No codes found.</p>`;
                return;
            }
            results.innerHTML = codes.map((c) => `
                <div class="doctor-card" style="cursor:pointer" onclick="addIcd10ToRecord(${recordId}, ${c.id}, this)">
                    <div class="doctor-info">
                        <h4>${c.code}</h4>
                        <p>${c.description}</p>
                    </div>
                </div>`).join("");
        }
        catch (e) {
            alert(e.message);
        }
    };
    modal.querySelector("#btn-icd10-search").addEventListener("click", doSearch);
    modal.querySelector("#icd10-search-input").addEventListener("keydown", (e) => {
        if (e.key === "Enter")
            doSearch();
    });
};
window.addIcd10ToRecord = async function (recordId, icd10Id, el) {
    try {
        await api("POST", `/api/doctors/records/${recordId}/icd10`, { icd10CodeId: icd10Id });
        el.closest(".modal-overlay")?.remove();
        alert("ICD-10 code added!");
    }
    catch (e) {
        alert(e.message);
    }
};
window.removeIcd10 = async function (recordId, icd10Id) {
    if (!confirm("Remove this ICD-10 code?"))
        return;
    try {
        await api("DELETE", `/api/doctors/records/${recordId}/icd10/${icd10Id}`);
        alert("ICD-10 code removed!");
    }
    catch (e) {
        alert(e.message);
    }
};
async function renderIcd10Search() {
    const content = document.getElementById("tab-content");
    content.innerHTML = `
        <h2>ICD-10 Code Search</h2>
        <div class="search-box">
            <input type="text" id="icd10-main-search" placeholder="Search by code or description...">
            <button class="btn btn-primary" id="btn-icd10-main-search">Search</button>
        </div>
        <div id="icd10-main-results"></div>`;
    const doSearch = async () => {
        const q = document.getElementById("icd10-main-search").value;
        if (!q)
            return;
        try {
            const codes = await api("GET", `/api/doctors/icd10/search?q=${encodeURIComponent(q)}`);
            const results = document.getElementById("icd10-main-results");
            if (codes.length === 0) {
                results.innerHTML = `<div class="empty-state"><p>No codes found.</p></div>`;
                return;
            }
            let html = `<table>
                <thead><tr><th>Code</th><th>Description</th></tr></thead>
                <tbody>`;
            for (const c of codes) {
                html += `<tr><td><strong>${c.code}</strong></td><td>${c.description}</td></tr>`;
            }
            html += `</tbody></table>`;
            results.innerHTML = html;
        }
        catch (e) {
            document.getElementById("icd10-main-results").innerHTML =
                `<div class="alert alert-error">${e.message}</div>`;
        }
    };
    document.getElementById("btn-icd10-main-search").addEventListener("click", doSearch);
    document.getElementById("icd10-main-search").addEventListener("keydown", (e) => {
        if (e.key === "Enter")
            doSearch();
    });
}
// ============================================================
// SUPERUSER VIEWS
// ============================================================
async function navigateSuperuser() {
    const tab = state.currentView;
    mainContent.innerHTML = `
        <div class="tabs">
            <button class="tab-btn ${tab === "su-doctors" || tab === "login" ? "active" : ""}" data-view="su-doctors">Manage Doctors</button>
            <button class="tab-btn ${tab === "su-add-doctor" ? "active" : ""}" data-view="su-add-doctor">Add Doctor</button>
            <button class="tab-btn ${tab === "su-stats" ? "active" : ""}" data-view="su-stats">Statistics</button>
        </div>
        <div id="tab-content"><div class="loading">Loading...</div></div>`;
    document.querySelectorAll(".tab-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            navigate(btn.getAttribute("data-view"));
        });
    });
    switch (tab) {
        case "su-add-doctor":
            renderAddDoctor();
            break;
        case "su-stats":
            await renderStats();
            break;
        default: await renderManageDoctors();
    }
}
async function renderManageDoctors() {
    const content = document.getElementById("tab-content");
    try {
        const doctors = await api("GET", "/api/superuser/doctors");
        let html = `<h2>Registered Doctors</h2>`;
        if (doctors.length === 0) {
            html += `<div class="empty-state"><p>No doctors registered yet.</p></div>`;
        }
        else {
            html += `<table>
                <thead>
                    <tr><th>Name</th><th>Email</th><th>Specialization</th><th>License</th><th>Status</th><th>Actions</th></tr>
                </thead>
                <tbody>`;
            for (const d of doctors) {
                html += `
                    <tr>
                        <td>Dr. ${d.firstName} ${d.lastName}</td>
                        <td>${d.email}</td>
                        <td>${d.specialization}</td>
                        <td>${d.medicalLicenseNumber}</td>
                        <td>${d.active
                    ? '<span class="confirmed-badge">Active</span>'
                    : '<span class="unconfirmed-badge">Inactive</span>'}</td>
                        <td>${d.active
                    ? `<button class="btn btn-danger btn-sm" onclick="toggleDoctor(${d.id}, false)">Deactivate</button>`
                    : `<button class="btn btn-success btn-sm" onclick="toggleDoctor(${d.id}, true)">Activate</button>`}</td>
                    </tr>`;
            }
            html += `</tbody></table>`;
        }
        content.innerHTML = html;
    }
    catch (e) {
        content.innerHTML = `<div class="alert alert-error">${e.message}</div>`;
    }
}
window.toggleDoctor = async function (doctorId, activate) {
    try {
        await api("PUT", `/api/superuser/doctors/${doctorId}/${activate ? "activate" : "deactivate"}`);
        renderManageDoctors();
    }
    catch (e) {
        alert(e.message);
    }
};
function renderAddDoctor() {
    const content = document.getElementById("tab-content");
    content.innerHTML = `
        <div class="card" style="max-width:600px">
            <h2>Register New Doctor</h2>
            <div class="form-group">
                <label>First Name</label>
                <input type="text" id="doc-firstName" placeholder="First name">
            </div>
            <div class="form-group">
                <label>Last Name</label>
                <input type="text" id="doc-lastName" placeholder="Last name">
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Email</label>
                    <input type="email" id="doc-email" placeholder="email@klinik.at">
                </div>
                <div class="form-group">
                    <label>Password</label>
                    <input type="password" id="doc-password" placeholder="Password">
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Medical License Number</label>
                    <input type="text" id="doc-license" placeholder="L-XXXXX">
                </div>
                <div class="form-group">
                    <label>Specialization</label>
                    <input type="text" id="doc-specialization" placeholder="e.g. Cardiology">
                </div>
            </div>
            <div id="add-doctor-error"></div>
            <button class="btn btn-primary" id="btn-add-doctor" style="margin-top:1rem">Register Doctor</button>
        </div>`;
    document.getElementById("btn-add-doctor").addEventListener("click", async () => {
        const body = {
            firstName: document.getElementById("doc-firstName").value,
            lastName: document.getElementById("doc-lastName").value,
            email: document.getElementById("doc-email").value,
            password: document.getElementById("doc-password").value,
            medicalLicenseNumber: document.getElementById("doc-license").value,
            specialization: document.getElementById("doc-specialization").value,
        };
        try {
            await api("POST", "/api/superuser/doctors", body);
            alert("Doctor registered successfully!");
            navigate("su-doctors");
        }
        catch (e) {
            document.getElementById("add-doctor-error").innerHTML =
                `<div class="alert alert-error">${e.message}</div>`;
        }
    });
}
async function renderStats() {
    const content = document.getElementById("tab-content");
    try {
        const stats = await api("GET", "/api/superuser/stats");
        content.innerHTML = `
            <h2>System Statistics</h2>
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-value">${stats.totalPatients}</div>
                    <div class="stat-label">Registered Patients</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value">${stats.totalDoctors}</div>
                    <div class="stat-label">Total Doctors</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value">${stats.activeDoctors}</div>
                    <div class="stat-label">Active Doctors</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value">${stats.totalRecords}</div>
                    <div class="stat-label">Health Records</div>
                </div>
            </div>`;
    }
    catch (e) {
        content.innerHTML = `<div class="alert alert-error">${e.message}</div>`;
    }
}
// ============================================================
// Initialize
// ============================================================
if (state.token) {
    navigate("login");
    // Determine role from stored data and navigate accordingly
    if (state.role === "PATIENT")
        navigate("patient-records");
    else if (state.role === "DOCTOR")
        navigate("doctor-patients");
    else if (state.role === "SUPERUSER")
        navigate("su-doctors");
}
else {
    renderLogin();
}
