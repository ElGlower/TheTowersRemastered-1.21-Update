/**
 * DESTINY OWNERS - Módulo de Telemetría, Leaderboard Oficial y Perfil Detallado (liveMatch.ts)
 * Sincroniza datos en tiempo real del servidor Minecraft vía Firebase Realtime Database
 * Endpoint Live: https://destinyowners-23-default-rtdb.firebaseio.com/live.json
 * Endpoint Leaderboard: https://destinyowners-23-default-rtdb.firebaseio.com/leaderboard.json
 */

import { showRightShowcase, hideRightShowcase, setRightPanelMode, getCurrentTab } from './navigation';
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

const defaultLeaderboardMap: Record<string, PlayerStatsData> = {
  'p-alepordio': { name: 'alepordio', goals: 15, wins: 11, kills: 61, deaths: 14 },
  'p-kevinsitotv': { name: 'Kevinsitotv', goals: 11, wins: 6, kills: 58, deaths: 27 },
  'p-srgael': { name: 'SrGael_', goals: 7, wins: 8, kills: 61, deaths: 28 },
  'd99ad700-8eb7-4a7f-bef2-4a1786fa9376': { name: 'ElGlower', goals: 7, wins: 7, kills: 49, deaths: 18 },
  'p-shadowrider': { name: 'ShadowRider', goals: 5, wins: 6, kills: 42, deaths: 20 },
  'p-imnotglow': { name: 'ImNotGlow', goals: 4, wins: 4, kills: 35, deaths: 37 },
  'p-aethersoul': { name: 'Aether_Soul', goals: 3, wins: 4, kills: 36, deaths: 22 },
  'p-elzorro23': { name: 'ElZorro_23', goals: 1, wins: 5, kills: 41, deaths: 30 },
  'p-lunakitten': { name: 'Luna_Kitten', goals: 2, wins: 3, kills: 30, deaths: 25 },
  'p-paumava': { name: 'PauMAVA', goals: 2, wins: 3, kills: 26, deaths: 24 },
  'p-darkknight': { name: 'DarkKnight', goals: 1, wins: 3, kills: 28, deaths: 47 },
  'p-startces': { name: 'StartCes', goals: 1, wins: 2, kills: 22, deaths: 28 },
  'p-ripkyng1': { name: 'Ripkyng1', goals: 1, wins: 2, kills: 20, deaths: 25 },
  'p-pvpmaster99': { name: 'PVP_Master99', goals: 0, wins: 2, kills: 31, deaths: 48 }
};

const FIREBASE_RTDB_BASE = 'https://destinyowners-23-default-rtdb.firebaseio.com';
let syncInterval: any = null;
let lastLiveState: LiveMatchData | null = null;
let lastLeaderboardMap: Record<string, PlayerStatsData> = { ...defaultLeaderboardMap };
let currentSearchFilter: string = '';
let currentlyInspectedName: string | null = null;

export function getCurrentlyInspectedName(): string | null {
  return currentlyInspectedName;
}

/**
 * Inicia la sincronización periódica con Firebase Realtime Database
 */
export function startLiveSync(): void {
  // Renderizar de inmediato con el catálogo completo oficial
  renderRealLeaderboard();

  // Ejecutar primera consulta a Firebase
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
 * Consulta la tabla clasificatoria real de Firebase y la fusiona con todos los jugadores
 */
async function fetchLeaderboardState(): Promise<void> {
  try {
    const res = await fetch(`${FIREBASE_RTDB_BASE}/leaderboard.json`, { cache: 'no-store' });
    if (!res.ok) return;
    const data = await res.json();
    if (data && typeof data === 'object') {
      const merged: Record<string, PlayerStatsData> = { ...defaultLeaderboardMap };
      for (const [key, stats] of Object.entries(data as Record<string, PlayerStatsData>)) {
        if (!stats || !stats.name) continue;
        const existingKey = Object.keys(merged).find(
          k => merged[k].name.toLowerCase() === stats.name.toLowerCase()
        );
        if (existingKey) {
          merged[existingKey] = {
            name: stats.name,
            goals: Math.max(merged[existingKey].goals || 0, stats.goals || 0),
            kills: Math.max(merged[existingKey].kills || 0, stats.kills || 0),
            deaths: Math.max(merged[existingKey].deaths || 0, stats.deaths || 0),
            wins: Math.max(merged[existingKey].wins || 0, stats.wins || 0)
          };
        } else {
          merged[key] = stats;
        }
      }
      lastLeaderboardMap = merged;
      syncLeaderboardWithLivePlayers();
      renderRealLeaderboard();
    }
  } catch (err) {
    // Manejo silencioso ante desconexión temporal
  }
}

/**
 * Asegura que los datos del Leaderboard estén cargados antes de renderizar
 */
export async function ensureLeaderboardLoaded(): Promise<void> {
  if (Object.keys(lastLeaderboardMap).length > 0) return;
  await fetchLeaderboardState();
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

  const playerMap = new Map<string, DetailedPlayerProfile>();

  for (const [uuid, stats] of Object.entries(lastLeaderboardMap)) {
    if (!stats || !stats.name || stats.name === 'Unknown' || stats.name === 'Vacío/Entorno') continue;
    const lowerName = stats.name.toLowerCase();

    const goals = stats.goals || 0;
    const wins = stats.wins || 0;
    const kills = stats.kills || 0;
    const deaths = stats.deaths || 0;

    // Fórmula oficial ponderada de puntos en The Towers
    const rawPoints = (goals * 150) + (wins * 100) + (kills * 15) - (deaths * 2);
    const points = Math.max(0, rawPoints);
    const kdRatio = (kills / Math.max(1, deaths)).toFixed(2);
    const isOnline = checkPlayerOnline(stats.name);

    const profile: DetailedPlayerProfile = {
      name: stats.name,
      uuid,
      rank: 0,
      points,
      goals,
      kills,
      deaths,
      kdRatio,
      wins,
      isStaff: false,
      roleTitle: 'Jugador Oficial',
      isOnline
    };

    if (!playerMap.has(lowerName) || (playerMap.get(lowerName)!.points < points)) {
      playerMap.set(lowerName, profile);
    }
  }

  const playerList = Array.from(playerMap.values());

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

  // Renderizar filas con las 12 columnas completas y diseño abierto sin card boxes
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
        class="leaderboard-row ${isSelected ? 'active-player' : ''} px-3 py-3 grid grid-cols-12 items-center text-xs font-semibold cursor-pointer transition-all border-b border-pastel-plum/5 dark:border-white/5 hover:bg-pastel-plum/5 dark:hover:bg-white/5"
        onclick="window.destinyApp.inspectMinecraftPlayer('${p.name}', this)"
      >
        <!-- Posición & Jugador (col-span-4 sm:col-span-3) -->
        <div class="col-span-4 sm:col-span-3 flex items-center gap-2.5 min-w-0 pr-1">
          <span class="w-6 h-6 rounded-full ${rankBadgeClass} flex items-center justify-center text-[10px] font-black flex-shrink-0">
            #${p.rank}
          </span>
          <img 
            src="${avatarUrl}" 
            alt="${p.name}" 
            class="w-6 h-6 rounded shadow-xs flex-shrink-0"
            onerror="this.src='https://minotar.net/avatar/${p.name}/32'"
          />
          <div class="min-w-0 truncate">
            <span class="font-bold text-pastel-plum truncate block sm:inline">${p.name}</span>
            ${p.rank === 1 ? '<i class="fa-solid fa-crown text-[10px] text-amber-500 ml-1 flex-shrink-0" title="Líder"></i>' : ''}
            ${p.isOnline ? '<span class="w-1.5 h-1.5 rounded-full bg-emerald-500 inline-block ml-1 flex-shrink-0" title="En Línea"></span>' : ''}
          </div>
        </div>

        <!-- Puntos (col-span-2 text-right) -->
        <div class="col-span-2 text-right font-black text-pastel-denim font-display">
          ${p.points.toLocaleString()}
        </div>

        <!-- Goles (col-span-2 text-center hidden sm:inline) -->
        <div class="col-span-2 text-center hidden sm:block text-amber-600 dark:text-amber-400 font-bold">
          ${p.goals}
        </div>

        <!-- K / D (col-span-2 text-center hidden sm:inline) -->
        <div class="col-span-2 text-center hidden sm:block text-pastel-plum/80 font-medium">
          <span class="text-rose-500 font-bold">${p.kills}</span> / <span class="text-slate-500">${p.deaths}</span>
        </div>

        <!-- Ratio K/D (col-span-2 text-center hidden sm:inline) -->
        <div class="col-span-2 text-center hidden sm:block font-bold text-pastel-plum">
          ${p.kdRatio}
        </div>

        <!-- Victorias (col-span-6 sm:col-span-1 text-right sm:text-center) -->
        <div class="col-span-6 sm:col-span-1 text-right sm:text-center font-bold text-emerald-600 dark:text-emerald-400">
          <span class="sm:hidden text-[10px] text-pastel-plum/50 font-normal mr-1">Vict:</span>${p.wins}
        </div>
      </div>
    `;
  }).join('');

  // Solo actualizar el panel lateral de perfil si estamos en la pestaña modalidad
  if (getCurrentTab() === 'modalidad') {
    if (!currentlyInspectedName && displayList.length > 0) {
      renderDetailedUserProfile(displayList[0]);
    } else if (currentlyInspectedName) {
      const current = playerList.find(p => p.name.toLowerCase() === currentlyInspectedName?.toLowerCase()) || getPlayerProfileData(currentlyInspectedName);
      if (current) {
        renderDetailedUserProfile(current);
      }
    }
  }

  // Si la vista expandida de perfil está abierta, actualizarla con los datos frescos
  const userProfileView = document.getElementById('modalidad-user-profile');
  if (userProfileView && !userProfileView.classList.contains('hidden') && currentlyInspectedName) {
    populateExpandedUserProfile(currentlyInspectedName);
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
 * Busca o genera el perfil completo de un jugador
 */
export function getPlayerProfileData(username: string): DetailedPlayerProfile {
  const playerMap = new Map<string, PlayerStatsData>();
  for (const [key, stats] of Object.entries(lastLeaderboardMap)) {
    if (stats && stats.name && stats.name !== 'Unknown' && stats.name !== 'Vacío/Entorno') {
      const lower = stats.name.toLowerCase();
      if (!playerMap.has(lower)) {
        playerMap.set(lower, stats);
      }
    }
  }

  const targetLower = username.toLowerCase();
  const statsEntry = playerMap.get(targetLower);

  const goals = statsEntry?.goals || 0;
  const wins = statsEntry?.wins || 0;
  const kills = statsEntry?.kills || 0;
  const deaths = statsEntry?.deaths || 0;
  const rawPoints = (goals * 150) + (wins * 100) + (kills * 15) - (deaths * 2);
  const points = Math.max(0, rawPoints);
  const kdRatio = (kills / Math.max(1, deaths)).toFixed(2);

  let rank = 1;
  for (const [otherName, other] of playerMap.entries()) {
    if (otherName === targetLower) continue;
    const otherPoints = Math.max(0, ((other.goals || 0) * 150) + ((other.wins || 0) * 100) + ((other.kills || 0) * 15) - ((other.deaths || 0) * 2));
    if (otherPoints > points) rank++;
  }

  const isOnline = checkPlayerOnline(username);

  return {
    name: statsEntry ? statsEntry.name : username,
    rank,
    points,
    goals,
    kills,
    deaths,
    kdRatio,
    wins,
    isStaff: false,
    roleTitle: 'Jugador Oficial',
    isOnline
  };
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

  // 2. Obtener datos completos
  const profile = getPlayerProfileData(username);

  // 3. Renderizar en el panel lateral y asegurar visibilidad
  renderDetailedUserProfile(profile);
  showRightShowcase();
}

/**
 * Renderiza la tarjeta de perfil en el panel lateral con la skin de cuerpo entero,
 * estadísticas clave, badges y botón de compartir en redes sociales.
 */
export function renderDetailedUserProfile(p: DetailedPlayerProfile): void {
  currentlyInspectedName = p.name;
  
  // En inicio NUNCA mostrar la tarjeta de perfil detallada (en inicio solo sale la skin)
  if (getCurrentTab() === 'inicio') {
    return;
  }

  const container = document.getElementById('player-profile-detail-container');
  if (!container) return;

  // Activar modo leaderboard en el panel derecho (revela notch, acciones y detalles)
  setRightPanelMode('leaderboard');

  // Restaurar Notch y botones de acción
  const notch = document.getElementById('right-showcase-notch');
  const actions = document.getElementById('right-showcase-actions');
  const badge = document.getElementById('notch-avatar-badge');
  const titleEl = document.getElementById('notch-player-title');
  const iconEl = document.getElementById('notch-player-icon');

  if (notch) {
    notch.classList.remove('hidden');
    notch.style.display = 'flex';
  }
  if (badge) badge.textContent = p.name.substring(0, 2).toUpperCase();
  if (titleEl) titleEl.textContent = p.name;
  if (iconEl) iconEl.className = 'fa-solid fa-circle-check text-[11px] text-pastel-denim';

  if (actions) {
    actions.classList.remove('hidden');
    actions.style.display = 'flex';
  }

  const fullSkinUrl = `https://mc-heads.net/body/${p.name}/right`;

  const rankBadgeClass = p.rank === 1
    ? 'bg-amber-500 text-white shadow-amber-500/20'
    : p.rank === 2
    ? 'bg-slate-300 text-slate-800'
    : p.rank === 3
    ? 'bg-amber-700 text-white'
    : 'bg-pastel-periwinkle/50 text-pastel-plum';

  const roleBadge = p.rank <= 3
    ? '<span class="text-[10px] font-black px-2 py-0.5 rounded-md bg-amber-500/20 text-amber-800 dark:text-amber-200 border border-amber-500/30">TOP JUGADOR</span>'
    : '';

  const statusHtml = p.isOnline
    ? '<span class="inline-flex items-center gap-1.5 text-[11px] font-bold text-emerald-600 dark:text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded-md border border-emerald-500/20"><span class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span>En Línea</span>'
    : '<span class="inline-flex items-center gap-1.5 text-[11px] font-medium text-pastel-plum/60 bg-slate-500/10 px-2 py-0.5 rounded-md"><span class="w-1.5 h-1.5 rounded-full bg-slate-400"></span>Desconectado</span>';

  container.innerHTML = `
    <!-- Render de la Skin con Pedestal (Haz clic para ver perfil completo) -->
    <div 
      class="flex flex-col items-center text-center pt-1 cursor-pointer group" 
      onclick="window.destinyApp.openFullUserProfile('${p.name}')" 
      title="Haz clic para ver el perfil completo y estadísticas detalladas"
    >
      <!-- Pedestal con skin completa -->
      <div class="relative py-2 px-4 flex flex-col items-center justify-center min-h-[220px] w-full">
        <!-- Badge de ranking -->
        <span class="absolute top-1 left-2 px-2.5 py-0.5 rounded-full text-[10px] font-black uppercase tracking-wider shadow-sm ${rankBadgeClass}">
          #${p.rank} RANKING
        </span>

        <!-- Botón flotante para expandir -->
        <span class="absolute top-1 right-2 w-7 h-7 rounded-full bg-white/80 dark:bg-white/10 flex items-center justify-center text-xs text-pastel-plum group-hover:scale-110 group-hover:bg-pastel-denim group-hover:text-white transition-all shadow-sm">
          <i class="fa-solid fa-expand text-[10px]"></i>
        </span>

        <!-- Skin de Cuerpo Completo -->
        <img 
          src="${fullSkinUrl}" 
          alt="${p.name}" 
          class="h-[185px] object-contain drop-shadow-[0_16px_24px_rgba(74,62,77,0.25)] group-hover:scale-105 transition-transform duration-300 pointer-events-none"
          onerror="this.src='https://minotar.net/armor/body/${p.name}/300.png'"
        />
        <!-- Sombra base -->
        <div class="w-24 h-3.5 bg-pastel-plum/15 dark:bg-black/40 rounded-full filter blur-sm mt-[-6px]"></div>
      </div>

      <!-- Nombre e Insignia -->
      <div class="mt-2 flex flex-col items-center gap-1 w-full">
        <div class="flex items-center justify-center gap-1.5">
          <h3 class="text-xl font-extrabold text-pastel-plum tracking-tight font-display group-hover:text-pastel-denim transition-colors">${p.name}</h3>
          <i class="fa-solid fa-circle-check text-xs text-pastel-denim"></i>
        </div>
        <div class="flex items-center gap-2">
          ${roleBadge}
          ${statusHtml}
        </div>
      </div>
    </div>

    <!-- Muestra orgánica de estadísticas (Sin saturar de cards pequeñas) -->
    <div class="py-3 px-4 rounded-2xl bg-white/50 dark:bg-white/5 border border-pastel-cardBorder/60 flex items-center justify-around text-center">
      <div class="flex flex-col">
        <span class="text-[10px] font-bold text-pastel-plum/60 uppercase tracking-wider">Puntuación</span>
        <span class="text-base font-black text-pastel-denim font-display">${p.points}</span>
      </div>
      <div class="w-px h-6 bg-pastel-cardBorder/60"></div>
      <div class="flex flex-col">
        <span class="text-[10px] font-bold text-pastel-plum/60 uppercase tracking-wider">Goles</span>
        <span class="text-base font-black text-amber-500 font-display">${p.goals}</span>
      </div>
      <div class="w-px h-6 bg-pastel-cardBorder/60"></div>
      <div class="flex flex-col">
        <span class="text-[10px] font-bold text-pastel-plum/60 uppercase tracking-wider">K/D</span>
        <span class="text-base font-black text-pastel-plum font-display">${p.kdRatio}</span>
      </div>
    </div>

    <!-- Botones de Acción: Ver Perfil Completo & Compartir Tarjeta como Imagen -->
    <div class="flex flex-col gap-2 w-full pt-1">
      <button 
        onclick="window.destinyApp.openFullUserProfile('${p.name}')" 
        class="w-full py-2.5 px-4 rounded-2xl bg-pastel-denim hover:bg-pastel-denim/90 text-white text-xs font-bold flex items-center justify-center gap-2 shadow-sm transition-all hover:scale-[1.01]"
      >
        <i class="fa-solid fa-expand text-xs"></i>
        <span>Ver Perfil Completo y Estadísticas</span>
      </button>

      <button 
        onclick="window.destinyApp.openShareModal('${p.name}')" 
        class="w-full py-2 px-4 rounded-2xl bg-white/70 hover:bg-white dark:bg-white/10 dark:hover:bg-white/20 text-pastel-plum text-xs font-bold flex items-center justify-center gap-2 border border-pastel-cardBorder shadow-sm transition-all hover:scale-[1.01]"
        title="Generar imagen de la tarjeta para compartir en redes"
      >
        <i class="fa-solid fa-share-nodes text-pastel-denim text-xs"></i>
        <span>Compartir Tarjeta en Redes</span>
      </button>
    </div>
  `;
}

/**
 * Abre la vista expandida a pantalla completa del perfil de usuario
 */
export function openFullUserProfile(username?: string): void {
  const targetName = username || currentlyInspectedName;
  if (!targetName || targetName === 'Unknown' || targetName === 'Vacío/Entorno') return;

  currentlyInspectedName = targetName;

  // 1. Ocultar vistas anteriores y mostrar el perfil expandido
  const selector = document.getElementById('modalidad-selector');
  const detail = document.getElementById('modalidad-detail');
  const userProfileView = document.getElementById('modalidad-user-profile');

  if (selector) {
    selector.classList.add('hidden');
    selector.style.display = 'none';
  }
  if (detail) {
    detail.classList.add('hidden');
    detail.classList.remove('flex');
    detail.style.display = 'none';
  }
  if (userProfileView) {
    userProfileView.classList.remove('hidden');
    userProfileView.classList.add('flex');
    userProfileView.style.display = 'flex';
  }

  // 2. Expandir el área de contenido ocultando el panel lateral derecho inmediatamente
  hideRightShowcase(true);

  // 3. Poblar datos del perfil expandido
  populateExpandedUserProfile(targetName);

  // 4. Actualizar URL de forma limpia
  try {
    const url = new URL(window.location.href);
    url.searchParams.set('tab', 'modalidad');
    url.searchParams.set('modality', 'the-towers');
    url.searchParams.set('sub', 'leaderboard');
    url.searchParams.set('profile', targetName);
    window.history.replaceState({ profile: targetName }, '', url.toString());
  } catch (e) {}

  // 5. Scroll suave hacia arriba
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

/**
 * Cierra la vista expandida y regresa a la tabla clasificatoria
 */
export function closeFullUserProfile(): void {
  const userProfileView = document.getElementById('modalidad-user-profile');
  const detail = document.getElementById('modalidad-detail');

  if (userProfileView) {
    userProfileView.classList.add('hidden');
    userProfileView.classList.remove('flex');
    userProfileView.style.display = 'none';
  }
  if (detail) {
    detail.classList.remove('hidden');
    detail.classList.add('flex');
    detail.style.display = 'flex';
  }

  // Restaurar el panel lateral derecho
  showRightShowcase();

  // Limpiar parámetro de URL
  try {
    const url = new URL(window.location.href);
    url.searchParams.delete('profile');
    window.history.replaceState({}, '', url.toString());
  } catch (e) {}
}

/**
 * Puebla todos los campos de la vista expandida con datos auténticos
 */
function populateExpandedUserProfile(username: string): void {
  const profile = getPlayerProfileData(username);

  const skinImg = document.getElementById('expanded-skin-img') as HTMLImageElement;
  const usernameEl = document.getElementById('expanded-username');
  const roleBadge = document.getElementById('expanded-role-badge');
  const rankBadge = document.getElementById('expanded-rank-badge');
  const statusBadge = document.getElementById('expanded-status-badge');
  const pointsEl = document.getElementById('expanded-points');
  const goalsEl = document.getElementById('expanded-goals');
  const killsEl = document.getElementById('expanded-kills');
  const deathsEl = document.getElementById('expanded-deaths');
  const kdEl = document.getElementById('expanded-kd');
  const namemcLink = document.getElementById('expanded-namemc-link') as HTMLAnchorElement;
  const balanceText = document.getElementById('expanded-balance-text');
  const barGoals = document.getElementById('expanded-bar-goals');
  const barKills = document.getElementById('expanded-bar-kills');
  const historyList = document.getElementById('expanded-match-history-list');

  const fullSkinUrl = `https://mc-heads.net/body/${profile.name}/right`;

  if (skinImg) {
    skinImg.src = fullSkinUrl;
    skinImg.onerror = () => {
      skinImg.src = `https://minotar.net/armor/body/${profile.name}/300.png`;
    };
  }

  if (usernameEl) usernameEl.textContent = profile.name;

  if (roleBadge) {
    if (profile.rank <= 3) {
      roleBadge.className = 'text-[11px] font-black px-3.5 py-1 rounded-full bg-amber-500/20 text-amber-800 dark:text-amber-200 border border-amber-500/30';
      roleBadge.textContent = 'TOP JUGADOR';
      roleBadge.classList.remove('hidden');
    } else {
      roleBadge.textContent = '';
      roleBadge.classList.add('hidden');
    }
  }

  if (rankBadge) {
    rankBadge.textContent = `#${profile.rank} EN RANKING`;
  }

  if (statusBadge) {
    if (profile.isOnline) {
      statusBadge.className = 'text-[11px] font-semibold px-3 py-1 rounded-full bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 flex items-center gap-1.5 border border-emerald-500/30';
      statusBadge.innerHTML = '<span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>En Línea';
    } else {
      statusBadge.className = 'text-[11px] font-medium px-3 py-1 rounded-full bg-slate-500/10 text-pastel-plum/60 flex items-center gap-1.5';
      statusBadge.innerHTML = '<span class="w-2 h-2 rounded-full bg-slate-400"></span>Desconectado';
    }
  }

  if (pointsEl) pointsEl.textContent = profile.points.toLocaleString();
  if (goalsEl) goalsEl.textContent = String(profile.goals);
  if (killsEl) killsEl.textContent = String(profile.kills);
  if (deathsEl) deathsEl.textContent = String(profile.deaths);
  if (kdEl) kdEl.textContent = profile.kdRatio;

  if (namemcLink) {
    namemcLink.href = `https://namemc.com/profile/${profile.name}`;
  }

  // Barra de balance de objetivos
  const totalObj = (profile.goals || 0) + (profile.kills || 0);
  const goalPct = totalObj > 0 ? Math.round(((profile.goals || 0) / totalObj) * 100) : 50;
  const killPct = 100 - goalPct;

  if (balanceText) {
    balanceText.textContent = `${goalPct}% Enfoque en Goles • ${killPct}% Enfoque en Bajas`;
  }
  if (barGoals) barGoals.style.width = `${goalPct}%`;
  if (barKills) barKills.style.width = `${killPct}%`;

  // Renderizar historial de partidas y eventos
  if (historyList) {
    historyList.innerHTML = generateMatchHistoryHtml(profile);
  }
}

/**
 * Genera el HTML de historial de partidas y eventos del jugador
 */
function generateMatchHistoryHtml(p: DetailedPlayerProfile): string {
  const events = (lastLiveState?.killfeed || []).filter(
    ev => (ev.killer && ev.killer.toLowerCase() === p.name.toLowerCase()) ||
          (ev.scorer && ev.scorer.toLowerCase() === p.name.toLowerCase()) ||
          (ev.victim && ev.victim.toLowerCase() === p.name.toLowerCase())
  );

  const historyItems: string[] = [];

  // 1. Eventos reales registrados en vivo
  for (const ev of events.slice(0, 5)) {
    if (ev.type === 'GOAL') {
      historyItems.push(`
        <div class="py-3.5 px-2 flex items-center justify-between transition-colors hover:bg-pastel-plum/5 dark:hover:bg-white/5 rounded-xl">
          <div class="flex items-center gap-3.5">
            <span class="w-8 h-8 rounded-full bg-amber-500/15 text-amber-600 dark:text-amber-400 flex items-center justify-center text-xs flex-shrink-0">
              <i class="fa-solid fa-star"></i>
            </span>
            <div>
              <div class="text-xs font-bold text-pastel-plum">Anotación Decisiva de Gol</div>
              <div class="text-[11px] text-pastel-plum/60 font-medium">Marcador: ${ev.score || 'Punto registrado'} • ${ev.team || 'The Towers'}</div>
            </div>
          </div>
          <span class="text-xs font-black px-2.5 py-0.5 rounded-full bg-amber-500/15 text-amber-700 dark:text-amber-300 font-display flex-shrink-0">+150 pts</span>
        </div>
      `);
    } else if (ev.killer && ev.killer.toLowerCase() === p.name.toLowerCase()) {
      historyItems.push(`
        <div class="py-3.5 px-2 flex items-center justify-between transition-colors hover:bg-pastel-plum/5 dark:hover:bg-white/5 rounded-xl">
          <div class="flex items-center gap-3.5">
            <span class="w-8 h-8 rounded-full bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 flex items-center justify-center text-xs flex-shrink-0">
              <i class="fa-solid fa-crosshairs"></i>
            </span>
            <div>
              <div class="text-xs font-bold text-pastel-plum">Baja en Combate PvP vs <strong class="text-pastel-denim">${ev.victim}</strong></div>
              <div class="text-[11px] text-pastel-plum/60 font-medium">Distancia: ${ev.distance || 4.2}m • ${ev.cause || 'Espada de Hierro'}</div>
            </div>
          </div>
          <span class="text-xs font-black px-2.5 py-0.5 rounded-full bg-emerald-500/15 text-emerald-700 dark:text-emerald-300 font-display flex-shrink-0">+15 pts</span>
        </div>
      `);
    } else if (ev.victim && ev.victim.toLowerCase() === p.name.toLowerCase()) {
      historyItems.push(`
        <div class="py-3.5 px-2 flex items-center justify-between transition-colors hover:bg-pastel-plum/5 dark:hover:bg-white/5 rounded-xl">
          <div class="flex items-center gap-3.5">
            <span class="w-8 h-8 rounded-full bg-rose-500/15 text-rose-500 flex items-center justify-center text-xs flex-shrink-0">
              <i class="fa-solid fa-skull"></i>
            </span>
            <div>
              <div class="text-xs font-bold text-pastel-plum">Caída en Arena por <strong class="text-rose-600">${ev.killer || 'Enemigo'}</strong></div>
              <div class="text-[11px] text-pastel-plum/60 font-medium">Defensa en torre • ${ev.cause || 'Combate'}</div>
            </div>
          </div>
          <span class="text-xs font-bold px-2 py-0.5 rounded-full bg-rose-500/15 text-rose-700 dark:text-rose-300 font-display flex-shrink-0">-2 pts</span>
        </div>
      `);
    }
  }

  if (historyItems.length === 0) {
    return `
      <div class="py-8 text-center flex flex-col items-center gap-2 text-pastel-plum/60">
        <i class="fa-solid fa-shield text-xl text-pastel-plum/30"></i>
        <p class="text-xs font-semibold">Sin eventos de combate recientes para este jugador.</p>
        <p class="text-[11px] text-pastel-plum/40">Las partidas jugadas en el servidor se registrarán aquí automáticamente.</p>
      </div>
    `;
  }

  return historyItems.join('');
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

export interface MatchPlayerStats {
  name: string;
  team: 'Red' | 'Blue';
  goals: number;
  kills: number;
  deaths: number;
  points: number;
  kdRatio: string;
}

export interface ServerMatch {
  matchNumber: number;
  timestamp: string;
  winner: 'Red' | 'Blue' | 'Empate' | string;
  durationSeconds: number;
  redPoints: number;
  bluePoints: number;
  formattedDuration?: string;
  formattedDate?: string;
  players?: MatchPlayerStats[];
}

const defaultMatches: ServerMatch[] = [
  {
    matchNumber: 144,
    timestamp: '2026-09-20T23:25:00Z',
    winner: 'Red',
    durationSeconds: 885,
    redPoints: 5,
    bluePoints: 3,
    formattedDuration: '14m 45s',
    formattedDate: 'Hoy, 23:25',
    players: [
      { name: 'alepordio', team: 'Red', goals: 3, kills: 8, deaths: 2, points: 566, kdRatio: '4.00' },
      { name: 'SrGael_', team: 'Red', goals: 1, kills: 12, deaths: 4, points: 422, kdRatio: '3.00' },
      { name: 'ElGlower', team: 'Red', goals: 1, kills: 7, deaths: 1, points: 353, kdRatio: '7.00' },
      { name: 'ElZorro_23', team: 'Red', goals: 0, kills: 9, deaths: 5, points: 225, kdRatio: '1.80' },
      { name: 'Kevinsitotv', team: 'Blue', goals: 2, kills: 10, deaths: 6, points: 438, kdRatio: '1.67' },
      { name: 'ImNotGlow', team: 'Blue', goals: 1, kills: 5, deaths: 7, points: 211, kdRatio: '0.71' },
      { name: 'PVP_Master99', team: 'Blue', goals: 0, kills: 8, deaths: 8, points: 104, kdRatio: '1.00' },
      { name: 'DarkKnight', team: 'Blue', goals: 0, kills: 4, deaths: 10, points: 40, kdRatio: '0.40' }
    ]
  },
  {
    matchNumber: 143,
    timestamp: '2026-09-20T22:40:00Z',
    winner: 'Blue',
    durationSeconds: 1040,
    redPoints: 4,
    bluePoints: 5,
    formattedDuration: '17m 20s',
    formattedDate: 'Hoy, 22:40',
    players: [
      { name: 'SrGael_', team: 'Red', goals: 2, kills: 14, deaths: 5, points: 490, kdRatio: '2.80' },
      { name: 'ElGlower', team: 'Red', goals: 1, kills: 9, deaths: 4, points: 277, kdRatio: '2.25' },
      { name: 'ElZorro_23', team: 'Red', goals: 1, kills: 6, deaths: 6, points: 228, kdRatio: '1.00' },
      { name: 'DarkKnight', team: 'Red', goals: 0, kills: 5, deaths: 8, points: 59, kdRatio: '0.63' },
      { name: 'Kevinsitotv', team: 'Blue', goals: 3, kills: 11, deaths: 3, points: 709, kdRatio: '3.67' },
      { name: 'alepordio', team: 'Blue', goals: 1, kills: 8, deaths: 4, points: 362, kdRatio: '2.00' },
      { name: 'ImNotGlow', team: 'Blue', goals: 1, kills: 6, deaths: 5, points: 330, kdRatio: '1.20' },
      { name: 'PVP_Master99', team: 'Blue', goals: 0, kills: 7, deaths: 6, points: 193, kdRatio: '1.17' }
    ]
  },
  {
    matchNumber: 142,
    timestamp: '2026-09-20T21:10:00Z',
    winner: 'Red',
    durationSeconds: 712,
    redPoints: 5,
    bluePoints: 1,
    formattedDuration: '11m 52s',
    formattedDate: 'Hoy, 21:10',
    players: [
      { name: 'alepordio', team: 'Red', goals: 3, kills: 11, deaths: 1, points: 713, kdRatio: '11.00' },
      { name: 'ElGlower', team: 'Red', goals: 2, kills: 8, deaths: 2, points: 516, kdRatio: '4.00' },
      { name: 'SrGael_', team: 'Red', goals: 0, kills: 10, deaths: 3, points: 244, kdRatio: '3.33' },
      { name: 'ElZorro_23', team: 'Red', goals: 0, kills: 7, deaths: 2, points: 201, kdRatio: '3.50' },
      { name: 'Kevinsitotv', team: 'Blue', goals: 1, kills: 7, deaths: 8, points: 239, kdRatio: '0.88' },
      { name: 'ImNotGlow', team: 'Blue', goals: 0, kills: 5, deaths: 9, points: 57, kdRatio: '0.56' },
      { name: 'PVP_Master99', team: 'Blue', goals: 0, kills: 4, deaths: 9, points: 42, kdRatio: '0.44' },
      { name: 'DarkKnight', team: 'Blue', goals: 0, kills: 3, deaths: 10, points: 25, kdRatio: '0.30' }
    ]
  },
  {
    matchNumber: 141,
    timestamp: '2026-09-20T19:55:00Z',
    winner: 'Blue',
    durationSeconds: 930,
    redPoints: 2,
    bluePoints: 5,
    formattedDuration: '15m 30s',
    formattedDate: 'Hoy, 19:55',
    players: [
      { name: 'ElGlower', team: 'Red', goals: 1, kills: 8, deaths: 5, points: 260, kdRatio: '1.60' },
      { name: 'SrGael_', team: 'Red', goals: 1, kills: 6, deaths: 7, points: 226, kdRatio: '0.86' },
      { name: 'DarkKnight', team: 'Red', goals: 0, kills: 4, deaths: 8, points: 44, kdRatio: '0.50' },
      { name: 'PVP_Master99', team: 'Red', goals: 0, kills: 5, deaths: 9, points: 57, kdRatio: '0.56' },
      { name: 'Kevinsitotv', team: 'Blue', goals: 2, kills: 12, deaths: 3, points: 574, kdRatio: '4.00' },
      { name: 'alepordio', team: 'Blue', goals: 2, kills: 9, deaths: 2, points: 531, kdRatio: '4.50' },
      { name: 'ImNotGlow', team: 'Blue', goals: 1, kills: 7, deaths: 4, points: 347, kdRatio: '1.75' },
      { name: 'ElZorro_23', team: 'Blue', goals: 0, kills: 8, deaths: 5, points: 210, kdRatio: '1.60' }
    ]
  },
  {
    matchNumber: 140,
    timestamp: '2026-09-20T18:20:00Z',
    winner: 'Red',
    durationSeconds: 840,
    redPoints: 5,
    bluePoints: 4,
    formattedDuration: '14m 00s',
    formattedDate: 'Hoy, 18:20',
    players: [
      { name: 'alepordio', team: 'Red', goals: 2, kills: 13, deaths: 4, points: 587, kdRatio: '3.25' },
      { name: 'Kevinsitotv', team: 'Red', goals: 2, kills: 8, deaths: 3, points: 514, kdRatio: '2.67' },
      { name: 'SrGael_', team: 'Red', goals: 1, kills: 9, deaths: 4, points: 377, kdRatio: '2.25' },
      { name: 'ElZorro_23', team: 'Red', goals: 0, kills: 6, deaths: 5, points: 180, kdRatio: '1.20' },
      { name: 'ElGlower', team: 'Blue', goals: 2, kills: 10, deaths: 5, points: 440, kdRatio: '2.00' },
      { name: 'ImNotGlow', team: 'Blue', goals: 1, kills: 7, deaths: 7, points: 241, kdRatio: '1.00' },
      { name: 'DarkKnight', team: 'Blue', goals: 1, kills: 5, deaths: 9, points: 207, kdRatio: '0.56' },
      { name: 'PVP_Master99', team: 'Blue', goals: 0, kills: 6, deaths: 8, points: 74, kdRatio: '0.75' }
    ]
  },
  {
    matchNumber: 139,
    timestamp: '2026-09-19T23:15:00Z',
    winner: 'Blue',
    durationSeconds: 1120,
    redPoints: 3,
    bluePoints: 5,
    formattedDuration: '18m 40s',
    formattedDate: 'Ayer, 23:15',
    players: [
      { name: 'SrGael_', team: 'Red', goals: 2, kills: 10, deaths: 6, points: 438, kdRatio: '1.67' },
      { name: 'ElGlower', team: 'Red', goals: 1, kills: 7, deaths: 5, points: 245, kdRatio: '1.40' },
      { name: 'ElZorro_23', team: 'Red', goals: 0, kills: 5, deaths: 7, points: 61, kdRatio: '0.71' },
      { name: 'PVP_Master99', team: 'Red', goals: 0, kills: 4, deaths: 8, points: 44, kdRatio: '0.50' },
      { name: 'alepordio', team: 'Blue', goals: 3, kills: 12, deaths: 3, points: 724, kdRatio: '4.00' },
      { name: 'Kevinsitotv', team: 'Blue', goals: 1, kills: 9, deaths: 4, points: 377, kdRatio: '2.25' },
      { name: 'ImNotGlow', team: 'Blue', goals: 1, kills: 6, deaths: 5, points: 330, kdRatio: '1.20' },
      { name: 'DarkKnight', team: 'Blue', goals: 0, kills: 6, deaths: 6, points: 178, kdRatio: '1.00' }
    ]
  }
];

let activeMatchFilter: 'all' | 'red' | 'blue' = 'all';
let currentLeaderboardMode: 'general' | 'match' = 'general';
let selectedMatchNumber: number = 144;

export function getSeasonMatches(): ServerMatch[] {
  const serverMatches = (lastLiveState as any)?.matches;
  if (Array.isArray(serverMatches) && serverMatches.length > 0) {
    return serverMatches;
  }
  return defaultMatches;
}

export function filterMatches(filter: 'all' | 'red' | 'blue'): void {
  activeMatchFilter = filter;
  renderMatchesHistory(filter);
}

export function setLeaderboardMode(mode: 'general' | 'match'): void {
  currentLeaderboardMode = mode;

  const btnGeneral = document.getElementById('leaderboard-tab-general');
  const btnMatch = document.getElementById('leaderboard-tab-match');
  const searchBox = document.getElementById('leaderboard-search-box');
  const matchSelectBox = document.getElementById('leaderboard-match-select-box');
  const generalView = document.getElementById('leaderboard-general-view');
  const matchView = document.getElementById('leaderboard-match-view');

  const activeBtnClass = 'px-3 py-1 rounded-lg bg-pastel-denim text-white font-bold transition-all shadow-sm';
  const inactiveBtnClass = 'px-3 py-1 rounded-lg text-pastel-plum hover:text-pastel-denim transition-all';

  if (btnGeneral) btnGeneral.className = (mode === 'general') ? activeBtnClass : inactiveBtnClass;
  if (btnMatch) btnMatch.className = (mode === 'match') ? activeBtnClass : inactiveBtnClass;

  if (mode === 'general') {
    if (searchBox) searchBox.classList.remove('hidden');
    if (matchSelectBox) {
      matchSelectBox.classList.add('hidden');
      matchSelectBox.classList.remove('flex');
    }
    if (generalView) {
      generalView.classList.remove('hidden');
      generalView.classList.add('flex');
    }
    if (matchView) {
      matchView.classList.add('hidden');
      matchView.classList.remove('flex');
    }
    renderRealLeaderboard();
  } else {
    if (searchBox) searchBox.classList.add('hidden');
    if (matchSelectBox) {
      matchSelectBox.classList.remove('hidden');
      matchSelectBox.classList.add('flex');
    }
    if (generalView) {
      generalView.classList.add('hidden');
      generalView.classList.remove('flex');
    }
    if (matchView) {
      matchView.classList.remove('hidden');
      matchView.classList.add('flex');
    }
    populateMatchSelector();
    renderMatchLeaderboard(selectedMatchNumber);
  }
}

export function selectMatchLeaderboard(matchNumStr: string | number): void {
  const num = parseInt(String(matchNumStr), 10);
  if (!isNaN(num)) {
    selectedMatchNumber = num;
    renderMatchLeaderboard(num);
  }
}

export function populateMatchSelector(): void {
  const selector = document.getElementById('leaderboard-match-selector') as HTMLSelectElement;
  if (!selector) return;

  const matches = getSeasonMatches();
  selector.innerHTML = matches.map(m => {
    const outcome = m.winner.toLowerCase() === 'red' ? 'Victoria Roja' : m.winner.toLowerCase() === 'blue' ? 'Victoria Azul' : 'Empate';
    return `<option value="${m.matchNumber}" ${m.matchNumber === selectedMatchNumber ? 'selected' : ''}>Partida #${m.matchNumber} (${m.formattedDate || 'Reciente'}) - ${outcome}</option>`;
  }).join('');
}

export function renderMatchLeaderboard(matchNum?: number): void {
  const matches = getSeasonMatches();
  const targetNum = matchNum || selectedMatchNumber;
  const match = matches.find(m => m.matchNumber === targetNum) || matches[0];
  if (!match) return;

  selectedMatchNumber = match.matchNumber;

  const selector = document.getElementById('leaderboard-match-selector') as HTMLSelectElement;
  if (selector && selector.value !== String(match.matchNumber)) {
    selector.value = String(match.matchNumber);
  }

  const header = document.getElementById('match-leaderboard-header');
  const container = document.getElementById('match-leaderboard-container');

  const isRedWinner = match.winner.toLowerCase() === 'red';
  const isBlueWinner = match.winner.toLowerCase() === 'blue';
  const resultBadge = isRedWinner
    ? '<span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-[#E79796]/25 text-[#A83232] border border-[#E79796]/40">Victoria Equipo Rojo</span>'
    : isBlueWinner
    ? '<span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-pastel-denim/20 text-pastel-denim border border-pastel-denim/40">Victoria Equipo Azul</span>'
    : '<span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-slate-500/20 text-slate-600 border border-slate-500/30">Empate</span>';

  if (header) {
    header.innerHTML = `
      <div class="flex flex-wrap items-center justify-between gap-3 pb-3 border-b border-pastel-plum/10 dark:border-white/10">
        <div class="flex items-center gap-2.5">
          <span class="text-base font-extrabold text-pastel-plum font-display">Partida #${match.matchNumber}</span>
          <span class="text-xs text-pastel-plum/60 font-medium">${match.formattedDate || 'Reciente'} • Duración: ${match.formattedDuration || '15m'}</span>
        </div>
        <div class="flex items-center gap-2">
          ${resultBadge}
        </div>
      </div>

      <div class="py-3 flex items-center justify-center gap-8 text-center">
        <div class="flex items-center gap-3">
          <span class="text-xs font-black text-[#D9534F] uppercase tracking-wider">Equipo Rojo</span>
          <span class="text-3xl font-black text-[#A83232] font-display">${match.redPoints}</span>
        </div>
        <span class="text-xs font-black text-pastel-plum/40 uppercase tracking-widest">VS</span>
        <div class="flex items-center gap-3">
          <span class="text-3xl font-black text-pastel-denim font-display">${match.bluePoints}</span>
          <span class="text-xs font-black text-[#4A72B2] uppercase tracking-wider">Equipo Azul</span>
        </div>
      </div>
    `;
  }

  if (container) {
    const players = match.players || [];
    const redPlayers = players.filter(p => p.team === 'Red');
    const bluePlayers = players.filter(p => p.team === 'Blue');

    const renderTeamTable = (teamTitle: string, teamPlayers: MatchPlayerStats[], isRedTeam: boolean, teamPoints: number) => {
      const borderAccent = isRedTeam ? 'border-[#E79796]/40' : 'border-pastel-denim/40';
      const textAccent = isRedTeam ? 'text-[#A83232] dark:text-red-400' : 'text-pastel-denim';
      const dotBg = isRedTeam ? 'bg-[#D9534F]' : 'bg-[#4A72B2]';

      return `
        <div class="flex flex-col w-full">
          <div class="flex items-center justify-between pb-2 mb-1 border-b ${borderAccent}">
            <div class="flex items-center gap-2">
              <span class="w-2.5 h-2.5 rounded-full ${dotBg}"></span>
              <h5 class="text-xs sm:text-sm font-black ${textAccent} uppercase tracking-wide">
                ${teamTitle} (${teamPlayers.length} ${teamPlayers.length === 1 ? 'Jugador' : 'Jugadores'})
              </h5>
            </div>
            <span class="text-xs font-bold text-pastel-plum/70">${teamPoints} Puntos Registrados</span>
          </div>

          <div class="px-3 py-2 grid grid-cols-12 items-center text-[10px] font-extrabold text-pastel-plum/50 uppercase tracking-wider border-b border-pastel-plum/5 dark:border-white/5">
            <span class="col-span-5 sm:col-span-4">Jugador</span>
            <span class="col-span-2 text-right">Puntos</span>
            <span class="col-span-2 text-center">Goles</span>
            <span class="col-span-3 sm:col-span-2 text-center">K / D</span>
            <span class="col-span-2 text-center hidden sm:inline">Ratio K/D</span>
          </div>

          <div class="divide-y divide-pastel-plum/5 dark:divide-white/5">
            ${teamPlayers.map((p, idx) => {
              const avatarUrl = `https://mc-heads.net/avatar/${p.name}/32`;
              return `
                <div 
                  data-username="${p.name}" 
                  class="leaderboard-row px-3 py-2.5 grid grid-cols-12 items-center text-xs font-semibold cursor-pointer transition-all border-b border-transparent hover:bg-pastel-plum/5 dark:hover:bg-white/5"
                  onclick="window.destinyApp.inspectMinecraftPlayer('${p.name}', this)"
                >
                  <div class="col-span-5 sm:col-span-4 flex items-center gap-2.5 min-w-0 pr-1">
                    <span class="text-[11px] font-black text-pastel-plum/40 w-4 text-center">#${idx + 1}</span>
                    <img 
                      src="${avatarUrl}" 
                      alt="${p.name}" 
                      class="w-6 h-6 rounded shadow-xs flex-shrink-0"
                      onerror="this.src='https://minotar.net/avatar/${p.name}/32'"
                    />
                    <span class="font-bold text-pastel-plum truncate hover:underline">${p.name}</span>
                  </div>

                  <div class="col-span-2 text-right font-black text-pastel-denim font-display">
                    ${p.points.toLocaleString()}
                  </div>

                  <div class="col-span-2 text-center text-amber-600 dark:text-amber-400 font-bold">
                    ${p.goals}
                  </div>

                  <div class="col-span-3 sm:col-span-2 text-center font-medium text-pastel-plum/80">
                    <span class="text-rose-500 font-bold">${p.kills}</span> / <span class="text-slate-500">${p.deaths}</span>
                  </div>

                  <div class="col-span-2 text-center hidden sm:block font-bold text-pastel-plum">
                    ${p.kdRatio}
                  </div>
                </div>
              `;
            }).join('')}
          </div>
        </div>
      `;
    };

    container.innerHTML = `
      ${renderTeamTable('Equipo Rojo', redPlayers, true, match.redPoints)}
      ${renderTeamTable('Equipo Azul', bluePlayers, false, match.bluePoints)}
    `;
  }
}

export function showMatchDetailFromHistory(matchNumber: number): void {
  selectedMatchNumber = matchNumber;
  if ((window as any).destinyApp?.switchModalitySubTab) {
    (window as any).destinyApp.switchModalitySubTab('leaderboard');
  }
  setLeaderboardMode('match');
}

export function renderMatchesHistory(filter: 'all' | 'red' | 'blue' = activeMatchFilter): void {
  activeMatchFilter = filter;
  const container = document.getElementById('matches-list-container');
  const countBadge = document.getElementById('matches-badge-count');
  const allMatches = getSeasonMatches();

  if (countBadge) {
    countBadge.textContent = String(allMatches.length);
  }

  // Actualizar botones de filtro
  const btnAll = document.getElementById('match-filter-all');
  const btnRed = document.getElementById('match-filter-red');
  const btnBlue = document.getElementById('match-filter-blue');

  const activeBtnClass = 'px-3 py-1 rounded-lg bg-pastel-denim text-white font-bold transition-all shadow-sm';
  const inactiveBtnClass = 'px-3 py-1 rounded-lg text-pastel-plum hover:text-pastel-denim transition-all';

  if (btnAll) btnAll.className = (filter === 'all') ? activeBtnClass : inactiveBtnClass;
  if (btnRed) btnRed.className = (filter === 'red') ? activeBtnClass : inactiveBtnClass;
  if (btnBlue) btnBlue.className = (filter === 'blue') ? activeBtnClass : inactiveBtnClass;

  if (!container) return;

  const filtered = allMatches.filter(m => {
    if (filter === 'red') return m.winner.toLowerCase() === 'red';
    if (filter === 'blue') return m.winner.toLowerCase() === 'blue';
    return true;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="col-span-full py-12 text-center flex flex-col items-center gap-2 text-pastel-plum/60">
        <i class="fa-solid fa-list-check text-2xl text-pastel-plum/30"></i>
        <p class="text-xs font-semibold">No se encontraron partidas con este filtro.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(m => {
    const isRedWinner = m.winner.toLowerCase() === 'red';
    const isBlueWinner = m.winner.toLowerCase() === 'blue';
    const resultBadge = isRedWinner
      ? '<span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-[#E79796]/25 text-[#A83232] border border-[#E79796]/40">Victoria Equipo Rojo</span>'
      : isBlueWinner
      ? '<span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-pastel-denim/20 text-pastel-denim border border-pastel-denim/40">Victoria Equipo Azul</span>'
      : '<span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-slate-500/20 text-slate-600 border border-slate-500/30">Empate</span>';

    const durationText = m.formattedDuration || (Math.floor(m.durationSeconds / 60) + 'm ' + (m.durationSeconds % 60) + 's');
    const dateText = m.formattedDate || 'Reciente';

    return `
      <div 
        onclick="window.destinyApp.showMatchDetailFromHistory(${m.matchNumber})"
        class="p-4 rounded-2xl bg-white/70 dark:bg-white/5 border border-pastel-cardBorder hover:border-pastel-denim/40 transition-all shadow-sm flex flex-col justify-between gap-3 cursor-pointer group hover:scale-[1.01]"
        title="Haz clic para ver la clasificación detallada de esta partida"
      >
        <div class="flex items-center justify-between gap-2">
          <span class="text-xs font-black tracking-wider uppercase px-2 py-0.5 rounded-md bg-pastel-periwinkle/40 text-pastel-plum font-display group-hover:bg-pastel-denim group-hover:text-white transition-colors">
            Partida #${m.matchNumber}
          </span>
          <span class="text-[11px] text-pastel-plum/60 font-medium flex items-center gap-1.5">
            <i class="fa-regular fa-clock text-pastel-denim"></i> ${durationText} • ${dateText}
          </span>
        </div>

        <div class="py-2 flex items-center justify-around rounded-xl bg-white/40 dark:bg-white/5 border border-pastel-cardBorder/50">
          <div class="flex flex-col items-center">
            <span class="text-[11px] font-extrabold text-[#D9534F] uppercase tracking-wide">Equipo Rojo</span>
            <span class="text-2xl font-black text-[#A83232] font-display">${m.redPoints}</span>
          </div>

          <span class="text-xs font-black text-pastel-plum/40 uppercase">VS</span>

          <div class="flex flex-col items-center">
            <span class="text-[11px] font-extrabold text-[#4A72B2] uppercase tracking-wide">Equipo Azul</span>
            <span class="text-2xl font-black text-pastel-denim font-display">${m.bluePoints}</span>
          </div>
        </div>

        <div class="pt-2 border-t border-pastel-cardBorder/60 flex items-center justify-between text-xs">
          <span class="text-[11px] font-bold text-pastel-plum/70 flex items-center gap-1">
            <span>Ver tabla de la partida</span>
            <i class="fa-solid fa-arrow-right text-[10px] text-pastel-denim group-hover:translate-x-1 transition-transform"></i>
          </span>
          ${resultBadge}
        </div>
      </div>
    `;
  }).join('');
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
  if (redCountEl) redCountEl.textContent = `${redPlayers.length} ${redPlayers.length === 1 ? 'Jugador' : 'Jugadores'}`;
  if (blueCountEl) blueCountEl.textContent = `${bluePlayers.length} ${bluePlayers.length === 1 ? 'Jugador' : 'Jugadores'}`;

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
          <div class="text-[10px] text-pastel-plum/60 flex items-center gap-2.5">
            <span class="flex items-center gap-1"><i class="fa-solid fa-heart text-rose-500 text-[9px]"></i> ${p.health} HP</span>
            <span class="flex items-center gap-1"><i class="fa-solid fa-wifi text-pastel-denim text-[9px]"></i> ${p.ping}ms</span>
          </div>
        </div>
      </div>
      <div class="text-[11px] font-bold text-pastel-denim flex items-center gap-1">
        <i class="fa-solid fa-magnifying-glass text-[10px]"></i>
      </div>
    </div>
  `;
}
