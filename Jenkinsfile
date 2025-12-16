#!groovy

pipeline {
	agent {
		node {
			label 'sw.os.linux && sw.tool.docker'
		}
	}

	options {
		ansiColor('xterm')
		buildDiscarder(logRotator(
            // number of builds to keep
            numToKeepStr: env.BRANCH_NAME ==~ /release\/.*|master/ ? '30' : '10',
            // number of builds to keep the artifacts from
            artifactNumToKeepStr: env.BRANCH_NAME ==~ /release\/.*|master/ ? '30' : '3',
            // number of days to keep builds
            daysToKeepStr: env.BRANCH_NAME ==~ /release\/.*|master/ ? '90' : '10',
            // number of days to keep artifacts
            artifactDaysToKeepStr: env.BRANCH_NAME ==~ /release\/.*|master/ ? '90' : '10'
        ))
		timeout(time: 1, unit: 'HOURS')
		timestamps()
		skipStagesAfterUnstable()
	}

	environment {
		APPLICATION_VERSION = getVersion()
	}

	parameters {
        booleanParam(name: 'PUBLISH', defaultValue: false, description: 'Publish build to docker.ergon.ch.')
    }

	stages {
		stage('BE-Compile') {
			steps {
			    sh './backend/buildstack/startDockerCompose.sh'
				sh './backend/buildstack/with-corretto-17.sh ./backend/pipeline-scripts/clean.sh'
				sh './backend/buildstack/with-corretto-17.sh ./backend/pipeline-scripts/compile-java.sh backend'
			}
			post {
			    always {
                    sh './backend/buildstack/stopDockerCompose.sh'
			    }
				failure {
					notifyFailure('BE-Compile')
					error 'BE-Compile failed'
				}
			}
		}
		stage('BE-Check') {
			steps {
			    sh './backend/buildstack/startDockerCompose.sh'
				sh './backend/buildstack/with-corretto-17.sh ./backend/pipeline-scripts/check.sh backend'
			}
			post {
			    always {
                    sh './backend/buildstack/stopDockerCompose.sh'
			    }
				failure {
					notifyFailure('BE-Check')
					error 'BE-Check failed'
				}
			}
		}
		stage('BE-Build') {
			steps {
			    sh './backend/buildstack/startDockerCompose.sh'
				sh './backend/buildstack/with-corretto-17.sh ./backend/pipeline-scripts/build.sh backend'
			}
			post {
			    always {
                    sh './backend/buildstack/stopDockerCompose.sh'
			    }
				failure {
					notifyFailure('BE-Build')
					error 'BE-Build failed'
				}
			}
		}
	    stage('BE-Code-Check') {
	        steps {
	            notifyJenkinsInProgress()
			    sh './backend/buildstack/startDockerCompose.sh'
	            sh './backend/buildstack/with-corretto-17.sh ./backend/pipeline-scripts/ktlint-check.sh'
	        }
	        post {
			    always {
                    sh './backend/buildstack/stopDockerCompose.sh'
			    }
	        }
	    }
		stage('BE-Test') {
			steps {
			    sh './backend/buildstack/startDockerCompose.sh'
				sh './backend/buildstack/with-corretto-17.sh ./backend/pipeline-scripts/test.sh backend'
			}
			post {
			    always {
                    sh './backend/buildstack/stopDockerCompose.sh'
                    recordIssues enabledForFailure: true, tool: junitParser(pattern: "**/build/test-results/**/*.xml")
                    junit '**/build/test-results/**/*.xml'
			    }
				failure {
					notifyFailure('BE-Test')
					error 'BE-Test failed'
				}
			}
		}
		stage('FE-Build') {
            steps {
                sh './frontend/buildstack/with-node-22.4.0.sh ./frontend/buildstack/build-frontend.sh frontend'
            }
            post {
                failure {
                    notifyFailure('FE-Build')
                    error 'FE-Build failed'
                }
            }
        }
        stage('FE-Code-Check') {
            steps {
                sh './frontend/buildstack/with-node-22.4.0.sh ./frontend/buildstack/eslint-check.sh'
            }
        }
		stage('BE-Docker'){
		    when {
                anyOf {
                    branch 'main'
                    expression { params.PUBLISH == true }
                }
            }
            steps{
                sh './backend/createImages.sh'
            }
            post {
                failure {
                    notifyFailure('BE-Docker')
                    error 'BE-Docker failed'
                }
                success {
                    notifyDockerPushBE()
                }
            }
        }
        stage('FE-Docker'){
            when {
                anyOf {
                    branch 'main'
                    expression { params.PUBLISH == true }
                }
            }
            steps{
                sh './frontend/createImages.sh'
            }
            post {
                failure {
                    notifyFailure('FE-Docker')
                    error 'FE-Docker failed'
                }
                success {
                    notifyDockerPushFE()
                }
            }
	    }
		stage('Backuper-Docker') {
			when {
				anyOf {
					branch 'main'
					expression { params.PUBLISH == true }
				}
			}
			steps {
				sh './backuper/createImages.sh'
			}
			post {
				failure {
					notifyFailure("Backuper-Docker")
				}
				success {
					notifyDockerPushBackuper()
				}
			}
		}
	}
	post{
	    success {
	        script {
	            archiveVersion()
	            notifySucceeded()
	            notifyJenkinsSucceeded()
	        }
	    }
	    failure {
            script {
                notifyJenkinsFailure()
            }
        }
        unstable {
            script {
                notifyJenkinsFailure()
            }
        }
        aborted {
            script {
                notifyJenkinsFailure()
            }
        }
	}
}

def getVersion() {
    sh ('chmod 777 ./print-version.sh')
	return sh(script: './print-version.sh', returnStdout: true).trim()
}

def archiveVersion() {
    sh 'echo ${APPLICATION_VERSION} > ./version.txt'
    archiveArtifacts 'version.txt'
}

def notifyFailure(stage) {
	GString messageText = "[Jenkins Build](${env.BUILD_URL}) #${env.BUILD_NUMBER} failed at ${stage}! :x:"
	mattermostSend (
			message: messageText,
			channel: '#marksystem-development', // channel name to post to starting with hashtag e.g. '#channel-name'. You can find the API channel name in the "View Info" window in the client at URL.
			endpoint: '', // optional, overrides global Jenkins setting, required if posting to private channels
			color: 'danger', // one of: 'good', 'warning', 'danger' or any hex color, e.g. '#439FE0'
	)
}

def notifySucceeded() {
	GString messageText = "[Jenkins Build](${env.BUILD_URL}) #${env.BUILD_NUMBER} was successfully! :white_check_mark:"
	mattermostSend (
			message: messageText,
			channel: '#marksystem-development', // channel name to post to starting with hashtag e.g. '#channel-name'. You can find the API channel name in the "View Info" window in the client at URL.
			endpoint: '', // optional, overrides global Jenkins setting, required if posting to private channels
			color: 'good', // one of: 'good', 'warning', 'danger' or any hex color, e.g. '#439FE0'
	)
}

def notifyDockerPushBE() {
	GString messageText = "**Backend** build for Version *${APPLICATION_VERSION}* was pushed on [docker.ergon.ch](https://docker.ergon.ch/manifests?name=marksystem%2Fbackend)."
	mattermostSend (
			message: messageText,
			channel: '#marksystem-development', // channel name to post to starting with hashtag e.g. '#channel-name'. You can find the API channel name in the "View Info" window in the client at URL.
			endpoint: '', // optional, overrides global Jenkins setting, required if posting to private channels
			color: '#2983cc', // one of: 'good', 'warning', 'danger' or any hex color, e.g. '#439FE0'
	)
}

def notifyDockerPushFE() {
	GString messageText = "**Frontend** build for Version *${APPLICATION_VERSION}* was pushed on [docker.ergon.ch](https://docker.ergon.ch/manifests?name=marksystem%2Ffrontend)."
	mattermostSend (
			message: messageText,
			channel: '#marksystem-development', // channel name to post to starting with hashtag e.g. '#channel-name'. You can find the API channel name in the "View Info" window in the client at URL.
			endpoint: '', // optional, overrides global Jenkins setting, required if posting to private channels
			color: '#2983cc', // one of: 'good', 'warning', 'danger' or any hex color, e.g. '#439FE0'
	)
}

def notifyDockerPushBackuper() {
	GString messageText = "**Backuper** build for Version *${APPLICATION_VERSION}* was pushed on [docker.ergon.ch](https://docker.ergon.ch/manifests?name=marksystem%2Fbackuper)."
	mattermostSend (
			message: messageText,
			channel: '#marksystem-development', // channel name to post to starting with hashtag e.g. '#channel-name'. You can find the API channel name in the "View Info" window in the client at URL.
			endpoint: '', // optional, overrides global Jenkins setting, required if posting to private channels
			color: '#2983cc', // one of: 'good', 'warning', 'danger' or any hex color, e.g. '#439FE0'
	)
}

def notifyJenkinsSucceeded() {
	notifyStash('SUCCESS')
}

def notifyJenkinsFailure() {
	notifyStash('FAILED')
}

def notifyJenkinsInProgress() {
	notifyStash('INPROGRESS')
}

def notifyStash(state) {
	if ('SUCCESS' == state || 'FAILED' == state) {
		currentBuild.result = state
	}

	step([$class                       : 'StashNotifier',
		  commitSha1                   : '',
		  credentialsId                : '9362d732-d9cc-4393-8005-5969dc5f2bc2',
		  disableInprogressNotification: false,
		  ignoreUnverifiedSSLPeer      : true,
		  includeBuildNumberInKey      : false,
		  prependParentProjectKey      : false,
		  projectKey                   : '',
		  stashServerBaseUrl           : 'https://stash.ergon.ch'])
}