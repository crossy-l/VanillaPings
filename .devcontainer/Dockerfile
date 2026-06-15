# Dev container for VanillaPings' multi-version (Mojmap) build.
# Minecraft's Java baseline differs per version, so the build needs three JDKs:
#   17  -> 1.18 .. 1.20.4
#   21  -> 1.20.5 .. 1.21.11
#   25  -> 26.x
# Gradle auto-detects JDKs under /usr/lib/jvm; the daemon runs on 25 (>= every target).
FROM mcr.microsoft.com/devcontainers/base:ubuntu-24.04

RUN install -d /usr/share/keyrings \
 && wget -qO- https://packages.adoptium.net/artifactory/api/gpg/key/public \
      | gpg --dearmor -o /usr/share/keyrings/adoptium.gpg \
 && echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(. /etc/os-release && echo $VERSION_CODENAME) main" \
      > /etc/apt/sources.list.d/adoptium.list \
 && apt-get update \
 && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
      temurin-17-jdk temurin-21-jdk temurin-25-jdk \
 && rm -rf /var/lib/apt/lists/*

# Arch-agnostic symlinks (Adoptium installs to temurin-<v>-jdk-amd64 / -arm64).
RUN set -eux; \
    for v in 17 21 25; do ln -sfn "$(ls -d /usr/lib/jvm/temurin-${v}-jdk-* | head -1)" "/opt/jdk${v}"; done

ENV JAVA_HOME=/opt/jdk25
ENV PATH="${JAVA_HOME}/bin:${PATH}"
