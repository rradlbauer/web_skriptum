// --- Superuser Views ---

import { state } from "./state";
import { api } from "./api";
import { navigate } from "./app";

const mainContent = document.getElementById("main-content") as HTMLElement;

export async function navigateSuperuser() {
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
            navigate(btn.getAttribute("data-view")!);
        });
    });

    switch (tab) {
        case "su-add-doctor": renderAddDoctor(); break;
        case "su-stats": await renderStats(); break;
        default: await renderManageDoctors();
    }
}

async function renderManageDoctors() {
    const content = document.getElementById("tab-content")!;
    try {
        const doctors = await api("GET", "/api/superuser/doctors");
        let html = `<h2>Registered Doctors</h2>`;
        if (doctors.length === 0) {
            html += `<div class="empty-state"><p>No doctors registered yet.</p></div>`;
        } else {
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
    } catch (e: any) {
        content.innerHTML = `<div class="alert alert-error">${e.message}</div>`;
    }
}

(window as any).toggleDoctor = async function(doctorId: number, activate: boolean) {
    try {
        await api("PUT", `/api/superuser/doctors/${doctorId}/${activate ? "activate" : "deactivate"}`);
        renderManageDoctors();
    } catch (e: any) {
        alert(e.message);
    }
};

function renderAddDoctor() {
    const content = document.getElementById("tab-content")!;
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

    document.getElementById("btn-add-doctor")!.addEventListener("click", async () => {
        const body = {
            firstName: (document.getElementById("doc-firstName") as HTMLInputElement).value,
            lastName: (document.getElementById("doc-lastName") as HTMLInputElement).value,
            email: (document.getElementById("doc-email") as HTMLInputElement).value,
            password: (document.getElementById("doc-password") as HTMLInputElement).value,
            medicalLicenseNumber: (document.getElementById("doc-license") as HTMLInputElement).value,
            specialization: (document.getElementById("doc-specialization") as HTMLInputElement).value,
        };
        try {
            await api("POST", "/api/superuser/doctors", body);
            alert("Doctor registered successfully!");
            navigate("su-doctors");
        } catch (e: any) {
            document.getElementById("add-doctor-error")!.innerHTML =
                `<div class="alert alert-error">${e.message}</div>`;
        }
    });
}

async function renderStats() {
    const content = document.getElementById("tab-content")!;
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
    } catch (e: any) {
        content.innerHTML = `<div class="alert alert-error">${e.message}</div>`;
    }
}
