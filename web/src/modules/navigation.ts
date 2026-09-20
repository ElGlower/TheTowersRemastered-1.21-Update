/**
 * Módulo de Navegación Fluida y Gestión de Columnas
 */

import { TabId } from '../types';
import { getIsDarkMode } from './theme';
import { isModalityDetailOpenState, getCurrentModalitySubTab } from './modalities';

let currentTab: TabId = 'inicio';
let showcaseTimer: number | null = null;

export function getCurrentTab(): TabId {
  return currentTab;
}

export function showRightShowcase(): void {
  // Si el perfil detallado expandido está activo o visible, NUNCA mostrar el panel lateral
  const userProfile = document.getElementById('modalidad-user-profile');
  if (userProfile && !userProfile.classList.contains('hidden')) {
    return;
  }

  const leftCol = document.getElementById('left-content-area');
  const rightPanel = document.getElementById('right-showcase-panel');
  if (!leftCol || !rightPanel) return;

  if (showcaseTimer !== null) {
    window.clearTimeout(showcaseTimer);
    showcaseTimer = null;
  }

  leftCol.className = 'lg:col-span-7 flex flex-col justify-stretch';
  rightPanel.classList.remove('hidden');
  rightPanel.style.display = 'flex';
  showcaseTimer = window.setTimeout(() => {
    rightPanel.style.opacity = '1';
    rightPanel.style.transform = 'translateX(0)';
  }, 20);
}

export function hideRightShowcase(immediate: boolean = false): void {
  const leftCol = document.getElementById('left-content-area');
  const rightPanel = document.getElementById('right-showcase-panel');
  if (!leftCol || !rightPanel) return;

  if (showcaseTimer !== null) {
    window.clearTimeout(showcaseTimer);
    showcaseTimer = null;
  }

  if (immediate) {
    rightPanel.style.opacity = '0';
    rightPanel.style.display = 'none';
    rightPanel.classList.add('hidden');
    leftCol.className = 'lg:col-span-12 flex flex-col justify-stretch';
    return;
  }

  rightPanel.style.opacity = '0';
  rightPanel.style.transform = 'translateX(24px)';
  showcaseTimer = window.setTimeout(() => {
    rightPanel.style.display = 'none';
    rightPanel.classList.add('hidden');
    leftCol.className = 'lg:col-span-12 flex flex-col justify-stretch';
  }, 220);
}

export function switchTab(tabId: TabId): void {
  if (!['inicio', 'modalidad', 'miembros'].includes(tabId)) return;
  currentTab = tabId;

  // 1. Alternar vistas activas
  document.querySelectorAll('.tab-view').forEach(view => {
    view.classList.remove('active');
  });
  const targetView = document.getElementById(`view-${tabId}`);
  if (targetView) {
    targetView.classList.add('active');
  }

  // 2. Actualizar estilos de los botones de navegación
  updateNavButtonsStyle();

  // 3. Control inteligente del panel lateral 3D
  if (tabId === 'inicio') {
    showRightShowcase();
    resetShowcaseNotch();
  } else if (tabId === 'modalidad') {
    if (isModalityDetailOpenState() && getCurrentModalitySubTab() === 'leaderboard') {
      showRightShowcase();
    } else {
      hideRightShowcase();
    }
  } else {
    // En miembros siempre se oculta para dar todo el ancho
    hideRightShowcase();
  }

  // 4. Sincronizar URL
  const url = new URL(window.location.href);
  url.searchParams.set('tab', tabId);
  window.history.replaceState({}, '', url.toString());
}

export function updateNavButtonsStyle(): void {
  const isDark = getIsDarkMode();
  const tabs: TabId[] = ['inicio', 'modalidad', 'miembros'];

  tabs.forEach(t => {
    const btn = document.getElementById(`nav-btn-${t}`);
    if (!btn) return;

    if (t === currentTab) {
      btn.className = 'px-7 py-2 rounded-full text-sm font-semibold transition-all duration-200 bg-pastel-denim text-white shadow-md flex items-center gap-2';
    } else {
      btn.className = isDark
        ? 'px-7 py-2 rounded-full text-sm font-medium text-slate-300 hover:text-white transition-all duration-200 flex items-center gap-2'
        : 'px-7 py-2 rounded-full text-sm font-medium text-pastel-plum hover:text-pastel-denim transition-all duration-200 flex items-center gap-2';
    }
  });
}

export function resetShowcaseNotch(): void {
  const badge = document.getElementById('notch-avatar-badge');
  const titleEl = document.getElementById('notch-player-title');
  const iconEl = document.getElementById('notch-player-icon');

  if (badge) badge.textContent = 'DO';
  if (titleEl) titleEl.textContent = 'Destiny Owners';
  if (iconEl) iconEl.className = 'fa-solid fa-circle-check text-[11px] text-pastel-denim';
}
