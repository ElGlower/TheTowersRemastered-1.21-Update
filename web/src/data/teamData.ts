import { TeamMember } from '../types';

/**
 * Datos auténticos y oficiales del equipo de desarrollo de Destiny Towers / Destiny Owners
 * Sin usuarios ficticios ni información simulada.
 */
export const OFFICIAL_TEAM: TeamMember[] = [
  {
    id: 'elglower',
    name: 'ElGlower',
    handle: '@ElGlower',
    role: 'Fundador & Desarrollador Principal',
    tag: 'Destiny Owners Core',
    bio: 'Líder del proyecto, modernización a Paper 1.21+, arquitectura de sistemas y gestión de infraestructura en la red.',
    githubUrl: 'https://github.com/ElGlower',
    isLead: true,
    avatarUrl: 'https://github.com/ElGlower.png'
  },
  {
    id: 'startces',
    name: 'StartCes',
    handle: '@StartCes',
    role: 'Desarrollador Core & Arquitectura',
    tag: 'Desarrollo',
    bio: 'Motor de regeneración atómica (Rollback Engine), soporte para Paper 26.x y optimización de rendimiento.',
    githubUrl: 'https://github.com/StartCes',
    avatarUrl: 'https://github.com/StartCes.png'
  },
  {
    id: 'ripkyng1',
    name: 'Ripkyng1',
    handle: '@Ripkyng1',
    role: 'Desarrollador Core & Diseño de Juego',
    tag: 'Desarrollo',
    bio: 'Economía de la Tienda del Faro, calibración de proyectiles (Wind Charges), eventos dinámicos y balance táctico.',
    githubUrl: 'https://github.com/Ripkyng1',
    avatarUrl: 'https://github.com/Ripkyng1.png'
  },
  {
    id: 'paumava',
    name: 'Pau Machetti',
    handle: '@PauMAVA',
    role: 'Autor Original del Minijuego',
    tag: 'Mención de Honor',
    bio: 'Creador de la modalidad clásica The Towers original. Agradecimiento especial por la inspiración comunitaria.',
    githubUrl: 'https://github.com/PauMAVA',
    avatarUrl: 'https://github.com/PauMAVA.png'
  }
];
