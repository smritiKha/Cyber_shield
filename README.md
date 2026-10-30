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

## Complete feature guide (October 2026)

CyberShield is a **client-side educational prototype**, not a production security scanner or certification platform.

| Section | What you can do |
| --- | --- |
| Overview | See learning statistics and shortcuts to practical activities |
| Learning roadmap | Follow six milestones, including four guided modules, phishing practice, and a beginner knowledge check |
| Learning academy | Read four guided lessons, answer a knowledge check, and mark each module complete |
| Phishing lab | Evaluate eight **fictional** message examples, review clues, and see round accuracy |
| Password lab | Compare fictional example passwords and learn about unique credentials, managers, and MFA |
| Knowledge quiz | Select topic and difficulty from a 15-question bank, submit answers, and review explanations |
| Achievements | See milestones, topic performance, recent quiz activity, and personalized next steps |
| Reports | Download a printable HTML report or export/restore a JSON progress backup |

### Progress and privacy

- All learning progress is stored **locally in the current browser** using the `cybershield-progress-v1` localStorage key. There is no account system or backend synchronization.
- Export JSON from **Achievements** to retain a backup before clearing browser storage or switching devices. Use **Restore exported JSON** to load it on another device. Restoration replaces the current device's progress after confirmation.
- The export contains completed lessons, phishing scores, and quiz history. Treat it as personal learning data; avoid publishing it.
- The password lab is illustrative and runs locally. **Never enter real passwords**. Example passwords are public demonstrations and must not be reused.
- The phishing lab uses fictional `.example` domains; it never asks users to visit suspicious links.
- Topic statistics reflect recorded quiz answers. The quiz history stores a maximum of 100 records, including topic-level breakdown entries. The beginner all-topic best score remains out of five.
- The printable report is a standalone HTML file. Open it in a browser and use **Print → Save as PDF**.

### Quality checks

```bash
npm ci
npm run build
npm run dev
```

The GitHub Actions validation workflow builds the project on pushes and pull requests. Passing a build confirms compilation, not a comprehensive browser or accessibility audit.

### Known limitations

- No authentication, database, cloud sync, certificates, or real threat detection.
- Progress is tied to the browser unless exported and restored.
- Lesson and quiz content is introductory, not a substitute for professional security training.
- The strength indicator is a simple educational heuristic and cannot reliably determine whether a password is compromised.

## Full-stack extension: Spring Boot accounts and saved progress

The project now includes a Spring Boot backend in [backend/](backend/README.md). To use accounts and cloud-saved learning progress, run **both** processes:

```bash
# Terminal 1
cd backend
mvn spring-boot:run

# Terminal 2, repository root
npm ci
npm run dev
```

Requirements: Java 21, Maven 3.9+, Node.js 24+, npm. The development API runs on port 8080; Vite proxies `/api` to it. Without the backend, guest lessons, quizzes, and local progress still work, but registration and cloud synchronization will not.

The backend provides BCrypt-hashed passwords, seven-day revocable bearer sessions, database-backed learning progress, and endpoints for signup/login/logout/account details. Local development uses a persistent H2 database; PostgreSQL can be configured for deployment using environment variables. The frontend session token is stored in browser sessionStorage.

**Important:** This is a functional educational full-stack prototype, **not a production-hardened authentication service**. See the backend README for the remaining production safeguards and deployment configuration. Avoid deploying public registration without abuse protection and monitoring.

### Expanded security education

- URL safety: offline inspection of fictional URL examples, including HTTPS, lookalike subdomains, user information in URLs, and shortened links. It does **not** contact or verify destinations.
- Phishing lab: 16 fictional practice scenarios.
- Knowledge quiz: 27 questions across phishing, authentication, privacy and network safety, with beginner, intermediate and advanced levels.
- Signed-in progress sync: account data is loaded when signing in and saved after changes. Export guest progress before signing in if you want to preserve it. The system does not yet merge simultaneous edits across devices.

## Role-based learning platform and PostgreSQL

The **Learning platform** sidebar page now contains distinct views:

- **Administrator:** content creation/editing/publishing/deletion, quiz and simulation question editing through JSON, aggregate learner statistics, assessment difficulty summaries, and CSV export. The backend checks the `ADMIN` role on every administrative endpoint.
- **Student:** published course enrollment, course completion records, quiz/phishing/privacy challenge submissions, score history, activity streaks, and suggested next steps. The backend checks the `STUDENT` role and only returns that student's records.
- **Guest:** browse published content; sign in to enroll or submit assessments.

The database seeds introductory courses, lessons, a quiz, phishing examples, and a privacy/URL challenge on first launch. Administrators can edit or unpublish these entries through the UI. Existing React academy lessons and labs remain available as additional built-in learning activities.

### Run PostgreSQL locally

Install Docker, Java 21, Maven and Node.js. From the repository root:

```bash
# Create a local .env file (ignored by Git) containing:
# POSTGRES_PASSWORD=<your strong local database password>
docker compose up -d postgres
```

Start Spring Boot from the `backend` folder with the PostgreSQL profile:

```bash
export SPRING_PROFILES_ACTIVE=postgres
export DATABASE_URL=jdbc:postgresql://localhost:5432/cybershield
export DATABASE_USERNAME=cybershield
export DATABASE_PASSWORD='<same password as POSTGRES_PASSWORD>'
export ADMIN_EMAIL='admin@example.com'
export ADMIN_PASSWORD='<a unique password of at least 16 characters>'
mvn spring-boot:run
```

**Important:** Set the environment variables in your own terminal; the angle-bracket examples are placeholders, not credentials. Never commit credentials. Admin creation occurs only when both `ADMIN_EMAIL` and `ADMIN_PASSWORD` are set and the specified email does not exist. Public signup always creates a **STUDENT** account. If the admin email already belongs to a student, it is **not** automatically promoted.

In another terminal, from the repository root:

```bash
npm ci
npm run dev
```

Open the Vite address, choose **My account**, and sign in with the admin credentials to access **Learning platform**. Create a different student account through normal registration to see the student dashboard.

### Assessment content JSON

For `QUIZ`, `PHISHING`, and `CHALLENGE`, use:

```json
{
  "questions": [
    {
      "question": "Which choice is safest?",
      "options": ["Verify independently", "Share a password"],
      "correct": 0,
      "explanation": "Always verify unexpected requests through trusted channels."
    }
  ]
}
```

Correct answers are **not included in the public catalog**. The backend scores student submissions and saves attempts to the database. Courses and lessons use `{"text":"Lesson content"}`.

### Scope and limitations

- The database-backed platform is functional for local academic demonstration, with backend-enforced student/admin roles and integration tests. It has **not** been verified in a deployed production PostgreSQL environment.
- Built-in academy lessons and quizzes remain defined in React, separate from the new database-managed catalog.
- The dashboard's course completion record is **self-reported**, not a verified certificate or externally accredited qualification.
- Student activity streaks count days with saved database assessment attempts in UTC.
- The prototype does not yet support production-grade email verification, password reset, rate limiting, conflict-free concurrent edits, enrollment prerequisites, full lesson-level completion tracking, or audited administrative actions.
