#!/bin/bash

set -eu

# Initialize
cd "$(dirname "$0")/../../platform"
source config.ini

# @description Install item assets
#
# @example
#   assets-install
#
# @arg $1 Task: "brief", "help" or "exec"
#
# @exitcode The result of the assets installation
#
# @stdout "Not implemented" message if the requested task is not implemented
#
function assets-install() {

    # Init
    local briefMessage
    local helpMessage

    briefMessage="Install assets"
    helpMessage=$(cat <<EOF
Install all product and service assets:

* Create all platform pieces directories with all permissions (777)
* Create the network "platform_services"
* Create the network "platform_products"
EOF
)

    # Task choosing
    case $1 in
        brief)
            showBriefMessage "${FUNCNAME[0]}" "$briefMessage"
            ;;
        help)
            showHelpMessage "${FUNCNAME[0]}" "$helpMessage"
            ;;
        exec)
            # Create directories
            SAVE_UMASK=$(umask)
            umask 0000
            for directory in ${assets[*]}; do
                if [ ! -d "${directory}" ]; then
                    echo -n "- Creating '${directory}' directory..."
                    mkdir -p "${directory}"
                    echo "[OK]"
                else
                    echo "The '${directory}' directory already exists, skipping"
                fi
            done
            for file in ${distfiles[*]}; do
                if [ ! -f "${file%.*}" ]; then
                    echo -n "- Creating '${file%.*}' config file from the dist file..."
                    cp -pn "${file}" "${file%.*}"
                    echo "[OK]"
                else
                    echo "The '${file%.*}' file already exists, skipping"
                fi
            done
            umask "${SAVE_UMASK}"
            ;;
        *)
            showNotImplemtedMessage "$1" "${FUNCNAME[0]}"
            return 1
    esac
}

# Main
assets-install "$@"
