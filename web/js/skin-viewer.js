/**
 * DESTINY OWNERS - 3D Skin Viewer (skin-viewer.js)
 * Motor WebGL basado en skinview3d (Three.js) con órbita 360°, animación Idle y controles.
 */

// Textura de la skin oficial de Destiny Owners en Base64 (100% autónoma, sin fallos de CORS ni latencia)
const DESTINY_SKIN_BASE64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAEhUlEQVR4Xu1ZS24UMRDNEbkAF+AGrDkCZ+ACrIkQgxQWQZFAgYCUDwsgZDGggRCpw6Lpcrvcz6/LY08yYbqHflLFnyrb9V57xu7Jzk4G1wfv6mVW7b6sq9leU87aEuuNj+e7CRbzi1qM27K+tGW9LrqF1XcjOJJ7r1sTsmK+7XxMmuo837qg63P/2hEEUPIgQrsDZp2hAN54vtGhdAdc/ryK7K53wD9DdgcY5FEEnm90yO6AZpsz8SDAVn0E/ucdsMyGsAOqN+9761h9JpjQpozzui1w7qX3CPlTnZ65hpRY5yRTlhu/mH8zrfVd1NXZp6wAbs4mTkzq7GdUJ6eB+O9fC2fq07YTpYRAznLjZaE/11VIROr6BDCxFJQ0ClAiGq6HbalrPu5GVR2f1vKZcSXUkeTV+TyySIDEeD0tNAn16+Lqi9PuwxGXOb0AWuc4BguA6AugJdSV4ON7950peW1HAhjjSwTgxBCOqBdT1qs+fHTm6ip0wZedJXLoc0fa4VFzZL1qS6gr0acPH9VP7j8IAkhd+tSfGh+OzKbuPuve78iDn3IL0Pm5X5Hz4/wogtadzwXtH7TJSAl1XeDHfFEfPnsRjjepS18QIDE+CNDUHWnvD/WMALknnPXn5g8CuBcYX2LdL8DnezjndYumxqsArm68LS5JDmGRtPpSsO4jVp8DEpx/v4wIS5vjGYF0wmSO0N4/bQ18PB/Dyq1kXDGYMLc5nsGE2WQONRUA+3g+hpVbybhiMGFuczwDyeq4SQASIJQggPbxfAwrt5JxSSBBNnwyaBiD5NhKYqJflAzjnEpzY55J8MSrLCJmEjVekfFUiONn7aVGS6xLeXLRt6Pzunr7xTbxnXQvPllwomhM3BRALjS6uAogZ3zTDjHiw3uBxuvF6fPXrsS6lEx+cAJ4spYAkVkC6MUpJUDj45xKc2OeSfDEqywiFpGVumx12b76pPwTcX3i43gx8bEA0jcWAbQfn6jz+S3r6v6JmvEqgH4HjFWAQMwLEMU1fb24rRBgt7vc8A6I4qwdoKeA9LMA0jeWY3AtAkippwL08TyluTHPJHjiVRYRiwSAU4Dnagm1/qQAKuDYBOC3OqnzXOzv2v3bHxrPU5ob80yCJ15lkZSxCEqW47r4PvFl5MU4J86NeU6YMGE4kO+Dkr4JY4W89JT0lUBeprCO7RRKYu4EeBGSoy/0u3O/uwjhGOdvbolaYj34C4lvHCiAa/sLT1vPC5DrGzyq5w1BsePu6QefvOR4v+VzJe4AY47BA1+Gej54Ger5VAD/Noh9o0RKAO5TbI0AN94B+p2BAhhxg8etBfC/B2DfqMCnQOQT8qlTQAUAgaw5Bo8UQUXOvzVQktE9IEOcfwtg/2jAP5hwm+MZGD/KHziYMLc5njEJMAkwMgEkSTYkvMx4XOl4ieE8NgZOvoQAEun9F1lM/6Fqmf/XOOfB4Fdobq8NTH4oAijujLiCya8qgGUcxyYxnMfGwMmXEEAilnEc2yTAJMAkwCQA57ExcPIlBJCIZRzHNgkwCbA+Af4CGrmggJaLwoQAAAAASUVORK5CYII=";

let skinViewer = null;
let isAutoRotating = false;

function initSkinViewer() {
  const canvas = document.getElementById("skin-canvas");
  if (!canvas) return;

  if (typeof skinview3d === "undefined") {
    console.warn("skinview3d no está disponible todavía, reintentando...");
    setTimeout(initSkinViewer, 100);
    return;
  }

  try {
    // Inicializar el visor con dimensiones y canvas
    skinViewer = new skinview3d.SkinViewer({
      canvas: canvas,
      width: 320,
      height: 420,
      skin: DESTINY_SKIN_BASE64,
      model: "slim" // Modelo slim (3px brazos estilo anime girl)
    });

    // Fondo transparente para integrarse con el pedestal pastel y efecto vidrio
    skinViewer.background = null;

    // Configuración de zoom, fov y posición
    skinViewer.zoom = 0.92;
    skinViewer.fov = 48;
    skinViewer.playerObject.position.y = -2;
    skinViewer.playerObject.rotation.y = 0.45; // Ángulo elegante 3/4 hacia el frente

    // Iluminación radiante equilibrada para colores nítidos y sin sobreexposición
    const isDark = document.body.classList.contains('dark-mode');
    if (skinViewer.globalLight) skinViewer.globalLight.intensity = isDark ? 2.9 : 3.2;
    if (skinViewer.cameraLight) skinViewer.cameraLight.intensity = isDark ? 1.05 : 1.1;

    // Desactivar movimiento de cámara por defecto para usar rotación tipo plataforma giratoria (turntable)
    if (skinViewer.controls) {
      skinViewer.controls.enabled = false;
    }

    // Animación suave de respiración / espera (Idle Animation)
    const idleAnim = new skinview3d.IdleAnimation();
    idleAnim.speed = 0.8;
    skinViewer.animation = idleAnim;

    // Configurar rotación manual interactiva por arrastre con inercia (Mouse y Touch)
    setupTurntableControls(canvas);

    console.log("Visor 3D WebGL de Destiny Owners inicializado correctamente.");
  } catch (err) {
    console.error("Error al inicializar el visor 3D, activando fallback visual:", err);
    activateSkinFallback();
  }
}

let isDragging = false;
let lastPointerX = 0;
let rotationVelocity = 0;

/**
 * Controles de plataforma giratoria (turntable) con arrastre intuitivo y desaceleración física
 */
function setupTurntableControls(canvas) {
  canvas.addEventListener('pointerdown', (e) => {
    isDragging = true;
    lastPointerX = e.clientX;
    rotationVelocity = 0;
    try { canvas.setPointerCapture(e.pointerId); } catch (_) {}
    canvas.classList.add('cursor-grabbing');
    canvas.classList.remove('cursor-grab');
  });

  canvas.addEventListener('pointermove', (e) => {
    if (!isDragging || !skinViewer || !skinViewer.playerObject) return;
    const deltaX = e.clientX - lastPointerX;
    rotationVelocity = deltaX * 0.012;
    skinViewer.playerObject.rotation.y += rotationVelocity;
    lastPointerX = e.clientX;
  });

  const endDrag = (e) => {
    if (isDragging) {
      isDragging = false;
      canvas.classList.remove('cursor-grabbing');
      canvas.classList.add('cursor-grab');
      try { canvas.releasePointerCapture(e.pointerId); } catch (_) {}
    }
  };

  canvas.addEventListener('pointerup', endDrag);
  canvas.addEventListener('pointercancel', endDrag);

  // Bucle de animación continuo para inercia y auto-giro
  function rotationLoop() {
    if (skinViewer && skinViewer.playerObject) {
      if (!isDragging) {
        if (Math.abs(rotationVelocity) > 0.0003) {
          skinViewer.playerObject.rotation.y += rotationVelocity;
          rotationVelocity *= 0.92; // Amortiguación física
        } else if (isAutoRotating) {
          skinViewer.playerObject.rotation.y += 0.016; // Giro continuo
        }
      }
    }
    requestAnimationFrame(rotationLoop);
  }
  requestAnimationFrame(rotationLoop);
}

function activateSkinFallback() {
  const canvas = document.getElementById("skin-canvas");
  const fallback = document.getElementById("skin-fallback-img");
  if (canvas) canvas.style.display = "none";
  if (fallback) fallback.classList.remove("hidden");
}

/**
 * Actualiza la intensidad de iluminación al alternar Modo Noche / Día
 */
function updateSkinViewerLighting(isDark) {
  if (!skinViewer) return;
  if (skinViewer.globalLight) skinViewer.globalLight.intensity = isDark ? 2.9 : 3.2;
  if (skinViewer.cameraLight) skinViewer.cameraLight.intensity = isDark ? 1.05 : 1.1;
}

/**
 * Gira la skin un incremento específico en grados (-30° o +30°)
 */
function rotateSkinStep(deltaDegrees) {
  if (!skinViewer || !skinViewer.playerObject) return;
  const radians = (deltaDegrees * Math.PI) / 180;
  skinViewer.playerObject.rotation.y += radians;
}

/**
 * Alterna el giro continuo automático
 */
function toggleAutoRotate() {
  if (!skinViewer) return;
  isAutoRotating = !isAutoRotating;
  rotationVelocity = 0;

  const btn = document.getElementById("btn-autorotate");
  if (btn) {
    if (isAutoRotating) {
      btn.classList.add("bg-pastel-denim", "text-white");
      btn.classList.remove("bg-white", "text-pastel-denim");
    } else {
      btn.classList.remove("bg-pastel-denim", "text-white");
      btn.classList.add("bg-white", "text-pastel-denim");
    }
  }
}

// Exportar funciones a la ventana global para los botones interactivos
window.initSkinViewer = initSkinViewer;
window.rotateSkinStep = rotateSkinStep;
window.toggleAutoRotate = toggleAutoRotate;
window.updateSkinViewerLighting = updateSkinViewerLighting;

// Inicializar cuando el DOM esté listo
if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initSkinViewer);
} else {
  initSkinViewer();
}
