pipeline { 
    agent any;
    parameters {
        string(name: 'BRANCH', defaultValue: 'main')
        credentials(name: 'GIT_CRED', defaultValue: 'GIT_CREDENTIALS', credentialType: 'Username with password', description: 'credentials')
        string(name: 'PROJECT_GIT_URL', defaultValue: '...')
	text(name: 'LATEST_BACKUP_FOLDER', defaultValue: 'One\nTwo\nThree\n', description: '')
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
		    LATEST_BACKUP_FOLDER = sh (
			    scripts: 'ls -t | head -n1',
		    	    returnStdout: true
		    ).trim()
                }
                sh """#!/bin/bash
                cd $JENKINS_HOME/backup/${LATEST_BACKUP_FOLDER}
		ls
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
