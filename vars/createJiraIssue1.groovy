// vars/createJiraIssue.groovy
def call() {
    pipeline {
        agent any
        environment {
            JIRA_URL = 'https://ngage.netapp.com/jira-test' // Replace with your JIRA URL
            PROJECT_KEY = 'NFSAASTEST'
            SUMMARY = 'Your new bug summary- polling Netapp'
            DESCRIPTION = 'Description of the new bug'
            ISSUE_TYPE_NAME = 'Bug'
            VERSIONS = '1.0'
        }
        stages {
            stage('Checkout') {
                steps {
                    // Checkout your code from SCM
                    checkout scm
                }
            }
            stage('Create JIRA Issue') {
                steps {
                    script {
                        withCredentials([usernamePassword(credentialsId: 'Test-Jira', usernameVariable: 'JIRA_USERNAME', passwordVariable: 'JIRA_API_TOKEN')]) {
                            // Export the username and API token as environment variables
                            env.JIRA_USERNAME = JIRA_USERNAME
                            env.JIRA_API_TOKEN = JIRA_API_TOKEN

                            // Print the current working directory
                            sh 'pwd'
                            
                            // List files in the current directory
                            sh 'ls -la'
                            
                            // Ensure Python is available
                            sh 'python3 --version'
                    
                            // Run the Python script
                            sh 'python3 jiraSearchCreateIssue.py'
                        }
                    }
                }
            }
        }
    }
}
