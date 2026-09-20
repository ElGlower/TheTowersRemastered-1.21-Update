/**
 * Módulo de Modalidades y Leaderboard
 */

import { ModalitySubTab } from '../types';
import { OFFICIAL_LEADERBOARD } from '../data/leaderboardData';
import { showRightShowcase, hideRightShowcase } from './navigation';
import { getSkinViewer, loadSkinTexture } from './skinViewer';
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
  currentSubTab = 'partida';
  const viewPartida = document.getElementById('subview-partida');
  if (viewPartida) {
    viewPartida.classList.remove('hidden');
    viewPartida.classList.add('flex');
  }
  hideRightShowcase();
}

export function renderLeaderboard(): void {
  // Leaderboard apagado
}

export function selectLeaderboardPlayer(name: string, title: string, element?: HTMLElement): void {
  // Leaderboard apagado
}
