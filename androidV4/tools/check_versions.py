#!/usr/bin/python

import sys
import os
import re
from xml.dom.minidom import parse as parseXml

ERROR_MSG = """versions are incorrect
build.gradle: %s
AndroidManifest.xml: %s
DefaultConfig.java: %s
"""

USAGE = "usage: check_versions.py [ANROID_PROJECT_ROOT]"

def checkVersion(root):
    versionInGradle = getVersionFromGradle(os.path.join(root, "build.gradle"))
    versionInManifest = getVersionFromManifest(os.path.join(root, "AndroidManifest.xml"))
    versionInConfig = getVersionFromConfig(os.path.join(root, "src/com/projectgoth/common/DefaultConfig.java"))

    if versionInGradle == versionInConfig and versionInConfig == versionInManifest:
        print "all versions are correct"
    else:
        print ERROR_MSG % (versionInGradle, versionInManifest, versionInConfig)
        sys.exit(100)

def getVersionFromManifest(manifestPath):
    domInManifest = parseXml(manifestPath)
    manifestAttrs = domInManifest.getElementsByTagName("manifest")[0].attributes
    return manifestAttrs["android:versionName"].value

def getVersionFromGradle(gradlePath):
    gradleFile = open(gradlePath, "r")
    for line in gradleFile.readlines():
        version = re.search("versionName \"(\d+\.\d+\.\d+)\"", line)
        if version:
            return version.group(1)

    return ""

#deprecated
def getVersionFromPom(pomPath):
    domInPom = parseXml(pomPath)
    return domInPom.getElementsByTagName("version")[0].firstChild.nodeValue.split("-")[0]

def getVersionFromConfig(configPath):
    configFile = open(configPath, "r")
    versionMajor = ""
    versionMinor = ""
    versionPatch = ""
    for line in configFile.readlines():
        major = getVersionInLine("VERSION_MAJOR", line)
        minor = getVersionInLine("VERSION_MINOR", line)
        patch = getVersionInLine("BUILD_PATCH", line)
        if major:
            versionMajor = major
        if minor:
            versionMinor = minor
        if patch:
            versionPatch = patch

    return "%s.%s.%s" % (versionMajor, versionMinor, versionPatch)

def getVersionInLine(variableName, line):
    if variableName in line:
        return re.findall(r'\"(.+?)\"', line.split("=")[1])[0]
    else:
        return ""

if __name__ == "__main__":
    argc = len(sys.argv)
    if argc > 2:
        print USAGE
        sys.exit(99)
    elif argc == 1:
        checkVersion(os.getcwd())
    else:
        checkVersion(sys.argv[1])
