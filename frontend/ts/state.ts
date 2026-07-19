// --- State Management ---

export interface AppState {
    token: string | null;
    role: string | null;
    userId: number | null;
    name: string | null;
    currentView: string;
}

export const state: AppState = {
    token: localStorage.getItem("token"),
    role: localStorage.getItem("role"),
    userId: parseInt(localStorage.getItem("userId") || "0"),
    name: localStorage.getItem("name"),
    currentView: "login",
};

export function saveSession(data: { token: string; role: string; userId: number; name: string }) {
    state.token = data.token;
    state.role = data.role;
    state.userId = data.userId;
    state.name = data.name;
    localStorage.setItem("token", data.token);
    localStorage.setItem("role", data.role);
    localStorage.setItem("userId", String(data.userId));
    localStorage.setItem("name", data.name);
}

export function clearSession() {
    state.token = null;
    state.role = null;
    state.userId = null;
    state.name = null;
    localStorage.clear();
}
