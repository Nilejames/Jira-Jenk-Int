// vars/createJiraIssue.groovy
def call(Map config = [:]) {
    org.netapp.jira.JiraUtils.createIssue(this, config)
}

