#!/bin/bash
set -e

PRISM_DIR="/usr/local/prism-src"
PRISM_VERSION_TAG="v4.7"


if [ -d "$PRISM_DIR" ]; then
    cd "$PRISM_DIR"; git pull;
else
    echo "Using sudo to install prism to $PRISM_DIR"
    sudo bash -c "mkdir '$PRISM_DIR' && chmod 777 '$PRISM_DIR'"
    git clone --depth 1 --branch "$PRISM_VERSION_TAG" https://github.com/prismmodelchecker/prism.git "$PRISM_DIR"
fi

cd "$PRISM_DIR/prism"
make
