import { PlayerRecord } from '../types';

/**
 * Registros de Clasificación Oficial y Pruebas Técnicas de Pre-Temporada
 * Utiliza únicamente identidades verificadas del equipo y colaboradores reales.
 */
export const OFFICIAL_LEADERBOARD: PlayerRecord[] = [
  {
    rank: 1,
    username: 'ElGlower',
    roleTitle: 'Fundador • Lead Dev',
    points: 3450,
    wins: 142,
    towersDestroyed: 398,
    kdRatio: 4.12,
    badge: 'Oficial',
    isStaff: true
  },
  {
    rank: 2,
    username: 'StartCes',
    roleTitle: 'Core Developer',
    points: 2980,
    wins: 118,
    towersDestroyed: 320,
    kdRatio: 3.45,
    badge: 'Desarrollo',
    isStaff: true
  },
  {
    rank: 3,
    username: 'Ripkyng1',
    roleTitle: 'Core Developer',
    points: 2640,
    wins: 96,
    towersDestroyed: 275,
    kdRatio: 3.10,
    badge: 'Desarrollo',
    isStaff: true
  }
];
