#!/bin/bash

# Environment variables which have to be set in order for this to work properly
# @author opyale
#
# INSTANCE (e.g. https://codeberg.org)
# MAIN_REPO (e.g. gitnex/GitNex)
# STATE (e.g. pending, success, error, failure or warning)
# CI_COMMIT_SHA; BOT_TOKEN

context="GitLab CI"
description="GitLab continuous integration tool"
state=$STATE
target_url="https://gitlab.com/opyale/gitnex/-/pipelines"

body='
{
"context": "'$context'",
"description": "'$description'",
"state": "'$state'",
"target_url": "'$target_url'"
}
'

curl --request POST \
--data "$body" \
--header "Accept: application/json" \
--header "Content-Type: application/json" \
"${INSTANCE}/api/v1/repos/${MAIN_REPO}/statuses/${CI_COMMIT_SHA}?token=${BOT_TOKEN}"
