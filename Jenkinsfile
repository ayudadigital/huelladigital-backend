#!groovy

@Library('github.com/tpbtools/jenkins-pipeline-library@v4.0.0') _

// Initialize global config
cfg = jplConfig('huellapositiva', 'java', '', [email: env.CI_NOTIFY_EMAIL_TARGETS])

// Disable commit message validation
cfg.commitValidation.enabled = false

/**
 * Build and publish docker images
 *
 * @param nextReleaseNumber String Release number to be used as tag
 */
def buildAndPublishDockerImages(String nextReleaseNumber="") {
    if (nextReleaseNumber == "") {
        nextReleaseNumber = sh (script: "kd get-next-release-number .", returnStdout: true).trim().substring(1)
    }
    def customImage = docker.build("${env.DOCKER_ORGANIZATION}/${cfg.projectName}:${nextReleaseNumber}", "--pull --no-cache backend")
    customImage.push()
    if (nextReleaseNumber != "beta") {
        customImage.push('latest')
    }
}

pipeline {
    agent { label 'docker' }

    stages {
        stage ('Initialize') {
            steps  {
                jplStart(cfg)
                deletedir("backend/target")
            }
        }
        stage('Build') {
            steps {
                sh "devcontrol backend build"
            }
        }
        stage('Unit tests') {
            steps {
                sh "devcontrol backend unit-tests"
            }
        }
        stage('Integration tests') {
            steps {
                sh "devcontrol backend integration-tests"
            }
        }
        stage('Acceptance Tests') {
            steps {
                sh "devcontrol backend acceptance-tests"
            }
        }
        stage('Sonar') {
            steps {
                withCredentials([string(credentialsId: 'sonarcloud_login', variable: 'sonarcloud_login')]) {
                    sh "devcontrol backend sonar"
                }
            }
        }
        stage("Docker Publish") {
            when { branch "develop" }
            steps {
                sh "devcontrol backend package"
                buildAndPublishDockerImages("beta")
            }
        }
        stage ('Make release') {
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
        cleanup {
            deleteDir()
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
