/**
 * DESTINY OWNERS - Lógica de Navegación y UI (main.js)
 * Conmutador fluido de pestañas, Modo Noche / Día, notificaciones y copiado al portapapeles.
 */

let currentTab = 'inicio';
let isDarkMode = false;

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

  // 2. Actualizar estados visuales de los botones de la píldora de navegación
  updateNavButtonsTheme();

  // 3. Controlar la visibilidad condicional del panel lateral 3D
  const leftCol = document.getElementById('left-content-area');
  const rightPanel = document.getElementById('right-showcase-panel');

  if (leftCol && rightPanel) {
    if (tabId === 'inicio') {
      leftCol.className = 'lg:col-span-7 flex flex-col justify-stretch';
      rightPanel.style.display = 'flex';
      setTimeout(() => {
        rightPanel.style.opacity = '1';
        rightPanel.style.transform = 'translateX(0)';
      }, 20);
    } else {
      rightPanel.style.opacity = '0';
      rightPanel.style.transform = 'translateX(24px)';
      setTimeout(() => {
        rightPanel.style.display = 'none';
        leftCol.className = 'lg:col-span-12 flex flex-col justify-stretch';
      }, 300);
    }
  }

  // 4. Actualizar parámetro en la URL sin recargar la página
  const url = new URL(window.location);
  url.searchParams.set('tab', tabId);
  window.history.replaceState({}, '', url);
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

  // Actualizar estilos de los botones sin resetear la pestaña
  updateNavButtonsTheme();

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

  // 2. Comprobar parámetro URL para pestañas
  const tabParam = urlParams.get('tab');
  if (tabParam && ['inicio', 'modalidad', 'miembros'].includes(tabParam)) {
    switchTab(tabParam);
  } else {
    switchTab('inicio');
  }
});

// Exportar globalmente
window.switchTab = switchTab;
window.toggleDarkMode = toggleDarkMode;
window.copyServerIP = copyServerIP;
window.showToast = showToast;
