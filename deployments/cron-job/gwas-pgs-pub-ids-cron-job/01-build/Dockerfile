ARG TARGET_PLATFORM
FROM --platform=${TARGET_PLATFORM} redis:bullseye
WORKDIR /data
RUN mkdir -p pgs-ids-reference-date

# Combine update and installation of curl and jq in a single RUN statement
RUN apt-get update && apt-get install -y \
    curl \
    jq \
    python3 \
    python3-pip && \
    pip3 install csvkit && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY run.sh .
RUN ["chmod", "+x", "run.sh"]
ENTRYPOINT ["/bin/bash", "-c" , "/data/run.sh"]
