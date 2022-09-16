FROM debian:bookworm

# Install some needed packages
RUN DEBIAN_FRONTEND=noninteractive \
    apt-get update && \
    apt-get install curl rlwrap apt-utils \
        ca-certificates-java java-common \
        emscripten \
        -yq --no-install-recommends && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Install graalvm-ce Debian package
RUN curl -fsSLO https://github.com/dongjinleekr/graalvm-ce-deb/releases/download/22.2.0-0/graalvm-ce-java17_amd64_22.2.0-0.deb && \
    dpkg -i graalvm-ce-java17_amd64_22.2.0-0.deb && \
    rm -rf graalvm-ce-java17_amd64_22.2.0-0.deb

ARG CLJ_VER=1.11.1.1155

RUN curl -fsSLO https://download.clojure.org/install/linux-install-${CLJ_VER}.sh && \
    chmod 755 linux-install-${CLJ_VER}.sh && \
    ./linux-install-${CLJ_VER}.sh && \
    rm -f linux-install-${CLJ_VER}.sh

# Install graalvm wasm tool
RUN gu install wasm

COPY . /work

WORKDIR /work

# Compile C code to wasm, exporting some functions
RUN emcc -s STANDALONE_WASM -g2 -s 'EXPORTED_FUNCTIONS=["_hello","_add"]' \
    -o c2wasm.wasm c2wasm.c

# Run wasm
RUN wasm --Builtins=wasi_snapshot_preview1 c2wasm.wasm

RUN java -version && \
    emcc --version

# Run via clojure tools
RUN clj -J-Dpolyglot.wasm.Builtins=wasi_snapshot_preview1 \
    -J-Dclojure.main.report=stderr \
    -X graal-wasm.c2wasm/add :a 1 :b 5

# Say hello from wasm via clojure
RUN clj -J-Dpolyglot.wasm.Builtins=wasi_snapshot_preview1 \
    -J-Dclojure.main.report=stderr \
    -X graal-wasm.c2wasm/hello

