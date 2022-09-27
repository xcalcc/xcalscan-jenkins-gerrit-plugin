# Xcalscan Jenkins Gerrit plugin

Xcalscan Jenkins Gerrit plugin is a plugin for Jenkins to post back the xcalscan result to Gerrit as review 

## Objective

Xcalscan is a SAST (Static Application Security Testing) tools which use for source code scanning and report potential hazard.
The Jenkins Gerrit plugin enable user to post back the result as review comment to the Gerrit server. 
## Build

Install [Maven](http://maven.apache.org) and run the following:

        git clone https://github.com/xcalcc/xcalscan-jenkins-gerrit-plugin.git
        cd xcalscan-jenkins-gerrit-plugin
        mvn package

## Install Plugin

The instruction how to install the plugin by uploading hpi file
[here](https://www.jenkins.io/doc/book/managing/plugins/)

1. Navigate to the **Manage Jenkins** > **Manage Plugins** page in the web UI.
2. Click on the Advanced tab.
3. Choose the .hpi file under the Upload Plugin section.
4. Upload the plugin file.
