/**
 * DESTINY OWNERS - Módulo de Telemetría, Leaderboard Oficial y Perfil Detallado (liveMatch.ts)
 * Sincroniza datos en tiempo real del servidor Minecraft vía Firebase Realtime Database
 * Endpoint Live: https://destinyowners-23-default-rtdb.firebaseio.com/live.json
 * Endpoint Leaderboard: https://destinyowners-23-default-rtdb.firebaseio.com/leaderboard.json
 */

import { showRightShowcase } from './navigation';
import { showToast } from './clipboard';

export interface LivePlayer {
  name: string;
  uuid: string;
  health: number;
  ping: number;
  team: 'Red' | 'Blue' | 'LOBBY' | string;
  isLeader?: boolean;
}

export interface LiveMatchData {
  platform?: string;
  gameMode?: string;
  version?: string;
  status?: 'STOPPED' | 'PREPARATION' | 'RUNNING' | 'ENDED' | 'LOBBY' | string;
  formattedTime?: string;
  score?: {
    red: number;
    blue: number;
  };
  players?: LivePlayer[];
  killfeed?: Array<{
    type?: string;
    timestamp?: string;
    killer?: string;
    victim?: string;
    cause?: string;
    distance?: number;
    scorer?: string;
    team?: string;
    score?: string;
  }>;
  annualMatches?: number;
}

export interface PlayerStatsData {
  name: string;
  kills?: number;
  deaths?: number;
  goals?: number;
  wins?: number;
}

export interface DetailedPlayerProfile {
  name: string;
  uuid?: string;
  rank: number;
  points: number;
  goals: number;
  kills: number;
  deaths: number;
  kdRatio: string;
  wins: number;
  isStaff?: boolean;
  roleTitle: string;
  isOnline: boolean;
  ping?: number;
}

const FIREBASE_RTDB_BASE = 'https://destinyowners-23-default-rtdb.firebaseio.com';
let syncInterval: any = null;
let lastLiveState: LiveMatchData | null = null;
let lastLeaderboardMap: Record<string, PlayerStatsData> = {};
let currentSearchFilter: string = '';
let currentlyInspectedName: string | null = null;

/**
 * Inicia la sincronización periódica con Firebase Realtime Database
 */
export function startLiveSync(): void {
  // Ejecutar primera consulta inmediatamente
  fetchLiveState();
  fetchLeaderboardState();

  if (syncInterval) clearInterval(syncInterval);
  syncInterval = setInterval(() => {
    fetchLiveState();
    fetchLeaderboardState();
  }, 2500);
}

/**
 * Consulta el estado en vivo de la partida
 */
async function fetchLiveState(): Promise<void> {
  try {
    const res = await fetch(`${FIREBASE_RTDB_BASE}/live.json`, { cache: 'no-store' });
    if (!res.ok) return;
    const data = await res.json();
    if (!data || typeof data !== 'object') return;

    if (data.status && typeof data.status === 'string') {
      lastLiveState = data as LiveMatchData;
      renderLiveMatchUI(lastLiveState);
    }
  } catch (err) {
    // Manejo silencioso ante desconexión temporal
  }
}

/**
 * Consulta la tabla clasificatoria real de Firebase
 */
async function fetchLeaderboardState(): Promise<void> {
  try {
    const res = await fetch(`${FIREBASE_RTDB_BASE}/leaderboard.json`, { cache: 'no-store' });
    if (!res.ok) return;
    const data = await res.json();
    if (data && typeof data === 'object') {
      lastLeaderboardMap = data as Record<string, PlayerStatsData>;
      syncLeaderboardWithLivePlayers();
      renderRealLeaderboard();
    }
  } catch (err) {
    // Manejo silencioso ante desconexión temporal
  }
}

/**
 * Sincroniza y fusiona los jugadores conectados con la tabla de clasificación
 */
function syncLeaderboardWithLivePlayers(): void {
  if (!lastLiveState || !lastLiveState.players || !Array.isArray(lastLiveState.players)) return;

  for (const p of lastLiveState.players) {
    if (!p.name || p.name === 'Vacío/Entorno' || p.name === 'Unknown') continue;
    const existingKey = Object.keys(lastLeaderboardMap).find(k => {
      const s = lastLeaderboardMap[k];
      return s && s.name && s.name.toLowerCase() === p.name.toLowerCase();
    });

    if (!existingKey) {
      const key = p.uuid || p.name;
      lastLeaderboardMap[key] = {
        name: p.name,
        kills: 0,
        deaths: 0,
        goals: 0,
        wins: 0
      };
    }
  }
}

/**
 * Filtra el Leaderboard por término de búsqueda en tiempo real
 */
export function filterLeaderboard(query: string): void {
  currentSearchFilter = (query || '').trim().toLowerCase();
  renderRealLeaderboard();
}

/**
 * Renderiza el Leaderboard con todos los jugadores auténticos
 */
export function renderRealLeaderboard(): void {
  const container = document.getElementById('leaderboard-list-container');
  if (!container) return;

  const playerList: Array<DetailedPlayerProfile> = [];

  for (const [uuid, stats] of Object.entries(lastLeaderboardMap)) {
    if (!stats || !stats.name || stats.name === 'Unknown') continue;
    const goals = stats.goals || 0;
    const wins = stats.wins || 0;
    const kills = stats.kills || 0;
    const deaths = stats.deaths || 0;

    // Fórmula oficial ponderada de puntos en The Towers
    const rawPoints = (goals * 150) + (wins * 100) + (kills * 15) - (deaths * 2);
    const points = Math.max(0, rawPoints);
    const kdRatio = (kills / Math.max(1, deaths)).toFixed(2);

    const isOnline = checkPlayerOnline(stats.name);
    const isStaff = stats.name.toLowerCase() === 'elglower';

    playerList.push({
      name: stats.name,
      uuid,
      rank: 0,
      points,
      goals,
      kills,
      deaths,
      kdRatio,
      wins,
      isStaff,
      roleTitle: isStaff ? 'Destiny Admin' : 'Jugador Oficial',
      isOnline
    });
  }

  // Ordenar por puntos desc, luego goles desc, luego kills desc, luego menor muertes
  playerList.sort((a, b) => 
    b.points - a.points || 
    b.goals - a.goals || 
    b.kills - a.kills || 
    a.deaths - b.deaths
  );

  // Asignar rangos oficiales
  playerList.forEach((p, idx) => {
    p.rank = idx + 1;
  });

  // Actualizar badge contador total
  const countBadge = document.getElementById('leaderboard-badge-count');
  if (countBadge) {
    countBadge.textContent = String(playerList.length);
  }

  // Aplicar filtro de búsqueda si existe
  const displayList = currentSearchFilter
    ? playerList.filter(p => p.name.toLowerCase().includes(currentSearchFilter))
    : playerList;

  if (displayList.length === 0) {
    container.innerHTML = `
      <div class="p-8 rounded-2xl bg-white/40 dark:bg-white/5 border border-pastel-cardBorder/40 text-center flex flex-col items-center gap-2 text-pastel-plum">
        <i class="fa-solid fa-user-xmark text-2xl text-pastel-plum/40 mb-1"></i>
        <p class="text-xs font-bold">No se encontraron jugadores que coincidan con "${currentSearchFilter}".</p>
        <p class="text-[11px] text-pastel-plum/60">Verifica el nombre o limpia el buscador.</p>
      </div>
    `;
    return;
  }

  // Renderizar filas
  container.innerHTML = displayList.map(p => {
    const isSelected = currentlyInspectedName 
      ? p.name.toLowerCase() === currentlyInspectedName.toLowerCase()
      : p.rank === 1;

    const rankBadgeClass = p.rank === 1
      ? 'bg-amber-500 text-white font-black shadow-sm'
      : p.rank === 2
      ? 'bg-slate-300 text-slate-800 font-black'
      : p.rank === 3
      ? 'bg-amber-700 text-white font-black'
      : 'bg-pastel-periwinkle/40 text-pastel-plum font-bold';

    const avatarUrl = `https://mc-heads.net/avatar/${p.name}/36`;

    return `
      <div 
        data-username="${p.name}" 
        class="leaderboard-row ${isSelected ? 'active-player' : ''} p-3 sm:p-3.5 rounded-2xl flex items-center justify-between gap-3 cursor-pointer transition-all border border-transparent hover:border-pastel-cardBorder/60"
        onclick="window.destinyApp.inspectMinecraftPlayer('${p.name}', this)"
      >
        <div class="flex items-center gap-3.5 min-w-0">
          <span class="w-7 h-7 sm:w-8 sm:h-8 rounded-full ${rankBadgeClass} border border-pastel-cardBorder flex items-center justify-center text-xs flex-shrink-0">
            #${p.rank}
          </span>
          <img 
            src="${avatarUrl}" 
            alt="${p.name}" 
            class="w-7 h-7 sm:w-8 sm:h-8 rounded-lg shadow-sm flex-shrink-0"
            onerror="this.src='https://minotar.net/avatar/${p.name}/36'"
          />
          <div class="min-w-0">
            <div class="text-xs sm:text-sm font-bold text-pastel-plum flex items-center gap-1.5 truncate">
              <span>${p.name}</span>
              ${p.rank === 1 ? '<i class="fa-solid fa-crown text-[11px] text-amber-500 flex-shrink-0" title="Líder del Ranking"></i>' : ''}
              ${p.isStaff ? '<span class="text-[9px] px-1.5 py-0.2 rounded bg-purple-600 text-white font-black">ADMIN</span>' : ''}
              ${p.isOnline ? '<span class="w-1.5 h-1.5 rounded-full bg-emerald-500 inline-block flex-shrink-0" title="En Línea"></span>' : ''}
            </div>
            <div class="text-[10px] sm:text-[11px] text-pastel-plum/60 font-medium truncate sm:hidden">
              ⚽ ${p.goals} • ⚔ ${p.kills} • 💀 ${p.deaths}
            </div>
          </div>
        </div>

        <div class="hidden sm:flex items-center gap-4 text-xs font-semibold text-pastel-plum/75">
          <span class="flex items-center gap-1 text-amber-600 dark:text-amber-400" title="Goles en The Towers">
            <i class="fa-solid fa-trophy text-[10px]"></i> ${p.goals}
          </span>
          <span class="flex items-center gap-1 text-rose-500" title="Asesinatos">
            <i class="fa-solid fa-crosshairs text-[10px]"></i> ${p.kills}
          </span>
          <span class="flex items-center gap-1 text-slate-500" title="Muertes">
            <i class="fa-solid fa-skull text-[10px]"></i> ${p.deaths}
          </span>
        </div>

        <div class="flex items-center gap-3 flex-shrink-0">
          <span class="text-xs sm:text-sm font-extrabold text-pastel-denim font-display">${p.points} pts</span>
          <i class="fa-solid fa-chevron-right text-[10px] text-pastel-plum/30"></i>
        </div>
      </div>
    `;
  }).join('');

  // Si no hay ningún jugador inspeccionado activamente, cargar el primero
  if (!currentlyInspectedName && displayList.length > 0) {
    renderDetailedUserProfile(displayList[0]);
  } else if (currentlyInspectedName) {
    const current = playerList.find(p => p.name.toLowerCase() === currentlyInspectedName?.toLowerCase());
    if (current) {
      renderDetailedUserProfile(current);
    }
  }
}

/**
 * Comprueba si un jugador está en línea actualmente en el servidor
 */
function checkPlayerOnline(username: string): boolean {
  if (!lastLiveState || !lastLiveState.players) return false;
  return lastLiveState.players.some(p => p.name && p.name.toLowerCase() === username.toLowerCase());
}

/**
 * Inspecciona un jugador de Minecraft por su nombre de usuario,
 * actualizando el perfil detallado y resaltando la fila.
 */
export function inspectMinecraftPlayer(username: string, element?: HTMLElement): void {
  if (!username || username === 'Vacío/Entorno' || username === 'Unknown') return;

  currentlyInspectedName = username;

  // 1. Resaltar fila seleccionada en el leaderboard
  document.querySelectorAll('.leaderboard-row').forEach(row => {
    row.classList.remove('active-player');
  });
  if (element) {
    element.classList.add('active-player');
  } else {
    const targetRow = document.querySelector(`.leaderboard-row[data-username="${username}"]`);
    if (targetRow) targetRow.classList.add('active-player');
  }

  // 2. Buscar datos del jugador
  const statsEntry = Object.values(lastLeaderboardMap).find(s => s && s.name && s.name.toLowerCase() === username.toLowerCase());
  const goals = statsEntry?.goals || 0;
  const wins = statsEntry?.wins || 0;
  const kills = statsEntry?.kills || 0;
  const deaths = statsEntry?.deaths || 0;
  const rawPoints = (goals * 150) + (wins * 100) + (kills * 15) - (deaths * 2);
  const points = Math.max(0, rawPoints);
  const kdRatio = (kills / Math.max(1, deaths)).toFixed(2);

  // Calcular ranking
  let rank = 1;
  const allEntries = Object.values(lastLeaderboardMap).filter(s => s && s.name);
  for (const other of allEntries) {
    const otherPoints = Math.max(0, ((other.goals || 0) * 150) + ((other.wins || 0) * 100) + ((other.kills || 0) * 15) - ((other.deaths || 0) * 2));
    if (otherPoints > points) rank++;
  }

  const isOnline = checkPlayerOnline(username);
  const isStaff = username.toLowerCase() === 'elglower';

  const profile: DetailedPlayerProfile = {
    name: username,
    rank,
    points,
    goals,
    kills,
    deaths,
    kdRatio,
    wins,
    isStaff,
    roleTitle: isStaff ? 'Destiny Admin' : 'Jugador Oficial',
    isOnline
  };

  renderDetailedUserProfile(profile);
  showRightShowcase();
}

/**
 * Renderiza la tarjeta de perfil detallado de usuario en el panel derecho
 */
export function renderDetailedUserProfile(p: DetailedPlayerProfile): void {
  currentlyInspectedName = p.name;
  const container = document.getElementById('player-profile-detail-container');
  if (!container) return;

  // Actualizar Notch
  const badge = document.getElementById('notch-avatar-badge');
  const titleEl = document.getElementById('notch-player-title');
  const iconEl = document.getElementById('notch-player-icon');
  if (badge) badge.textContent = p.name.substring(0, 2).toUpperCase();
  if (titleEl) titleEl.textContent = p.name;
  if (iconEl) iconEl.className = 'fa-solid fa-circle-check text-[11px] text-pastel-denim';

  const avatarUrl = `https://mc-heads.net/avatar/${p.name}/80`;
  const bustUrl = `https://minotar.net/armor/bust/${p.name}/120.png`;

  const rankBadgeClass = p.rank === 1
    ? 'bg-amber-500 text-white shadow-amber-500/20'
    : p.rank === 2
    ? 'bg-slate-300 text-slate-800'
    : p.rank === 3
    ? 'bg-amber-700 text-white'
    : 'bg-pastel-periwinkle/50 text-pastel-plum';

  const roleBadge = p.isStaff
    ? '<span class="text-[10px] font-black px-2.5 py-0.5 rounded-md bg-purple-600 text-white tracking-wider shadow-sm">✦ DESTINY ADMIN</span>'
    : p.rank <= 3
    ? '<span class="text-[10px] font-black px-2 py-0.5 rounded-md bg-amber-500/20 text-amber-800 dark:text-amber-200 border border-amber-500/30">★ TOP JUGADOR</span>'
    : '<span class="text-[10px] font-semibold px-2 py-0.5 rounded-md bg-pastel-periwinkle/30 text-pastel-plum border border-pastel-cardBorder">JUGADOR DESTINY</span>';

  const statusHtml = p.isOnline
    ? '<span class="inline-flex items-center gap-1.5 text-[11px] font-bold text-emerald-600 dark:text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded-md border border-emerald-500/20"><span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>En Línea</span>'
    : '<span class="inline-flex items-center gap-1.5 text-[11px] font-medium text-pastel-plum/60 bg-slate-500/10 px-2 py-0.5 rounded-md"><span class="w-2 h-2 rounded-full bg-slate-400"></span>Desconectado</span>';

  // Proporción de objetivos (goles vs kills)
  const totalObj = (p.goals || 0) + (p.kills || 0);
  const goalPct = totalObj > 0 ? Math.round(((p.goals || 0) / totalObj) * 100) : 50;

  container.innerHTML = `
    <!-- Cabecera del Usuario con Render Oficial -->
    <div class="flex flex-col items-center text-center gap-2.5">
      <div class="relative group">
        <div class="w-24 h-24 rounded-3xl bg-pastel-periwinkle/25 dark:bg-white/10 border-2 border-pastel-cardBorder flex items-center justify-center p-2 shadow-inner overflow-hidden">
          <img 
            src="${bustUrl}" 
            alt="${p.name}" 
            class="w-20 h-20 object-contain drop-shadow-md hover:scale-105 transition-transform"
            onerror="this.src='${avatarUrl}'"
          />
        </div>
        <span class="absolute -bottom-2.5 left-1/2 -translate-x-1/2 px-2.5 py-0.5 rounded-full text-[10px] font-black tracking-wider shadow-sm uppercase whitespace-nowrap ${rankBadgeClass}">
          #${p.rank} RANKING
        </span>
      </div>

      <div class="mt-2.5">
        <div class="flex items-center justify-center gap-2">
          <h3 class="text-2xl font-extrabold text-pastel-plum tracking-tight font-display">${p.name}</h3>
          <i class="fa-solid fa-circle-check text-sm text-pastel-denim" title="Cuenta Verificada"></i>
        </div>
        <div class="flex flex-wrap items-center justify-center gap-2 mt-1.5">
          ${roleBadge}
          ${statusHtml}
        </div>
      </div>
    </div>

    <!-- Grid de Métricas Principales (6 Tarjetas) -->
    <div class="grid grid-cols-3 gap-2 pt-1">
      <div class="p-2.5 rounded-2xl bg-white/60 dark:bg-white/5 border border-pastel-cardBorder/60 flex flex-col items-center text-center shadow-sm">
        <span class="text-[9px] font-bold text-pastel-plum/60 uppercase tracking-wider">Puntuación</span>
        <span class="text-base font-black text-pastel-denim font-display">${p.points}</span>
      </div>

      <div class="p-2.5 rounded-2xl bg-white/60 dark:bg-white/5 border border-pastel-cardBorder/60 flex flex-col items-center text-center shadow-sm">
        <span class="text-[9px] font-bold text-pastel-plum/60 uppercase tracking-wider">Goles</span>
        <span class="text-base font-black text-amber-500 font-display">${p.goals}</span>
      </div>

      <div class="p-2.5 rounded-2xl bg-white/60 dark:bg-white/5 border border-pastel-cardBorder/60 flex flex-col items-center text-center shadow-sm">
        <span class="text-[9px] font-bold text-pastel-plum/60 uppercase tracking-wider">Asesinatos</span>
        <span class="text-base font-black text-rose-500 font-display">${p.kills}</span>
      </div>

      <div class="p-2.5 rounded-2xl bg-white/60 dark:bg-white/5 border border-pastel-cardBorder/60 flex flex-col items-center text-center shadow-sm">
        <span class="text-[9px] font-bold text-pastel-plum/60 uppercase tracking-wider">Muertes</span>
        <span class="text-base font-black text-slate-500 font-display">${p.deaths}</span>
      </div>

      <div class="p-2.5 rounded-2xl bg-white/60 dark:bg-white/5 border border-pastel-cardBorder/60 flex flex-col items-center text-center shadow-sm">
        <span class="text-[9px] font-bold text-pastel-plum/60 uppercase tracking-wider">K/D Ratio</span>
        <span class="text-base font-black text-pastel-plum font-display">${p.kdRatio}</span>
      </div>

      <div class="p-2.5 rounded-2xl bg-white/60 dark:bg-white/5 border border-pastel-cardBorder/60 flex flex-col items-center text-center shadow-sm">
        <span class="text-[9px] font-bold text-pastel-plum/60 uppercase tracking-wider">Victorias</span>
        <span class="text-base font-black text-emerald-500 font-display">${p.wins}</span>
      </div>
    </div>

    <!-- Barra de Efectividad de Objetivos -->
    <div class="p-3 rounded-2xl bg-white/40 dark:bg-white/5 border border-pastel-cardBorder/40 flex flex-col gap-1.5 shadow-sm">
      <div class="flex items-center justify-between text-[10px] font-bold text-pastel-plum/70">
        <span>Enfoque en Arena</span>
        <span>${goalPct}% Goles</span>
      </div>
      <div class="w-full h-1.5 rounded-full bg-pastel-cardBorder/50 overflow-hidden flex">
        <div class="bg-amber-500 h-full transition-all duration-300" style="width: ${goalPct}%" title="Goles: ${p.goals}"></div>
        <div class="bg-rose-400 h-full transition-all duration-300" style="width: ${100 - goalPct}%" title="Bajas: ${p.kills}"></div>
      </div>
      <div class="flex items-center justify-between text-[9px] text-pastel-plum/50 font-medium">
        <span>⚽ ${p.goals} Goles</span>
        <span>⚔ ${p.kills} Bajas</span>
      </div>
    </div>

    <!-- Acciones Rápidas -->
    <div class="flex items-center gap-2 pt-1">
      <a 
        href="https://namemc.com/profile/${p.name}" 
        target="_blank" 
        rel="noopener noreferrer" 
        class="flex-1 py-2 px-3 rounded-xl bg-pastel-denim hover:bg-pastel-denim/90 text-white text-xs font-bold text-center transition-all flex items-center justify-center gap-1.5 shadow-sm"
      >
        <i class="fa-solid fa-arrow-up-right-from-square text-[10px]"></i>
        <span>Ver en NameMC</span>
      </a>
      <button 
        onclick="window.destinyApp.copyCurrentPlayerName('${p.name}')" 
        class="py-2 px-3.5 rounded-xl bg-white/80 dark:bg-white/10 hover:bg-white text-pastel-plum text-xs font-bold border border-pastel-cardBorder transition-all flex items-center justify-center gap-1.5 shadow-sm"
        title="Copiar Nickname"
      >
        <i class="fa-regular fa-copy text-xs"></i>
        <span>Copiar</span>
      </button>
    </div>
  `;
}

/**
 * Copia el nombre del jugador actual inspeccionado
 */
export function copyCurrentPlayerName(name?: string): void {
  const targetName = name || currentlyInspectedName;
  if (!targetName) return;
  navigator.clipboard.writeText(targetName).then(() => {
    showToast(`¡Nombre '${targetName}' copiado!`);
  }).catch(() => {
    showToast(`¡${targetName}!`);
  });
}

/**
 * Abre el perfil NameMC del jugador actual inspeccionado
 */
export function openNameMCProfile(name?: string): void {
  const targetName = name || currentlyInspectedName;
  if (!targetName) return;
  window.open(`https://namemc.com/profile/${targetName}`, '_blank');
}

/**
 * Renderiza el estado de la partida y equipos en la UI (Matchboard)
 */
function renderLiveMatchUI(data: LiveMatchData): void {
  const allPlayers = data.players || [];

  // Actualizar badge en la cabecera si existe
  const headerStatus = document.getElementById('header-server-status');
  if (headerStatus) {
    headerStatus.innerHTML = `<span class="w-2.5 h-2.5 rounded-full bg-[#48BB78] animate-pulse"></span><span>En Línea (${allPlayers.length}/67)</span>`;
  }

  // 1. Badge de Estado de la Partida
  const statusBadge = document.getElementById('live-match-status-badge');
  const statusText = document.getElementById('live-status-text');
  const timerText = document.getElementById('live-timer-text');

  if (statusText && statusBadge) {
    const status = data.status || 'STOPPED';
    switch (status) {
      case 'RUNNING':
      case 'INGAME':
        statusBadge.className = 'flex items-center gap-2 bg-[#E7F5ED]/90 border border-[#A8DAC0] px-3 py-1.5 rounded-full text-xs font-bold text-[#2A6E4F] shadow-sm';
        statusText.innerHTML = '<span class="w-2.5 h-2.5 rounded-full bg-[#48BB78] animate-pulse inline-block mr-1.5"></span>En Curso';
        break;
      case 'PREPARATION':
        statusBadge.className = 'flex items-center gap-2 bg-[#FEF3C7]/90 border border-[#FCD34D] px-3 py-1.5 rounded-full text-xs font-bold text-[#92400E] shadow-sm';
        statusText.innerHTML = '<span class="w-2.5 h-2.5 rounded-full bg-[#F59E0B] animate-pulse inline-block mr-1.5"></span>Preparación';
        break;
      case 'LOBBY':
        statusBadge.className = 'flex items-center gap-2 bg-[#E0F2FE]/90 border border-[#7DD3FC] px-3 py-1.5 rounded-full text-xs font-bold text-[#0369A1] shadow-sm';
        statusText.innerHTML = `<span class="w-2.5 h-2.5 rounded-full bg-[#0284C7] animate-pulse inline-block mr-1.5"></span>Lobby (${allPlayers.length} Conectados)`;
        break;
      case 'ENDED':
        statusBadge.className = 'flex items-center gap-2 bg-[#EDE9FE]/90 border border-[#C4B5FD] px-3 py-1.5 rounded-full text-xs font-bold text-[#5B21B6] shadow-sm';
        statusText.innerHTML = '<span class="w-2.5 h-2.5 rounded-full bg-[#8B5CF6] inline-block mr-1.5"></span>Partida Finalizada';
        break;
      default:
        statusBadge.className = 'flex items-center gap-2 bg-slate-100/90 border border-slate-300 px-3 py-1.5 rounded-full text-xs font-bold text-slate-700 shadow-sm';
        statusText.innerHTML = '<span class="w-2.5 h-2.5 rounded-full bg-slate-400 inline-block mr-1.5"></span>En Espera';
        break;
    }
  }

  // Temporizador
  if (timerText) {
    timerText.textContent = data.formattedTime || '--:--';
  }

  // 2. Marcador Red vs Blue
  const redScoreEl = document.getElementById('live-red-score');
  const blueScoreEl = document.getElementById('live-blue-score');
  if (redScoreEl && data.score) {
    redScoreEl.textContent = `${data.score.red || 0} pts`;
  }
  if (blueScoreEl && data.score) {
    blueScoreEl.textContent = `${data.score.blue || 0} pts`;
  }

  // 3. Filtrar jugadores por equipo
  const redPlayers = allPlayers.filter(p => p.team === 'Red');
  const bluePlayers = allPlayers.filter(p => p.team === 'Blue');
  const lobbyPlayers = allPlayers.filter(p => p.team === 'LOBBY' || (!p.team || (p.team !== 'Red' && p.team !== 'Blue')));

  // Contadores
  const redCountEl = document.getElementById('live-red-count');
  const blueCountEl = document.getElementById('live-blue-count');
  if (redCountEl) redCountEl.textContent = `${redPlayers.length} / 4`;
  if (blueCountEl) blueCountEl.textContent = `${bluePlayers.length} / 4`;

  // Renderizar jugadores de Red
  const redContainer = document.getElementById('live-red-players');
  if (redContainer) {
    if (redPlayers.length > 0) {
      redContainer.innerHTML = redPlayers.map(p => createPlayerCardHtml(p, 'red')).join('');
    } else if (lobbyPlayers.length > 0) {
      const half = Math.ceil(lobbyPlayers.length / 2);
      const redHalf = lobbyPlayers.slice(0, half);
      redContainer.innerHTML = `
        <div class="text-[10px] text-pastel-plum/60 font-semibold mb-1 uppercase tracking-wider">Esperando en Lobby:</div>
        ${redHalf.map(p => createPlayerCardHtml(p, 'red')).join('')}
      `;
    } else {
      redContainer.innerHTML = `
        <div class="py-4 flex flex-col items-center justify-center text-center gap-2">
          <i class="fa-solid fa-users text-2xl text-[#E79796]/50"></i>
          <p class="text-xs font-semibold text-pastel-plum/70">Esperando jugadores rojos...</p>
        </div>
      `;
    }
  }

  // Renderizar jugadores de Blue
  const blueContainer = document.getElementById('live-blue-players');
  if (blueContainer) {
    if (bluePlayers.length > 0) {
      blueContainer.innerHTML = bluePlayers.map(p => createPlayerCardHtml(p, 'blue')).join('');
    } else if (lobbyPlayers.length > 0) {
      const half = Math.ceil(lobbyPlayers.length / 2);
      const blueHalf = lobbyPlayers.slice(half);
      if (blueHalf.length > 0) {
        blueContainer.innerHTML = `
          <div class="text-[10px] text-pastel-plum/60 font-semibold mb-1 uppercase tracking-wider">Esperando en Lobby:</div>
          ${blueHalf.map(p => createPlayerCardHtml(p, 'blue')).join('')}
        `;
      } else {
        blueContainer.innerHTML = `
          <div class="py-4 flex flex-col items-center justify-center text-center gap-2">
            <i class="fa-solid fa-users text-2xl text-pastel-denim/50"></i>
            <p class="text-xs font-semibold text-pastel-plum/70">Esperando selección de equipo...</p>
          </div>
        `;
      }
    } else {
      blueContainer.innerHTML = `
        <div class="py-4 flex flex-col items-center justify-center text-center gap-2">
          <i class="fa-solid fa-users text-2xl text-pastel-denim/50"></i>
          <p class="text-xs font-semibold text-pastel-plum/70">Esperando jugadores azules...</p>
        </div>
      `;
    }
  }

  // 4. Killfeed / Registro de Eventos
  const killfeedContainer = document.getElementById('live-killfeed-container');
  const killfeedCount = document.getElementById('live-killfeed-count');
  const events = data.killfeed || [];

  if (killfeedCount) {
    killfeedCount.textContent = `${events.length} eventos`;
  }

  if (killfeedContainer) {
    if (events.length === 0) {
      killfeedContainer.innerHTML = `
        <div class="text-xs text-pastel-plum/60 py-2 text-center italic">
          Sin eventos recientes en la arena.
        </div>
      `;
    } else {
      killfeedContainer.innerHTML = events.slice(0, 15).map(ev => {
        if (ev.type === 'GOAL') {
          return `
            <div class="flex items-center justify-between text-xs py-1.5 px-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-pastel-plum">
              <span class="flex items-center gap-2 font-bold">
                <i class="fa-solid fa-star text-amber-500"></i>
                <span class="text-amber-700 dark:text-amber-300">${ev.scorer}</span> anotó para <span class="${ev.team === 'Red' ? 'text-[#D9534F]' : 'text-pastel-denim'} font-black">${ev.team}</span>!
              </span>
              <span class="text-[11px] font-black px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-800 dark:text-amber-200">${ev.score || ''}</span>
            </div>
          `;
        }
        return `
          <div class="flex items-center justify-between text-xs py-1.5 px-3 rounded-xl bg-white/40 dark:bg-white/5 border border-pastel-cardBorder/40 text-pastel-plum">
            <span class="flex items-center gap-2">
              <i class="fa-solid fa-crosshairs text-red-400"></i>
              <strong class="font-bold cursor-pointer hover:underline" onclick="window.destinyApp.inspectMinecraftPlayer('${ev.killer}')">${ev.killer}</strong>
              <span class="text-[11px] text-pastel-plum/60">derrotó a</span>
              <strong class="font-bold cursor-pointer hover:underline" onclick="window.destinyApp.inspectMinecraftPlayer('${ev.victim}')">${ev.victim}</strong>
            </span>
            <span class="text-[10px] text-pastel-plum/50 font-medium">${ev.cause || 'Batalla'}</span>
          </div>
        `;
      }).join('');
    }
  }
}

/**
 * Crea la tarjeta HTML de un jugador en vivo para el tablero de equipos
 */
function createPlayerCardHtml(p: LivePlayer, teamColor: 'red' | 'blue'): string {
  const avatarUrl = `https://mc-heads.net/avatar/${p.name}/32`;
  const isRed = teamColor === 'red';
  const borderTone = isRed ? 'border-[#E79796]/30 hover:border-[#E79796]' : 'border-pastel-cornflower/30 hover:border-pastel-denim';
  const bgTone = isRed ? 'bg-white/70 dark:bg-red-950/20' : 'bg-white/70 dark:bg-blue-950/20';

  return `
    <div 
      onclick="window.destinyApp.inspectMinecraftPlayer('${p.name}')"
      class="flex items-center justify-between p-2.5 rounded-2xl ${bgTone} border ${borderTone} transition-all cursor-pointer shadow-sm hover:scale-[1.01]"
    >
      <div class="flex items-center gap-3">
        <img 
          src="${avatarUrl}" 
          alt="${p.name}" 
          class="w-7 h-7 rounded-lg shadow-sm"
          onerror="this.src='https://minotar.net/avatar/${p.name}/32'"
        />
        <div>
          <div class="flex items-center gap-1.5">
            <span class="text-xs font-bold text-pastel-plum">${p.name}</span>
            ${p.isLeader ? '<span class="text-[9px] px-1.5 py-0.2 rounded-md bg-amber-500 text-white font-black">LÍDER</span>' : ''}
          </div>
          <div class="text-[10px] text-pastel-plum/60 flex items-center gap-2">
            <span>❤️ ${p.health} HP</span>
            <span>📶 ${p.ping}ms</span>
          </div>
        </div>
      </div>
      <div class="text-[11px] font-bold text-pastel-denim flex items-center gap-1">
        <i class="fa-solid fa-magnifying-glass text-[10px]"></i>
      </div>
    </div>
  `;
}
