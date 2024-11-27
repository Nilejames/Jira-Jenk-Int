def call(Map config=[:]) {
  def rawBody = libraryResource 'com/planetpope/api/jira/createIssue.json'
  def binding = [
    key: "${config.key}",
    summary: "${config.summary}",
    description: "${config.description}",
    issueTypeName: "${config.issueTypeName}",
    labels: "${config.labels}",
    versions: "${config.versions}"
  ]
  def render = renderTemplate(rawBody,binding)
  sh('curl -D-  -H "Authorization: Bearer "$JIRA_CREDENTIALS -X POST --data "'+render+'" -H "Content-Type: application/json" $JIRA_URL/rest/api/2/issue')
}
