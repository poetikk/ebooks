#!/bin/bash -ex

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd $SCRIPT_DIR/..

echo "Starting: $(date)"
rm -rf files*
git pull
gradle installDist
./build/install/wiki2epub/bin/wiki2epub
ls -1 files/ | wc -l | grep 439 && sleep 3600 && ./build/install/wiki2epub/bin/wiki2epub
ls -1 files/ | wc -l | grep 439 && sleep 3600 && ./build/install/wiki2epub/bin/wiki2epub
ls -1 files/ | wc -l | grep 439 || exit
bin/epubcheck docs/download/iliaden.epub && git commit -a -m updated && git push
echo "Finished: $(date)"

