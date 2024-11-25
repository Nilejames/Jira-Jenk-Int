package org.netapp.jira

import groovy.json.JsonOutput

class JiraUtils {
    static Map getJiraIssueRequestParams(Map params) {
        def jiraUrl = params.jiraUrl
        def projectKey = params.projectKey
        def issueType = params.issueType ?: 'Bug'
        def summary = params.summary
        def description = params.description
        def jiraUser = params.jiraUser
        def jiraApiToken = params.jiraApiToken

	//def authString = "${jiraApiToken}".bytes.encodeBase64().toString()
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

        return [
            httpMode: 'POST',
            url: "${jiraUrl}/rest/api/2/issue",
            contentType: 'APPLICATION_JSON',
            customHeaders: [[name: 'Authorization', value: "Bearer ${jiraApiToken}"]],
            requestBody: requestBody
        ]
    }
}

