# PLM Low-Code Platform

Internal AMIS page designer for PLM. The first milestone provides a React-based
AMIS Editor, a Spring Boot API, PostgreSQL persistence, and immutable page
versions.

## Development

Module directory: `D:\PLMLowCode\app`. Paths below are relative to this directory.
The PLM deployment sources are in `../plm`; the Vue Widget is in
`../dashboard/TWX_ENOPS_app`. See `../PROJECT_OVERVIEW.md` for the project layout.

1. Start the backend with `scripts/start-backend.ps1`.
2. Start the frontend with `scripts/start-frontend.ps1`.
3. Open `http://127.0.0.1:5173`.

The backend listens on `http://127.0.0.1:8080`.

Chinese usage manual: `docs/PLM低代码设计器使用手册.md`.

## Build

Run `scripts/build.ps1`. The frontend is built first and bundled into the
Spring Boot executable jar.
