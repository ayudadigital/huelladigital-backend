#!/bin/bash

env=$1

ssh -t -t -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no master@${env}.huelladigital.ayudadigital.org << EOF
set -eu
set -o pipefail
cd platform/huelladigital
git --no-pager diff
git stash save "Deploy $(date '+%Y-%M-%d %H:%M:%S')"
git checkout .
git fetch -pv
git pull
cd platform/products/backend
exit
EOF
