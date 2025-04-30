pipeline {
    agent { label 'MASTER' }

    parameters {
        choice(name: 'ENV', choices: ['dev', 'qa', 'prod'], description: 'Select the environment to deploy')
        string(name: 'JIRA_TICKET', defaultValue: '', description: 'Required for PROD deployments')
    }

    environment {
        APP_NAME = 'java-spring-app'
        JAR_NAME = 'target/my-app.jar'
        DEV_SERVER = 'dev.project-training.in'
        QA_SERVER  = 'qa.project-training.in'
        PROD_SERVER = 'web.project-training.in'
        JIRA_SITE = 'ET_JIRA_SITE' // Configure this in Jenkins Jira Settings
        JIRA_CREDENTIALS_ID = 'ET_JIRA_TOKEN' // Create in Jenkins Credentials
        SONAR_HOST_URL = 'https://sonarcloud.io/'
        SONAR_TOKEN = credentials('ET_SONAR_TOKEN')  // Securely inject token
        SONAR_ORG = 'bashxperts'
    }

    stages {
        stage('Checkout from GitHub') {
            steps {
                echo "Checking out source code from GitHub..."
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/bashxperts/java-spring-app.git',
                        credentialsId: env.GIT_CREDS
                    ]]
                ])
            }
        }
        stage('Build') {
            when { expression { params.ENV == 'dev' } }
            steps {
                echo "Building application with Maven for DEV..."
                sh 'mvn clean package'
            }
        }

        stage('Sonar Scan') {
            when {
                anyOf {
                    expression { params.ENV == 'dev' }
                    expression { params.ENV == 'qa' }
                }
            }
            steps {
                withSonarQubeEnv('SONAR_SITE') { // Must match name in Jenkins config
                    sh """
                    mvn sonar:sonar \
                      -Dsonar.projectKey=${env.APP_NAME} \
                      -Dsonar.organization=${env.SONAR_ORG} \
                      -Dsonar.host.url=${env.SONAR_HOST_URL} \
                      -Dsonar.login=${env.SONAR_TOKEN}
                    """
                }
            }
        }

		stage('Approval') {
			when {
				expression { params.ENV == 'prod' }
			}
			steps {
				script {
					def response = jiraGetIssue idOrKey: params.JIRA_TICKET
					if (!response.successful) {
						error "Failed to fetch Jira issue: ${params.JIRA_TICKET}"
					}

					def issue = response.data
					def status = issue.fields.status.name

					echo "Jira ticket status: ${status}"

					if (status != 'Approved') {
						error "Deployment blocked. Jira ticket is not in 'Approved' state."
					}
				}
			}
		}


		stage('Deploy') {
			steps {
				script {
					def functionName = ""
					if (params.ENV == 'dev') {
						functionName = "java-app-dev"
					} else if (params.ENV == 'qa') {
						functionName = "java-app-qa"
					} else if (params.ENV == 'prod') {
						functionName = "java-app-prod"
					}

					echo "Deploying to Lambda function: ${functionName}"

					withCredentials([[
						$class: 'AmazonWebServicesCredentialsBinding',
						credentialsId: 'AWS_ADMIN' 
					]]) {
						sh """
							zip -j lambda.zip target/*.jar
							aws lambda update-function-code \
								--function-name ${functionName} \
								--zip-file fileb://lambda.zip \
								--region us-east-1
						"""
					}

					echo "Deployment to ${functionName} completed."
				}
			}
		}

    post {
        success {
            script {
                if (params.ENV == 'prod' && params.JIRA_TICKET?.trim()) {
                    jiraAddComment site: env.JIRA_SITE, idOrKey: params.JIRA_TICKET, comment: "✅ Deployment successful via Jenkins for ticket ${params.JIRA_TICKET}."

                    // Update this transition ID based on your Jira workflow
                    jiraTransitionIssue site: env.JIRA_SITE, idOrKey: params.JIRA_TICKET, transition: [id: '31'] 
                    echo "JIRA ticket ${params.JIRA_TICKET} closed."
                }
            }
        }

        failure {
            script {
                if (params.ENV == 'prod' && params.JIRA_TICKET?.trim()) {
                    jiraAddComment site: env.JIRA_SITE, idOrKey: params.JIRA_TICKET, comment: "❌ Deployment failed via Jenkins for ticket ${params.JIRA_TICKET}."
                }
            }
        }
    }
}
