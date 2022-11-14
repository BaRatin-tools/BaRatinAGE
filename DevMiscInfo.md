## External library

### Downloads

On 14/11/2022: 
 - opencsv jar file was downloaded from sourceforge at https://sourceforge.net/projects/opencsv/files/opencsv/
 - juniversalchardet jar file was downloaded from github at https://github.com/albfernandez/juniversalchardet/releases/
 - jfreechart source code was downloaded from github at https://github.com/jfree/jfreechart/releases/
 - orsonpdf source code was downloaded from github at https://github.com/jfree/orsonpdf/releases/

jfreechart and orsonpdf required compiling following the method described below
 
### Compiling

 - prerequesite: have mavel installed and its bin path added to PATH
 - download latest source file from github
 - unzip in externalLib folder
 - install by running the command mvn clean verify or mvn clean install (see the github readme to have to proper way of building the lib)
 - the jar file resulting from the build will typically be found in the target folder
 
 
### Updating the build path
 Updating the build path in eclipe may be a bit confusing, so as a memo:
   - right click on the externalLib folder and refresh it
   - update the build path by rightclicking the project Build Path > Configure Build Path
   - in Library Path click on the classath section then click on add JAR on the right (not external jar so paths stay relative)
 