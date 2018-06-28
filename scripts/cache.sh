#!/usr/bin/env bash

(
set -e
basedir="$(cd "$pwd" && pwd -P)"
srcdir="$basedir/projects/Forge"
tempdir="$basedir/cache"

(
	echo "[Akarin Forge] Clean cache.."
	if [ -d "$tempdir" ]; then
		\rm "$tempdir" -rf
	fi
	mkdir "$tempdir"
	
	echo "[Akarin Forge] Touch sources: workspace -> cache"
	\cp -rf "$srcdir/src" "$tempdir/"
	\cp -rf "$srcdir/build.gradle" "$tempdir/"
)

)