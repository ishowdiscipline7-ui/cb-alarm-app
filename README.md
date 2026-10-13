# CB — Exam Alarm

A responsive exam planner and alarm dashboard with optional Supabase accounts and cloud sync. The frontend is vanilla HTML/CSS/JavaScript; no build process is needed.

## What works without setup

- Exam countdown, alarms, sound choices, focus timer, and wake-up streak.
- Local persistence in the current browser.
- Mobile-responsive interface.
- Best-effort browser notifications and alarms (see limitations below).

## Optional cloud accounts and cross-device sync

The cloud integration uses **Supabase Auth + Postgres + Row Level Security (RLS) + Realtime**. There is no custom server to run. Every signed-in user has one `public.user_state` row keyed by their authenticated user ID. RLS policies only permit access where `auth.uid() = user_id`.

### 1. Create the free Supabase project

1. Create a project at https://supabase.com/dashboard. The Free plan is suitable for a small personal project; review the current quotas and inactivity/pause rules before relying on it.
2. In **SQL Editor**, open a new query, paste the complete contents of [`supabase/schema.sql`](supabase/schema.sql), and run it.
3. In **Project Settings → API** (the exact menu labels may change), copy the **Project URL** and the **publishable key** (or legacy anon key).
4. Edit [`supabase-config.js`](supabase-config.js). Replace `https://YOUR_PROJECT_REF.supabase.co` and `YOUR_SUPABASE_PUBLISHABLE_OR_ANON_KEY` with those values. This browser file is intentionally public: use only the publishable/anon key. **Never place a service-role key, database password, or other secret in frontend code or Git.**
5. In Supabase **Authentication → URL Configuration**, set **Site URL** to `https://starlit-cobbler-1fd9b0.netlify.app` and add `https://starlit-cobbler-1fd9b0.netlify.app/**` to **Redirect URLs** (or add the exact URL `https://starlit-cobbler-1fd9b0.netlify.app/`). Add `http://localhost:8000/**` only if you use the local development server.
6. In **Authentication → Providers → Email**, keep email/password enabled. Email confirmation may be enabled; if it is, users must confirm their email before signing in.
7. Deploy/redeploy the site, then click **Sign in to sync** → **Create account**. Sign in with the same account on another device to load that account's cloud-saved exam plan and alarms.

### 2. Security notes

- `supabase/schema.sql` enables RLS, removes anonymous table access, and grants authenticated users only the required row operations.
- The browser uses only the public publishable/anon key. The database must never trust a user-supplied user ID; the policies compare it with the verified `auth.uid()`.
- Supabase handles password hashing, login sessions, and token refresh. CB does not store passwords in its own database.
- Realtime updates propagate changes made on another signed-in device. This simple version uses a last-write-wins state document; simultaneous edits can overwrite one another. It is designed for personal use, not collaborative multi-user editing.
- The uploaded custom sound remains local to each device and is not uploaded to the cloud. Exam details, alarm settings, focus duration, and streak data are synced. Cloud account deletion and account recovery are managed in Supabase.
- For production, add tested conflict resolution, data export/deletion UX, monitoring, and a privacy policy before inviting many users.

## Free hosting options

| Host | Good for | Notes |
| --- | --- | --- |
| **GitHub Pages** | Easiest option for this repo | Static HTML/CSS/JS hosting; no backend runtime. Supabase provides the auth/database API separately. |
| **Cloudflare Pages** | Fast static hosting with Git deployment | Connect the repo and use the repository root as the build output directory; no build command is needed. |
| **Netlify** | Simple Git-based static deploys | Import the repo and publish the root folder. Netlify Functions are optional, not required for the Supabase setup. |
| **Vercel** | Git-based static deployment | Import the repo; use no build command and the root directory. |

Free tiers and quotas can change; check the providers' current pricing pages. Supabase Free currently advertises limited free database/storage/egress quotas and projects may pause after inactivity. Keep a backup for important exam data.

## Run locally

1. Clone/download the repo.
2. Start a local static server from the repository root, for example:
   ```bash
   python3 -m http.server 8000
   ```
3. Open `http://localhost:8000`. Add the local URL to Supabase Auth's allowed redirect URLs if you use accounts locally.

## Browser alarm limitations

CB is a **best-effort browser alarm, not a guaranteed replacement for a phone's native alarm clock**. JavaScript timers can be throttled or suspended when the tab is backgrounded, the device sleeps, the browser is closed, or the OS stops the page. Browser notifications may be blocked or delayed and cannot reliably wake a powered-off/sleeping device. For an exam or critical wake-up, always set a backup alarm in your phone's native Clock app. A service worker alone cannot provide a dependable exact-time audio alarm.

## Data and custom audio

Without sign-in, data is saved in the current browser profile only. With sign-in, the exam plan, alarms, focus duration, and streaks sync to the authenticated account. Uploaded audio stays local to each browser; use the same device for that sound. Clearing browser data can remove local-only settings and uploaded audio.

## Files

- `index.html` — interface and dialogs
- `styles.css` — responsive styling
- `app.js` — alarm/timer logic, local persistence, auth, cloud sync
- `supabase-config.js` — public project URL and publishable key placeholders
- `supabase/schema.sql` — database schema, RLS policies, and Realtime setup
