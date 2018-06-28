#!/usr/bin/env bash

(
set -e
basedir="$(cd "$pwd" && pwd -P)"
srcdir="$basedir/projects/Forge"
tempdir="$basedir/cache"

echo "[Akarin Forge] Setup Workspace.."
(
	echo "[Akarin Forge] Clean cache.."
	if [ -d "$tempdir" ]; then
		rm "$tempdir" -r
	fi
	mkdir "$tempdir"
	
	echo "[Akarin Forge] Touch sources: workspace -> cache"
	\cp -rf "$srcdir/src" "$tempdir/"
	\cp -rf "$srcdir/build.gradle" "$tempdir/"
	
	echo "[Akarin Forge] Setup Forge.."
	cd "$basedir"
	gradle setupForge
	rm "$srcdir/src" -r
	
	echo "[Akarin Forge] Touch sources: cache -> workspace"
	\cp -rf "$tempdir/src" "$srcdir/"
	\cp -rf "$tempdir/build.gradle" "$srcdir/build.gradle"
	
	echo "[Akarin Forge] Clean cache.."
	if [ -d "$tempdir" ]; then
		rm "$tempdir" -r
	fi
	echo "[Akarin Forge] Finished"
)

)