/**
 * Módulo de Equipo y Miembros
 * Renderiza slots limpios de "Unknown Member" mientras se confirman los integrantes oficiales.
 */

export function renderTeamRoster(): void {
  const container = document.getElementById('team-roster-container');
  if (!container) return;

  const slots = [1, 2, 3, 4];
  container.innerHTML = slots.map(num => `
    <article class="roster-member-item p-5 rounded-3xl flex items-center justify-between gap-4 transition-all">
      <div class="flex items-center gap-4">
        <div class="w-12 h-12 rounded-2xl bg-pastel-periwinkle/30 border border-pastel-cardBorder flex items-center justify-center text-pastel-denim text-lg shadow-inner">
          <i class="fa-solid fa-user-secret text-base"></i>
        </div>
        <div>
          <h4 class="text-sm font-bold text-pastel-plum">Unknown Member</h4>
          <span class="text-[10px] text-pastel-plum/60 font-medium">#DESTINY-0${num}</span>
        </div>
      </div>
    </article>
  `).join('');
}
