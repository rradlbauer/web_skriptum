// --- Doctor Views ---

import { state } from "./state";
import { api } from "./api";
import { navigate } from "./app";

const mainContent = document.getElementById("main-content") as HTMLElement;

export async function navigateDoctor() {
    const tab = state.currentView;
    mainContent.innerHTML = `
        <div class="tabs">
            <button class="tab-btn ${tab === "doctor-patients" || tab === "login" ? "active" : ""}" data-view="doctor-patients">My Patients</button>
            <button class="tab-btn ${tab === "doctor-icd10" ? "active" : ""}" data-view="doctor-icd10">ICD-10 Search</button>
        </div>
        <div id="tab-content"><div class="loading">Loading...</div></div>`;

    document.querySelectorAll(".tab-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            navigate(btn.getAttribute("data-view")!);
        });
    });

    if (tab === "doctor-icd10") {
        await renderIcd10Search();
    } else {
        await renderDoctorPatients();
    }
}

async function renderDoctorPatients() {
    const content = document.getElementById("tab-content")!;
    try {
        const patients = await api("GET", "/api/doctors/me/patients");
        let html = `<h2>My Patients</h2>`;
        if (patients.length === 0) {
            html += `<div class="empty-state"><p>No patients assigned yet.</p></div>`;
        } else {
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
    } catch (e: any) {
        content.innerHTML = `<div class="alert alert-error">${e.message}</div>`;
    }
}

(window as any).viewPatientRecords = async function(patientId: number, patientName: string) {
    const content = document.getElementById("tab-content")!;
    try {
        const records = await api("GET", `/api/doctors/me/patients/${patientId}/records`);
        let html = `
            <div class="card-header">
                <h2>Records of ${patientName}</h2>
                <button class="btn btn-outline" onclick="navigate('doctor-patients')">Back</button>
            </div>`;

        if (records.length === 0) {
            html += `<div class="empty-state"><p>No records found.</p></div>`;
        } else {
            for (const r of records) {
                html += renderDoctorRecordCard(r, patientId);
            }
        }
        content.innerHTML = html;
    } catch (e: any) {
        content.innerHTML = `<div class="alert alert-error">${e.message}</div>`;
    }
};

function renderDoctorRecordCard(record: any, patientId: number): string {
    const catClass = "cat-" + record.category.toLowerCase();
    const confirmBadge = record.confirmed
        ? `<span class="confirmed-badge">Confirmed</span>`
        : `<span class="unconfirmed-badge">Unconfirmed</span>`;

    let details = "";
    if (record.severity) {
        const sevClass = "severity-" + record.severity.toLowerCase();
        details += `<span class="detail-chip ${sevClass}">${record.severity}</span>`;
    }
    if (record.dateFrom) details += `<span class="detail-chip">${record.dateFrom}${record.dateTo ? " - " + record.dateTo : ""}</span>`;
    if (record.medicationName) details += `<span class="detail-chip">${record.medicationName} ${record.dosage || ""}</span>`;
    if (record.vaccineName) details += `<span class="detail-chip">${record.vaccineName} Dose ${record.doseNumber || ""}</span>`;

    let icdHtml = "";
    if (record.icd10Codes && record.icd10Codes.length > 0) {
        icdHtml = `<div class="record-details" style="margin-top:0.5rem">` +
            record.icd10Codes.map((icd: any) =>
                `<span class="icd-chip">${icd.code}: ${icd.description} <button onclick="removeIcd10(${record.id}, ${icd.id})" title="Remove">&times;</button></span>`
            ).join("") + `</div>`;
    }

    let attachHtml = "";
    if (record.attachments && record.attachments.length > 0) {
        attachHtml = `<div class="record-attachments" style="margin-top:0.5rem">` +
            record.attachments.map((a: any) =>
                `<span class="attachment-chip" style="cursor:pointer" onclick="downloadDoctorAttachment(${record.id}, ${a.id}, '${a.originalFilename}')">${a.originalFilename}</span>`
            ).join("") + `</div>`;
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
            ${attachHtml}
            ${actions}
        </div>`;
}

(window as any).confirmRecord = async function(recordId: number) {
    const comment = prompt("Confirmation comment (optional):");
    try {
        await api("POST", `/api/doctors/records/${recordId}/confirm`, { comment: comment || "" });
        alert("Record confirmed!");
        const tabContent = document.getElementById("tab-content");
        if (tabContent) {
            const backBtn = tabContent.querySelector("[onclick*='navigate']");
            if (backBtn) (backBtn as HTMLElement).click();
        }
    } catch (e: any) {
        alert(e.message);
    }
};

(window as any).showIcd10Modal = function(recordId: number) {
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
    modal.addEventListener("click", (e) => { if (e.target === modal) modal.remove(); });
    modal.querySelector("#icd10-cancel")!.addEventListener("click", () => modal.remove());

    const doSearch = async () => {
        const q = (modal.querySelector("#icd10-search-input") as HTMLInputElement).value;
        if (!q) return;
        try {
            const codes = await api("GET", `/api/doctors/icd10/search?q=${encodeURIComponent(q)}`);
            const results = modal.querySelector("#icd10-results")!;
            if (codes.length === 0) {
                results.innerHTML = `<p style="color:var(--gray-500)">No codes found.</p>`;
                return;
            }
            results.innerHTML = codes.map((c: any) => `
                <div class="doctor-card" style="cursor:pointer" onclick="addIcd10ToRecord(${recordId}, ${c.id}, this)">
                    <div class="doctor-info">
                        <h4>${c.code}</h4>
                        <p>${c.description}</p>
                    </div>
                </div>`).join("");
        } catch (e: any) {
            alert(e.message);
        }
    };

    modal.querySelector("#btn-icd10-search")!.addEventListener("click", doSearch);
    modal.querySelector("#icd10-search-input")!.addEventListener("keydown", (e: Event) => {
        if ((e as KeyboardEvent).key === "Enter") doSearch();
    });
};

(window as any).addIcd10ToRecord = async function(recordId: number, icd10Id: number, el: HTMLElement) {
    try {
        await api("POST", `/api/doctors/records/${recordId}/icd10`, { icd10CodeId: icd10Id });
        el.closest(".modal-overlay")?.remove();
        alert("ICD-10 code added!");
    } catch (e: any) {
        alert(e.message);
    }
};

(window as any).removeIcd10 = async function(recordId: number, icd10Id: number) {
    if (!confirm("Remove this ICD-10 code?")) return;
    try {
        await api("DELETE", `/api/doctors/records/${recordId}/icd10/${icd10Id}`);
        alert("ICD-10 code removed!");
    } catch (e: any) {
        alert(e.message);
    }
};

async function renderIcd10Search() {
    const content = document.getElementById("tab-content")!;
    content.innerHTML = `
        <h2>ICD-10 Code Search</h2>
        <div class="search-box">
            <input type="text" id="icd10-main-search" placeholder="Search by code or description...">
            <button class="btn btn-primary" id="btn-icd10-main-search">Search</button>
        </div>
        <div id="icd10-main-results"></div>`;

    const doSearch = async () => {
        const q = (document.getElementById("icd10-main-search") as HTMLInputElement).value;
        if (!q) return;
        try {
            const codes = await api("GET", `/api/doctors/icd10/search?q=${encodeURIComponent(q)}`);
            const results = document.getElementById("icd10-main-results")!;
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
        } catch (e: any) {
            document.getElementById("icd10-main-results")!.innerHTML =
                `<div class="alert alert-error">${e.message}</div>`;
        }
    };

    document.getElementById("btn-icd10-main-search")!.addEventListener("click", doSearch);
    document.getElementById("icd10-main-search")!.addEventListener("keydown", (e) => {
        if (e.key === "Enter") doSearch();
    });
}
