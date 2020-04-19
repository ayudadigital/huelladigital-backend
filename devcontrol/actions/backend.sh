#!/bin/bash

set -eu

# Initialize
rootdir="$(pwd)"

# @description Set of actions for the backend
#
# @example
#   backend build 
#   backend unit-tests
#   backend integration-tests
#   backend acceptance-test
#   backend sonar
#   backend package
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

    briefMessage="Set of actions for the backend"
    helpMessage=$(cat <<EOF
Set of actions for the backend

Usage:

$ devcontrol backend build # Execute "mvn clean compile"

[...]

$ devcontrol backend unit-tests # Execute unit test suite

[...]

$ devcontrol backend integration-tests # Execute integration test suite

[...]

$ devcontrol backend acceptance-tests # Execute acceptance test suite

[...]

$ devcontrol backend sonar # Execute sonar analysis

[...]

$ devcontrol backend package # Make "jar" package

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
                echo >&2 "ERROR - You should specify the action type: [start] or [stop]"
                echo >&2 
                showHelpMessage "${FUNCNAME[0]}" "$helpMessage"
                exit 1
            fi
            cd "${rootdir}/backend"
            backendActions=${param[1]}
            case ${backendActions} in
                "build")                mvn compile ;;
                "unit-tests")           mvn test ;;
                "integration-tests")    mvn verify -P integration-test -Dtest=BlakenTest -DfailIfNoTests=false ;;
                "acceptance-tests")     mvn verify -P acceptance-test -Dtest=BlakenTest -DfailIfNoTests=false ;;
                "sonar")                mvn sonar:sonar -Dsonar.login=${sonarcloud_login} ;;
                "package")              mvn package spring-boot:repackage -DskipTests ;;
                *)
                    echo "ERROR - Unknown action [${backendActions}], use [start] or [stop]"
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
