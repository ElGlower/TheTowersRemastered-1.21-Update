/**
 * DESTINY OWNERS - Lógica de Navegación y UI (main.js)
 * Conmutador fluido de pestañas, selector y detalle de modalidades,
 * Leaderboard interactivo con inspección 3D, Modo Noche/Día y portapapeles.
 */

let currentTab = 'inicio';
let isDarkMode = false;
let isModalityDetailOpen = false;
let currentModalitySubTab = 'partida';
let showcaseTimer = null;

/**
 * Muestra el panel lateral 3D (7 columnas a la izquierda, 5 a la derecha)
 */
function showRightShowcase() {
  const leftCol = document.getElementById('left-content-area');
  const rightPanel = document.getElementById('right-showcase-panel');
  if (!leftCol || !rightPanel) return;

  if (showcaseTimer) {
    clearTimeout(showcaseTimer);
    showcaseTimer = null;
  }

  leftCol.className = 'lg:col-span-7 flex flex-col justify-stretch';
  rightPanel.style.display = 'flex';
  showcaseTimer = setTimeout(() => {
    rightPanel.style.opacity = '1';
    rightPanel.style.transform = 'translateX(0)';
  }, 20);
}

/**
 * Oculta el panel lateral 3D (12 columnas completas a la izquierda)
 */
function hideRightShowcase() {
  const leftCol = document.getElementById('left-content-area');
  const rightPanel = document.getElementById('right-showcase-panel');
  if (!leftCol || !rightPanel) return;

  if (showcaseTimer) {
    clearTimeout(showcaseTimer);
    showcaseTimer = null;
  }

  rightPanel.style.opacity = '0';
  rightPanel.style.transform = 'translateX(24px)';
  showcaseTimer = setTimeout(() => {
    rightPanel.style.display = 'none';
    leftCol.className = 'lg:col-span-12 flex flex-col justify-stretch';
  }, 220);
}

/**
 * Cambia fluidamente entre las pestañas (Inicio, Modalidad, Miembros)
 * @param {string} tabId 'inicio' | 'modalidad' | 'miembros'
 */
function switchTab(tabId) {
  if (!['inicio', 'modalidad', 'miembros'].includes(tabId)) return;
  currentTab = tabId;

  // 1. Ocultar todas las vistas y activar la seleccionada
  document.querySelectorAll('.tab-view').forEach(view => {
    view.classList.remove('active');
  });
  const targetView = document.getElementById(`view-${tabId}`);
  if (targetView) {
    targetView.classList.add('active');
  }

  // 2. Actualizar estados visuales de los botones de navegación
  updateNavButtonsTheme();

  // 3. Controlar visibilidad del panel lateral 3D según la pestaña y contexto
  if (tabId === 'inicio') {
    showRightShowcase();
    resetShowcaseNotch();
  } else if (tabId === 'modalidad') {
    // Si estamos dentro del detalle de The Towers y en la subpestaña Leaderboard, mostrar el 3D
    if (isModalityDetailOpen && currentModalitySubTab === 'leaderboard') {
      showRightShowcase();
    } else {
      hideRightShowcase();
    }
  } else {
    // En 'miembros' siempre se oculta para dar todo el ancho a los 8 slots
    hideRightShowcase();
  }

  // 4. Actualizar parámetro en la URL sin recargar la página
  const url = new URL(window.location);
  url.searchParams.set('tab', tabId);
  window.history.replaceState({}, '', url);
}

/**
 * Abre el detalle de una modalidad específica (The Towers)
 * @param {string} modalityId 
 * @param {boolean} immediate
 */
function openModalityDetail(modalityId, immediate = false) {
  const selector = document.getElementById('modalidad-selector');
  const detail = document.getElementById('modalidad-detail');
  if (!selector || !detail) return;

  isModalityDetailOpen = true;

  if (immediate) {
    selector.classList.add('hidden');
    selector.style.display = 'none';
    detail.classList.remove('hidden');
    detail.style.display = 'flex';
    detail.style.opacity = '1';
    detail.style.transform = 'scale(1)';
    switchModalitySubTab(currentModalitySubTab || 'partida');
    return;
  }

  // Transición suave: ocultar selector
  selector.style.opacity = '0';
  selector.style.transform = 'scale(0.98)';

  setTimeout(() => {
    selector.classList.add('hidden');
    selector.style.display = 'none';
    detail.classList.remove('hidden');
    detail.style.display = 'flex';
    detail.style.opacity = '0';
    detail.style.transform = 'scale(0.98)';

    // Restablecer o sincronizar subpestaña (por defecto 'partida')
    switchModalitySubTab(currentModalitySubTab || 'partida');

    setTimeout(() => {
      detail.style.opacity = '1';
      detail.style.transform = 'scale(1)';
    }, 20);
  }, 180);
}

/**
 * Cierra la vista detallada y regresa al selector de modalidades (cards lado a lado)
 */
function closeModalityDetail() {
  const selector = document.getElementById('modalidad-selector');
  const detail = document.getElementById('modalidad-detail');
  if (!selector || !detail) return;

  isModalityDetailOpen = false;

  detail.style.opacity = '0';
  detail.style.transform = 'scale(0.98)';

  setTimeout(() => {
    detail.classList.add('hidden');
    detail.style.display = 'none';
    selector.classList.remove('hidden');
    selector.style.display = 'flex';
    selector.style.opacity = '0';
    selector.style.transform = 'scale(0.98)';

    // En el selector, el panel 3D se oculta para que las cards abarquen el 100%
    hideRightShowcase();

    setTimeout(() => {
      selector.style.opacity = '1';
      selector.style.transform = 'scale(1)';
    }, 20);
  }, 180);
}

/**
 * Conmuta entre las subpestañas de The Towers ('partida' | 'leaderboard')
 * @param {string} subTabId 
 */
function switchModalitySubTab(subTabId) {
  currentModalitySubTab = subTabId;
  const btnPartida = document.getElementById('subtab-btn-partida');
  const btnLeaderboard = document.getElementById('subtab-btn-leaderboard');
  const viewPartida = document.getElementById('subview-partida');
  const viewLeaderboard = document.getElementById('subview-leaderboard');

  if (subTabId === 'partida') {
    // Actualizar estilos botones
    if (btnPartida) {
      btnPartida.className = 'px-5 py-1.5 rounded-full text-xs font-bold transition-all duration-200 bg-pastel-denim text-white shadow-sm flex items-center gap-2';
    }
    if (btnLeaderboard) {
      btnLeaderboard.className = isDarkMode
        ? 'px-5 py-1.5 rounded-full text-xs font-medium text-slate-300 hover:text-white transition-all duration-200 flex items-center gap-2'
        : 'px-5 py-1.5 rounded-full text-xs font-medium text-pastel-plum hover:text-pastel-denim transition-all duration-200 flex items-center gap-2';
    }

    // Mostrar partida (equipos divididos rojo y azul en 12 columnas), ocultar 3D
    if (viewLeaderboard) {
      viewLeaderboard.classList.add('hidden');
      viewLeaderboard.classList.remove('flex');
    }
    if (viewPartida) {
      viewPartida.classList.remove('hidden');
      viewPartida.classList.add('grid');
    }

    hideRightShowcase();
  } else if (subTabId === 'leaderboard') {
    // Actualizar estilos botones
    if (btnLeaderboard) {
      btnLeaderboard.className = 'px-5 py-1.5 rounded-full text-xs font-bold transition-all duration-200 bg-pastel-denim text-white shadow-sm flex items-center gap-2';
    }
    if (btnPartida) {
      btnPartida.className = isDarkMode
        ? 'px-5 py-1.5 rounded-full text-xs font-medium text-slate-300 hover:text-white transition-all duration-200 flex items-center gap-2'
        : 'px-5 py-1.5 rounded-full text-xs font-medium text-pastel-plum hover:text-pastel-denim transition-all duration-200 flex items-center gap-2';
    }

    // Ocultar partida, mostrar leaderboard
    if (viewPartida) {
      viewPartida.classList.add('hidden');
      viewPartida.classList.remove('grid');
    }
    if (viewLeaderboard) {
      viewLeaderboard.classList.remove('hidden');
      viewLeaderboard.classList.add('flex');
    }

    // Mostrar el panel lateral 3D para inspeccionar los perfiles
    showRightShowcase();
  }
}

/**
 * Selecciona e inspecciona un jugador de la tabla de clasificación
 */
function selectLeaderboardPlayer(name, title, wins, towers, kd, element) {
  // 1. Resaltar la fila seleccionada
  document.querySelectorAll('.leaderboard-row').forEach(row => {
    row.classList.remove('active-player');
  });
  if (element) {
    element.classList.add('active-player');
  }

  // 2. Actualizar el Notch del panel 3D
  const badge = document.getElementById('notch-avatar-badge');
  const titleEl = document.getElementById('notch-player-title');
  const iconEl = document.getElementById('notch-player-icon');

  if (badge) {
    badge.textContent = name.substring(0, 2).toUpperCase();
  }
  if (titleEl) {
    titleEl.textContent = name;
  }
  if (iconEl) {
    if (name === 'Destiny_Leader') {
      iconEl.className = 'fa-solid fa-crown text-[11px] text-[#D97706]';
    } else {
      iconEl.className = 'fa-solid fa-circle-check text-[11px] text-pastel-denim';
    }
  }

  // 3. Orientar la skin al frente con un sutil giro de interacción
  if (window.skinViewer && window.skinViewer.playerObject) {
    window.skinViewer.playerObject.rotation.y = 0.45;
  }

  // 4. Asegurarse de que el panel 3D esté visible
  showRightShowcase();

  // 5. Mostrar Toast
  showToast(`Inspeccionando a ${name} (${title})`);
}

/**
 * Restablece el notch superior al nombre oficial
 */
function resetShowcaseNotch() {
  const badge = document.getElementById('notch-avatar-badge');
  const titleEl = document.getElementById('notch-player-title');
  const iconEl = document.getElementById('notch-player-icon');

  if (badge) badge.textContent = 'DO';
  if (titleEl) titleEl.textContent = 'Destiny Owners';
  if (iconEl) iconEl.className = 'fa-solid fa-circle-check text-[11px] text-pastel-denim';
}

/**
 * Conmutador de Modo Noche / Modo Día (apagadito y acogedor)
 */
function toggleDarkMode() {
  setDarkMode(!isDarkMode);
}

function setDarkMode(enable) {
  isDarkMode = enable;
  const icon = document.getElementById('theme-toggle-icon');

  if (isDarkMode) {
    document.body.classList.add('dark-mode');
    if (icon) {
      icon.className = 'fa-solid fa-sun text-sm text-[#FDE68A]';
    }
    localStorage.setItem('destiny_theme', 'dark');
  } else {
    document.body.classList.remove('dark-mode');
    if (icon) {
      icon.className = 'fa-solid fa-moon text-sm text-pastel-plum';
    }
    localStorage.setItem('destiny_theme', 'light');
  }

  // Actualizar estilos de navegación y subpestañas
  updateNavButtonsTheme();
  if (isModalityDetailOpen) {
    switchModalitySubTab(currentModalitySubTab);
  }

  // Informar al visor 3D para calibrar la luz ambiental
  if (window.updateSkinViewerLighting) {
    window.updateSkinViewerLighting(isDarkMode);
  }
}

/**
 * Notificación Toast flotante
 */
function showToast(message) {
  const toast = document.getElementById('toast-notification');
  const toastMsg = document.getElementById('toast-message');
  if (!toast || !toastMsg) return;

  toastMsg.textContent = message;
  toast.classList.add('show');

  setTimeout(() => {
    toast.classList.remove('show');
  }, 3200);
}

/**
 * Copia la dirección IP del servidor al portapapeles
 */
function copyServerIP() {
  const ip = 'play.destinyowners.com';
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(ip).then(() => {
      showToast(`¡IP copiada al portapapeles: ${ip}!`);
    }).catch(() => {
      fallbackCopyText(ip);
    });
  } else {
    fallbackCopyText(ip);
  }
}

function fallbackCopyText(text) {
  const textArea = document.createElement("textarea");
  textArea.value = text;
  textArea.style.position = "fixed";
  textArea.style.left = "-999999px";
  document.body.appendChild(textArea);
  textArea.focus();
  textArea.select();
  try {
    document.execCommand('copy');
    showToast(`¡IP copiada al portapapeles: ${text}!`);
  } catch (err) {
    showToast(`IP: ${text}`);
  }
  document.body.removeChild(textArea);
}

/**
 * Actualiza los estilos visuales de los botones de navegación según la pestaña y tema activo
 */
function updateNavButtonsTheme() {
  const tabs = ['inicio', 'modalidad', 'miembros'];
  tabs.forEach(t => {
    const btn = document.getElementById(`nav-btn-${t}`);
    if (btn) {
      if (t === currentTab) {
        btn.className = 'px-7 py-2 rounded-full text-sm font-semibold transition-all duration-200 bg-pastel-denim text-white shadow-md flex items-center gap-2';
      } else {
        btn.className = isDarkMode 
          ? 'px-7 py-2 rounded-full text-sm font-medium text-slate-300 hover:text-white transition-all duration-200 flex items-center gap-2'
          : 'px-7 py-2 rounded-full text-sm font-medium text-pastel-plum hover:text-pastel-denim transition-all duration-200 flex items-center gap-2';
      }
    }
  });
}

// Inicialización de la navegación y el tema al cargar el documento
window.addEventListener('DOMContentLoaded', () => {
  const urlParams = new URLSearchParams(window.location.search);

  // 1. Cargar preferencia de tema guardada (por defecto modo pastel día, o dark si está guardado o solicitado)
  const themeParam = urlParams.get('theme');
  const savedTheme = localStorage.getItem('destiny_theme');
  if (themeParam === 'dark' || (!themeParam && savedTheme === 'dark')) {
    setDarkMode(true);
  } else {
    setDarkMode(false);
  }

  // 2. Comprobar parámetro URL para modalidad abierta directamente (ej: ?tab=modalidad&modality=the-towers)
  const tabParam = urlParams.get('tab');
  const modalityParam = urlParams.get('modality');

  if (tabParam && ['inicio', 'modalidad', 'miembros'].includes(tabParam)) {
    if (tabParam === 'modalidad' && modalityParam === 'the-towers') {
      switchTab('modalidad');
      const subParam = urlParams.get('sub') || 'partida';
      currentModalitySubTab = subParam;
      openModalityDetail('the-towers', true);
      switchModalitySubTab(subParam);
    } else {
      switchTab(tabParam);
    }
  } else {
    switchTab('inicio');
  }
});

// Exportar globalmente
window.switchTab = switchTab;
window.openModalityDetail = openModalityDetail;
window.closeModalityDetail = closeModalityDetail;
window.switchModalitySubTab = switchModalitySubTab;
window.selectLeaderboardPlayer = selectLeaderboardPlayer;
window.toggleDarkMode = toggleDarkMode;
window.copyServerIP = copyServerIP;
window.showToast = showToast;
window.showRightShowcase = showRightShowcase;
window.hideRightShowcase = hideRightShowcase;
