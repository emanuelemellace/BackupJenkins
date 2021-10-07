pipeline { 
    agent any;
    parameters {
        string(name: 'BRANCH', defaultValue: 'main')
        credentials(name: 'GIT_CRED', defaultValue: 'GIT_CREDENTIALS', credentialType: 'Username with password', description: 'credentials')
        string(name: 'PROJECT_GIT_URL', defaultValue: 'https://github.com/emanuelemellace/BackupJenkins.git')
	
    }
    stages {
        stage('Checkout') {
            steps{
                checkout([
                $class: 'GitSCM',
                branches: [[name: params.BRANCH]],
                doGenerateSubmoduleConfigurations: false,
                extensions: [[
                $class: 'RelativeTargetDirectory'
		]],
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
		    DATETIME_TAG = java.time.LocalDateTime.now()
		    LATEST_BACKUP_FOLDER = sh (
			    script: 'cd $JENKINS_HOME/backup/ && ls -t | head -n1',
		    	    returnStdout: true
		    ).trim()
                }
                sh """#!/bin/bash
		echo ${LATEST_BACKUP_FOLDER}
                cd $JENKINS_HOME/backup/${LATEST_BACKUP_FOLDER}
		
		git add --all
                git commit -m "Backup ${DATETIME_TAG}"
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
