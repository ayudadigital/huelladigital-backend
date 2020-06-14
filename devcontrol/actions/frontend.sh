#!/bin/bash

set -eu

# Initialize
rootdir="$(pwd)"

# @description Set of actions for the frontend
#
# @example
#   frontend build
#   frontend build-docker-images
#
# @arg $1 Task: "brief", "help" or "exec"
#
# @exitcode 0 operation sucesful
#
# @stdout "Not implemented" message if the requested task is not implemented
#
function backend() {

    # Init
    local briefMessage
    local helpMessage

    briefMessage="Set of actions for the frontend: [install], [test], [build] or [build-docker-image]"
    helpMessage=$(cat <<EOF
Set of actions for the frontend

Usage:

$ devcontrol frontend install                           # Execute "npm install" within docker
[...]

$ devcontrol frontend test                              # Execute "npm run test" within docker
[...]

$ devcontrol frontend build                             # Execute "npm run build"
[...]

$ devcontrol frontend build-docker-image [docker_tag]   # Build docker image, or "beta" if you don't specify it
[...]

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
                echo >&2 "ERROR - You should specify the action type:"
                echo >&2
                showHelpMessage "${FUNCNAME[0]}" "$helpMessage"
                exit 1
            fi
            cd "${rootdir}/frontend"
            frontendActions=${param[1]}
            case ${frontendActions} in
                "install")
                    docker run -e HOME=. -e npm_config_cache=npm_cache -ti --rm -v $(pwd):$(pwd):delegated -u $(id -u):$(id -g) -w $(pwd) node:12.18.0-alpine3.9 \
                        npm install
                    ;;
                "test")
                    docker run -e CI=true -e HOME=. -e npm_config_cache=npm_cache -ti --rm -v $(pwd):$(pwd):delegated -u $(id -u):$(id -g) -w $(pwd) node:12.18.0-alpine3.9 \
                        npm run test
                    ;;
                "build")
                    docker run -e HOME=. -e npm_config_cache=npm_cache -ti --rm -v $(pwd):$(pwd):delegated -u $(id -u):$(id -g) -w $(pwd) node:12.18.0-alpine3.9 \
                        npm run build
                    ;;
                "build-docker-image")
                    if [ ${#param[@]} -lt 3 ]; then
                        dockerTag=beta
                    else
                        dockerTag=${param[2]}
                    fi
                    echo "# Build frontend docker image with tag '${dockerTag}'"
                    echo "## Create node_modules cache"
                    devcontrol frontend install
                    echo "## Build docker image"
                    docker build -t ayudadigital/huelladigital-frontend:${dockerTag} .
                    echo "## Remove build cache docker image"
                    docker image prune -f --filter label=stage=builder --filter label=BUILD_ID=local
                    ;;
                *)
                    echo "ERROR - Unknown action [${frontendActions}], use [install], [test], [build] or [build-docker-image]"
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
backend "$*"
