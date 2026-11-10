# Phase 7 Slice 1 smoke: build the production Docker image, boot it against
# a real Postgres via docker compose, and verify the app answers on /actuator/health
# and renders the anonymous home page. Prints PASS / FAIL and exits non-zero on failure.
#
# Usage (from repo root):
#   .\scripts\smoke-postgres.ps1
#
# Teardown on success: `docker compose down -v` (wipes the pg volume too).
# Teardown on failure: left running so you can inspect `docker compose logs app`.

$ErrorActionPreference = 'Stop'

function Fail($msg) {
    Write-Host "FAIL: $msg" -ForegroundColor Red
    Write-Host "Inspect with: docker compose logs app" -ForegroundColor Yellow
    exit 1
}

Write-Host "==> Building and starting compose stack..." -ForegroundColor Cyan
docker compose up --build -d
if ($LASTEXITCODE -ne 0) { Fail "docker compose up failed" }

Write-Host "==> Waiting for /actuator/health to report UP (max 90s)..." -ForegroundColor Cyan
# Invoke-RestMethod parses JSON into an object regardless of Content-Type.
# Spring Boot Actuator uses application/vnd.spring-boot.actuator.v3+json, which
# makes Invoke-WebRequest return Content as a byte array — regex matches
# against bytes silently fail, which is how we lost 90s on the previous run.
$deadline = (Get-Date).AddSeconds(90)
$healthy = $false
$lastErr = $null
while ((Get-Date) -lt $deadline) {
    try {
        $r = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' `
                               -TimeoutSec 3 -ErrorAction Stop
        if ($r.status -eq 'UP') {
            $healthy = $true
            break
        }
    } catch {
        $lastErr = $_.Exception.Message
    }
    Start-Sleep -Seconds 2
}
if (-not $healthy) { Fail "/actuator/health never reported UP within 90s. Last error: $lastErr" }
Write-Host "    OK - /actuator/health UP" -ForegroundColor Green

Write-Host "==> Smoke: anonymous GET / should be 200..." -ForegroundColor Cyan
# Note: avoid $home as a variable name — it's a read-only PowerShell built-in.
try {
    $homeResp = Invoke-WebRequest -Uri 'http://localhost:8080/' `
                                  -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    if ($homeResp.StatusCode -ne 200) { Fail "GET / returned $($homeResp.StatusCode), expected 200" }
} catch { Fail "GET / threw: $_" }
Write-Host "    OK - home page renders" -ForegroundColor Green

Write-Host "==> Smoke: anonymous GET /login should be 200..." -ForegroundColor Cyan
try {
    $loginResp = Invoke-WebRequest -Uri 'http://localhost:8080/login' `
                                   -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    if ($loginResp.StatusCode -ne 200) { Fail "GET /login returned $($loginResp.StatusCode), expected 200" }
} catch { Fail "GET /login threw: $_" }
Write-Host "    OK - login page renders" -ForegroundColor Green

Write-Host ""
Write-Host "PASS - app boots against real Postgres, Flyway migrations applied cleanly," -ForegroundColor Green
Write-Host "       entity-schema validation passed, anonymous surfaces render." -ForegroundColor Green
Write-Host ""
Write-Host "Stack still running. Open http://localhost:8080 to click around." -ForegroundColor Cyan
Write-Host "Tear down with: docker compose down -v" -ForegroundColor Cyan
