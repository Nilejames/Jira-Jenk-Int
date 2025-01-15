// vars/jiraIssueManager.groovy
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def call(Map config) {
    def jiraUrl = config.jiraUrl
    def projectKey = config.projectKey
    def summary = config.summary
    def description = config.description
    def issueTypeName = config.issueTypeName
    def versions = config.versions
    def keywords = config.keywords

    withCredentials([usernamePassword(credentialsId: config.credentialsId, usernameVariable: 'JIRA_USERNAME', passwordVariable: 'JIRA_API_TOKEN')]) {
        // Search for existing bugs
        def existingBugs = searchExistingBugs(jiraUrl, projectKey, issueTypeName)
        
        // Check if a bug with similar keywords exists
        if (!bugExists(existingBugs, keywords)) {
            // Load the JSON template
            def rawBody = libraryResource 'com/netapp/api/jira/createIssue.json'
            
            // Prepare the binding map
            def binding = [
                key: projectKey,
                summary: summary,
                description: description,
                issueTypeName: issueTypeName,
                versions: versions
            ]
            
            // Render the JSON template
            def render = renderTemplate(rawBody, binding)
            
            // Print the rendered JSON for debugging
            echo "Rendered JSON: ${render}"
            
            // Ensure the JIRA_CREDENTIALS and JIRA_URL environment variables are set
            if (!env.JIRA_API_TOKEN || !jiraUrl) {
                error("JIRA_API_TOKEN or jiraUrl environment variables are not set.")
            }
            
            // Execute the curl command and capture the response
            def response = sh(
                script: 'curl -D- -s -o response.json -w "%{http_code}"  -H "Authorization: Bearer ' + env.JIRA_API_TOKEN + '" -X POST --data \'' + render + '\' -H "Content-Type: application/json" ' + jiraUrl + '/rest/api/2/issue',
                returnStdout: true
            ).trim()

            def responseBody = readFile('response.json')
            
            // Parse the response to get the issue key
            def jsonResponse = new JsonSlurper().parseText(responseBody)
            def issueKey = jsonResponse.key
            
            // Construct the issue URL
            def issueUrl = "${jiraUrl}/browse/${issueKey}"
            
            // Return the issue key and URL
            echo "Created new bug: ${issueKey}"
            return [issueKey: issueKey, issueUrl: issueUrl]
        } else {
            echo "A similar bug already exists."
            return null
        }
    }
}

def searchExistingBugs(String jiraUrl, String projectKey, String issueTypeName) {
    def jqlQuery = "project = '${projectKey}' AND status in (Open, New) AND issuetype = '${issueTypeName}'"
    def searchUrl = "${jiraUrl}/rest/api/2/search"
    def response = httpRequest(
        url: searchUrl,
        httpMode: 'GET',
        customHeaders: [[name: 'Authorization', value: "Bearer ${env.JIRA_API_TOKEN}", maskValue: true]],
        queryString: "jql=${URLEncoder.encode(jqlQuery, 'UTF-8')}&fields=key,summary,description,status",
        validResponseCodes: '200'
    )
    return new JsonSlurper().parseText(response.content)
}

def bugExists(Map existingBugs, List<String> keywords) {
    existingBugs.issues.any { bug ->
        def summary = bug.fields.summary.toLowerCase()
        def description = (bug.fields.description ?: "").toLowerCase()
        keywords.any { keyword -> keyword.toLowerCase() in summary || keyword.toLowerCase() in description }
    }
}

