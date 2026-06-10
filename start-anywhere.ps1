param(
    [int]$Port = 8080
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$env:SERVER_PORT = "$Port"
$env:SERVER_ADDRESS = "0.0.0.0"

$addresses = Get-NetIPAddress -AddressFamily IPv4 |
    Where-Object {
        $_.IPAddress -notlike "127.*" -and
        $_.IPAddress -notlike "169.254.*" -and
        $_.PrefixOrigin -ne "WellKnown"
    } |
    Select-Object -ExpandProperty IPAddress -Unique

Write-Host "Starting airport-flight-simulation with local H2 database..."
Write-Host "Local URL: http://localhost:$Port/"
foreach ($address in $addresses) {
    Write-Host "LAN URL:   http://$address`:$Port/"
}
Write-Host ""
Write-Host "Use Ctrl+C to stop the server."

mvn package "-DskipTests"
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

java -jar "target/airport-flight-simulation-0.0.1-SNAPSHOT.jar" "--spring.profiles.active=local"
