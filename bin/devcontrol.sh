#!/bin/bash
#
# @file devcontrol
# @brief Development Environment Control script
set -euo pipefail

# Global Variables
declare -a actionList

# @description Output a error message through the standard output
#
# @example
#   echo_err error
#
# @arg $@ The string to be displayed
#
# @exitcode 0
#
# @stdout The input parameter
#
function echo_err () {
    echo >&2 -e "$@"
}
export -f echo_err

# @description Output a debug message through the standard output
# The string is displayed only if the environment variable DC_DEBUG variable has the value "1"
#
# @example
#   echo_debug error
#
# @arg $@ The string to be displayed
#
# @exitcode 0
#
# @stdout The input parameter
#
function echo_debug () {
    if [ "${DC_DEBUG-}" == "1" ]; then
        echo_err ">>>> DEBUG >>>>> $(date "+%Y-%m-%d %H:%M:%S") devcontrol: $*"
    fi
}
export -f echo_debug

# @description devcontrol startup
# - Go to the root directory of the project and
# - Sets the "rootdir" variable to his absolute path
#
# @example
#   startup
#
# @noargs
#
# @exitcode 0
#
# @stdout none
#
function startup() {
    command="$0"
    echo_debug "Startup"
    cd "$(git rev-parse --show-toplevel)"
    rootdir="$(pwd)"
    export rootdir
    declare -a actionList
}

# @description Execute "delegeted" startup if exist in the 'devcontrol/global' directory
#
# @example
#   startup
#
# @noargs
#
# @exitcode 0
#
# @stdout "Warning: missing devcontrol/global/startup.sh"
#
function delegatedStartup() {
    echo_debug "Execute delegate startup with 'devcontrol/global/startup.sh' script"
    if [ -f "devcontrol/global/startup.sh" ]; then
        #bash "devcontrol/global/startup.sh"
        source "devcontrol/global/startup.sh"
    else
        echo "Warning: missing devcontrol/global/startup.sh"
    fi
}

# @description Load action list from the 'devcontrol/actions' directory
#
# @example
#   loadActionList
#
# @noargs
#
# @exitcode 0
#
# @stdout none
#
function loadActionList() {
    local scriptList
    echo_debug "Load action list from 'devcontrol/actions' directory"
    MAXWIDTH=0
    scriptList=$(find devcontrol/actions -maxdepth 1 -mindepth 0 -type f -name "*.sh"|sort)
    local i=0
    for script in $scriptList; do
        action=$(basename "${script/\.sh/}")
        actionList[$i]=$action
        if [ ${#action} -gt $MAXWIDTH ]; then
            MAXWIDTH=${#action}
        fi
        echo_debug "- Found action '${actionList[i]}'"
        i=$((i+1))
    done
    echo_debug "Located $i actions"
}

# @description Assert an action exists
#
# @example
#   showNotImplementedMessage stop operate
#
# @arg $1 Action name
#
# @exitcode 0  If the action exist, abort execution if other case and return 1 to the system
#
# @stdout Show "The actin <action> does not exist. Aborting" if the check fails
#
function assertActionExist() {
    local search="$1"
    for action in ${actionList[*]}; do
        [[ $(echo "$action"|cut -f 1 -d ':') == "$search" ]] && echo_debug "The requested action '$1' exist" && return 0
    done
    echo_err "The action '$search' does not exist. Aborting"
    echo_debug "end"
    exit 1
}

# @description Display a message when a task is not implemented in an action
# The function should be called from the actions
#
# @example
#   showNotImplementedMessage operate stop
#
# @arg $1 Action name
# @arg $2 Task name
#
# @exitcode 0
#
# @stdout "Task <task> not implemented in <action> action"
#
function showNotImplemtedMessage() {
    echo_err "Task '$2' not implemted in '$1' action"
}
export -f showNotImplemtedMessage

# @description Display brief message of an action
#
# @example
#   showBriefMessage operate "Operate the platform"
#
# @arg $1 Action name
# @arg $2 Brief message
#
# @exitcode 0
#
# @stdout "message"
#
function showBriefMessage() {
    echo "$2"
}
export -f showBriefMessage

# @description Display help message of an action
# The function should be called from the actions
#
# @example
#   showHelpMessage operate "Long message explaining how to operate the platform, can be multiline"
#
# @arg $1 Action name
# @arg $2 Help message
#
# @exitcode 0
#
# @stdout "message"
#
function showHelpMessage() {
    echo "Help for the '$1' action"
    echo
    echo "$2"
}
export -f showHelpMessage

# @description Show the usage of the platform
# Loop through all actions and get the "brief", calling the functions with the 'brief' task
#
# @example
#   showUsage
#
# @exitcode 0
#
# @stdout The usage of the platform
#
function showUsage () {
    echo "Usage:"
    printf "    - $command %-${MAXWIDTH}s # This information page" "help"
    echo
    for action in ${actionList[*]}; do
        printf "    - $command %-${MAXWIDTH}s # " "$action"
        "devcontrol/actions/${action}.sh" "brief"
    done
    echo
    echo "Use '$command help <action>' to display his help (e.g. $command help ${actionList[0]})"
    echo
}

# Main
echo_debug "begin"
echo_debug "Devcontrol (c) 2020 Ayuda Digital"
startup
delegatedStartup
loadActionList

# Show usage when the script:
# - Was called withhout arguments
# - Was called with "help" action
SHOW_USAGE=0
if [ $# -eq 0 ]; then
    SHOW_USAGE=1
elif [ $# -eq 1 ] && [ "$1" = "help" ]; then
    SHOW_USAGE=1
fi
if [ ${SHOW_USAGE} -eq 1 ]; then
    showUsage
    exit 0
fi

# Determine action type "exec" or "help"
action=$1
shift
if [ "${action}" == "help" ]; then
    action=$1
    task="help"
else
    task="exec"
fi

# Get arguments
arguments=$*

# Execute the action if exist
assertActionExist "${action}"
"devcontrol/actions/${action}.sh" "${task}" "${arguments}"
echo_debug "end"
