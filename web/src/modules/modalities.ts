/**
 * Módulo de Modalidades y Subpestañas (Partida vs Leaderboard)
 */

import { ModalitySubTab } from '../types';
import { showRightShowcase, hideRightShowcase, setRightPanelMode } from './navigation';
import { getIsDarkMode } from './theme';
import { renderRealLeaderboard, inspectMinecraftPlayer, renderMatchesHistory } from './liveMatch';

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
  const btnPartidas = document.getElementById('subtab-btn-partidas');
  const viewPartida = document.getElementById('subview-partida');
  const viewLeaderboard = document.getElementById('subview-leaderboard');
  const viewPartidas = document.getElementById('subview-partidas');

  const activeBtnClass = 'px-4 py-1.5 rounded-full text-xs font-bold transition-all duration-200 bg-pastel-denim text-white shadow-sm flex items-center gap-2 flex-shrink-0';
  const inactiveBtnClass = isDark
    ? 'px-4 py-1.5 rounded-full text-xs font-medium text-slate-300 hover:text-white transition-all duration-200 flex items-center gap-2 flex-shrink-0'
    : 'px-4 py-1.5 rounded-full text-xs font-medium text-pastel-plum hover:text-pastel-denim transition-all duration-200 flex items-center gap-2 flex-shrink-0';

  if (btnPartida) btnPartida.className = (subTabId === 'partida') ? activeBtnClass : inactiveBtnClass;
  if (btnLeaderboard) btnLeaderboard.className = (subTabId === 'leaderboard') ? activeBtnClass : inactiveBtnClass;
  if (btnPartidas) btnPartidas.className = (subTabId === 'partidas') ? activeBtnClass : inactiveBtnClass;

  if (viewPartida) {
    if (subTabId === 'partida') {
      viewPartida.classList.remove('hidden');
      viewPartida.classList.add('flex');
    } else {
      viewPartida.classList.add('hidden');
      viewPartida.classList.remove('flex');
    }
  }

  if (viewLeaderboard) {
    if (subTabId === 'leaderboard') {
      viewLeaderboard.classList.remove('hidden');
      viewLeaderboard.classList.add('flex');
    } else {
      viewLeaderboard.classList.add('hidden');
      viewLeaderboard.classList.remove('flex');
    }
  }

  if (viewPartidas) {
    if (subTabId === 'partidas') {
      viewPartidas.classList.remove('hidden');
      viewPartidas.classList.add('flex');
    } else {
      viewPartidas.classList.add('hidden');
      viewPartidas.classList.remove('flex');
    }
  }

  if (subTabId === 'leaderboard') {
    renderRealLeaderboard();
    setRightPanelMode('leaderboard');
    showRightShowcase();
  } else if (subTabId === 'partidas') {
    renderMatchesHistory();
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
