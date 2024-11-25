package org.netapp.jira

import groovy.json.JsonOutput
import groovy.json.JsonSlurperClassic
import hudson.AbortException

class JiraUtils {
    static void createIssue(Map params) {
        def jiraUrl = params.jiraUrl
        def projectKey = params.projectKey
        def issueType = params.issueType ?: 'Bug'
        def summary = params.summary
        def description = params.description
        def jiraApiToken = params.jiraApiToken

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

        def response = script.httpRequest(
            httpMode: 'POST',
            url: "${jiraUrl}/rest/api/2/issue",
            contentType: 'APPLICATION_JSON',
            customHeaders: [[name: 'Authorization', value: "Bearer ${jiraApiToken}"]],
            requestBody: requestBody,
            validResponseCodes: '200:201'
        )

        if (response.status >= 200 && response.status < 300) {
            def jsonResponse = new JsonSlurperClassic().parseText(response.content)
            echo "Created JIRA issue: ${jsonResponse.key}"
        } else {
            throw new AbortException("Failed to create JIRA issue. Response: ${response.content}")
        }
    }
}

