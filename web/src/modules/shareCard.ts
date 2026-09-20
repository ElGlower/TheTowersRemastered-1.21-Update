/**
 * DESTINY OWNERS - Módulo de Generación y Compartición de Tarjetas para Redes Sociales (shareCard.ts)
 * Permite exportar y compartir tarjetas de estadísticas de jugadores como imagen PNG de alta calidad
 * y publicarlas en X/Twitter, Discord, etc.
 */

import { DetailedPlayerProfile, getPlayerProfileData, getCurrentlyInspectedName, ensureLeaderboardLoaded } from './liveMatch';
import { showToast } from './clipboard';

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
 * Dibuja un canvas HTML5 de alta fidelidad (1200 x 675, estándar redes sociales)
 */
async function generateCardCanvas(p: DetailedPlayerProfile): Promise<HTMLCanvasElement> {
  const canvas = document.createElement('canvas');
  canvas.width = 1200;
  canvas.height = 675;
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('No 2D context');

  // 1. Fondo estilizado con gradiente temático Destiny Owners Twilight
  const bgGrad = ctx.createLinearGradient(0, 0, 1200, 675);
  bgGrad.addColorStop(0, '#121624');
  bgGrad.addColorStop(0.5, '#1B2236');
  bgGrad.addColorStop(1, '#0F131F');
  ctx.fillStyle = bgGrad;
  ctx.fillRect(0, 0, 1200, 675);

  // Acentos circulares suaves de iluminación (Glow)
  const glow1 = ctx.createRadialGradient(280, 360, 40, 280, 360, 320);
  glow1.addColorStop(0, 'rgba(105, 130, 181, 0.28)');
  glow1.addColorStop(1, 'rgba(105, 130, 181, 0)');
  ctx.fillStyle = glow1;
  ctx.fillRect(0, 0, 1200, 675);

  const glow2 = ctx.createRadialGradient(900, 200, 20, 900, 200, 400);
  glow2.addColorStop(0, 'rgba(231, 151, 150, 0.16)');
  glow2.addColorStop(1, 'rgba(231, 151, 150, 0)');
  ctx.fillStyle = glow2;
  ctx.fillRect(0, 0, 1200, 675);

  // Borde exterior suave del card
  drawRoundedRect(ctx, 30, 30, 1140, 615, 32);
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.12)';
  ctx.lineWidth = 2;
  ctx.stroke();

  // 2. Cabecera de Marca
  // Insignia DO
  drawRoundedRect(ctx, 70, 65, 48, 48, 14);
  ctx.fillStyle = '#6982B5';
  ctx.fill();
  ctx.fillStyle = '#FFFFFF';
  ctx.font = 'bold 20px "Outfit", sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText('DO', 94, 96);

  // Nombre de Marca
  ctx.textAlign = 'left';
  ctx.font = '800 24px "Outfit", sans-serif';
  ctx.fillStyle = '#FFFFFF';
  ctx.fillText('DESTINY OWNERS', 130, 96);

  // Modalidad Oficial
  ctx.font = '600 14px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = 'rgba(255, 255, 255, 0.55)';
  ctx.fillText('THE TOWERS REMASTERED • TEMPORADA OFICIAL', 130, 116);

  // IP del servidor (derecha)
  drawRoundedRect(ctx, 890, 65, 240, 44, 22);
  ctx.fillStyle = 'rgba(255, 255, 255, 0.08)';
  ctx.fill();
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.15)';
  ctx.lineWidth = 1;
  ctx.stroke();

  ctx.textAlign = 'center';
  ctx.font = '700 13px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = '#C1CFE6';
  ctx.fillText('play.destinyowners.com', 1010, 92);

  // 3. Tarjeta de Skin (Izquierda)
  drawRoundedRect(ctx, 70, 150, 380, 450, 28);
  ctx.fillStyle = 'rgba(255, 255, 255, 0.04)';
  ctx.fill();
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.08)';
  ctx.lineWidth = 1.5;
  ctx.stroke();

  // Pedestal Sombra
  ctx.save();
  ctx.beginPath();
  ctx.ellipse(260, 520, 110, 18, 0, 0, Math.PI * 2);
  ctx.fillStyle = 'rgba(0, 0, 0, 0.45)';
  ctx.filter = 'blur(8px)';
  ctx.fill();
  ctx.restore();

  // Cargar Render de la Skin
  try {
    const skinImg = await loadImage(`https://mc-heads.net/body/${p.name}/right`);
    ctx.drawImage(skinImg, 120, 175, 280, 350);
  } catch (err) {
    try {
      const fallbackImg = await loadImage(`https://minotar.net/armor/body/${p.name}/300.png`);
      ctx.drawImage(fallbackImg, 150, 195, 220, 330);
    } catch (e) {}
  }

  // Insignia de Ranking sobre la skin
  const rankText = `#${p.rank} RANKING`;
  drawRoundedRect(ctx, 95, 175, 120, 30, 15);
  ctx.fillStyle = p.rank === 1 ? '#F59E0B' : p.rank === 2 ? '#94A3B8' : p.rank === 3 ? '#B45309' : '#6982B5';
  ctx.fill();
  ctx.fillStyle = '#FFFFFF';
  ctx.font = '900 12px "Outfit", sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText(rankText, 155, 195);

  // 4. Panel de Estadísticas y Credenciales (Derecha)
  // Nombre de Usuario
  ctx.textAlign = 'left';
  ctx.font = '900 44px "Outfit", sans-serif';
  ctx.fillStyle = '#FFFFFF';
  ctx.fillText(p.name, 490, 205);

  // Tag de Rol
  const roleText = p.isStaff ? '✦ DESTINY ADMIN' : p.rank <= 3 ? '★ TOP JUGADOR' : 'JUGADOR OFICIAL';
  drawRoundedRect(ctx, 490, 225, 140, 28, 14);
  ctx.fillStyle = p.isStaff ? '#7C3AED' : p.rank <= 3 ? '#F59E0B' : 'rgba(255,255,255,0.12)';
  ctx.fill();
  ctx.fillStyle = '#FFFFFF';
  ctx.font = '800 11px "Plus Jakarta Sans", sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText(roleText, 560, 243);

  // Puntos Elo Principales
  ctx.textAlign = 'left';
  ctx.font = '700 12px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = 'rgba(255, 255, 255, 0.5)';
  ctx.fillText('PUNTUACIÓN DE TEMPORADA', 490, 290);

  ctx.font = '900 60px "Outfit", sans-serif';
  ctx.fillStyle = '#6982B5';
  ctx.fillText(p.points.toLocaleString(), 490, 355);

  ctx.font = '700 16px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = 'rgba(255, 255, 255, 0.6)';
  const ptsWidth = ctx.measureText(p.points.toLocaleString()).width;
  ctx.fillText('PTS ELO', 490 + ptsWidth + 14, 345);

  // Grid de Métricas (4 Cajas Bento Estilizadas)
  const metricBoxes = [
    { label: 'GOLES EN TORRE', value: String(p.goals), sub: 'Puntos Decisivos', color: '#F59E0B' },
    { label: 'ASESINATOS PVP', value: String(p.kills), sub: 'Bajas en Arena', color: '#FB7185' },
    { label: 'MUERTES', value: String(p.deaths), sub: 'Caídas en Combate', color: '#94A3B8' },
    { label: 'RATIO K/D', value: p.kdRatio, sub: 'Eficiencia', color: '#6EE7B7' },
  ];

  const startX = 490;
  const startY = 385;
  const boxW = 150;
  const boxH = 120;
  const gap = 16;

  metricBoxes.forEach((m, idx) => {
    const col = idx % 4;
    const x = startX + (col * (boxW + gap));
    const y = startY;

    drawRoundedRect(ctx, x, y, boxW, boxH, 20);
    ctx.fillStyle = 'rgba(255, 255, 255, 0.05)';
    ctx.fill();
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.08)';
    ctx.lineWidth = 1;
    ctx.stroke();

    ctx.textAlign = 'left';
    ctx.font = '700 10px "Plus Jakarta Sans", sans-serif';
    ctx.fillStyle = 'rgba(255, 255, 255, 0.45)';
    ctx.fillText(m.label, x + 16, y + 28);

    ctx.font = '900 32px "Outfit", sans-serif';
    ctx.fillStyle = m.color;
    ctx.fillText(m.value, x + 16, y + 72);

    ctx.font = '500 10px "Plus Jakarta Sans", sans-serif';
    ctx.fillStyle = 'rgba(255, 255, 255, 0.4)';
    ctx.fillText(m.sub, x + 16, y + 98);
  });

  // Pie de Tarjeta
  ctx.font = '600 12px "Plus Jakarta Sans", sans-serif';
  ctx.fillStyle = 'rgba(255, 255, 255, 0.35)';
  ctx.fillText('Clasificación Oficial Destiny Towers • Estadísticas sincronizadas en tiempo real con Firebase', 490, 560);

  return canvas;
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
