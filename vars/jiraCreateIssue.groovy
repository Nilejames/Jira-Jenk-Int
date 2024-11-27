import groovy.json.JsonSlurper

def call(Map config = [:]) {
    // Load the JSON template
    def rawBody = libraryResource 'com/netapp/api/jira/createIssue.json'
    
    // Prepare the binding map
    def binding = [
        key: "${config.key}",
        summary: "${config.summary}",
        description: "${config.description}",
        issueTypeName: "${config.issueTypeName}",
        versions: "${config.versions}"
    ]
    
    // Render the JSON template
    def render = renderTemplate(rawBody, binding)
    
    // Print the rendered JSON for debugging
    echo "Rendered JSON: ${render}"
    
    // Ensure the JIRA_CREDENTIALS and JIRA_URL environment variables are set
    if (!env.JIRA_CREDENTIALS || !env.JIRA_URL) {
        error("JIRA_CREDENTIALS or JIRA_URL environment variables are not set.")
    }
    
    // Execute the curl command and capture the response
    def response = sh(
        script: 'curl -D- -s -o response.json -w "%{http_code}"  -H "Authorization: Bearer "$JIRA_CREDENTIALS -X PUT --data "'+render+'" -H "Content-Type: application/json" $JIRA_URL/rest/api/2/issue',
        returnStdout: true
    ).trim()

    def responseBody = readFile('response.json')
    // Print the raw response body for debugging
    echo "Raw Response Body: ${responseBody}"
    
    // Parse the response to get the issue key
    def jsonResponse = new JsonSlurper().parseText(responseBody)
    def issueKey = jsonResponse.key
    
    // Construct the issue URL
    def issueUrl = "${env.JIRA_URL}/browse/${issueKey}"
    
    // Print the issue key and URL for debugging
    echo "Issue Key: ${issueKey}"
    echo "Issue URL: ${issueUrl}"
    
    // Return the issue key and URL
    return [issueKey: issueKey, issueUrl: issueUrl]
}
