## External library

### Downloads

On 14/11/2022: 
- opencsv jar file was downloaded from sourceforge at https://sourceforge.net/projects/opencsv/files/opencsv/
- juniversalchardet jar file was downloaded from github at https://github.com/albfernandez/juniversalchardet/releases/
- jfreechart source code was downloaded from github at https://github.com/jfree/jfreechart/releases/
- orsonpdf source code was downloaded from github at https://github.com/jfree/orsonpdf/releases/

jfreechart and orsonpdf required compiling following the method described below
 
### Compiling

- prerequesite: have mavel installed and its bin path added to PATH. For example (on windows):
   - downloaded the zip file from https://maven.apache.org/index.html
   - unzipped it in `C:\Program Files\Java` folder (admistrator privilege requrired) 
   - update the env variable by adding the path `C:\Program Files\Java\apache-maven-3.8.6\bin`
   - restart the console. The command `mvn -version` should return the version of Maven you're working with.
- download latest source file from github
- unzip in externalLib folder
- install by running the command `mvn clean verify` or `mvn clean install` (see the github `README.md` file to have to proper way of building the lib)
- the jar file resulting from the build will typically be found in the target folder
 
 
### Updating the build path
Updating the build path in eclipe may be a bit confusing, so as a memo:
- right click on the externalLib folder and refresh it
- update the build path by rightclicking the project Build Path > Configure Build Path
- in Library Path click on the classath section then click on add JAR on the right (not external jar so paths stay relative)


## Java version

For you're development environment, you'll need a recent enough version of Java Standard Edition (see https://en.wikipedia.org/wiki/Java_version_history for a list of Java version). It is recommended to have the most recent Long Term Support (LTS) release.
At the time of writing (15/11/2022), you need at least the Java SE 10. 