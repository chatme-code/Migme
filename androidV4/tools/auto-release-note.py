#!/usr/bin/python

import os
import urllib
import xml.dom.minidom as minidom

JOB_NAME = os.environ["JOB_NAME"]
BUILD_NUMBER = os.environ["BUILD_NUMBER"]
USER="lemonpassion"
PASSWORD="034db8ac7840ff4590213e8f31e776d3"
RELEASE_NOTE="./deploy/release_note.txt"
URL_TEMPLATE = "https://%s:%s@tools.projectgoth.com/jenkins/job/%s/%s/api/xml/?wrapper=changes&xpath=//changeSet//item"

def get_change_set():
    url =  URL_TEMPLATE % (USER, PASSWORD, JOB_NAME, BUILD_NUMBER)
    print "get changesets from %s" % url
    dom = minidom.parse(urllib.urlopen(url))
    create_release_note(dom)

def create_release_note(dom):
    release_note = open(RELEASE_NOTE, "w")
    itemNodes = dom.getElementsByTagName('item')
    if len(itemNodes) > 0:
        for itemNode in itemNodes:
            msgNode = itemNode.getElementsByTagName('msg')
            for node in msgNode:
                msg = node.firstChild.data.encode('utf-8')
            authorNode = itemNode.getElementsByTagName('author')
            for node in authorNode:
                for node in node.getElementsByTagName('fullName'):
                    author = node.firstChild.data.encode('utf-8')
            release_note.write("%s - %s\n" % (msg, author))
    else:
        release_note.write("No changes\n")
    
    release_note.close()
    print "release note is created in %s" % RELEASE_NOTE

if __name__ == "__main__":
    get_change_set()
