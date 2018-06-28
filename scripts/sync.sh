#!/usr/bin/env bash

(
set -e
basedir="$(cd "$pwd" && pwd -P)"
srcdir="$basedir/projects/Forge"

(
	chmod +x scripts/cache.sh
	./scripts/cache.sh
	
	echo "[Akarin Forge] Setup upstream project.."
	git submodule update --init --remote
	cd "$srcdir/src"
	if [ -d "$srcdir/src/main" ]; then
		\rm "$srcdir/src/main" -rf
	fi
	cd "$basedir"
	\cp -f "$basedir/git-module" "$srcdir/src"
	\mv -f "$srcdir/src/git-module" "$srcdir/src/.git"
	
	chmod +x scripts/retrieve.sh
	./scripts/retrieve.sh
	
	cd "$srcdir/src"
	git add . && git commit -m '[Akarin Forge] Track changes' || true
	echo "[Akarin Forge] Sync with upstream.."
	git pull origin master
	echo "[Akarin Forge] Clean upstream cache.."
	\rm ".git" -rf
	
	echo "[Akarin Forge] Finished sync"
)

)