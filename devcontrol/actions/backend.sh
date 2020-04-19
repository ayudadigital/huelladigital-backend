#!/bin/bash

set -eu

# Initialize
rootdir="$(pwd)"

# @description Execute one command using the maven docker image
#
# @example
#   executeWithDockerMaven "clean compile"
#
# @arg $1 Command to execute
#
# @exitcode 0 operation successsful
#
function executeWithDockerMaven() {
    if [ "$1" == "" ]; then
        echo >&2 "[ERROR] executeWithDockerMaven: Empty argument"
    fi
    cd "${rootdir}/backend"
    docker run -i --rm --workdir="$(pwd)" -v "$(pwd)":"$(pwd)":consistent -u "$(id -u):$(id -g)" maven:3.6.3-jdk-11 $1
}

# @description Set of actions for the backend
#
# @example
#   backend build 
#   backend unit-tests
#   backend integration-tests
#   backend acceptance-test
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
                "build")                executeWithDockerMaven "mvn compile" ;;
                "unit-tests")           executeWithDockerMaven "mvn test" ;;
                "integration-tests")    executeWithDockerMaven "mvn verify -P integration-test -Dtest=BlakenTest -DfailIfNoTests=false" ;;
                "acceptance-tests")     executeWithDockerMaven "mvn verify -P acceptance-test -Dtest=BlakenTest -DfailIfNoTests=false" ;;
                "sonar")
                    executeWithDockerMaven "mvn sonar:sonar -Dsonar.login=${sonarcloud_login}"
                    ;;
                "package")              executeWithDockerMaven "mvn package spring-boot:repackage -DskipTests" ;;
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
