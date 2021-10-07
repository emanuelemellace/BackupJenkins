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
		sh """#!/bin/bash
                cd $JENKINS_HOME/backup/
		ls
                """
                script {
		    LATEST_BACKUP_FOLDER = sh (
			    script: 'ls -t | head -n1',
		    	    returnStdout: true
		    ).trim()
                }
                sh """#!/bin/bash
		echo ${LATEST_BACKUP_FOLDER}
                cd ${LATEST_BACKUP_FOLDER}
		ls
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
