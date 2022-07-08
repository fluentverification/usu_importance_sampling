#!/bin/sh
set -e

PRISM_DIR="/usr/local/prism-src"
PRISM_VERSION_TAG="v4.7"


echo "Installing Prism $PRISM_VERSION_TAG"

if [ -d "$PRISM_DIR" ]; then
    cd "$PRISM_DIR"
    sudo git pull
else
    echo "Using sudo to make prism directory at $PRISM_DIR"
    sudo mkdir "$PRISM_DIR"
    sudo chmod 777 "$PRISM_DIR"
    git clone --depth 1 --branch "$PRISM_VERSION_TAG" https://github.com/prismmodelchecker/prism.git "$PRISM_DIR"
fi

cd "$PRISM_DIR/prism"
make

echo "Adding prism and xprism to /usr/local/bin/"
sudo ln -sf "$PRISM_DIR/prism/bin/prism" /usr/local/bin/prism
sudo ln -sf "$PRISM_DIR/prism/bin/xprism" /usr/local/bin/xprism
