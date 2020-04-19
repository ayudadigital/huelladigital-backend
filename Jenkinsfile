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
    echo "ToDo"
    /*
    if (nextReleaseNumber == "") {
        nextReleaseNumber = sh (script: "kd get-next-release-number .", returnStdout: true).trim().substring(1)
    }
    dir ('backend') {
        sh """
        cat .env_test.dist |grep -v "^tag" > .env
        export tag_app="${nextReleaseNumber}"
        docker-compose build
        """
    }
    docker.withRegistry("", 'hotelcovid-docker-credentials') {
        docker.image("ticparabien/hospitalizados-hoteles-app:${nextReleaseNumber}").push()
        docker.image("ticparabien/hospitalizados-hoteles-desktop:${nextReleaseNumber}").push()
    }
    */
}

pipeline {
    agent { label 'docker' }

    stages {
        stage ('Initialize') {
            steps  {
                jplStart(cfg)
                sh "cat backend/.env_test.dist > backend/.env"
                sh "devcontrol backend prepare"
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
    //    stage('Sonar') {
    //       withSonarQubeEnv(credentialsId: 'sonar-token', installationName: 'sonar-tpb') {
    //            sh 'mvn sonar:sonar'
    //        }
    //    }
    //    stage('Snyk dependencies') {
    //      snykSecurity failOnIssues: false, organisation: 'ibai.eus', projectName: 'hospitalizacion-hoteles', snykInstallation: 'snyk-latest', snykTokenId: 'snyk-tpb'
    //    }
        stage('Package JAR') {
            steps {
                sh "devcontrol backend package"
            }
        }
        stage("Docker Publish") {
            when { branch "develop" }
            steps {
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
