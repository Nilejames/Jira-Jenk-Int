// vars/createJiraIssue.groovy
def call(String jiraUrl, String projectKey, String issueType, String summary, String description, String jiraUser, String jiraApiToken) {
    def jiraIssueData = [
        fields: [
            project: [
                key: projectKey
            ],
            summary: summary,
            description: description,
            issuetype: [
                name: issueType
            ]
        ]
    ]

    def response = httpRequest(
        acceptType: 'APPLICATION_JSON',
        contentType: 'APPLICATION_JSON',
        httpMode: 'POST',
        requestBody: groovy.json.JsonOutput.toJson(jiraIssueData),
        url: "${jiraUrl}/rest/api/2/issue",
        customHeaders: [
            [name: 'Authorization', value: "Basic ${"${jiraUser}:${jiraApiToken}".bytes.encodeBase64().toString()}"]
        ],
        validResponseCodes: '201'
    )

    def jsonResponse = new groovy.json.JsonSlurper().parseText(response.content)
    echo "Created Jira issue with key: ${jsonResponse.key}"
}

