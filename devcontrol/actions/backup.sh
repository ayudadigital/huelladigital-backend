#!/bin/bash

set -e

# Initialize
cd "$(dirname "$0")/../../platform"
rootdir=$(pwd)
source config.ini

# Backup docker volumes separatelly
function backupDockerVolumes() {
    cd "${volumes_path}" || exit 1
    for volume in ${volume_list}; do
        target=${backup_path}/volumes/latest_${volume}.tar.gz
        echo "Making backup of '${volumes_path}/${volume}' docker volume on '${target}'"
        mv "${backup_path}/volumes/latest_${volume}.tar.gz" "${backup_path}/volumes/previous_${volume}.tar.gz" > /dev/null || true
        tar -czf "${target}" "${volume}"
    done
}

# Backup platform containers (services and products) separatelly
function backupPlatformContainers() {
    cd "${services_path}" || exit 1
    for service in ${services[*]}; do
        target=${backup_path}/services/latest_${service}.tar.gz
        echo "Making backup of '${services_path}/${service}' docker service on '${target}'"
        mv "${backup_path}/services/latest_${service}.tar.gz" "${backup_path}/services/previous_${service}.tar.gz" 2> /dev/null || true
        tar -czf "${target}" "${service}"
    done
    cd "${products_path}" || exit 1
    for product in ${products[*]}; do
        target=${backup_path}/products/latest_${product}.tar.gz
        echo "Making backup of '${products_path}/${product}' docker product on '${target}'"
        mv "${backup_path}/products/latest_${product}.tar.gz" "${backup_path}/products/previous_${product}.tar.gz" 2> /dev/null || true
        tar -czf "${target}" "${product}"
    done
}

# @description Do backup of all docker containers (products and services) and all docker volumes
#
# @example
#   backup
#
# @exitcode 0 operation successsful
#
function backup() {

    # Init
    local briefMessage
    local helpMessage

    briefMessage="Platform backup"
    helpMessage=$(cat <<EOF
Do backup of all docker containers (products and services) and all docker volumes

Usage:

$ devcontrol backup

[...]

Platform services list: ${services[*]}
Platform products list: ${products[*]}

The backup will be stored into three directories

- /var/backups/docker/volumes
- /var/backups/docker/services
- /var/backups/docker/products
EOF
)
    # Task choosing
    read -r -a param <<< "$1"
    action=${param[0]}

    case $action in
        brief)
            showBriefMessage "${FUNCNAME[0]}" "$briefMessage"
            ;;
        help)
            showHelpMessage "${FUNCNAME[0]}" "$helpMessage"
            ;;
        exec)
            # Initialize
            services_path="${rootdir}/services"
            products_path="${rootdir}/products"
            volumes_path="/var/lib/docker/volumes"
            volume_list=$(cd "${volumes_path}"; ls)
            backup_path="/var/backups/docker"
            mkdir -p "${backup_path}/volumes" "${backup_path}/services" "${backup_path}/products"
            echo "Start daily backup of $(date +%A) on $(date)"
            # Do daily backup
            devcontrol platform cleanup
            devcontrol platform stop
            backupDockerVolumes
            backupPlatformContainers
            devcontrol platform start
            echo "End daily backup of $(date +%A) on $(date)"
            echo "======================================"
            ;;
        *)
            showNotImplemtedMessage "$1" "${FUNCNAME[0]}"
            return 1
    esac
}

# Main
backup "$*"
