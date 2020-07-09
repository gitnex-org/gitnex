#!/usr/bin/env bash

#make sure needed var's are here
[ -z "${BOT_TOKEN}" ] && { echo "missing BOT_TOKEN"; exit 1; }
[ -z "${KS_PASS}" ] && { echo "missing KS_PASS"; exit 1; }
[ -z "${KEY_PASS}" ] && { echo "missing KEY_PASS"; exit 1; }
[ -z "${GITEA}" ] && { echo "missing GITEA"; exit 1; }
[ -z "${KS_REPO}" ] && { echo "missing KS_REPO"; exit 1; }
[ -z "${KS_FILE}" ] && { echo "missing KS_FILE"; exit 1; }
[ -z "${OUTPUT}" ] && { echo "missing OUTPUT"; exit 1; }


KEYFILE=$(mktemp)
curl -X GET "${GITEA}/api/v1/repos/${KS_REPO}/contents/${KS_FILE}?token=${BOT_TOKEN}" -H  "accept: application/json" | sed 's|"content":"|#|g' | cut -d '#' -f 2 | cut -d '"' -f 1 | base64 -d > ${KEYFILE}

/opt/android-sdk-linux/build-tools/*/apksigner sign -v --ks-pass pass:$KS_PASS --key-pass pass:$KEY_PASS --ks-key-alias GitNexBot --ks ${KEYFILE} --out signed.apk $(find . -name "*release*.apk")
