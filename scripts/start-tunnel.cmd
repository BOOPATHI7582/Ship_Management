@echo off
REM Starts a public Cloudflare tunnel to your local backend (port 8080).
REM Keep this window OPEN while using the Netlify site.
REM When the URL below changes, update frontend\netlify.toml and redeploy Netlify.
echo ===============================================
echo  ExportPlatform public API tunnel
echo ===============================================
"C:\Program Files (x86)\cloudflared\cloudflared.exe" tunnel --url http://localhost:8080 --protocol http2
pause
