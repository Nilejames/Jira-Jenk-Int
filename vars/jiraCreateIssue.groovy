import groovy.json.JsonSlurper
def call(Map config=[:]) {
  def rawBody = libraryResource 'com/netapp/api/jira/createIssue.json'
  def binding = [
    key: "${config.key}",
    summary: "${config.summary}",
    description: "${config.description}",
    issueTypeName: "${config.issueTypeName}",
    versions:"${config.versions}"
  ]
  def render = renderTemplate(rawBody,binding)
  def response = sh(
        script: sh('curl -D-  -H "Authorization: Bearer "$JIRA_CREDENTIALS -X PUT --data "'+render+'" -H "Content-Type: application/json" $JIRA_URL/rest/api/2/issue'),
        returnStdout: true
    ).trim()
      // Print the raw response for debugging
    echo "Raw Response: ${response}"
    
    // Parse the response to get the issue key
    def jsonResponse = new JsonSlurper().parseText(response)
    def issueKey = jsonResponse.key
    
    // Construct the issue URL
    def issueUrl = "${JIRA_URL}/browse/${issueKey}"
    
    // Print the issue key and URL for debugging
    echo "Issue Key: ${issueKey}"
    echo "Issue URL: ${issueUrl}"
    
    // Return the issue key and URL
    return [issueKey: issueKey, issueUrl: issueUrl]
  
}
