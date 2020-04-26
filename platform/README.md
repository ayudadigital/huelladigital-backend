# Huella Digital - Docker Generic Platform

Based on the [Docker Generic Platform](https://github.com/ayudadigital/docker-generic-platform)

## Server host configuration

We will use Ubuntu 18.04 as base system

- [Install docker](https://docs.docker.com/engine/install/ubuntu/)
- [Install docker-compose](https://docs.docker.com/compose/install/)
- Prepare system firewall opening ports

    ```shell
    $ sudo ufw default deny incoming
    $ sudo ufw default allow outgoing
    $ sudo ufw allow 22
    $ sudo ufw allow 80
    $ sudo ufw allow 443
    $ sudo ufw allow 8080
    $ sudo ufw enable
    ```

[...]
