import sys
import os
import subprocess
import optparse
from optparse import OptionParser, Option, IndentedHelpFormatter
from getpass import getpass

class PosOptionParser(OptionParser):
    def format_help(self, formatter=None):
        class Positional(object):
            def __init__(self, args):
                self.option_groups = []
                self.option_list = args

        positional = Positional(self.positional)
        formatter = IndentedHelpFormatter()
        formatter.store_option_strings(positional)
        output = ['\n', formatter.format_heading("Positional Arguments")]
        formatter.indent()
        pos_help = [formatter.format_option(opt) for opt in self.positional]
        pos_help = [line.replace('--','') for line in pos_help]
        output += pos_help
        return OptionParser.format_help(self, formatter) + ''.join(output)

    def add_positional_argument(self, option):
        try:
            args = self.positional
        except AttributeError:
            args = []
        args.append(option)
        self.positional = args

    def set_out(self, out):
        self.out = out

def exec_cmd(cmd, displayText=None, raiseException=False):
    try:
        if (displayText is None):
            print cmd
        else:
            print displayText
        ret = subprocess.check_call(cmd, shell=True)
    except subprocess.CalledProcessError:
        if (raiseException):
            raise subprocess.CalledProcessError

usage = "usage: %prog [options] hash releaseVersion releaseJira nextversion"
parser = PosOptionParser(usage=usage)
parser.add_option("--storepass", help="Password for the keystore", dest="storepass")
parser.add_option("--keypass", help="Password for the key (alias)", dest="keypass")
parser.add_option("-n", "--dryrun", help="No merging, clean, package, no tag, no branching, runs on current local branch", dest="dryrun", action='store_true')
parser.add_option("-s", "--sdk", help="Path to android sdk. You can also add ANDROID_HOME on your environment variable", dest="sdk")
parser.add_option("--skipmerge", help="Skip merge to trunk", dest="skipmerge", action='store_true')
parser.add_option("--skipmaven", help="Skip running maven & tag", dest="skipmaven", action='store_true')
parser.add_positional_argument(Option('--hash', help='Hash number of latest commit to merge', action='store_true'))
parser.add_positional_argument(Option('--releaseVersion', help='Version for the release in major.minor.patch. Example: 3.10.232', action='store_true'))
parser.add_positional_argument(Option('--releaseJira', help='Jira ticket for the release', action='store_true'))
parser.add_positional_argument(Option('--nextversion', help='Version for the next development branch in major.minor.patch. Example: 3.10.233', action='store_true'))

(options, args) = parser.parse_args()
if (len(args) < 4):
    parser.error('Incorrect number of arguments')
    
if (options.sdk is None):
    try:
        options.sdk = os.environ['ANDROID_HOME']
    except KeyError:
        parser.error('No Android SDK path could be found. You may configure it on command-line using -s=... or by setting environment variable ANDROID_HOME')

if (options.storepass is None):
    try:
        options.storepass = getpass('Keystore Password:')
    except:
        parser.error('You need to provide password for keystore')
    
if (options.keypass is None):
    try:
        options.keypass = getpass('Key (alias) Password:')
    except:
        parser.error('You need to provide password for key (alias)')
    
hash = args[0];
version = args[1]
jira = args[2]
nextversion = args[3]

prefix = 'SP_'
working_dir = os.getcwd()

if (options.dryrun):
    exec_cmd('mvn clean package -Psign -Dstorepass="%s" -Dkeypass="%s" -Dandroid.sdk.path="%s" -DscmCommentPrefix="%s: " -DreleaseVersion="%s" -DdevelopmentVersion="%s" -Dtag="%s%s"' % (options.storepass, options.keypass, options.sdk, jira, version, nextversion, prefix, version))
    exit()

print 'Checking if origin and upstream values are set properly...'
try:
	exec_cmd('git remote show upstream', raiseException=True)
except subprocess.CalledProcessError:
	print 'ERROR: Upstream remote is not set. Please configure properly before continuing. See https://help.github.com/articles/fork-a-repo for more info.'
	exit()
try:
	exec_cmd('git remote show origin', raiseException=True)
except subprocess.CalledProcessError:
	print 'ERROR: Origin/Fork is not set. Please configure properly before continuing. See https://help.github.com/articles/fork-a-repo for more info.'
	exit()
	
print 'Updating master...'
exec_cmd('git fetch upstream')
exec_cmd('git checkout master')
exec_cmd('git pull upstream master')
print

if (options.skipmerge is None):
	print 'Merging development to trunk...'
	is_dev_branch_exists = False
	try:
		exec_cmd('git show-branch remotes/upstream/development', raiseException=True)
		is_dev_branch_exists = True
	except subprocess.CalledProcessError:
		print 'ERROR: Development branch not found...skipping merge and exiting'
		exit()
	if (is_dev_branch_exists):
		exec_cmd('echo "\nMerging from development branch up to %s for %s release:" >> release_notes.txt' % (hash, version))
		exec_cmd('git log remotes/upstream/master..%s --oneline >> release_notes.txt' % (hash))
		exec_cmd('git add release_notes.txt')
		exec_cmd('git commit -m "[#%s]: added list of changes for version %s from development branch up to %s"' % (jira, version, hash))
		exec_cmd('git merge --no-ff remotes/upstream/development -m "[#%s]: merge for version %s from development branch up to %s"' % (jira, version, hash))
	print

if (options.skipmaven is None):
	try:
    		exec_cmd('mvn release:prepare -DscmCommentPrefix="[#%s]: " -DreleaseVersion="%s" -DdevelopmentVersion="%s" -Dtag="%s%s" release:perform -Psign -Darguments="-Dandroid.sdk.path=%s -Dstorepass=%s -Dkeypass=%s"' % (jira, version, nextversion, prefix, version, options.sdk, options.storepass, options.keypass), raiseException=True)
	except subprocess.CalledProcessError:
		print 'ERROR: Maven release failed. Exiting...'
		exit()
else:
	exec_cmd('git push upstream master')
print

print 'Updating fork copy...'
exec_cmd('git fetch upstream')
exec_cmd('git checkout master')
exec_cmd('git pull upstream master')
exec_cmd('git push origin master')
print

print 'Merging trunk back to development...'
is_create_local_branch = True
try:
	exec_cmd('git show-branch development', raiseException=True)
	is_create_local_branch = False
except:
    	print 'Local development branch not found. Creating...'

if (is_create_local_branch):
	exec_cmd('git checkout remotes/upstream/development -b development')

exec_cmd('git checkout development')
exec_cmd('git pull upstream development')

#exec_cmd('echo "\nMerge from trunk to development branch after %s release" >> merge_from_trunk.txt' % (hash))
#exec_cmd('git log remotes/upstream/development..remotes/upstream/master --oneline >> merge_from_trunk.txt')
#exec_cmd('git add merge_from_trunk.txt')
#exec_cmd('git commit -m "[#%s]: added list of changes from trunk"' % (jira))

#exec_cmd('git merge remotes/upstream/master -m "[#%s]: merge from trunk"' % (jira))

exec_cmd('git rebase remotes/upstream/master')

exec_cmd('git push origin development')
exec_cmd('git push upstream development')
print
    
print 'Finish'
