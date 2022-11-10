workflow to update external libraries:
 - prerequesite: have mavel installed and its bin path added to PATH
 - download latest source file from github
 - unzip in externalLib folder
 - install by running the command mvn clean verify or mvn clean install (see the github readme to have to proper way of building the lib)
 - update the build paths in eclipse
   - right click on the externalLib folder and refresh it
   - update the build path by rightclicking the project Build Path > Configure Build Path
   - in Library Path click on the classath section then click on add JAR on the right (not external jar so paths stay relative)
 


