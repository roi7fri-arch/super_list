# Strict Prompt — Armbian-Style Download UI (Short)

You are a senior UI/UX + frontend engineer.
Create an **original** dark download-portal UI inspired by the structure/feel of image.armbian.com/#downloads (no copied branding, assets, or code).

## Hard Requirements
- Stack: React + Tailwind + TypeScript.
- Layout: Hero/title → filters/tabs + search → download cards/grid → footer.
- Responsive: mobile, tablet, desktop.
- Accessibility: WCAG AA contrast, keyboard navigation, visible focus states, semantic HTML.
- Performance: lightweight components, no unnecessary dependencies.
- Theme: dark-first + optional light toggle.

## Visual Spec
- Background: deep charcoal with subtle gradient/noise.
- Accent: electric blue/cyan.
- Cards: clean border + soft shadow, compact technical style.
- Typography: modern sans-serif, dense but readable.
- Interactions: hover, active, selected, loading, error states.

## Components to Build
- `Header`
- `Hero`
- `FilterTabs`
- `SearchBar`
- `DownloadCard`
- `Badge`
- `PrimaryButton` / `SecondaryButton`
- `EmptyState`
- `NoResultsState`
- `Footer`

## Data Model
Define a typed sample schema for download entries (id, board, os, version, size, checksum, status, url, updatedAt).

## Required Output
1. Design tokens (color, spacing, radius, shadow, typography).
2. Wireframe-level section plan.
3. High-fidelity UI description.
4. Production-ready React + Tailwind code.
5. Component states: default/hover/selected/loading/error.
6. Desktop and mobile screenshots.

## Quality Bar
- Keep code modular and reusable.
- No placeholder junk text beyond minimal sample data.
- Keep result concise, polished, and deployable.
