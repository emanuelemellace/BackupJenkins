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
		cp -r $JENKINS_HOME/backup/${LATEST_BACKUP_FOLDER}/* .
                """
            }
        } 
        stage("Push"){
		steps{
			withCredentials([usernamePassword(credentialsId:GIT_CRED, usernameVariable:'GIT_USERNAME', passwordVariable: 'GIT_TOKEN')]) {
			    sh("git config user.email emanuele.mellace97@gmail.com")
                	    sh("git config user.name $GIT_USERNAME")
			    sh("git add .")
			    sh('git commit -m "Backup ${DATETIME_TAG}"')
			    sh("git push http://$GIT_USERNAME:$GIT_TOKEN@github.com/emanuelemellace/BackupJenkins.git HEAD:main")
			     echo "backup done"
			}
		}
	} 
    }
	post {
		always {
			cleanWs(deleteDirs: true, disableDeferredWipeout: true)
			}
		success {
			echo '*********************** BACKUP DONE !**********************'
			}
		failure {
			echo '************************* FAILED ********************* '
			}
	}
}
