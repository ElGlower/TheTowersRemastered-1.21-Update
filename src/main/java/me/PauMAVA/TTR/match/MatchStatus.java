package me.PauMAVA.TTR.match;

public enum MatchStatus {
    LOBBY,      // Esperando jugadores
    STARTING,   // Conteo regresivo
    INGAME,     // Partida en curso (Antes se llamaba RUNNING)
    STOPPED,    // Desactivado
    ENDED       // Partida terminada
}