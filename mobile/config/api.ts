const urlConfigurada = process.env.EXPO_PUBLIC_API_URL;

if (!urlConfigurada) {
  throw new Error(
    "EXPO_PUBLIC_API_URL não foi configurada. Crie mobile/.env.local usando .env.example como modelo."
  );
}

export const API_URL = urlConfigurada.replace(/\/$/, "");
