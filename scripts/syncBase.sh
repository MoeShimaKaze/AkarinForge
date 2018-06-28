#!/usr/bin/env bash

(
set -e
basedir="$(cd "$pwd" && pwd -P)"
srcdir="$basedir/projects/Forge"

(
	echo "[Akarin Forge] Setup base.."
	git remote add upstream https://github.com/Akarin-project/MinecraftForge.git || true
	
	echo "[Akarin Forge] Sync with base.."
	git pull upstream 1.12.x
	
	echo "[Akarin Forge] Finished sync"
)

)