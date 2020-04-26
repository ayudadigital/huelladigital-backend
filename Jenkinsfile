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
    docker.withRegistry('', 'docker-token') {
        def customImage = docker.build("${env.DOCKER_ORGANIZATION}/${cfg.projectName}:${nextReleaseNumber}", '--pull --no-cache backend')
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
        stage('Build') {
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
        stage('Unit tests') {
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
        stage('Integration tests') {
            agent {
                docker {
                    image 'maven:3.6.3-jdk-11'
                    label 'docker'
                }
            }
            steps {
                sh 'bin/devcontrol.sh backend integration-tests'
            }
        }
        stage('Acceptance Tests') {
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
        stage('Sonar') {
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
        stage('Package JAR') {
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
        stage("Docker Publish") {
            agent { label 'docker' }
            when {
                anyOf {
                    branch "develop"
                    branch "feature/platform"
                }
            }
            steps {
                buildAndPublishDockerImages('beta')
            }
        }
        stage("Remote deploy") {
            agent { label 'docker' }
            when {
                anyOf {
                    branch "develop"
                    branch "feature/platform"
                }
            }
            steps {
                sshagent (credentials: [jpl-ssh-credentials]) {
                    sh "bin/deploy.sh dev"
                }
            }
        }
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
