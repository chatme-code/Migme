#!/bin/sh

function change_app_properties {
    echo "Changing app.properties..."
    sed -i -e "s/build\.id=001/build\.id=$1/g" assets/app.properties
    echo "build.id=$1"
    sed -i -e "s/crashlytics=off/crashlytics=on/g" assets/app.properties
    echo "crashlytics=on"
    sed -i -e "s/ga=off/ga=on/g" assets/app.properties
    echo "ga=on"
    sed -i -e "s/connection\.settings=on/connection\.settings=off/g" assets/app.properties
    echo "connection.settings=off"
    sed -i -e "s/fiksu=off/fiksu=on/g" assets/app.properties
    echo "fiksu=on"
}

if [ -z $ANDROID_KEYSTORE_PASSWORD ]; then
    echo "need to export ANDROID_KEYSTORE_PASSWORD"
    exit 1
fi

if [ $# -ne 1 ]; then
    echo "Usage: $0 VAS_ID"
    exit 10
fi

echo "Checking version number..."
python tools/check_versions.py

if [ $? -eq 0 ]; then
    change_app_properties $1
else
    exit 1
fi


