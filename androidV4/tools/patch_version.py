#!/usr/bin/python

import sys
import os
import re
import tempfile
import shutil

USAGE = "usage: patch_versions.py VERSION_NUMBER"

def patch_version(new_version):
    root = os.getcwd()
    if re.search("\d+\.\d+\.\d+", new_version):
        new_version = correct_version_format(new_version)
        patch_version_for_gradle(os.path.join(root, "build.gradle"), new_version)
        patch_version_for_manifest(os.path.join(root, "AndroidManifest.xml"), new_version)
        patch_version_for_config(os.path.join(root, "src/com/projectgoth/common/DefaultConfig.java"), new_version)

def correct_version_format(new_version):
    if new_version.find("-SNAPSHOT") >= 0:
        new_version = new_version.replace("-SNAPSHOT", "")
    major, minor, patch = new_version.split(".")
    return "%s.%s.%03d" % (major, minor, int(patch))
def patch_version_for_manifest(manifest_path, new_version):
    manifest_file = open(manifest_path, "r")
    fd, temp_file_path = tempfile.mkstemp()
    new_manifest_file = open(temp_file_path, "w")
    for line in manifest_file.readlines():
        version_code = re.search("android:versionCode=\"(\d+)\"", line)
        version_name = re.search("android:versionName=\"(\d+\.\d+\.\d+)\"", line)
        if version_code:
            new_version_code = int(version_code.group(1)) + 1
            print "update versionCode %s in AndroidManifest.xml" % new_version_code
            line = line.replace(version_code.group(1), str(new_version_code))
        if version_name:
            print "update versionName %s in AndroidManifest.xml" % new_version
            line = line.replace(version_name.group(1), new_version)

        new_manifest_file.write(line)

    manifest_file.close()
    new_manifest_file.close()

    os.remove(manifest_path)
    shutil.move(temp_file_path, manifest_path)

def patch_version_for_gradle(gradle_path, new_version):
    gradle_file = open(gradle_path, "r")
    fd, temp_file_path = tempfile.mkstemp()
    new_gradle_file = open(temp_file_path, "w")
    for line in gradle_file.readlines():
        version_code = re.search("versionCode (\d+)", line)
        version_name = re.search("versionName \"(\d+\.\d+\.\d+)\"", line)
        if version_code:
            new_version_code = int(version_code.group(1)) + 1
            print "update versionCode %s in build.gradle" % new_version_code
            line = line.replace(version_code.group(1), str(new_version_code))
        if version_name:
            print "update versionName %s in build.gradle" % new_version
            line = line.replace(version_name.group(1), new_version)

        new_gradle_file.write(line)

    gradle_file.close()
    new_gradle_file.close()

    os.remove(gradle_path)
    shutil.move(temp_file_path, gradle_path)

def patch_version_for_config(config_path, new_version):
    config_file = open(config_path, "r")
    fd, temp_file_path = tempfile.mkstemp()
    new_config_file = open(temp_file_path, "w")
    version_major, version_minor, version_patch = new_version.split(".")
    for line in config_file.readlines():
        major = get_version_from_variable("VERSION_MAJOR", line)
        minor = get_version_from_variable("VERSION_MINOR", line)
        patch = get_version_from_variable("BUILD_PATCH", line)
        if major:
            print "update VERSION_MAJOR %s in DefaultConfig" % version_major
            line = line.replace(major, version_major)
        if minor:
            print "update VERSION_MINOR %s in DefaultConfig" % version_minor
            line = line.replace(minor, version_minor)
        if patch:
            print "update BUILD_PATCH %s in DefaultConfig" % version_patch
            line = line.replace(patch, version_patch)

        new_config_file.write(line)

    new_config_file.close()
    config_file.close()

    os.remove(config_path)
    shutil.move(temp_file_path, config_path)

def get_version_from_variable(variable_name, line):
    if variable_name in line:
        return re.findall(r'\"(.+?)\"', line.split("=")[1])[0]
    else:
        return ""

if __name__ == "__main__":
    argc = len(sys.argv)
    if argc != 2:
        print USAGE
        sys.exit(99)
    else:
        patch_version(sys.argv[1])
