#!/bin/sh

function change_app_properties {
    echo "Changing app.properties..."
    sed -i -e "s/crashlytics=off/crashlytics=on/g" assets/app.properties
}

if [ -z $ANDROID_KEYSTORE_PASSWORD ]; then
    echo "need to export ANDROID_KEYSTORE_PASSWORD"
    exit 1
fi

echo "Checking version number..."
python tools/check_versions.py

if [ $? -eq 0 ]; then
    change_app_properties
else
    exit 1
fi


