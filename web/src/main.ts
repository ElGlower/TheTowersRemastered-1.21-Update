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
import { 
  startLiveSync, 
  inspectMinecraftPlayer, 
  filterLeaderboard, 
  copyCurrentPlayerName, 
  openNameMCProfile,
  openFullUserProfile,
  closeFullUserProfile 
} from './modules/liveMatch';
import {
  openShareModal,
  closeShareModal,
  downloadCardImage,
  copyCardImage,
  shareOnTwitter,
  copyProfileLink
} from './modules/shareCard';
import { TabId, ModalitySubTab } from './types';

// Puente global unificado para controladores HTML
const app = {
  switchTab: (tabId: TabId) => switchTab(tabId),
  toggleDarkMode: () => toggleDarkMode(),
  openModalityDetail: (modalityId: string) => openModalityDetail(modalityId),
  closeModalityDetail: () => closeModalityDetail(),
  switchModalitySubTab: (subTabId: ModalitySubTab) => switchModalitySubTab(subTabId),
  selectPlayer: (name: string, title: string, element?: HTMLElement) => selectLeaderboardPlayer(name, title, element),
  inspectMinecraftPlayer: (username: string, element?: HTMLElement) => inspectMinecraftPlayer(username, element),
  filterLeaderboard: (query: string) => filterLeaderboard(query),
  copyCurrentPlayerName: (name?: string) => copyCurrentPlayerName(name),
  openNameMCProfile: (name?: string) => openNameMCProfile(name),
  openFullUserProfile: (username?: string) => openFullUserProfile(username),
  closeFullUserProfile: () => closeFullUserProfile(),
  openShareModal: (username?: string) => openShareModal(username || (window as any).destinyApp.currentlyInspectedName),
  closeShareModal: () => closeShareModal(),
  downloadCardImage: () => downloadCardImage(),
  copyCardImage: () => copyCardImage(),
  shareOnTwitter: () => shareOnTwitter(),
  copyProfileLink: () => copyProfileLink(),
  showRightShowcase: () => showRightShowcase(),
  hideRightShowcase: () => hideRightShowcase(),
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
(window as any).inspectMinecraftPlayer = app.inspectMinecraftPlayer;
(window as any).filterLeaderboard = app.filterLeaderboard;
(window as any).copyCurrentPlayerName = app.copyCurrentPlayerName;
(window as any).openNameMCProfile = app.openNameMCProfile;
(window as any).openFullUserProfile = app.openFullUserProfile;
(window as any).closeFullUserProfile = app.closeFullUserProfile;
(window as any).openShareModal = app.openShareModal;
(window as any).closeShareModal = app.closeShareModal;
(window as any).downloadCardImage = app.downloadCardImage;
(window as any).copyCardImage = app.copyCardImage;
(window as any).shareOnTwitter = app.shareOnTwitter;
(window as any).copyProfileLink = app.copyProfileLink;
(window as any).toggleAutoRotate = app.toggleAutoRotate;
(window as any).copyServerIP = app.copyServerIP;
(window as any).showToast = app.showToast;

// Inicialización cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
  // 1. Inicializar tema (Día / Noche)
  initTheme();

  // 2. Renderizar datos oficiales auténticos
  renderTeamRoster();

  // 3. Inicializar visor 3D WebGL (fallback / background)
  initSkinViewer();

  // 4. Iniciar sincronización en tiempo real de Partida y Leaderboard con Firebase RTDB
  startLiveSync();

  // 5. Procesar parámetros de URL (?tab=...&modality=...&sub=...&profile=...)
  const params = new URLSearchParams(window.location.search);
  const tabParam = params.get('tab') as TabId | null;
  const modalityParam = params.get('modality');
  const profileParam = params.get('profile');
  const shareParam = params.get('share');

  if (tabParam && ['inicio', 'modalidad', 'miembros'].includes(tabParam)) {
    if (tabParam === 'modalidad' && modalityParam === 'the-towers') {
      switchTab('modalidad');
      const subParam = (params.get('sub') as ModalitySubTab) || 'partida';
      openModalityDetail('the-towers', true);
      switchModalitySubTab(subParam);

      if (profileParam) {
        setTimeout(() => {
          openFullUserProfile(profileParam);
        }, 350);
      }

      if (shareParam) {
        setTimeout(() => {
          openShareModal(shareParam);
        }, 500);
      }
    } else {
      switchTab(tabParam);
    }
  } else if (shareParam) {
    switchTab('modalidad');
    openModalityDetail('the-towers', true);
    setTimeout(() => {
      openShareModal(shareParam);
    }, 500);
  } else {
    switchTab('inicio');
  }

  console.log('Destiny Towers TypeScript Web Companion 2026 inicializado.');
});
