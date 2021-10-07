pipeline { 
    agent any;
    parameters {
        string(name: 'BRANCH', defaultValue: 'main')
        credentials(name: 'GIT_CRED', defaultValue: 'GIT_CREDENTIALS', credentialType: 'Username with password', description: 'credentials')
        string(name: 'PROJECT_GIT_URL', defaultValue: 'https://github.com/emanuelemellace/BackupJenkins.git')
	
    }
    stages {
	    
	stage('Checkout') {
            steps {
                cleanWs(deleteDirs: true, disableDeferredWipeout: true)
                checkout([$class: 'GitSCM',
                branches: [[name: params.BRANCH]],
                gitTool: 'git',
                userRemoteConfigs: [[
                    credentialsId: params.GIT_CRED,
                    url: params.PROJECT_GIT_URL
                ]]])
            }
        }
        stage("Move on folder and Commit"){ 
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
                """
            }
        } 
        stage("Push"){
		steps{
			withCredentials([usernamePassword(credentialsId:git_mellace, usernameVariable:'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
                		sh "git remote set-url origin https://${GIT_USERNAME}:${GIT_PASSWORD}@${params.PROJECT_GIT_URL}"
                		sh 'git push origin'
				echo "backup done"
			}
		}
	} 
    }
}
