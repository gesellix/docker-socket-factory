docker-socket-factory
=====================

* Adds configurable TLS support
* Adds unix socket support to the Apache HttpClient by providing a SchemeSocketFactory.

This library should help the [docker-client](https://github.com/gesellix-docker/docker-client) to connect to your Docker daemon.
To accomplish this, the underlying HttpClient is configured with a matching SocketFactory which is selected dependent on your DOCKER_HOST setting.

## TLS support configuration

Configuration is possible by setting:

1. system property `docker.cert.path`
2. environment variable `DOCKER_CERT_PATH` (default like documented in the [Docker documentation](https://docs.docker.com/articles/https/))

The variables are evaluated in the above order, so `docker.cert.path` wins and `DOCKER_CERT_PATH` is fallback.
