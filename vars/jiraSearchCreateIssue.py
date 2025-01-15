import requests
import os
import json

# Read Jira instance details from environment variables
JIRA_URL = os.getenv('JIRA_URL')
API_TOKEN = os.getenv('JIRA_CREDENTIALS')

if not JIRA_URL or not API_TOKEN:
    raise ValueError("JIRA_URL, and JIRA_API_TOKEN environment variables must be set")

# Read bug details from environment variables
PROJECT_KEY = os.getenv('PROJECT_KEY')
SUMMARY = os.getenv('SUMMARY')
DESCRIPTION = os.getenv('DESCRIPTION')
ISSUE_TYPE_NAME = os.getenv('ISSUE_TYPE_NAME')
VERSIONS = os.getenv('VERSIONS')

if not PROJECT_KEY or not SUMMARY or not DESCRIPTION or not ISSUE_TYPE_NAME:
    raise ValueError("PROJECT_KEY, SUMMARY, DESCRIPTION, and ISSUE_TYPE_NAME environment variables must be set")

# Function to search for existing bugs
def search_existing_bugs(query):
    search_url = f"{JIRA_URL}/rest/api/2/search"
    headers = {
        "Accept": "application/json",
        "Authorization": f"Bearer {API_TOKEN}"
    }
    params = {
        'jql': query,
        'fields': 'key,summary,description,status'
    }

    response = requests.get(search_url, headers=headers, params=params)
    response.raise_for_status()  # Raise an error for bad status codes

    return response.json()

# Function to create a new bug
def create_bug(new_bug_data):
    create_url = f"{JIRA_URL}/rest/api/2/issue"
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {API_TOKEN}"
    }

    response = requests.post(create_url, headers=headers, data=json.dumps(new_bug_data))
    response.raise_for_status()

    return response.json()

# Function to check if a bug with similar keywords exists
def bug_exists(existing_bugs, keywords):
    for bug in existing_bugs['issues']:
        summary = bug['fields']['summary'].lower()
        description = (bug['fields'].get('description') or "").lower()
        if any(keyword.lower() in summary or keyword.lower() in description for keyword in keywords):
            return True
    return False

# Example usage
jql_query = f'project = "{PROJECT_KEY}" AND status = "Open" AND issuetype = "Bug"'
existing_bugs = search_existing_bugs(jql_query)

# Keywords to look for in existing bugs
keywords = ["polling ONTAP"]

if not bug_exists(existing_bugs, keywords):
    new_bug = {
        "fields": {
            "project": {
                "key": PROJECT_KEY
            },
            "summary": SUMMARY,
            "description": DESCRIPTION,
            "issuetype": {
                "name": ISSUE_TYPE_NAME
            },
            "versions": [{"name": version.strip()} for version in VERSIONS.split(',')] if VERSIONS else []
        }
    }
    created_bug = create_bug(new_bug)
    print(f"Created new bug: {created_bug['key']}")
else:
    print("A similar bug already exists.")
