#!/usr/bin/env bash
curl -s https://aerokube.com/cm/bash | bash \
    && ./cm selenoid start --vnc --tmpfs 256 --args '-limit 10' --browsers 'chrome' 'firefox' --last-versions 2 \
    && ./cm selenoid-ui start --port 666

docker pull selenoid/vnc_chrome:76.0
docker pull selenoid/vnc_firefox:68.0

#to start from proxy
#HTTP_PROXY=http://proxy.example.com:80/ ./cm selenoid start