/**
 * Tipos e interfaces centrales de Destiny Towers Web Companion
 */

export type TabId = 'inicio' | 'modalidad' | 'miembros';
export type ModalitySubTab = 'partida' | 'leaderboard' | 'partidas';
export type ThemeMode = 'light' | 'dark';

export interface TeamMember {
  id: string;
  name: string;
  handle: string;
  role: string;
  tag: string;
  bio: string;
  githubUrl: string;
  isLead?: boolean;
  avatarUrl: string;
  skinTexture?: string;
}

export interface PlayerRecord {
  rank: number;
  username: string;
  roleTitle: string;
  points: number;
  wins: number;
  towersDestroyed: number;
  kdRatio: number;
  badge?: string;
  isStaff?: boolean;
}

export interface ModalityInfo {
  id: string;
  name: string;
  status: 'active' | 'development';
  statusLabel: string;
  badge: string;
  description: string;
  playerCapacity: string;
  iconClass: string;
}
