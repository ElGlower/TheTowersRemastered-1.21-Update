/**
 * Módulo de Equipo Oficial y Colaboradores de Destiny Owners
 */

import { OFFICIAL_TEAM } from '../data/teamData';

export function renderTeamRoster(): void {
  const container = document.getElementById('team-roster-container');
  if (!container) return;

  container.innerHTML = OFFICIAL_TEAM.map(member => `
    <article class="roster-member-item p-6 rounded-3xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-5 transition-all">
      <div class="flex items-center gap-4">
        <div class="relative">
          <img 
            src="${member.avatarUrl}" 
            alt="${member.name}" 
            class="w-14 h-14 rounded-2xl object-cover shadow-sm border border-pastel-cardBorder bg-white/70"
            onerror="this.src='https://visage.surgeplay.com/bust/128/Steve'"
          />
          ${member.isLead ? `
            <span class="absolute -top-1.5 -right-1.5 w-6 h-6 rounded-full bg-pastel-denim text-white flex items-center justify-center text-[10px] shadow" title="Lead Developer">
              <i class="fa-solid fa-crown text-[9px] text-[#FEF08A]"></i>
            </span>
          ` : ''}
        </div>
        <div>
          <div class="flex items-center gap-2.5">
            <h4 class="text-base font-bold text-pastel-plum tracking-tight">${member.name}</h4>
            <span class="text-[11px] font-semibold text-pastel-denim">${member.handle}</span>
            <span class="text-[10px] px-2.5 py-0.5 rounded-full bg-pastel-blush/60 text-pastel-plum border border-[#E79796]/30 font-semibold hidden md:inline-block">
              ${member.tag}
            </span>
          </div>
          <p class="text-xs text-pastel-plum/70 font-medium mt-0.5">${member.role}</p>
          <p class="text-xs text-pastel-plum/60 mt-1 max-w-xl leading-relaxed">${member.bio}</p>
        </div>
      </div>

      <div class="flex items-center gap-3 self-end sm:self-center shrink-0">
        <a 
          href="${member.githubUrl}" 
          target="_blank" 
          rel="noopener noreferrer" 
          class="px-4 py-2 rounded-full text-xs font-semibold bg-pastel-periwinkle/30 hover:bg-pastel-periwinkle text-pastel-plum border border-pastel-cardBorder transition-all flex items-center gap-2 shadow-sm"
        >
          <i class="fa-brands fa-github text-sm"></i>
          <span>GitHub</span>
        </a>
      </div>
    </article>
  `).join('');
}
