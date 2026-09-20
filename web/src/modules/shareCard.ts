/**
 * DESTINY OWNERS - Módulo de Generación y Compartición de Tarjetas para Redes Sociales (shareCard.ts)
 * Permite exportar y compartir tarjetas de estadísticas de jugadores como imagen PNG de alta calidad
 * y publicarlas en X/Twitter, Discord, etc.
 */

import { DetailedPlayerProfile, getPlayerProfileData, getCurrentlyInspectedName, ensureLeaderboardLoaded } from './liveMatch';
import { showToast } from './clipboard';
import { getIsDarkMode } from './theme';

let currentSharingProfile: DetailedPlayerProfile | null = null;
let currentCanvas: HTMLCanvasElement | null = null;

/**
 * Abre el modal para previsualizar y compartir la tarjeta de jugador como imagen
 */
export async function openShareModal(username?: string): Promise<void> {
  const modal = document.getElementById('share-card-modal');
  const previewContainer = document.getElementById('share-card-preview-container');
  const modalPlayerName = document.getElementById('share-modal-player-name');

  if (previewContainer) {
    previewContainer.innerHTML = `
      <div class="flex flex-col items-center justify-center p-12 text-pastel-plum">
        <i class="fa-solid fa-circle-notch fa-spin text-3xl text-pastel-denim mb-3"></i>
        <p class="text-xs font-semibold">Cargando estadísticas oficiales y generando tarjeta...</p>
      </div>
    `;
  }

  if (modal) {
    modal.classList.remove('hidden');
    modal.classList.add('flex');
  }

  try {
    await ensureLeaderboardLoaded();
    const target = username || getCurrentlyInspectedName() || 'alepordio';
    const profile = getPlayerProfileData(target);
    if (!profile || !profile.name) return;

    currentSharingProfile = profile;
    if (modalPlayerName) modalPlayerName.textContent = profile.name;

    const canvas = await generateCardCanvas(profile);
    currentCanvas = canvas;
    if (previewContainer) {
      previewContainer.innerHTML = '';
      const img = document.createElement('img');
      img.src = canvas.toDataURL('image/png');
      img.className = 'w-full max-w-[620px] rounded-2xl shadow-2xl border border-white/20 select-none';
      img.alt = `Tarjeta de ${profile.name}`;
      previewContainer.appendChild(img);
    }
  } catch (err) {
    console.error('Error generando tarjeta:', err);
    if (previewContainer) {
      previewContainer.innerHTML = `
        <div class="p-8 text-center text-xs text-rose-500 font-medium">
          No se pudo generar la tarjeta de imagen. Inténtalo de nuevo.
        </div>
      `;
    }
  }
}

/**
 * Cierra el modal de compartir
 */
export function closeShareModal(): void {
  const modal = document.getElementById('share-card-modal');
  if (modal) {
    modal.classList.add('hidden');
    modal.classList.remove('flex');
  }
  currentSharingProfile = null;
  currentCanvas = null;
}

/**
 * Descarga la tarjeta generada como archivo PNG
 */
export function downloadCardImage(): void {
  if (!currentCanvas || !currentSharingProfile) {
    showToast('Generando tarjeta...');
    return;
  }

  const link = document.createElement('a');
  link.download = `DestinyTowers_${currentSharingProfile.name}_Stats.png`;
  link.href = currentCanvas.toDataURL('image/png');
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  showToast('¡Imagen descargada con éxito!');
}

/**
 * Copia la imagen PNG directamente al portapapeles del sistema para pegar en Discord, WhatsApp, etc.
 */
export async function copyCardImage(): Promise<void> {
  if (!currentCanvas) {
    showToast('Generando tarjeta...');
    return;
  }

  try {
    currentCanvas.toBlob(async (blob) => {
      if (!blob) {
        showToast('No se pudo procesar la imagen.');
        return;
      }
      try {
        await navigator.clipboard.write([
          new ClipboardItem({ 'image/png': blob })
        ]);
        showToast('¡Imagen copiada al portapapeles! Puedes pegarla en Discord o WhatsApp.');
      } catch (err) {
        // Fallback a copiar enlace
        copyProfileLink();
      }
    }, 'image/png');
  } catch (err) {
    copyProfileLink();
  }
}

/**
 * Comparte en X (Twitter) mediante intent
 */
export function shareOnTwitter(): void {
  if (!currentSharingProfile) return;
  const p = currentSharingProfile;
  const text = `¡Mira mis estadísticas en The Towers de @DestinyOwners! ⚔️\n\n🏆 ${p.points} PTS ELO • #${p.rank} Ranking\n⚽ ${p.goals} Goles • 🎯 K/D: ${p.kdRatio}\n\nÚnete a la arena: play.destinyowners.com`;
  const url = `https://destinyowners-23.web.app/?tab=modalidad&modality=the-towers&profile=${encodeURIComponent(p.name)}`;
  const tweetUrl = `https://twitter.com/intent/tweet?text=${encodeURIComponent(text)}&url=${encodeURIComponent(url)}`;
  window.open(tweetUrl, '_blank', 'noopener,noreferrer');
}

/**
 * Copia el enlace directo al perfil del jugador
 */
export function copyProfileLink(): void {
  if (!currentSharingProfile) return;
  const url = `${window.location.origin}/?tab=modalidad&modality=the-towers&sub=leaderboard&profile=${encodeURIComponent(currentSharingProfile.name)}`;
  navigator.clipboard.writeText(url).then(() => {
    showToast('¡Enlace directo al perfil copiado!');
  }).catch(() => {
    showToast('Enlace listo para compartir.');
  });
}

/**
 * Dibuja un canvas HTML5 de alta fidelidad sincronizado 1:1 con el diseño visual oficial de Destiny Owners
 */
async function generateCardCanvas(p: DetailedPlayerProfile): Promise<HTMLCanvasElement> {
  const canvas = document.createElement('canvas');
  canvas.width = 1200;
  canvas.height = 675;
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('No 2D context');

  const isDark = getIsDarkMode() || document.body.classList.contains('dark-mode');

  // 1. Fondo Base Temático Oficial Destiny Owners
  const bgGrad = ctx.createLinearGradient(0, 0, 1200, 675);
  if (isDark) {
    bgGrad.addColorStop(0, '#161322');
    bgGrad.addColorStop(1, '#1C182B');
  } else {
    bgGrad.addColorStop(0, '#FAF8F8');
    bgGrad.addColorStop(1, '#F4F5F9');
  }
  ctx.fillStyle = bgGrad;
  ctx.fillRect(0, 0, 1200, 675);

  // 1.1. Ilustración Vectorial: Duna Ondulada Rosa Pastel (Esquina Superior Izquierda)
  ctx.save();
  ctx.beginPath();
  ctx.moveTo(0, 0);
  ctx.lineTo(0, 260);
  ctx.bezierCurveTo(180, 220, 360, 280, 520, 200);
  ctx.bezierCurveTo(580, 170, 620, 120, 650, 60);
  ctx.lineTo(650, 0);
  ctx.closePath();
  ctx.fillStyle = isDark ? '#241C33' : '#F8E1E1';
  ctx.globalAlpha = 0.85;
  ctx.fill();
  ctx.restore();

  // 1.2. Ilustración Vectorial: Acento Circular de Luna
  ctx.save();
  ctx.beginPath();
  ctx.arc(1090, 85, 32, 0, Math.PI * 2);
  ctx.fillStyle = isDark ? '#C1CFE6' : '#806E83';
  ctx.globalAlpha = isDark ? 0.12 : 0.16;
  ctx.fill();
  ctx.restore();

  // 1.3. Ilustración Vectorial: Siluetas de Gatos y Colinas (Esquina Inferior Derecha)
  ctx.save();
  const catScale = 0.88;
  ctx.translate(1200 - (580 * catScale), 675 - (440 * catScale));
  ctx.scale(catScale, catScale);

  const catPath1 = new Path2D("M60 440 C 60 330, 110 220, 220 220 C 240 170, 275 165, 295 210 C 340 210, 375 165, 395 210 C 445 220, 490 310, 530 440 Z");
  ctx.fillStyle = isDark ? '#222B44' : '#6982B5';
  ctx.globalAlpha = 0.88;
  ctx.fill(catPath1);

  const catPath2 = new Path2D("M160 440 C 160 320, 250 250, 370 250 C 385 200, 415 195, 430 240 C 470 240, 500 200, 515 240 C 550 260, 570 330, 580 440 Z");
  ctx.fillStyle = isDark ? '#2E3C61' : '#99B4DA';
  ctx.globalAlpha = 0.92;
  ctx.fill(catPath2);

  const catPath3 = new Path2D("M260 440 C 310 370, 420 330, 530 350 C 560 355, 575 370, 580 390 V 440 Z");
  ctx.fillStyle = isDark ? '#3C4D78' : '#C1CFE6';
  ctx.globalAlpha = 0.85;
  ctx.fill(catPath3);
  ctx.restore();

  // 1.4. Borde Perimetral Orgánico Fino
  drawRoundedRect(ctx, 24, 24, 1152, 627, 36);
  ctx.strokeStyle = isDark ? 'rgba(255, 255, 255, 0.12)' : 'rgba(105, 130, 181, 0.22)';
  ctx.lineWidth = 1.5;
  ctx.stroke();

  // 2. Cabecera de Marca Oficial (DESTINY OWNERS)
  // Insignia DO
  drawRoundedRect(ctx, 65, 55, 46, 46, 14);
  ctx.fillStyle = '#6982B5';
  ctx.fill();
  ctx.fillStyle = '#FFFFFF';
  ctx.font = '900 20px "Outfit", sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText('DO', 88, 86);

  // Nombre de Marca (DESTINY en Malva / Blanco + OWNERS en Azul Denim)
  ctx.textAlign = 'left';
  ctx.font = '900 26px "Outfit", sans-serif';
  ctx.fillStyle = isDark ? '#FFFFFF' : '#5B4B6E';
  ctx.fillText('DESTINY ', 124, 87);
  const dW = ctx.measureText('DESTINY ').width;
  ctx.fillStyle = isDark ? '#8FA4D1' : '#6982B5';
  ctx.fillText('OWNERS', 124 + dW, 87);

  // Píldora del Servidor Oficial (play.destinyowners.com)
  drawRoundedRect(ctx, 890, 56, 245, 44, 22);
  ctx.fillStyle = isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(255, 255, 255, 0.92)';
  ctx.fill();
  ctx.strokeStyle = isDark ? 'rgba(255, 255, 255, 0.15)' : 'rgba(193, 207, 230, 0.8)';
  ctx.lineWidth = 1.5;
  ctx.stroke();

  // Punto verde de servidor en línea
  ctx.beginPath();
  ctx.arc(914, 78, 4, 0, Math.PI * 2);
  ctx.fillStyle = '#10B981';
  ctx.fill();

  ctx.font = '700 13px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = isDark ? '#C1CFE6' : '#5B4B6E';
  ctx.fillText('play.destinyowners.com', 928, 83);

  // 3. Render de la Skin del Jugador (Izquierda - Sobre Pedestal Cálido)
  // Sombra ambiental difuminada
  ctx.save();
  ctx.beginPath();
  ctx.ellipse(250, 535, 110, 18, 0, 0, Math.PI * 2);
  ctx.fillStyle = isDark ? 'rgba(0, 0, 0, 0.55)' : 'rgba(91, 75, 110, 0.18)';
  ctx.filter = 'blur(10px)';
  ctx.fill();
  ctx.restore();

  // Halo cálido del pedestal
  ctx.save();
  ctx.beginPath();
  ctx.ellipse(250, 520, 130, 32, 0, 0, Math.PI * 2);
  const pedGlow = ctx.createRadialGradient(250, 520, 10, 250, 520, 130);
  if (isDark) {
    pedGlow.addColorStop(0, 'rgba(105, 130, 181, 0.35)');
    pedGlow.addColorStop(1, 'rgba(105, 130, 181, 0)');
  } else {
    pedGlow.addColorStop(0, 'rgba(248, 225, 225, 0.95)');
    pedGlow.addColorStop(0.6, 'rgba(193, 207, 230, 0.45)');
    pedGlow.addColorStop(1, 'rgba(193, 207, 230, 0)');
  }
  ctx.fillStyle = pedGlow;
  ctx.filter = 'blur(12px)';
  ctx.fill();
  ctx.restore();

  // Cargar Render de la Skin
  try {
    const skinImg = await loadImage(`https://mc-heads.net/body/${p.name}/right`);
    ctx.drawImage(skinImg, 110, 165, 280, 365);
  } catch (err) {
    try {
      const fallbackImg = await loadImage(`https://minotar.net/armor/body/${p.name}/300.png`);
      ctx.drawImage(fallbackImg, 140, 185, 220, 345);
    } catch (e) {}
  }

  // Insignia de Ranking sobre la skin
  const rankText = `#${p.rank} EN RANKING`;
  drawRoundedRect(ctx, 90, 150, 130, 32, 16);
  ctx.fillStyle = p.rank === 1 ? '#F59E0B' : p.rank === 2 ? '#64748B' : p.rank === 3 ? '#B45309' : '#6982B5';
  ctx.fill();
  ctx.fillStyle = '#FFFFFF';
  ctx.font = '900 11px "Outfit", sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText(rankText, 155, 171);

  // 4. Panel de Estadísticas y Credenciales (Derecha)
  // Nombre de Usuario
  ctx.textAlign = 'left';
  ctx.font = '900 50px "Outfit", sans-serif';
  ctx.fillStyle = isDark ? '#FFFFFF' : '#362B3A';
  ctx.fillText(p.name, 460, 195);

  // Ícono de Verificación Auténtica
  const nameW = ctx.measureText(p.name).width;
  drawCheckmark(ctx, 460 + nameW + 18, 178, 11, '#6982B5');

  // Fila de Badges: Rol y Estado
  const roleText = p.isStaff ? '✦ DESTINY ADMIN' : p.rank <= 3 ? '★ TOP JUGADOR' : 'JUGADOR OFICIAL';
  const roleW = p.isStaff ? 142 : p.rank <= 3 ? 132 : 136;
  drawRoundedRect(ctx, 460, 216, roleW, 28, 14);

  if (p.isStaff) {
    ctx.fillStyle = isDark ? '#7C3AED' : '#F3E8FF';
    ctx.fill();
    ctx.strokeStyle = isDark ? '#7C3AED' : '#DDD6FE';
    ctx.stroke();
    ctx.fillStyle = isDark ? '#FFFFFF' : '#7C3AED';
  } else if (p.rank <= 3) {
    ctx.fillStyle = isDark ? '#F59E0B' : '#FEF3C7';
    ctx.fill();
    ctx.strokeStyle = isDark ? '#F59E0B' : '#FDE68A';
    ctx.stroke();
    ctx.fillStyle = isDark ? '#FFFFFF' : '#D97706';
  } else {
    ctx.fillStyle = isDark ? '#6982B5' : '#EFF6FF';
    ctx.fill();
    ctx.strokeStyle = isDark ? '#6982B5' : '#BFDBFE';
    ctx.stroke();
    ctx.fillStyle = isDark ? '#FFFFFF' : '#2563EB';
  }
  ctx.font = '800 11px "Plus Jakarta Sans", sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText(roleText, 460 + (roleW / 2), 234);

  // Chip de Estado En Línea
  drawRoundedRect(ctx, 460 + roleW + 10, 216, 96, 28, 14);
  ctx.fillStyle = isDark ? 'rgba(16, 185, 129, 0.2)' : 'rgba(16, 185, 129, 0.12)';
  ctx.fill();
  ctx.beginPath();
  ctx.arc(460 + roleW + 24, 230, 3.5, 0, Math.PI * 2);
  ctx.fillStyle = '#10B981';
  ctx.fill();
  ctx.textAlign = 'left';
  ctx.font = '700 11px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = isDark ? '#34D399' : '#059669';
  ctx.fillText('En Línea', 460 + roleW + 33, 234);

  // Sección Elo Monumental
  ctx.font = '800 11px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = isDark ? 'rgba(255, 255, 255, 0.55)' : '#806E83';
  ctx.fillText('PUNTOS ELO DE TEMPORADA', 460, 275);

  const ptsStr = p.points.toLocaleString();
  ctx.font = '900 64px "Outfit", sans-serif';
  ctx.fillStyle = isDark ? '#8FA4D1' : '#6982B5';
  ctx.fillText(ptsStr, 460, 335);

  // Medición con la fuente del número para alineación exacta de PTS ELO
  const ptsWidth = ctx.measureText(ptsStr).width;
  ctx.font = '700 14px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = isDark ? 'rgba(255, 255, 255, 0.6)' : '#806E83';
  ctx.fillText('PTS ELO', 460 + ptsWidth + 14, 325);

  // 5. Panel Orgánico de Métricas (Idéntico a la vista de perfil de la web)
  const stripX = 460;
  const stripY = 365;
  const stripW = 675;
  const stripH = 148;
  const colW = stripW / 3;

  // Contenedor frosted
  drawRoundedRect(ctx, stripX, stripY, stripW, stripH, 24);
  ctx.fillStyle = isDark ? 'rgba(26, 31, 48, 0.9)' : 'rgba(255, 255, 255, 0.94)';
  ctx.fill();
  ctx.strokeStyle = isDark ? 'rgba(255, 255, 255, 0.12)' : 'rgba(193, 207, 230, 0.75)';
  ctx.lineWidth = 1.5;
  ctx.stroke();

  // Columna 1: Goles en Torre
  ctx.textAlign = 'left';
  ctx.font = '800 11px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = '#D97706';
  ctx.fillText('★ GOLES EN TORRE', stripX + 25, stripY + 32);

  ctx.font = '900 34px "Outfit", sans-serif';
  ctx.fillStyle = isDark ? '#FFFFFF' : '#362B3A';
  ctx.fillText(String(p.goals), stripX + 25, stripY + 70);

  ctx.font = '600 11px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = isDark ? 'rgba(255, 255, 255, 0.5)' : '#806E83';
  ctx.fillText('+150 pts Elo c/u', stripX + 25, stripY + 90);

  // Divisor 1
  ctx.beginPath();
  ctx.moveTo(stripX + colW, stripY + 18);
  ctx.lineTo(stripX + colW, stripY + 100);
  ctx.strokeStyle = isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(193, 207, 230, 0.7)';
  ctx.lineWidth = 1;
  ctx.stroke();

  // Columna 2: Bajas PvP
  ctx.font = '800 11px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = '#E11D48';
  ctx.fillText('⊕ BAJAS EN ARENA', stripX + colW + 25, stripY + 32);

  ctx.font = '900 34px "Outfit", sans-serif';
  ctx.fillStyle = isDark ? '#FFFFFF' : '#362B3A';
  ctx.fillText(String(p.kills), stripX + colW + 25, stripY + 70);

  ctx.font = '600 11px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = isDark ? 'rgba(255, 255, 255, 0.5)' : '#806E83';
  ctx.fillText('+15 pts Elo c/u', stripX + colW + 25, stripY + 90);

  // Divisor 2
  ctx.beginPath();
  ctx.moveTo(stripX + (colW * 2), stripY + 18);
  ctx.lineTo(stripX + (colW * 2), stripY + 100);
  ctx.strokeStyle = isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(193, 207, 230, 0.7)';
  ctx.lineWidth = 1;
  ctx.stroke();

  // Columna 3: Ratio K/D
  ctx.font = '800 11px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = '#6982B5';
  ctx.fillText('⚡ RATIO K/D', stripX + (colW * 2) + 25, stripY + 32);

  ctx.font = '900 34px "Outfit", sans-serif';
  ctx.fillStyle = isDark ? '#8FA4D1' : '#6982B5';
  ctx.fillText(p.kdRatio, stripX + (colW * 2) + 25, stripY + 70);

  ctx.font = '600 11px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = isDark ? 'rgba(255, 255, 255, 0.5)' : '#806E83';
  ctx.fillText(`${p.deaths} caídas en combate`, stripX + (colW * 2) + 25, stripY + 90);

  // Barra Continua de Especialidad Táctica (Goles vs Bajas)
  const totalTactical = p.goals + p.kills;
  const goalsPct = totalTactical > 0 ? Math.round((p.goals / totalTactical) * 100) : 50;
  const killsPct = 100 - goalsPct;

  ctx.beginPath();
  ctx.moveTo(stripX + 25, stripY + 112);
  ctx.lineTo(stripX + stripW - 25, stripY + 112);
  ctx.strokeStyle = isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(193, 207, 230, 0.4)';
  ctx.stroke();

  ctx.font = '700 10px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = isDark ? 'rgba(255, 255, 255, 0.55)' : '#806E83';
  ctx.fillText(`Especialidad Táctica: ${goalsPct}% Enfoque en Goles • ${killsPct}% Enfoque en Bajas`, stripX + 25, stripY + 128);

  const barW = stripW - 50;
  const barH = 6;
  const barY = stripY + 134;
  const goalsBarW = Math.max(8, Math.min(barW - 8, Math.round(barW * (goalsPct / 100))));

  // Barra de Goles (Ámbar)
  drawRoundedRect(ctx, stripX + 25, barY, goalsBarW, barH, 3);
  ctx.fillStyle = '#F59E0B';
  ctx.fill();

  // Barra de Bajas (Rosa)
  drawRoundedRect(ctx, stripX + 25 + goalsBarW, barY, barW - goalsBarW, barH, 3);
  ctx.fillStyle = '#FB7185';
  ctx.fill();

  // 6. Pie de Tarjeta
  ctx.font = '600 11px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = isDark ? 'rgba(255, 255, 255, 0.45)' : '#806E83';
  ctx.fillText('Clasificación Oficial Destiny Towers • Estadísticas sincronizadas en tiempo real con Firebase', stripX, 545);

  return canvas;
}

/**
 * Traza un círculo con ícono de verificación nítido en Canvas
 */
function drawCheckmark(ctx: CanvasRenderingContext2D, cx: number, cy: number, r: number, color: string): void {
  ctx.save();
  ctx.beginPath();
  ctx.arc(cx, cy, r, 0, Math.PI * 2);
  ctx.fillStyle = color;
  ctx.fill();

  ctx.beginPath();
  ctx.moveTo(cx - (r * 0.45), cy);
  ctx.lineTo(cx - (r * 0.1), cy + (r * 0.35));
  ctx.lineTo(cx + (r * 0.45), cy - (r * 0.35));
  ctx.strokeStyle = '#FFFFFF';
  ctx.lineWidth = 2;
  ctx.lineCap = 'round';
  ctx.lineJoin = 'round';
  ctx.stroke();
  ctx.restore();
}

/**
 * Carga una imagen asegurando CORS anonymous
 */
function loadImage(src: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => resolve(img);
    img.onerror = (e) => reject(e);
    img.src = src;
  });
}

/**
 * Utilidad para trazar rectángulos redondeados en Canvas 2D
 */
function drawRoundedRect(
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  width: number,
  height: number,
  radius: number
): void {
  ctx.beginPath();
  ctx.moveTo(x + radius, y);
  ctx.lineTo(x + width - radius, y);
  ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
  ctx.lineTo(x + width, y + height - radius);
  ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
  ctx.lineTo(x + radius, y + height);
  ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
  ctx.lineTo(x, y + radius);
  ctx.quadraticCurveTo(x, y, x + radius, y);
  ctx.closePath();
}
