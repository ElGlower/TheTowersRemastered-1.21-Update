/**
 * Módulo de Utilidades de Portapapeles y Notificaciones Toast
 */

let toastTimeout: number | null = null;

export function showToast(message: string): void {
  const toast = document.getElementById('toast-notification');
  const toastMsg = document.getElementById('toast-message');
  if (!toast || !toastMsg) return;

  toastMsg.textContent = message;
  toast.classList.add('show');

  if (toastTimeout !== null) {
    window.clearTimeout(toastTimeout);
  }

  toastTimeout = window.setTimeout(() => {
    toast.classList.remove('show');
    toastTimeout = null;
  }, 3200);
}

export function copyServerIP(ip: string = 'play.destinyowners.com'): void {
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(ip)
      .then(() => {
        showToast(`¡IP copiada: ${ip}!`);
      })
      .catch(() => {
        fallbackCopyText(ip);
      });
  } else {
    fallbackCopyText(ip);
  }
}

function fallbackCopyText(text: string): void {
  const textArea = document.createElement('textarea');
  textArea.value = text;
  textArea.style.position = 'fixed';
  textArea.style.left = '-999999px';
  document.body.appendChild(textArea);
  textArea.focus();
  textArea.select();
  try {
    document.execCommand('copy');
    showToast(`¡IP copiada: ${text}!`);
  } catch {
    showToast(`IP: ${text}`);
  }
  document.body.removeChild(textArea);
}
