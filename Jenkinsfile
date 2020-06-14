#!groovy

@Library('github.com/ayudadigital/jenkins-pipeline-library@v5.0.0') _

// Initialize global config
cfg = jplConfig('huelladigital', 'java', '', [email: env.CI_NOTIFY_EMAIL_TARGETS])

// Disable commit message validation
cfg.commitValidation.enabled = false

/**
 * Build and publish docker images
 *
 * @param nextReleaseNumber String Release number to be used as tag
 */
def buildAndPublishDockerImages(String nextReleaseNumber='') {
    if (nextReleaseNumber == '') {
        nextReleaseNumber = sh (script: 'kd get-next-release-number .', returnStdout: true).trim().substring(1)
    }
    // Backend
    docker.withRegistry('', 'docker-token') {
        def customImage = docker.build("${env.DOCKER_ORGANIZATION}/${cfg.projectName}-backend:${nextReleaseNumber}", '--pull --no-cache backend')
        customImage.push()
        if (nextReleaseNumber != 'beta') {
            customImage.push('latest')
        }
    }
    // Frontend
    docker.withRegistry('', 'docker-token') {
        def customImage = docker.build("${env.DOCKER_ORGANIZATION}/${cfg.projectName}-frontend:${nextReleaseNumber}", "--pull --no-cache --build-arg BUILD_ID=$BUILD_ID frontend")
        sh "docker image prune --filter label=stage=builder --filter label=build=$BUILD_ID"
        customImage.push()
        if (nextReleaseNumber != 'beta') {
            customImage.push('latest')
        }
    }
}

pipeline {
    agent none

    stages {
        stage ('Initialize') {
            agent { label 'docker' }
            steps  {
                jplStart(cfg)
                sh 'rm -rf backend/target'
            }
        }
        // Backend
        stage('Backend: uild') {
            agent {
                docker {
                    image 'maven:3.6.3-jdk-11'
                    label 'docker'
                }
            }
            steps {
                sh 'bin/devcontrol.sh backend build'
            }
        }
        stage('Backend: Unit tests') {
            agent {
                docker {
                    image 'maven:3.6.3-jdk-11'
                    label 'docker'
                }
            }
            steps {
                sh 'bin/devcontrol.sh backend unit-tests'
            }
        }
        stage('Backend: Integration tests') {
            agent { label 'docker'}
            steps {
                script {
                    docker.image('docker:dind').withRun('--privileged -v "$WORKSPACE":"$WORKSPACE" --workdir "$WORKSPACE"') { c ->
                        sh """
                        sleep 5
                        docker exec ${c.id} apk add openjdk11-jdk maven bash
                        docker exec ${c.id} chmod 777 /var/run/docker.sock
                        docker exec -u \$(id -u):\$(id -g) ${c.id} bin/devcontrol.sh backend integration-tests
                        """
                    }
                }
            }
        }
        stage('Backend: Acceptance Tests') {
            agent {
                docker {
                    image 'maven:3.6.3-jdk-11'
                    label 'docker'
                }
            }
            steps {
                sh 'bin/devcontrol.sh backend acceptance-tests'
            }
        }
        stage('Backend: Sonar') {
            agent {
                docker {
                    image 'maven:3.6.3-jdk-11'
                    label 'docker'
                }
            }
            steps {
                withCredentials([string(credentialsId: 'sonarcloud_login', variable: 'sonarcloud_login')]) {
                    sh 'bin/devcontrol.sh backend sonar'
                }
            }
        }
        stage('Backend: Package JAR') {
            agent {
                docker {
                    image 'maven:3.6.3-jdk-11'
                    label 'docker'
                }
            }
            steps {
                sh 'bin/devcontrol.sh backend package'
            }
        }
        // Frontend
        stage ('Frontend: install') {
            agent { label "docker" }
            stepd {
                sh "bin/devcontrol.sh frontend install"
            }
        }
        stage ('Frontend: test') {
            agent { label "docker" }
            stepd {
                sh "bin/devcontrol.sh frontend test"
            }
        }
        stage ('Frontend: build') {
            agent { label "docker" }
            stepd {
                sh "bin/devcontrol.sh frontend build"
            }
        }
        // Publish backend + frontend
        stage("Docker Publish") {
            agent { label 'docker' }
            when { branch 'develop' }
            steps {
                buildAndPublishDockerImages('beta')
            }
        }
        stage("Remote deploy") {
            agent { label 'docker' }
            when { branch 'develop' }
            steps {
                sshagent (credentials: ['jpl-ssh-credentials']) {
                    sh "bin/deploy.sh dev"
                }
            }
        }
        // Close release
        stage ('Make release') {
            agent { label 'docker' }
            when { branch 'release/new' }
            steps {
                buildAndPublishDockerImages()
                jplMakeRelease(cfg, true)
            }
        }
    }

    post {
        always {
            jplPostBuild(cfg)
        }
    }

    options {
        timestamps()
        ansiColor('xterm')
        buildDiscarder(logRotator(artifactNumToKeepStr: '20',artifactDaysToKeepStr: '30'))
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
    }
}
