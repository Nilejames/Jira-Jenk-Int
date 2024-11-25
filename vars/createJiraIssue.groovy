def call(Map params) {
    def requestParams = org.netapp.jira.JiraUtils.getJiraIssueRequestParams(params)
    def response = httpRequest(requestParams)
    def jsonResponse = new groovy.json.JsonSlurperClassic().parseText(response.content)
    echo "Created JIRA issue: ${jsonResponse.key}"
}

