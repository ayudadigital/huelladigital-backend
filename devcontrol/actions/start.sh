#!/bin/bash

set -eu

# @description Start docker platform
#
# @example
#   hello-world
#
# @arg $1 Task: "brief", "help" or "exec"
#
# @exitcode 0
#
# @stdout "Not implemented" message if the requested task is not implemented
#
function start() {

    # Init
    local briefMessage="Start Platform"
    local helpMessage="Start front and back of the 'hotel covid' docker platform"

    # Task choosing
    case $1 in
        brief)
            showBriefMessage "${FUNCNAME[0]}" "$briefMessage"
            ;;
        help)
            showHelpMessage "${FUNCNAME[0]}" "$helpMessage"
            ;;
        exec)
            cd backend
            docker-compose -f src/main/docker/docker-compose.yml up -d
            echo ""
            ;;
        *)
            showNotImplemtedMessage "$1" "${FUNCNAME[0]}"
            return 1
    esac
}

# Main
start "$@"
