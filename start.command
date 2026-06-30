#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

show_help() {
    cat <<'EOF'
Usage:
  ./start.command        Start on default port 8080
  ./start.command 8090   Start on port 8090

Then open:
  http://localhost:PORT/dashboard
EOF
}

PORT="${1:-8080}"
if [[ "$PORT" == "-h" || "$PORT" == "--help" || "$PORT" == "/?" ]]; then
    show_help
    exit 0
fi

if ! [[ "$PORT" =~ ^[0-9]+$ ]]; then
    echo "[ERROR] Port must be a number, got: $PORT"
    show_help
    exit 1
fi

if ! command -v java >/dev/null 2>&1; then
    echo "[ERROR] Java was not found."
    echo "Install JDK 17 first, for example: brew install openjdk@17"
    read -r -p "Press Enter to exit..."
    exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
    echo "[ERROR] Maven was not found."
    echo "Install Maven first, for example: brew install maven"
    read -r -p "Press Enter to exit..."
    exit 1
fi

export SERVER_PORT="$PORT"
export SERVER_ADDRESS="0.0.0.0"

echo "Starting Airport Flight Simulation..."
echo
echo "Local URL: http://localhost:$PORT/dashboard"
echo "Data import: http://localhost:$PORT/operations/data"

if command -v ipconfig >/dev/null 2>&1; then
    for iface in en0 en1; do
        address="$(ipconfig getifaddr "$iface" 2>/dev/null || true)"
        if [[ -n "$address" ]]; then
            echo "LAN URL:   http://$address:$PORT/dashboard"
        fi
    done
fi

echo
echo "Keep this window open while using the system."
echo "Press Ctrl+C to stop the server."
echo

mvn package -DskipTests
java -jar "target/airport-flight-simulation-0.0.1-SNAPSHOT.jar" --spring.profiles.active=local
