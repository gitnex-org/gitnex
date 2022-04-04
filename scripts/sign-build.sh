#!/usr/bin/env bash

# Make sure needed var's are here
[ -z "${BOT_TOKEN}" ] && { echo "Token is missing (BOT_TOKEN)"; exit 1; }
[ -z "${KS_PASS}" ] && { echo "Missing keystore password (KS_PASS)"; exit 1; }
[ -z "${KEY_PASS}" ] && { echo "Missing KEY_PASS"; exit 1; }
[ -z "${INSTANCE}" ] && { echo "Instance url is missing (INSTANCE)"; exit 1; }
[ -z "${KS_REPO}" ] && { echo "Missing repo of keystore (KS_REPO)"; exit 1; }
[ -z "${KS_FILE}" ] && { echo "Filename of keystore is missing (KS_FILE)"; exit 1; }
[ -z "${OUTPUT}" ] && { echo "Missing filename of signed output (OUTPUT)"; exit 1; }

KEYFILE=$(mktemp)
curl -X GET "${INSTANCE}/api/v1/repos/${KS_REPO}/contents/${KS_FILE}?token=${BOT_TOKEN}" -H  "accept: application/json" | sed 's|"content":"|#|g' | cut -d '#' -f 2 | cut -d '"' -f 1 | base64 -d > ${KEYFILE}

apksigner sign -v --ks-pass pass:$KS_PASS --key-pass pass:$KEY_PASS --ks-key-alias GitNexBot --ks ${KEYFILE} --out signed.apk $(find . -name "*release*.apk")
