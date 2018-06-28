#!/usr/bin/env bash

(
set -e
basedir="$(cd "$pwd" && pwd -P)"
srcdir="$basedir/projects/Forge"
tempdir="$basedir/cache"

echo "[Akarin Forge] Setup workspace.."
(
	chmod +x scripts/cache.sh
	./scripts/cache.sh
	
	echo "[Akarin Forge] Setup Forge.."
	cd "$basedir"
	gradle setupForge
	
	cd "$basedir"
	chmod +x scripts/retrieve.sh
	./scripts/retrieve.sh
	echo "[Akarin Forge] Finished setup workspace"
)

)