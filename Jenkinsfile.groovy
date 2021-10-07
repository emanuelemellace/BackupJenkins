pipeline { 
    agent any;
    parameters {
        string(name: 'BRANCH', defaultValue: 'master')
        credentials(name: 'GIT_CRED', defaultValue: 'GIT_CREDENTIALS', credentialType: 'Username with password', description: 'bitbucket.nexicloud.it credentials')
        string(name: 'PROJECT_GIT_URL', defaultValue: '...')
    }
    stages {
        stage('Checkout') {
            steps{
                checkout([
                $class: 'GitSCM',
                branches: [[name: params.BRANCH]],
                doGenerateSubmoduleConfigurations: false,
                extensions: [[
                $class: 'RelativeTargetDirectory',
                relativeTargetDir: 'jenkins-backup']],
                submoduleCfg: [],
                userRemoteConfigs: [
                    [ credentialsId: params.GIT_CRED,
                    url: params.PROJECT_GIT_URL]
                ]            
                ]);
 
            }
        }
        stage("BACKUP"){ 
            steps{
                script {
                    DATE_TAG = java.time.LocalDate.now()
                    DATETIME_TAG = java.time.LocalDateTime.now()
                }
                sh """#!/bin/bash
                cd $JENKINS_HOME
				pwd
                "
                """
            }
        } 
        stage("PUSH"){ 
            steps{
                sh 'git push'
                echo "backup pushed successfully" 
            }
        } 
    }
}