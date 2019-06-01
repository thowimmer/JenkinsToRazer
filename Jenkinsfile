void publishBuildStatusToGitHub(String message, String state) {
  step([
      $class: "GitHubCommitStatusSetter",
      reposSource: [$class: "ManuallyEnteredRepositorySource", url: "${env.REPO_URL}"],
      contextSource: [$class: "ManuallyEnteredCommitContextSource", context: "ci/jenkins/build-status"],
      errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
      statusResultSource: [ $class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]] ]
  ]);
}

pipeline {
    agent any
    
    options {
        skipStagesAfterUnstable()
    }
    
    environment {
        REPO_URL = 'https://github.com/thowimmer/JenkinsToRazer'
    }
    
    stages {
        stage('Notify about build start'){
            steps {
                publishBuildStatusToGitHub("Build running", "PENDING")
            }
        }
		
		stage('Build'){
		    steps {
                sh 'source/JenkinsToRazer/gradlew build'
            }
		}
	}
	
	post {
		success {
		   publishBuildStatusToGitHub("Build succeeded", "SUCCESS")
		}
		
		failure {
		   publishBuildStatusToGitHub("Build failed", "FAILURE")
		}
	}
}