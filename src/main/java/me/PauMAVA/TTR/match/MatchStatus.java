package me.PauMAVA.TTR.match;

public enum MatchStatus {
    LOBBY,        // Esperando jugadores
    STARTING,     // Conteo regresivo en lobby
    PREPARATION,  // Fase de calentamiento e inspección en bases
    INGAME,       // Partida en curso (combate activo)
    STOPPED,      // Desactivado
    ENDED         // Partida terminada
}