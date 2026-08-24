# ExportPlatform API smoke suite
# Usage: powershell -File scripts\smoke-api.ps1 [-BaseUrl http://localhost:8080]
param($BaseUrl = 'http://localhost:8080')

$ErrorActionPreference = 'Stop'
$script:passed = 0
$script:failed = 0

function Check($name, $condition) {
  if ($condition) { $script:passed++; Write-Host "  PASS $name" -ForegroundColor Green }
  else { $script:failed++; Write-Host "  FAIL $name" -ForegroundColor Red }
}

function Api($method, $path, $token, $body) {
  $headers = @{}
  if ($token) { $headers.Authorization = "Bearer $token" }
  try {
    $resp = Invoke-RestMethod -Uri "$BaseUrl$path" -Method $method -Headers $headers `
      -ContentType 'application/json' -Body ($body | ConvertTo-Json) -TimeoutSec 20
    return @{ ok = $true; data = $resp.data; message = $resp.message; success = $resp.success }
  } catch {
    return @{ ok = $false; status = [int]$_.Exception.Response.StatusCode }
  }
}

Write-Host "== health & public endpoints =="
$stats = Api Get '/api/public/stats'
Check 'public stats reachable' $stats.ok
$reviews = Api Get '/api/public/reviews'
Check 'public reviews list' ($reviews.ok -and $reviews.data.Count -ge 0)

Write-Host "== auth =="
$admin = Api Post '/api/auth/login' $null @{ email = 'admin@exportplatform.com'; password = 'Admin@123' }
Check 'admin login' ($admin.ok -and $admin.data.accessToken)
$badLogin = Api Post '/api/auth/login' $null @{ email = 'admin@exportplatform.com'; password = 'nope' }
Check 'wrong password rejected' (-not $badLogin.ok)
$client = Api Post '/api/auth/login' $null @{ email = 'inv5553@test.com'; password = 'Test@123' }
Check 'client login' ($client.ok -and $client.data.accessToken)

if (-not $admin.ok) { Write-Host 'Admin unavailable, aborting.' -ForegroundColor Red; exit 1 }

Write-Host "== role guards =="
$forbidden = Api Get '/api/manager/audit' $client.data.accessToken
Check 'client blocked from audit (401/403)' (-not $forbidden.ok)
$anon = Api Get '/api/manager/reports/overview' $null
Check 'anonymous blocked from reports' (-not $anon.ok)

Write-Host "== manager surfaces =="
$report = Api Get '/api/manager/reports/overview' $admin.data.accessToken
Check 'reports overview' ($report.ok -and $report.data.revenueTrend.Count -eq 12)
$audit = Api Get '/api/manager/audit?size=5' $admin.data.accessToken
Check 'audit log paged' ($audit.ok -and $audit.data.content.Count -ge 1)
$shipments = Api Get '/api/manager/shipments?size=5' $admin.data.accessToken
Check 'shipment list' ($shipments.ok)
$invoices = Api Get '/api/manager/invoices?size=5' $admin.data.accessToken
Check 'invoice list' ($invoices.ok)

Write-Host "== client surfaces =="
$mine = Api Get '/api/client/shipments' $client.data.accessToken
Check 'client shipments scoped' ($mine.ok)
$myInvoices = Api Get '/api/client/invoices' $client.data.accessToken
Check 'client invoices scoped' ($myInvoices.ok)

Write-Host ""
Write-Host "Smoke result: $passed passed, $failed failed" -ForegroundColor ($(if ($failed -eq 0) { 'Green' } else { 'Red' }))
exit $(if ($failed -eq 0) { 0 } else { 1 })
