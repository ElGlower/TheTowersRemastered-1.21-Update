/**
 * Módulo de Modalidades y Subpestañas (Partida vs Leaderboard)
 */

import { ModalitySubTab } from '../types';
import { showRightShowcase, hideRightShowcase, setRightPanelMode } from './navigation';
import { getIsDarkMode } from './theme';
import { renderRealLeaderboard, inspectMinecraftPlayer } from './liveMatch';

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
  const userProfile = document.getElementById('modalidad-user-profile');
  if (!selector || !detail) return;

  isDetailOpen = false;

  detail.style.opacity = '0';
  detail.style.transform = 'scale(0.98)';
  if (userProfile) {
    userProfile.style.opacity = '0';
    userProfile.classList.add('hidden');
    userProfile.style.display = 'none';
  }

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
      btnPartida.className = 'px-4 py-1.5 rounded-full text-xs font-bold transition-all duration-200 bg-pastel-denim text-white shadow-sm flex items-center gap-2';
    }
    if (btnLeaderboard) {
      btnLeaderboard.className = isDark
        ? 'px-4 py-1.5 rounded-full text-xs font-medium text-slate-300 hover:text-white transition-all duration-200 flex items-center gap-2'
        : 'px-4 py-1.5 rounded-full text-xs font-medium text-pastel-plum hover:text-pastel-denim transition-all duration-200 flex items-center gap-2';
    }

    if (viewLeaderboard) {
      viewLeaderboard.classList.add('hidden');
      viewLeaderboard.classList.remove('flex');
    }
    if (viewPartida) {
      viewPartida.classList.remove('hidden');
      viewPartida.classList.add('flex');
    }
  } else if (subTabId === 'leaderboard') {
    if (btnLeaderboard) {
      btnLeaderboard.className = 'px-4 py-1.5 rounded-full text-xs font-bold transition-all duration-200 bg-pastel-denim text-white shadow-sm flex items-center gap-2';
    }
    if (btnPartida) {
      btnPartida.className = isDark
        ? 'px-4 py-1.5 rounded-full text-xs font-medium text-slate-300 hover:text-white transition-all duration-200 flex items-center gap-2'
        : 'px-4 py-1.5 rounded-full text-xs font-medium text-pastel-plum hover:text-pastel-denim transition-all duration-200 flex items-center gap-2';
    }

    if (viewPartida) {
      viewPartida.classList.add('hidden');
      viewPartida.classList.remove('flex');
    }
    if (viewLeaderboard) {
      viewLeaderboard.classList.remove('hidden');
      viewLeaderboard.classList.add('flex');
    }

    renderRealLeaderboard();
    setRightPanelMode('leaderboard');
    showRightShowcase();
  }
}

export function renderLeaderboard(): void {
  renderRealLeaderboard();
}

export function selectLeaderboardPlayer(name: string, _title: string, element?: HTMLElement): void {
  inspectMinecraftPlayer(name, element);
}
