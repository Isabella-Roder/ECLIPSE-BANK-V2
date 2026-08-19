import { API_URL } from "./api";

let tokenCsrf: string | null = null;

export function definirTokenCsrf(token:string) {
    tokenCsrf = token;
}

export async function buscarTokenCsrf() {
    const resposta = await fetch(`${API_URL}/csrf`, {
        credentials: "include"
    });

    const corpo = await resposta.json();

    definirTokenCsrf(corpo.token);
}

export function obterTokenCsrf() {
    return tokenCsrf;
}