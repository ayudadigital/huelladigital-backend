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
#   backend build-docker-image
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

    briefMessage="Set of actions for the backend: [build], [unit-tests], [integration-tests], [acceptance-tests], [sonar], [package] or [build-docker-image]"
    helpMessage=$(cat <<EOF
Set of actions for the backend

Usage:

$ devcontrol backend build                              # Execute "mvn clean compile"
[...]

$ devcontrol backend unit-tests                         # Execute unit test suite
[...]

$ devcontrol backend integration-tests                  # Execute integration test suite
[...]

$ devcontrol backend acceptance-tests                   # Execute acceptance test suite
[...]

$ devcontrol backend sonar                              # Execute sonar analysis
[...]

$ devcontrol backend package                            # Make "jar" package
[...]

$ devcontrol backend build-docker-image [docker_tag]    # Build docker image, or "beta" if you don't specify it
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
            cd "${rootdir}/backend"
            backendActions=${param[1]}
            branchName=${param[2]:-develop}
            case ${backendActions} in
                "build")                mvn compile ;;
                "unit-tests")           mvn test ;;
                "integration-tests")    mvn verify -P integration-test -Dtest=BlakenTest -DfailIfNoTests=false ;;
                "acceptance-tests")     mvn verify -P acceptance-test -Dtest=BlakenTest -DfailIfNoTests=false ;;
                "sonar")
                    sonarcloud_login=${sonarcloud_login:-fake}
                    mvn sonar:sonar -Dsonar.login=${sonarcloud_login}  -Dsonar.branch.name=${branchName}
                    ;;
                "package")              mvn package spring-boot:repackage -DskipTests ;;
                "build-docker-image")
                    if [ ${#param[@]} -lt 3 ]; then
                        dockerTag=beta
                    else
                        dockerTag=${param[2]}
                    fi
                    echo "# Using docker tag '${dockerTag}'"
                    echo
                    echo "## Building jar package"
                    devcontrol backend package
                    echo "## Building backend docker image"
                    docker build -t ayudadigital/huelladigital-backend:${dockerTag} --pull --no-cache .
                    ;;
                *)
                    echo "ERROR - Unknown action [${backendActions}], use [build], [unit-tests], [integration-tests], [acceptance-tests], [sonar], [package] or [build-docker-image]"
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
