/**
 * DESTINY OWNERS - Aplicación Principal (main.ts)
 * Arquitectura modular moderna en TypeScript (Estándar 2026)
 */

import { initTheme, toggleDarkMode } from './modules/theme';
import { initSkinViewer, toggleAutoRotate } from './modules/skinViewer';
import { switchTab, showRightShowcase, hideRightShowcase } from './modules/navigation';
import { 
  openModalityDetail, 
  closeModalityDetail, 
  switchModalitySubTab, 
  selectLeaderboardPlayer,
  renderLeaderboard 
} from './modules/modalities';
import { renderTeamRoster } from './modules/roster';
import { copyServerIP, showToast } from './modules/clipboard';
import { TabId, ModalitySubTab } from './types';

// Puente global unificado para retrocompatibilidad con controladores HTML
const app = {
  switchTab: (tabId: TabId) => switchTab(tabId),
  toggleDarkMode: () => toggleDarkMode(),
  openModalityDetail: (modalityId: string) => openModalityDetail(modalityId),
  closeModalityDetail: () => closeModalityDetail(),
  switchModalitySubTab: (subTabId: ModalitySubTab) => switchModalitySubTab(subTabId),
  selectPlayer: (name: string, title: string, element?: HTMLElement) => selectLeaderboardPlayer(name, title, element),
  toggleAutoRotate: () => toggleAutoRotate(),
  copyServerIP: () => copyServerIP(),
  showToast: (msg: string) => showToast(msg)
};

// Asignar al objeto global de la ventana
(window as any).destinyApp = app;
(window as any).switchTab = app.switchTab;
(window as any).toggleDarkMode = app.toggleDarkMode;
(window as any).openModalityDetail = app.openModalityDetail;
(window as any).closeModalityDetail = app.closeModalityDetail;
(window as any).switchModalitySubTab = app.switchModalitySubTab;
(window as any).toggleAutoRotate = app.toggleAutoRotate;
(window as any).copyServerIP = app.copyServerIP;
(window as any).showToast = app.showToast;

// Inicialización cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
  // 1. Inicializar tema (Día / Noche)
  initTheme();

  // 2. Renderizar datos oficiales auténticos
  renderLeaderboard();
  renderTeamRoster();

  // 3. Inicializar visor 3D WebGL
  initSkinViewer();

  // 4. Procesar parámetros de URL (?tab=...&modality=...&sub=...)
  const params = new URLSearchParams(window.location.search);
  const tabParam = params.get('tab') as TabId | null;
  const modalityParam = params.get('modality');

  if (tabParam && ['inicio', 'modalidad', 'miembros'].includes(tabParam)) {
    if (tabParam === 'modalidad' && modalityParam === 'the-towers') {
      switchTab('modalidad');
      const subParam = (params.get('sub') as ModalitySubTab) || 'partida';
      openModalityDetail('the-towers', true);
      switchModalitySubTab(subParam);
    } else {
      switchTab(tabParam);
    }
  } else {
    switchTab('inicio');
  }

  console.log('Destiny Towers TypeScript Web Companion 2026 inicializado.');
});
