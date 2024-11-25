package org.netapp.jira

import groovy.json.JsonOutput
import groovy.json.JsonSlurperClassic

class JiraUtils {
    static void createIssue(Map params) {
        def jiraUrl = params.jiraUrl
        def projectKey = params.projectKey
        def issueType = params.issueType ?: 'Bug'
        def summary = params.summary
        def description = params.description
        def jiraUser = params.jiraUser
        def jiraApiToken = params.jiraApiToken

        def authString = "${jiraUser}:${jiraApiToken}".bytes.encodeBase64().toString()
        def requestBody = JsonOutput.toJson([
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
        ])

        def response = httpRequest(
            httpMode: 'POST',
            url: "${jiraUrl}/rest/api/2/issue",
            contentType: 'APPLICATION_JSON',
            customHeaders: [[name: 'Authorization', value: "Basic ${authString}"]],
            requestBody: requestBody
        )

        def jsonResponse = new JsonSlurperClassic().parseText(response.content)
        echo "Created JIRA issue: ${jsonResponse.key}"
    }
}

