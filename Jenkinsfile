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
def buildAndPublishDockerImages(String nextReleaseNumber='') {
    if (nextReleaseNumber == '' {
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
    agent { label 'docker' }

    stages {
        stage ('Initialize') {
            steps  {
                jplStart(cfg)
                sh 'rm -rf backend/target'
            }
        }
        stage('Build') {
            agent {
                docker { image 'maven:3.6.3-jdk-11' }
            }
            steps {
                sh 'bin/devcontrol.sh backend build'
                stash name: 'target', includes: 'backend/target/*'
            }
        }
        stage('Unit tests') {
            agent {
                docker { image 'maven:3.6.3-jdk-11' }
            }
            steps {
                unstash 'target'
                sh 'bin/devcontrol.sh backend unit-tests'
                stash name: 'target', includes: 'backend/target/*'
            }
        }
        stage('Integration tests') {
            agent {
                docker { image 'maven:3.6.3-jdk-11' }
            }
            steps {
                unstash 'target'
                sh 'bin/devcontrol.sh backend integration-tests'
                stash name: 'target', includes: 'backend/target/*'
            }
        }
        stage('Acceptance Tests') {
            agent {
                docker { image 'maven:3.6.3-jdk-11' }
            }
            steps {
                unstash 'target'
                sh 'bin/devcontrol.sh backend acceptance-tests'
                stash name: 'target', includes: 'backend/target/*'
            }
        }
        stage('Sonar') {
            agent {
                docker { image 'maven:3.6.3-jdk-11' }
            }
            steps {
                withCredentials([string(credentialsId: 'sonarcloud_login', variable: 'sonarcloud_login')]) {
                    unstash 'target'
                    sh 'bin/devcontrol.sh backend sonar'
                    stash name: 'target', includes: 'backend/target/*'
                }
            }
        }
        stage('Package JAR') {
            agent {
                docker { image 'maven:3.6.3-jdk-11' }
            }
            steps {
                unstash 'target'
                sh 'bin/devcontrol.sh backend package'
                stash name: 'packagefile', includes: 'backend/target/*.jar'
            }
        }
        stage("Docker Publish") {
            when { branch "develop" }
            steps {
                unstash 'packagefile'
                buildAndPublishDockerImages('beta')
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
    }

    options {
        timestamps()
        ansiColor('xterm')
        buildDiscarder(logRotator(artifactNumToKeepStr: '20',artifactDaysToKeepStr: '30'))
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
    }
}
