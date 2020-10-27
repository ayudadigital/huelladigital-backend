#!groovy

@Library('github.com/ayudadigital/jenkins-pipeline-library@v5.0.0') _

// Initialize global config
cfg = jplConfig('huelladigital-backend', 'java', '', [email: env.CI_NOTIFY_EMAIL_TARGETS])

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
        def customImage = docker.build("${env.DOCKER_ORGANIZATION}/huelladigital-backend:${nextReleaseNumber}", '--pull --no-cache backend')
        customImage.push()
    }
}

pipeline {
    agent none

    stages {
        stage ('Initialize') {
            agent { label 'docker' }
            steps {
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
            when { branch 'servus' }
            steps {
                sh 'bin/devcontrol.sh backend unit-tests'
            }
        }
        stage('Integration tests') {
            agent { label 'docker'}
            when { branch 'servus' }
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
        stage('Acceptance Tests') {
            agent {
                docker {
                    image 'maven:3.6.3-jdk-11'
                    label 'docker'
                }
            }
            when { branch 'servus' }
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
            when { branch 'servus' }
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
            steps {
                buildAndPublishDockerImages('beta-aws-ibai')
                configFileProvider([configFile(fileId: 'huellapositiva-backend-task-definition', variable: 'HUELLAPOSITIVA_BACKEND_ECS_TASK')]) {
                    script {
                        env.HUELLAPOSITIVA_BACKEND_ECS_TASK = sh(script:"cat ${HUELLAPOSITIVA_BACKEND_ECS_TASK}", returnStdout: true).trim()
                    }
                }
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
        stage("AWS deploy") {
            agent {
                docker {
                    image 'amazon/aws-cli'
                    args '--privileged --entrypoint=/bin/bash'
                    label 'docker'
                }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'aws-ibai', usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    sh "bin/deploy-aws-ibai.sh dev ${AWS_ACCESS_KEY_ID} ${AWS_SECRET_ACCESS_KEY} ${env.HUELLAPOSITIVA_BACKEND_ECS_TASK}"
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
