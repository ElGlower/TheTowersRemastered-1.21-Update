/**
 * Módulo de Visor 3D WebGL con Controles de Plataforma Giratoria (Turntable)
 */

declare const skinview3d: any;

// Skin oficial de Destiny Owners en Base64 para carga autónoma y sin CORS
export const DEFAULT_SKIN_BASE64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAEhUlEQVR4Xu1ZS24UMRDNEbkAF+AGrDkCZ+ACrIkQgxQWQZFAgYCUDwsgZDGggRCpw6Lpcrvcz6/LY08yYbqHflLFnyrb9V57xu7Jzk4G1wfv6mVW7b6sq9leU87aEuuNj+e7CRbzi1qM27K+tGW9LrqF1XcjOJJ7r1sTsmK+7XxMmuo837qg63P/2hEEUPIgQrsDZp2hAN54vtGhdAdc/ryK7K53wD9DdgcY5FEEnm90yO6AZpsz8SDAVn0E/ucdsMyGsAOqN+9761h9JpjQpozzui1w7qX3CPlTnZ65hpRY5yRTlhu/mH8zrfVd1NXZp6wAbs4mTkzq7GdUJ6eB+O9fC2fq07YTpYRAznLjZaE/11VIROr6BDCxFJQ0ClAiGq6HbalrPu5GVR2f1vKZcSXUkeTV+TyySIDEeD0tNAn16+Lqi9PuwxGXOb0AWuc4BguA6AugJdSV4ON7950peW1HAhjjSwTgxBCOqBdT1qs+fHTm6ip0wZedJXLoc0fa4VFzZL1qS6gr0acPH9VP7j8IAkhd+tSfGh+OzKbuPuve78iDn3IL0Pm5X5Hz4/wogtadzwXtH7TJSAl1XeDHfFEfPnsRjjepS18QIDE+CNDUHWnvD/WMALknnPXn5g8CuBcYX2LdL8DnezjndYumxqsArm68LS5JDmGRtPpSsO4jVp8DEpx/v4wIS5vjGYF0wmSO0N4/bQ18PB/Dyq1kXDGYMLc5nsGE2WQONRUA+3g+hpVbybhiMGFuczwDyeq4SQASIJQggPbxfAwrt5JxSSBBNnwyaBiD5NhKYqJflAzjnEpzY55J8MSrLCJmEjVekfFUiONn7aVGS6xLeXLRt6Pzunr7xTbxnXQvPllwomhM3BRALjS6uAogZ3zTDjHiw3uBxuvF6fPXrsS6lEx+cAJ4spYAkVkC6MUpJUDj45xKc2OeSfDEqywiFpGVumx12b76pPwTcX3i43gx8bEA0jcWAbQfn6jz+S3r6v6JmvEqgH4HjFWAQMwLEMU1fb24rRBgt7vc8A6I4qwdoKeA9LMA0jeWY3AtAkippwL08TyluTHPJHjiVRYRiwSAU4Dnagm1/qQAKuDYBOC3OqnzXOzv2v3bHxrPU5ob80yCJ15lkZSxCEqW47r4PvFl5MU4J86NeU6YMGE4kO+Dkr4JY4W89JT0lUBeprCO7RRKYu4EeBGSoy/0u3O/uwjhGOdvbolaYj34C4lvHCiAa/sLT1vPC5DrGzyq5w1BsePu6QefvOR4v+VzJe4AY47BA1+Gej54Ger5VAD/Noh9o0RKAO5TbI0AN94B+p2BAhhxg8etBfC/B2DfqMCnQOQT8qlTQAUAgaw5Bo8UQUXOvzVQktE9IEOcfwtg/2jAP5hwm+MZGD/KHziYMLc5njEJMAkwMgEkSTYkvMx4XOl4ieE8NgZOvoQAEun9F1lM/6Fqmf/XOOfB4Fdobq8NTH4oAijujLiCya8qgGUcxyYxnMfGwMmXEEAilnEc2yTAJMAkwCQA57ExcPIlBJCIZRzHNgkwCbA+Af4CGrmggJaLwoQAAAAASUVORK5CYII=";

let viewerInstance: any = null;
let isAutoRotating = false;
let isDragging = false;
let lastPointerX = 0;
let rotationVelocity = 0;

export function getSkinViewer() {
  return viewerInstance;
}

export function initSkinViewer(): void {
  const canvas = document.getElementById('skin-canvas') as HTMLCanvasElement | null;
  if (!canvas) return;

  if (typeof skinview3d === 'undefined') {
    setTimeout(initSkinViewer, 100);
    return;
  }

  try {
    viewerInstance = new skinview3d.SkinViewer({
      canvas: canvas,
      width: 320,
      height: 420,
      skin: DEFAULT_SKIN_BASE64,
      model: 'slim'
    });

    viewerInstance.background = null;
    viewerInstance.zoom = 0.92;
    viewerInstance.fov = 48;
    viewerInstance.playerObject.position.y = -2;
    viewerInstance.playerObject.rotation.y = 0.45;

    // Desactivar controles estándar para usar plataforma giratoria
    if (viewerInstance.controls) {
      viewerInstance.controls.enabled = false;
    }

    // Animación Idle de respiración suave
    const idleAnim = new skinview3d.IdleAnimation();
    idleAnim.speed = 0.8;
    viewerInstance.animation = idleAnim;

    // Configurar rotación manual con amortiguación
    setupTurntableControls(canvas);

    // Sincronizar iluminación inicial
    const isDark = document.body.classList.contains('dark-mode');
    updateSkinViewerLighting(isDark);
  } catch (err) {
    console.error('Error al inicializar el visor WebGL:', err);
    activateSkinFallback();
  }
}

function setupTurntableControls(canvas: HTMLCanvasElement): void {
  canvas.addEventListener('pointerdown', (e: PointerEvent) => {
    isDragging = true;
    lastPointerX = e.clientX;
    rotationVelocity = 0;
    try { canvas.setPointerCapture(e.pointerId); } catch (_) {}
    canvas.classList.add('cursor-grabbing');
    canvas.classList.remove('cursor-grab');
  });

  canvas.addEventListener('pointermove', (e: PointerEvent) => {
    if (!isDragging || !viewerInstance || !viewerInstance.playerObject) return;
    const deltaX = e.clientX - lastPointerX;
    rotationVelocity = deltaX * 0.012;
    viewerInstance.playerObject.rotation.y += rotationVelocity;
    lastPointerX = e.clientX;
  });

  const endDrag = (e: PointerEvent) => {
    if (isDragging) {
      isDragging = false;
      canvas.classList.remove('cursor-grabbing');
      canvas.classList.add('cursor-grab');
      try { canvas.releasePointerCapture(e.pointerId); } catch (_) {}
    }
  };

  canvas.addEventListener('pointerup', endDrag);
  canvas.addEventListener('pointercancel', endDrag);

  function rotationLoop() {
    if (viewerInstance && viewerInstance.playerObject) {
      if (!isDragging) {
        if (Math.abs(rotationVelocity) > 0.0003) {
          viewerInstance.playerObject.rotation.y += rotationVelocity;
          rotationVelocity *= 0.92;
        } else if (isAutoRotating) {
          viewerInstance.playerObject.rotation.y += 0.016;
        }
      }
    }
    requestAnimationFrame(rotationLoop);
  }
  requestAnimationFrame(rotationLoop);
}

export function updateSkinViewerLighting(isDark: boolean): void {
  if (!viewerInstance) return;
  if (viewerInstance.globalLight) viewerInstance.globalLight.intensity = isDark ? 2.9 : 3.2;
  if (viewerInstance.cameraLight) viewerInstance.cameraLight.intensity = isDark ? 1.05 : 1.1;
}

export function toggleAutoRotate(): void {
  if (!viewerInstance) return;
  isAutoRotating = !isAutoRotating;
  rotationVelocity = 0;

  const btn = document.getElementById('btn-autorotate');
  if (btn) {
    if (isAutoRotating) {
      btn.classList.add('bg-pastel-denim', 'text-white');
      btn.classList.remove('bg-white', 'text-pastel-denim');
    } else {
      btn.classList.remove('bg-pastel-denim', 'text-white');
      btn.classList.add('bg-white', 'text-pastel-denim');
    }
  }
}

export function loadSkinTexture(textureOrUrl: string): void {
  if (!viewerInstance) return;
  try {
    viewerInstance.loadSkin(textureOrUrl);
  } catch (e) {
    console.warn('No se pudo cargar la skin alternativa, manteniendo oficial:', e);
  }
}

export function resetSkinTexture(): void {
  loadSkinTexture(DEFAULT_SKIN_BASE64);
}

function activateSkinFallback(): void {
  const canvas = document.getElementById('skin-canvas');
  const fallback = document.getElementById('skin-fallback-img');
  if (canvas) canvas.style.display = 'none';
  if (fallback) fallback.classList.remove('hidden');
}
