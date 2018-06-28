#!/usr/bin/env bash

(
set -e
basedir="$(cd "$pwd" && pwd -P)"
srcdir="$basedir/projects/Forge"
tempdir="$basedir/cache"

(
	echo "[Akarin Forge] Touch sources: cache -> workspace"
	if [ -d "$srcdir/src/main" ]; then
		\rm "$srcdir/src/main" -rf
	fi
	\cp -rf "$tempdir/src" "$srcdir/"
	\cp -rf "$tempdir/build.gradle" "$srcdir/build.gradle"
)

)