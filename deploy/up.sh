#!/bin/sh
set -eu

cd "$(dirname "$0")/.."

docker compose \
  --env-file ../private-config/ai-chat-workspace.env \
  -f deploy/docker-compose.yml \
  up -d