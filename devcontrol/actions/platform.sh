#!/bin/bash

set -e

# Initialize
cd "$(dirname "$0")/../../platform"
rootdir=$(pwd)
source config.ini

# @description Operate [start or stop] one platform service or product
#
# @example
#   operateItem service jenkins start
#   operateItem product webapp stop
#
# @arg $1 Type of item: "platform" or "service"
# @arg $2 Name of the item, like "jenkins" or "webapp"
# @art $3 Action to perform with the item: start, stop, destroy
#
# @exitcode 0 operation successsful
#
function operateItem() {
    type=$1
    item=$2
    itemAction=$3
    case ${itemAction} in
        start)
            dockerAction="up -d"
            actionTextPre="Starting"
            actionTextPost="Started"
            ;;
        stop)
            dockerAction="stop"
            actionTextPre="Stopping"
            actionTextPost="Stopped"
            ;;
        destroy)
            dockerAction="down"
            actionTextPre="Stopping"
            actionTextPost="Stopped"
            ;;
        *)
            echo "Unknown item action ${itemAction}"
            exit 1
            ;;
    esac
    cd "${rootdir}/${type}s/${item}" || exit 1
    echo "## ${actionTextPre} platform ${type} '${item}'"
    eval "docker-compose ${dockerAction}"
    echo "## ${actionTextPost} platform ${type} '${item}'"
    echo
}

# @description Operate [start, stop, destroy] all services and products
#
# @example
#   platform start
#   platform stop 
#
# @arg $1 Task: "brief", "help" or "exec"
#
# @exitcode 0 operation sucesful
#
# @stdout "Not implemented" message if the requested task is not implemented
#
function platform() {

    # Init
    local briefMessage
    local helpMessage

    briefMessage="Operate [start, stop or destroy] all platform services and products"
    helpMessage=$(cat <<EOF
Start, stop or destroy all platform services and products from the [config.ini] file

Usage:

$ devcontrol start # Will start all

[...]

$ devcontrol stop # Will stop all

[...]

$ devcontrol destroy # Will remove all related platform items: containers, networks and volumes

[...]

The action will read the [config.ini] file and then it will start or stop:

- All platform services from the "services" config key
- All platform products from the "products" config key

In both cases each service or product will be started or stopped sequentially in the same order that it appears in the corresponding config key

Platform services list: ${services[*]}
Platform products list: ${products[*]}
EOF
)
    # Task choosing
    read -r -a param <<< "$1"
    action=${param[0]}

    case ${action} in
        brief)
            showBriefMessage "${FUNCNAME[0]}" "$briefMessage"
            ;;
        help)
            showHelpMessage "${FUNCNAME[0]}" "$helpMessage"
            ;;
        exec)
            if [ ${#param[@]} -lt 2 ]; then
                echo "ERROR - You should specify the action type: [start] or [stop]"
                echo
                showHelpMessage "${FUNCNAME[0]}" "$helpMessage"
                exit 1
            fi
            platformAction=${param[1]}
            case ${platformAction} in
                cleanup)
                    # Jenkins workspace & docker data
                    echo "# Jenkins Cleanup"
                    cd "${rootdir}/services/jenkins"
                    docker-compose exec -T jenkins docker system prune --all --volumes -f || true
                    docker-compose exec -T jenkins rm -rf /var/jenkins_home/workspace/*
                    ;;
                start|stop|destroy)
                    echo "# Doing [${platformAction}] over the services: ${services[*]}"
                    echo
                    for service in ${services[*]}; do
                        operateItem service "${service}" "${platformAction}"
                    done
                    echo "# Doing [${platformAction}] over the products: ${products[*]}"
                    echo
                    for product in ${products[*]}; do
                        operateItem product "${product}" "${platformAction}"
                    done
                    ;;
                *)
                    echo "ERROR - Unknown action [${platformAction}], use [start] or [stop]"
                    echo
                    showHelpMessage "${FUNCNAME[0]}" "$helpMessage"
                    exit 1
            esac
            ;;
        *)
            showNotImplemtedMessage "$1" "${FUNCNAME[0]}"
            return 1
    esac
}

# Main
platform "$*"
