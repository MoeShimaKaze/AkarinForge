#!/usr/bin/env bash

(
set -e
basedir="$(cd "$pwd" && pwd -P)"
srcdir="$basedir/projects/Forge"

echo "[Akarin Forge] Setup Project.."
(
	cd "$srcdir/src"
	if [ -d "$srcdir/src/main" ]; then
		\rm "$srcdir/src/main" -rf
	fi
	if [ ! -d "$srcdir/src/.git" ]; then
		git init --separate-git-dir ../../../repo/src.git
		git remote add origin https://github.com/Akarin-project/ForgeBedrock.git
	fi
	git pull origin master
)

)