#!/bin/bash

SSH_USER=$1
SSH_HOST=$2
SSH_KEY=$3

echo """
set -eu
set -o pipefail
cd huelladigital-platform
docker-compose pull
docker-compose up -d backend
exit 0
""" | ssh -t -t -i ${SSH_KEY} -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ${SSH_USER}@${SSH_HOST} bash -
