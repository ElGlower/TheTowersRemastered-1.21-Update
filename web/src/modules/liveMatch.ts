/**
 * DESTINY OWNERS - Módulo de Telemetría y Puente en Tiempo Real (liveMatch.ts)
 * Sincroniza datos en vivo del servidor Minecraft vía Firebase Realtime Database
 * Endpoint: https://destinyowners-23-default-rtdb.firebaseio.com/live.json
 * Leaderboard: https://destinyowners-23-default-rtdb.firebaseio.com/leaderboard.json
 */

import { selectLeaderboardPlayer } from './modalities';
import { loadSkinTexture } from './skinViewer';

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
  status?: 'STOPPED' | 'PREPARATION' | 'RUNNING' | 'ENDED' | string;
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

const FIREBASE_RTDB_BASE = 'https://destinyowners-23-default-rtdb.firebaseio.com';
let syncInterval: any = null;
let lastLiveState: LiveMatchData | null = null;
let lastLeaderboardMap: Record<string, PlayerStatsData> = {};

/**
 * Inicia la sincronización periódica con Firebase Realtime Database
 */
export function startLiveSync(): void {
  // Ejecutar primera consulta de inmediato
  fetchLiveState();
  fetchLeaderboardState();

  // Polling regular cada 2 segundos
  if (syncInterval) clearInterval(syncInterval);
  syncInterval = setInterval(() => {
    fetchLiveState();
    fetchLeaderboardState();
  }, 2000);
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
      syncLeaderboardWithLivePlayers();
    }
  } catch (err) {
    // Si falla la red temporalmente, no romper la UI
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
    }
    syncLeaderboardWithLivePlayers();
  } catch (err) {
    // Ignorar temporalmente
  }
}

/**
 * Sincroniza y fusiona los jugadores conectados con la tabla de clasificación
 */
function syncLeaderboardWithLivePlayers(): void {
  const mergedMap: Record<string, PlayerStatsData> = { ...lastLeaderboardMap };

  // Incorporar a todos los jugadores online detectados en vivo
  if (lastLiveState && lastLiveState.players && Array.isArray(lastLiveState.players)) {
    for (const p of lastLiveState.players) {
      if (!p.name || p.name === 'Vacío/Entorno') continue;
      const foundEntry = Object.values(mergedMap).find(s => s && s.name && s.name.toLowerCase() === p.name.toLowerCase());
      if (!foundEntry) {
        const key = p.uuid || p.name;
        mergedMap[key] = {
          name: p.name,
          kills: 0,
          deaths: 0,
          goals: 0,
          wins: 0
        };
      }
    }
  }

  renderRealLeaderboard(mergedMap);
}

/**
 * Renderiza el estado de la partida y equipos en la UI
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
      // Si están en el lobby, mostrar los jugadores esperando
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

/**
 * Renderiza el Leaderboard con los datos auténticos de Firebase
 */
function renderRealLeaderboard(statsMap: Record<string, PlayerStatsData>): void {
  const container = document.getElementById('leaderboard-list-container');
  if (!container) return;

  const playerList: Array<PlayerStatsData & { points: number; uuid: string }> = [];

  for (const [uuid, stats] of Object.entries(statsMap)) {
    if (!stats || !stats.name) continue;
    const goals = stats.goals || 0;
    const wins = stats.wins || 0;
    const kills = stats.kills || 0;
    const points = (goals * 100) + (wins * 50) + (kills * 10);

    playerList.push({
      ...stats,
      uuid,
      points
    });
  }

  // Ordenar por puntos desc, luego goles desc, luego kills desc
  playerList.sort((a, b) => b.points - a.points || (b.goals || 0) - (a.goals || 0) || (b.kills || 0) - (a.kills || 0));

  if (playerList.length === 0) {
    // Si la base de datos aún no tiene registros de jugadores
    container.innerHTML = `
      <div class="p-5 rounded-2xl bg-white/40 dark:bg-white/5 border border-pastel-cardBorder/40 text-center text-xs text-pastel-plum/70">
        <i class="fa-solid fa-gamepad text-pastel-denim text-base mb-1 block"></i>
        Esperando partidas oficiales en el servidor para generar el ranking en vivo...
      </div>
    `;
    return;
  }

  container.innerHTML = playerList.map((p, idx) => {
    const rank = idx + 1;
    const isFirst = rank === 1;
    const rankColor = rank === 1 ? 'bg-amber-500 text-white font-black' : rank === 2 ? 'bg-slate-300 text-slate-800 font-black' : rank === 3 ? 'bg-amber-700 text-white font-black' : 'bg-pastel-periwinkle/30 text-pastel-plum';
    const avatarUrl = `https://mc-heads.net/avatar/${p.name}/32`;

    return `
      <div 
        data-username="${p.name}" 
        class="leaderboard-row ${isFirst ? 'active-player' : ''} p-3.5 rounded-2xl flex items-center justify-between gap-3 cursor-pointer transition-all"
        onclick="window.destinyApp.inspectMinecraftPlayer('${p.name}', this)"
      >
        <div class="flex items-center gap-3.5">
          <span class="w-8 h-8 rounded-full ${rankColor} border border-pastel-cardBorder flex items-center justify-center text-xs">#${rank}</span>
          <img 
            src="${avatarUrl}" 
            alt="${p.name}" 
            class="w-7 h-7 rounded-lg shadow-sm"
            onerror="this.src='https://minotar.net/avatar/${p.name}/32'"
          />
          <div>
            <div class="text-sm font-bold text-pastel-plum flex items-center gap-1.5">
              <span>${p.name}</span>
              ${isFirst ? '<i class="fa-solid fa-crown text-[11px] text-[#D97706]"></i>' : ''}
            </div>
            <div class="text-[11px] text-pastel-plum/60 font-medium">
              Goles: ${p.goals || 0} • Bajas: ${p.kills || 0} • Victorias: ${p.wins || 0}
            </div>
          </div>
        </div>
        <div class="flex items-center gap-4">
          <span class="text-sm font-extrabold text-pastel-denim font-display">${p.points} pts</span>
        </div>
      </div>
    `;
  }).join('');
}

/**
 * Inspecciona un jugador de Minecraft por su nombre de usuario,
 * cargando su skin 3D real y actualizando el plinto.
 */
export function inspectMinecraftPlayer(username: string, element?: HTMLElement): void {
  if (!username || username === 'Vacío/Entorno' || username === 'Unknown') return;

  // 1. Resaltar fila seleccionada si existe
  if (element) {
    document.querySelectorAll('.leaderboard-row').forEach(row => {
      row.classList.remove('active-player');
    });
    element.classList.add('active-player');
  }

  // 2. Actualizar Notch superior
  const badge = document.getElementById('notch-avatar-badge');
  const titleEl = document.getElementById('notch-player-title');
  const iconEl = document.getElementById('notch-player-icon');

  if (badge) badge.textContent = username.substring(0, 2).toUpperCase();
  if (titleEl) titleEl.textContent = username;
  if (iconEl) iconEl.className = 'fa-solid fa-circle-check text-[11px] text-pastel-denim';

  // 3. Cargar la skin del jugador en el visor 3D WebGL
  const skinUrl = `https://minotar.net/skin/${username}`;
  loadSkinTexture(skinUrl);

  // 4. Mostrar el panel 3D
  if ((window as any).destinyApp && (window as any).destinyApp.showRightShowcase) {
    (window as any).destinyApp.showRightShowcase();
  }
}
