/**
 * Módulo de Gestión de Tema Día (Pastel) y Noche (Twilight #1A1F30)
 */

import { ThemeMode } from '../types';
import { updateSkinViewerLighting } from './skinViewer';

let isDark = false;

export function getIsDarkMode(): boolean {
  return isDark;
}

export function setDarkMode(enable: boolean): void {
  isDark = enable;
  const icon = document.getElementById('theme-toggle-icon');

  if (isDark) {
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

  // Ajustar iluminación del visor 3D según el tema
  updateSkinViewerLighting(isDark);
}

export function toggleDarkMode(): void {
  setDarkMode(!isDark);
}

export function initTheme(): void {
  const urlParams = new URLSearchParams(window.location.search);
  const themeParam = urlParams.get('theme') as ThemeMode | null;
  const savedTheme = localStorage.getItem('destiny_theme');

  if (themeParam === 'dark' || (!themeParam && savedTheme === 'dark')) {
    setDarkMode(true);
  } else {
    setDarkMode(false);
  }
}
