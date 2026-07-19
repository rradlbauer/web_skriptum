// ============================================================
// Clinical Diary - Entry Point
// ============================================================

import { state, clearSession } from "./state";
import { renderLogin } from "./login";
import { navigatePatient } from "./patient";
import { navigateDoctor } from "./doctor";
import { navigateSuperuser } from "./superuser";

const mainContent = document.getElementById("main-content") as HTMLElement;
const navbar = document.getElementById("navbar") as HTMLElement;
const navUserName = document.getElementById("nav-user-name") as HTMLElement;
const navUserRole = document.getElementById("nav-user-role") as HTMLElement;
const btnLogout = document.getElementById("btn-logout") as HTMLElement;

function showNavbar() {
    navbar.classList.remove("hidden");
    navUserName.textContent = state.name || "";
    navUserRole.textContent = state.role || "";
    navUserRole.className = "badge badge-" + (state.role || "").toLowerCase();
}

function hideNavbar() {
    navbar.classList.add("hidden");
}

export function navigate(view: string) {
    state.currentView = view;
    render();
}

export function render() {
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

// Make navigate and render available globally for onclick handlers and login
(window as any).navigate = navigate;
(window as any).render = render;

btnLogout.addEventListener("click", () => {
    clearSession();
    render();
});

// --- Initialize ---
if (state.token) {
    navigate("login");
    if (state.role === "PATIENT") navigate("patient-records");
    else if (state.role === "DOCTOR") navigate("doctor-patients");
    else if (state.role === "SUPERUSER") navigate("su-doctors");
} else {
    renderLogin();
}
