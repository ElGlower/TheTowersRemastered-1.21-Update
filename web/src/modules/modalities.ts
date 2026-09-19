/**
 * Módulo de Modalidades y Leaderboard
 */

import { ModalitySubTab } from '../types';
import { OFFICIAL_LEADERBOARD } from '../data/leaderboardData';
import { showRightShowcase, hideRightShowcase } from './navigation';
import { getSkinViewer } from './skinViewer';
import { getIsDarkMode } from './theme';
import { showToast } from './clipboard';

let isDetailOpen = false;
let currentSubTab: ModalitySubTab = 'partida';

export function isModalityDetailOpenState(): boolean {
  return isDetailOpen;
}

export function getCurrentModalitySubTab(): ModalitySubTab {
  return currentSubTab;
}

export function openModalityDetail(modalityId: string, immediate: boolean = false): void {
  const selector = document.getElementById('modalidad-selector');
  const detail = document.getElementById('modalidad-detail');
  if (!selector || !detail) return;

  isDetailOpen = true;

  if (immediate) {
    selector.classList.add('hidden');
    selector.style.display = 'none';
    detail.classList.remove('hidden');
    detail.style.display = 'flex';
    detail.style.opacity = '1';
    detail.style.transform = 'scale(1)';
    switchModalitySubTab(currentSubTab || 'partida');
    return;
  }

  selector.style.opacity = '0';
  selector.style.transform = 'scale(0.98)';

  setTimeout(() => {
    selector.classList.add('hidden');
    selector.style.display = 'none';
    detail.classList.remove('hidden');
    detail.style.display = 'flex';
    detail.style.opacity = '0';
    detail.style.transform = 'scale(0.98)';

    switchModalitySubTab(currentSubTab || 'partida');

    setTimeout(() => {
      detail.style.opacity = '1';
      detail.style.transform = 'scale(1)';
    }, 20);
  }, 180);
}

export function closeModalityDetail(): void {
  const selector = document.getElementById('modalidad-selector');
  const detail = document.getElementById('modalidad-detail');
  if (!selector || !detail) return;

  isDetailOpen = false;

  detail.style.opacity = '0';
  detail.style.transform = 'scale(0.98)';

  setTimeout(() => {
    detail.classList.add('hidden');
    detail.style.display = 'none';
    selector.classList.remove('hidden');
    selector.style.display = 'flex';
    selector.style.opacity = '0';
    selector.style.transform = 'scale(0.98)';

    hideRightShowcase();

    setTimeout(() => {
      selector.style.opacity = '1';
      selector.style.transform = 'scale(1)';
    }, 20);
  }, 180);
}

export function switchModalitySubTab(subTabId: ModalitySubTab): void {
  currentSubTab = subTabId;
  const isDark = getIsDarkMode();
  const btnPartida = document.getElementById('subtab-btn-partida');
  const btnLeaderboard = document.getElementById('subtab-btn-leaderboard');
  const viewPartida = document.getElementById('subview-partida');
  const viewLeaderboard = document.getElementById('subview-leaderboard');

  if (subTabId === 'partida') {
    if (btnPartida) {
      btnPartida.className = 'px-5 py-1.5 rounded-full text-xs font-bold transition-all duration-200 bg-pastel-denim text-white shadow-sm flex items-center gap-2';
    }
    if (btnLeaderboard) {
      btnLeaderboard.className = isDark
        ? 'px-5 py-1.5 rounded-full text-xs font-medium text-slate-300 hover:text-white transition-all duration-200 flex items-center gap-2'
        : 'px-5 py-1.5 rounded-full text-xs font-medium text-pastel-plum hover:text-pastel-denim transition-all duration-200 flex items-center gap-2';
    }

    if (viewLeaderboard) {
      viewLeaderboard.classList.add('hidden');
      viewLeaderboard.classList.remove('flex');
    }
    if (viewPartida) {
      viewPartida.classList.remove('hidden');
      viewPartida.classList.add('flex');
    }

    hideRightShowcase();
  } else if (subTabId === 'leaderboard') {
    if (btnLeaderboard) {
      btnLeaderboard.className = 'px-5 py-1.5 rounded-full text-xs font-bold transition-all duration-200 bg-pastel-denim text-white shadow-sm flex items-center gap-2';
    }
    if (btnPartida) {
      btnPartida.className = isDark
        ? 'px-5 py-1.5 rounded-full text-xs font-medium text-slate-300 hover:text-white transition-all duration-200 flex items-center gap-2'
        : 'px-5 py-1.5 rounded-full text-xs font-medium text-pastel-plum hover:text-pastel-denim transition-all duration-200 flex items-center gap-2';
    }

    if (viewPartida) {
      viewPartida.classList.add('hidden');
      viewPartida.classList.remove('flex');
    }
    if (viewLeaderboard) {
      viewLeaderboard.classList.remove('hidden');
      viewLeaderboard.classList.add('flex');
    }

    showRightShowcase();
  }
}

export function renderLeaderboard(): void {
  const container = document.getElementById('leaderboard-list-container');
  if (!container) return;

  container.innerHTML = OFFICIAL_LEADERBOARD.map((p, idx) => {
    const isFirst = idx === 0;

    return `
      <div 
        data-username="${p.username}" 
        data-role="${p.roleTitle}"
        class="leaderboard-row ${isFirst ? 'active-player' : ''} p-4 rounded-2xl flex items-center justify-between gap-3 cursor-pointer transition-all"
        onclick="window.destinyApp.selectPlayer('${p.username} #${p.rank}', '${p.roleTitle}', this)"
      >
        <div class="flex items-center gap-3.5">
          <span class="w-8 h-8 rounded-full bg-pastel-periwinkle/30 text-pastel-plum border border-pastel-cardBorder flex items-center justify-center text-xs font-bold">#${p.rank}</span>
          <div>
            <div class="text-sm font-bold text-pastel-plum">
              <span>${p.username}</span>
            </div>
            <div class="text-[11px] text-pastel-plum/60 font-medium">
              Esperando datos del juego...
            </div>
          </div>
        </div>
        <div class="flex items-center gap-4">
          <span class="text-xs font-semibold text-pastel-plum/50 font-display">-- pts</span>
        </div>
      </div>
    `;
  }).join('');
}

export function selectLeaderboardPlayer(name: string, title: string, element?: HTMLElement): void {
  document.querySelectorAll('.leaderboard-row').forEach(row => {
    row.classList.remove('active-player');
  });
  if (element) {
    element.classList.add('active-player');
  }

  const badge = document.getElementById('notch-avatar-badge');
  const titleEl = document.getElementById('notch-player-title');
  const iconEl = document.getElementById('notch-player-icon');

  if (badge) badge.textContent = name.substring(0, 2).toUpperCase();
  if (titleEl) titleEl.textContent = name;
  if (iconEl) {
    iconEl.className = 'fa-solid fa-circle-check text-[11px] text-pastel-denim';
  }

  const viewer = getSkinViewer();
  if (viewer && viewer.playerObject) {
    viewer.playerObject.rotation.y = 0.45;
  }

  showRightShowcase();
}
