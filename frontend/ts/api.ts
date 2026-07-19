// --- API Client ---

import { state } from "./state";

export async function api(method: string, url: string, body?: any): Promise<any> {
    const headers: Record<string, string> = { "Content-Type": "application/json" };
    if (state.token) {
        headers["Authorization"] = "Bearer " + state.token;
    }
    const opts: RequestInit = { method, headers };
    if (body) opts.body = JSON.stringify(body);

    const response = await fetch(url, opts);
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.error || "Request failed");
    }
    return data;
}

export async function apiUpload(url: string, file: File): Promise<any> {
    const formData = new FormData();
    formData.append("file", file);
    const headers: Record<string, string> = {};
    if (state.token) {
        headers["Authorization"] = "Bearer " + state.token;
    }
    const response = await fetch(url, { method: "POST", headers, body: formData });
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.error || "Upload failed");
    }
    return data;
}

(window as any).downloadAttachment = async function(recordId: number, attachmentId: number, filename: string) {
    try {
        const response = await fetch(`/api/patients/me/records/${recordId}/attachments/${attachmentId}`, {
            headers: { "Authorization": "Bearer " + state.token }
        });
        if (!response.ok) throw new Error("Download failed");
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
    } catch (e: any) {
        alert(e.message);
    }
};

(window as any).downloadDoctorAttachment = async function(recordId: number, attachmentId: number, filename: string) {
    try {
        const response = await fetch(`/api/doctors/records/${recordId}/attachments/${attachmentId}`, {
            headers: { "Authorization": "Bearer " + state.token }
        });
        if (!response.ok) throw new Error("Download failed");
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
    } catch (e: any) {
        alert(e.message);
    }
};
