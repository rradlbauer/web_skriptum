// --- Login / Register ---

import { state, saveSession } from "./state";
import { api } from "./api";

const mainContent = document.getElementById("main-content") as HTMLElement;

export function renderLogin() {
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

    document.querySelectorAll(".login-tab").forEach(tab => {
        tab.addEventListener("click", () => {
            document.querySelectorAll(".login-tab").forEach(t => t.classList.remove("active"));
            tab.classList.add("active");
            const tabName = tab.getAttribute("data-tab");
            document.getElementById("login-form")!.classList.toggle("hidden", tabName !== "login");
            document.getElementById("register-form")!.classList.toggle("hidden", tabName !== "register");
        });
    });

    document.getElementById("btn-login")!.addEventListener("click", async () => {
        const email = (document.getElementById("login-email") as HTMLInputElement).value;
        const password = (document.getElementById("login-password") as HTMLInputElement).value;
        try {
            const data = await api("POST", "/api/auth/login", { email, password });
            saveSession(data);
            (window as any).render();
        } catch (e: any) {
            document.getElementById("login-error")!.innerHTML =
                `<div class="alert alert-error">${e.message}</div>`;
        }
    });

    document.getElementById("btn-register")!.addEventListener("click", async () => {
        const body = {
            firstName: (document.getElementById("reg-firstName") as HTMLInputElement).value,
            lastName: (document.getElementById("reg-lastName") as HTMLInputElement).value,
            email: (document.getElementById("reg-email") as HTMLInputElement).value,
            password: (document.getElementById("reg-password") as HTMLInputElement).value,
            ssn: (document.getElementById("reg-ssn") as HTMLInputElement).value,
            dateOfBirth: (document.getElementById("reg-dob") as HTMLInputElement).value,
        };
        try {
            const data = await api("POST", "/api/auth/register/patient", body);
            saveSession(data);
            (window as any).render();
        } catch (e: any) {
            document.getElementById("register-error")!.innerHTML =
                `<div class="alert alert-error">${e.message}</div>`;
        }
    });

    document.getElementById("login-password")!.addEventListener("keydown", (e) => {
        if (e.key === "Enter") document.getElementById("btn-login")!.click();
    });
}
