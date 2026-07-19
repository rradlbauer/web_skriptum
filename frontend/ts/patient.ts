// --- Patient Views ---

import { state } from "./state";
import { api, apiUpload } from "./api";
import { navigate } from "./app";

const mainContent = document.getElementById("main-content") as HTMLElement;

export async function navigatePatient() {
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
            navigate(btn.getAttribute("data-view")!);
        });
    });

    switch (tab) {
        case "patient-doctors": await renderPatientDoctors(); break;
        case "patient-search": await renderPatientSearch(); break;
        default: await renderPatientRecords();
    }
}

export async function renderPatientRecords() {
    const content = document.getElementById("tab-content")!;
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
        } else {
            for (const r of records) {
                html += renderRecordCard(r, true);
            }
        }
        html += `</div>`;
        content.innerHTML = html;

        document.getElementById("btn-new-record")!.addEventListener("click", () => showRecordModal(null));
    } catch (e: any) {
        content.innerHTML = `<div class="alert alert-error">${e.message}</div>`;
    }
}

function renderRecordCard(record: any, isPatient: boolean): string {
    const catClass = "cat-" + record.category.toLowerCase();
    const confirmBadge = record.confirmed
        ? `<span class="confirmed-badge">Confirmed by ${record.confirmedBy}</span>`
        : `<span class="unconfirmed-badge">Unconfirmed</span>`;

    let details = "";
    if (record.severity) {
        const sevClass = "severity-" + record.severity.toLowerCase();
        details += `<span class="detail-chip ${sevClass}">${record.severity}</span>`;
    }
    if (record.dateFrom) details += `<span class="detail-chip">${record.dateFrom}${record.dateTo ? " - " + record.dateTo : ""}</span>`;
    if (record.medicationName) details += `<span class="detail-chip">${record.medicationName} ${record.dosage || ""}</span>`;
    if (record.vaccineName) details += `<span class="detail-chip">${record.vaccineName} Dose ${record.doseNumber || ""}</span>`;
    if (record.institution) details += `<span class="detail-chip">${record.institution}</span>`;

    let icdHtml = "";
    if (record.icd10Codes && record.icd10Codes.length > 0) {
        icdHtml = `<div class="record-details" style="margin-top:0.5rem">` +
            record.icd10Codes.map((icd: any) =>
                `<span class="icd-chip">${icd.code}: ${icd.description}</span>`
            ).join("") + `</div>`;
    }

    let confirmInfo = "";
    if (record.confirmed && record.confirmationComment) {
        confirmInfo = `<div style="font-size:0.85rem; color:var(--gray-500); margin-top:0.3rem; font-style:italic;">"${record.confirmationComment}"</div>`;
    }

    let attachHtml = "";
    if (record.attachments && record.attachments.length > 0) {
        attachHtml = `<div class="record-attachments" style="margin-top:0.5rem">` +
            record.attachments.map((a: any) =>
                `<span class="attachment-chip" style="cursor:pointer" onclick="downloadAttachment(${record.id}, ${a.id}, '${a.originalFilename}')">${a.originalFilename}</span>`
            ).join("") + `</div>`;
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
            ${attachHtml}
            ${confirmInfo}
            ${actions}
        </div>`;
}

(window as any).editRecord = function(recordId: number) {
    api("GET", "/api/patients/me/records").then(records => {
        const record = records.find((r: any) => r.id === recordId);
        if (record) showRecordModal(record);
    });
};

(window as any).deleteRecord = async function(recordId: number) {
    if (!confirm("Delete this record?")) return;
    try {
        await api("DELETE", `/api/patients/me/records/${recordId}`);
        renderPatientRecords();
    } catch (e: any) {
        alert(e.message);
    }
};

(window as any).deleteAttachment = async function(recordId: number, attachmentId: number) {
    if (!confirm("Delete this attachment?")) return;
    try {
        await api("DELETE", `/api/patients/me/records/${recordId}/attachments/${attachmentId}`);
        renderPatientRecords();
    } catch (e: any) {
        alert(e.message);
    }
};

function showRecordModal(record: any | null) {
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
            <div id="m-attachment-section" class="${isEdit ? "" : "hidden"}">
                <hr style="margin:1rem 0; border-color:var(--gray-200)">
                <h3>Attachments</h3>
                <div id="m-existing-attachments"></div>
                <div class="form-group">
                    <label>Upload file (PDF, JPEG, PNG — max 10 MB)</label>
                    <input type="file" id="m-file-input" accept=".pdf,.jpg,.jpeg,.png" multiple>
                </div>
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

    (modal.querySelector("#m-category") as HTMLSelectElement).addEventListener("change", (e) => {
        const val = (e.target as HTMLSelectElement).value;
        modal.querySelector("#m-vaccination-fields")!.classList.toggle("hidden", val !== "VACCINATION");
        modal.querySelector("#m-medication-fields")!.classList.toggle("hidden", val !== "MEDICATION");
    });

    modal.querySelector("#m-cancel")!.addEventListener("click", () => modal.remove());
    modal.addEventListener("click", (e) => { if (e.target === modal) modal.remove(); });

    if (isEdit && record) {
        const attContainer = modal.querySelector("#m-existing-attachments") as HTMLElement;
        api("GET", `/api/patients/me/records/${record.id}/attachments`).then((atts: any[]) => {
            if (atts.length > 0) {
                attContainer.innerHTML = atts.map((a: any) =>
                    `<div class="attachment-chip" style="display:flex;align-items:center;gap:0.5rem;margin-bottom:0.5rem">
                        <span style="cursor:pointer" onclick="downloadAttachment(${record.id}, ${a.id}, '${a.originalFilename}')">${a.originalFilename}</span>
                        <button class="btn btn-danger btn-sm" onclick="deleteAttachment(${record.id}, ${a.id})" style="padding:0.1rem 0.4rem;font-size:0.75rem">&times;</button>
                    </div>`
                ).join("");
            }
        });
    }

    modal.querySelector("#m-save")!.addEventListener("click", async () => {
        const body: any = {
            category: (modal.querySelector("#m-category") as HTMLSelectElement).value,
            title: (modal.querySelector("#m-title") as HTMLInputElement).value,
            description: (modal.querySelector("#m-description") as HTMLTextAreaElement).value,
            dateFrom: (modal.querySelector("#m-dateFrom") as HTMLInputElement).value,
            dateTo: (modal.querySelector("#m-dateTo") as HTMLInputElement).value,
            severity: (modal.querySelector("#m-severity") as HTMLSelectElement).value,
            notes: (modal.querySelector("#m-notes") as HTMLTextAreaElement).value,
            vaccineName: (modal.querySelector("#m-vaccineName") as HTMLInputElement).value,
            doseNumber: (modal.querySelector("#m-doseNumber") as HTMLInputElement).value || null,
            batchNumber: (modal.querySelector("#m-batchNumber") as HTMLInputElement).value,
            institution: (modal.querySelector("#m-institution") as HTMLInputElement).value,
            medicationName: (modal.querySelector("#m-medicationName") as HTMLInputElement).value,
            dosage: (modal.querySelector("#m-dosage") as HTMLInputElement).value,
            frequency: (modal.querySelector("#m-frequency") as HTMLInputElement).value,
            prescribingDoctor: (modal.querySelector("#m-prescribingDoctor") as HTMLInputElement).value,
        };
        try {
            let savedRecord: any;
            if (isEdit) {
                savedRecord = await api("PUT", `/api/patients/me/records/${record.id}`, body);
            } else {
                savedRecord = await api("POST", "/api/patients/me/records", body);
            }
            const fileInput = modal.querySelector("#m-file-input") as HTMLInputElement;
            if (fileInput && fileInput.files) {
                for (let i = 0; i < fileInput.files.length; i++) {
                    try {
                        await apiUpload(`/api/patients/me/records/${savedRecord.id}/attachments`, fileInput.files[i]);
                    } catch (err: any) {
                        alert("Failed to upload " + fileInput.files[i].name + ": " + err.message);
                    }
                }
            }
            modal.remove();
            renderPatientRecords();
        } catch (e: any) {
            alert(e.message);
        }
    });
}

async function renderPatientDoctors() {
    const content = document.getElementById("tab-content")!;
    try {
        const doctors = await api("GET", "/api/patients/me/doctors");
        let html = `<h2>My Doctors</h2>`;
        if (doctors.length === 0) {
            html += `<div class="empty-state"><p>No doctors assigned yet.</p><p>Go to "Find Doctor" to add one.</p></div>`;
        } else {
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
    } catch (e: any) {
        content.innerHTML = `<div class="alert alert-error">${e.message}</div>`;
    }
}

(window as any).removeDoctor = async function(doctorId: number) {
    if (!confirm("Remove this doctor?")) return;
    try {
        await api("DELETE", `/api/patients/me/doctors/${doctorId}`);
        renderPatientDoctors();
    } catch (e: any) {
        alert(e.message);
    }
};

async function renderPatientSearch() {
    const content = document.getElementById("tab-content")!;
    content.innerHTML = `
        <h2>Find a Doctor</h2>
        <div class="search-box">
            <input type="text" id="doctor-search-input" placeholder="Search by name, specialization, or license number...">
            <button class="btn btn-primary" id="btn-search-doctor">Search</button>
        </div>
        <div id="search-results"></div>`;

    const doSearch = async () => {
        const q = (document.getElementById("doctor-search-input") as HTMLInputElement).value;
        if (!q) return;
        try {
            const doctors = await api("GET", `/api/patients/doctors/search?q=${encodeURIComponent(q)}`);
            const results = document.getElementById("search-results")!;
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
        } catch (e: any) {
            document.getElementById("search-results")!.innerHTML =
                `<div class="alert alert-error">${e.message}</div>`;
        }
    };

    document.getElementById("btn-search-doctor")!.addEventListener("click", doSearch);
    document.getElementById("doctor-search-input")!.addEventListener("keydown", (e) => {
        if (e.key === "Enter") doSearch();
    });
}

(window as any).assignDoctor = async function(doctorId: number) {
    try {
        await api("POST", `/api/patients/me/doctors/${doctorId}`);
        alert("Doctor assigned successfully!");
    } catch (e: any) {
        alert(e.message);
    }
};
