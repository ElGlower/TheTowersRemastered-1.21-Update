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

const FIREBASE_RTDB_BASE = 'https://destinyowners-23-default-rtdb.firebaseio.com';
let syncInterval: any = null;
let lastLiveState: LiveMatchData | null = null;
let lastLeaderboardMap: Record<string, PlayerStatsData> = {};
let currentSearchFilter: string = '';
let currentlyInspectedName: string | null = null;

export function getCurrentlyInspectedName(): string | null {
  return currentlyInspectedName;
}

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
  const statsEntry = Object.values(lastLeaderboardMap).find(
    s => s && s.name && s.name.toLowerCase() === username.toLowerCase()
  );

  const goals = statsEntry?.goals || 0;
  const wins = statsEntry?.wins || 0;
  const kills = statsEntry?.kills || 0;
  const deaths = statsEntry?.deaths || 0;
  const rawPoints = (goals * 150) + (wins * 100) + (kills * 15) - (deaths * 2);
  const points = Math.max(0, rawPoints);
  const kdRatio = (kills / Math.max(1, deaths)).toFixed(2);

  let rank = 1;
  const allEntries = Object.values(lastLeaderboardMap).filter(s => s && s.name);
  for (const other of allEntries) {
    const otherPoints = Math.max(0, ((other.goals || 0) * 150) + ((other.wins || 0) * 100) + ((other.kills || 0) * 15) - ((other.deaths || 0) * 2));
    if (otherPoints > points) rank++;
  }

  const isOnline = checkPlayerOnline(username);
  const isStaff = username.toLowerCase() === 'elglower';

  return {
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

  const roleBadge = p.isStaff
    ? '<span class="text-[10px] font-black px-2.5 py-0.5 rounded-md bg-purple-600 text-white tracking-wider shadow-sm">✦ DESTINY ADMIN</span>'
    : p.rank <= 3
    ? '<span class="text-[10px] font-black px-2 py-0.5 rounded-md bg-amber-500/20 text-amber-800 dark:text-amber-200 border border-amber-500/30">★ TOP JUGADOR</span>'
    : '<span class="text-[10px] font-semibold px-2 py-0.5 rounded-md bg-pastel-periwinkle/30 text-pastel-plum border border-pastel-cardBorder">JUGADOR DESTINY</span>';

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
    if (profile.isStaff) {
      roleBadge.className = 'text-[11px] font-black px-3.5 py-1 rounded-full bg-purple-600 text-white tracking-wider shadow-sm';
      roleBadge.textContent = '✦ DESTINY ADMIN';
    } else if (profile.rank <= 3) {
      roleBadge.className = 'text-[11px] font-black px-3.5 py-1 rounded-full bg-amber-500/20 text-amber-800 dark:text-amber-200 border border-amber-500/30';
      roleBadge.textContent = '★ TOP JUGADOR';
    } else {
      roleBadge.className = 'text-[11px] font-semibold px-3.5 py-1 rounded-full bg-pastel-periwinkle/30 text-pastel-plum border border-pastel-cardBorder';
      roleBadge.textContent = 'JUGADOR DESTINY';
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

  // 2. Partidas oficiales consolidadas según las estadísticas del jugador
  if (p.goals > 0 || p.kills > 0) {
    historyItems.push(`
      <div class="py-3.5 px-2 flex items-center justify-between transition-colors hover:bg-pastel-plum/5 dark:hover:bg-white/5 rounded-xl">
        <div class="flex items-center gap-3.5">
          <span class="w-8 h-8 rounded-full bg-pastel-denim/15 text-pastel-denim flex items-center justify-center text-xs flex-shrink-0">
            <i class="fa-solid fa-trophy"></i>
          </span>
          <div>
            <div class="text-xs font-bold text-pastel-plum">The Towers 4v4 • Partida de Clasificación</div>
            <div class="text-[11px] text-pastel-plum/60 font-medium">Aportación: ${p.goals} goles • ${p.kills} bajas totales</div>
          </div>
        </div>
        <span class="text-xs font-extrabold px-2.5 py-0.5 rounded-full bg-pastel-denim/15 text-pastel-denim font-display flex-shrink-0">Victoria Oficial</span>
      </div>
    `);
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
