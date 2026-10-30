# CyberShield — Learn. Detect. Defend.

CyberShield is a responsive, interactive **cybersecurity education platform** built with React, TypeScript, Vite, and Lucide icons. It is intended as a demonstrable academic software project and learning prototype.

## Requirements

- Node.js 22.12+ or 24+ (your Node.js 24.4.1 is suitable)
- npm
- VS Code (recommended)

## Install and run on macOS

```bash
cd ~/Downloads/cybershield
npm install
npm run dev
```

Open the **Local** URL printed by Vite. If port 5173 is occupied, Vite may use 5174 or another port.

## Production build

```bash
npm run build
npm run preview
```

The build output is in `dist/`. For a static deployment, use `npm run build` as the build command and `dist` as the output directory.

## Features

1. **Overview dashboard** — progress, completion metrics, and learning shortcuts.
2. **Phishing lab** — four entirely fictional email scenarios, immediate explanations, and accuracy tracking.
3. **Password lab** — a *local-only* illustrative passphrase strength indicator; never enter a real password.
4. **Learning academy** — four introductory lessons covering phishing, authentication, privacy, and safer connections.
5. **Achievements** — three milestones based on completed activities.
6. **Progress report** — export learning statistics as a local JSON file.
7. **Responsive design** — desktop sidebar and mobile navigation.
8. **Persistence** — learning progress is saved to browser `localStorage`, with a reset option.

## Safety and privacy

- All simulated email addresses and URLs use reserved `.example` domains and are displayed as **non-clickable text**.
- The password demonstration evaluates input in memory and **does not store or send the sample**.
- Only lesson progress and phishing scores are stored in the browser; no server or account is involved.
- No scanning, hacking, breach checking, or real-time threat detection is performed.
- Educational scores and badges are **not** certifications or guarantees of cybersecurity competence.

## Technology and architecture

- React 19 for component-based UI
- TypeScript for type checking
- Vite for local development and bundling
- Lucide React for icons
- CSS for responsive layout and visual design
- Browser `localStorage` for a simple client-side progress store

```text
User → React UI → Phishing / Password / Academy modules
                 ↘ localStorage (learning progress only)
                 ↘ JSON report download (on user request)
```

## Current scope and future full-stack work

This release is a **complete standalone frontend MVP**, not a production-ready full-stack system. It does **not** include Java Spring Boot, PostgreSQL, login, multi-user accounts, server-side analytics, or an AI model. Those require a separate backend implementation and testing.

A sensible next phase is a Spring Boot REST API, PostgreSQL user progress storage, secure account management, automated tests, and a deployment pipeline.

## Academic presentation

When presenting the project, describe the problem (cybersecurity awareness), target audience (beginner learners), implemented features, architecture, design decisions, limitations, and planned extensions. Avoid claiming it detects real threats or uses AI.
