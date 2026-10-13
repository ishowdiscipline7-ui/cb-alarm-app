# CB — Exam Alarm

A polished, responsive exam-planning and alarm dashboard built with vanilla HTML, CSS, and JavaScript. It works as a static site and requires no server, account, API key, or build step.

## Features

- **Exam countdown:** Save an exam name, date, time, and personal note.
- **Alarms:** Create, edit, enable, disable, and dismiss one-time or weekly repeating alarms.
- **Sound choices:** Three generated tones plus an optional uploaded audio file (maximum 2.5 MB).
- **Snooze:** Choose 5, 10, 15 minutes, or turn snooze off.
- **Focus timer:** 25-, 45-, and 60-minute sessions with pause and reset.
- **Wake-up streak:** Check in once per day and see your recent seven-day history.
- **Persistent storage:** Alarms, exam settings, focus preference, streaks, and uploaded audio are stored in the browser's localStorage. No data is sent to a server.
- **Responsive UI:** Desktop sidebar layout adapts for tablets and phones.
- **Notifications:** Requests browser notification permission when you tap the notification icon.

## Run locally

1. Download or clone this repository.
2. Open `index.html` in a modern browser, or serve the folder locally:
   ```bash
   python3 -m http.server 8000
   ```
3. Visit `http://localhost:8000`.
4. Create an alarm and interact with the page once so the browser can allow audio playback. For notifications and best results, use HTTPS or localhost.

There is no backend in this initial implementation. A static deployment such as GitHub Pages can host it.

## Important: browser alarm limitations

CB is a **best-effort browser alarm, not a guaranteed replacement for a phone's native alarm clock**.

- Alarm checks run while the page's JavaScript is active. Browsers throttle background tabs, and a sleeping device may suspend JavaScript.
- If you close the tab/browser, quit the browser, restart the device, or the operating system suspends the page, the alarm may not run at the expected time.
- Browser notifications are permission-based and can be blocked, delayed, or suppressed by OS focus modes. They do not reliably wake a sleeping or powered-off device.
- Audio can be blocked until the user has interacted with the page. Keep the page open, keep the device awake, and test sound volume beforehand.
- For an exam or any high-stakes wake-up, set a backup alarm in your phone's native Clock app.
- A service worker alone cannot run a dependable exact-time audio alarm in the background. Reliable alarms when the app is closed generally require a native mobile application with platform alarm capabilities, or a carefully designed push-notification service (which still cannot guarantee an audible wake-up on every device).

## Storage and privacy

This version stores data only in the current browser profile on this device. Clearing site data, using private browsing, switching browsers/devices, or browser storage eviction may remove it. Uploaded audio is stored as a data URL in localStorage, so it is limited to small files. Export/backup and cross-device sync are not included yet.

## Accessibility and support

- Uses semantic labels, keyboard-focus styles, native dialog forms, and reduced-motion preferences.
- Modern Chrome, Edge, Firefox, and Safari are intended targets. Audio and notification support varies by browser and operating system.
- This is an initial static implementation; it has not been independently security-audited or verified on every device.

## Project structure

- `index.html` — accessible page structure and dialogs
- `styles.css` — responsive dark interface
- `app.js` — alarms, audio, countdown, timer, streaks, notifications, and local persistence
