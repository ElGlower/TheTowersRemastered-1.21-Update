import { PlayerRecord } from '../types';

/**
 * Registros de Clasificación
 * Mientras no haya datos del servidor/juego, se muestran slots Unknown sin datos ficticios.
 */
export const OFFICIAL_LEADERBOARD: PlayerRecord[] = [
  {
    rank: 1,
    username: 'Unknown',
    roleTitle: 'Esperando datos del juego',
    points: 0,
    wins: 0,
    towersDestroyed: 0,
    kdRatio: 0
  },
  {
    rank: 2,
    username: 'Unknown',
    roleTitle: 'Esperando datos del juego',
    points: 0,
    wins: 0,
    towersDestroyed: 0,
    kdRatio: 0
  },
  {
    rank: 3,
    username: 'Unknown',
    roleTitle: 'Esperando datos del juego',
    points: 0,
    wins: 0,
    towersDestroyed: 0,
    kdRatio: 0
  }
];
