# Development environment

To have fully working environment for you'll need:
- an IDE that you've settup correctly for Java
- a recent enough version of Java JDK
- BaRatin executable
- Download and compile external dependencies

Here we detail some aspects to have a working environment to develop and modify BaRatinAGE.

## Java version

For youre development environment, you'll need a recent enough [version of Java Standard Edition](https://en.wikipedia.org/wiki/Java_version_history).
It is recommended to have the most recent Long Term Support (LTS) release.
At the time of writing (15/11/2022), you need at least the Java SE 10. 


## External library

At the time of writing, BaRatinAGE depends on 4 libraries you need to download and install.
You can follow the steps described below. 

### Downloads

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
 
 
### Eclipse IDE: updating the build path
Updating the build path in eclipe may be a bit confusing, so as a memo:
- right click on the externalLib folder and refresh it
- update the build path by rightclicking the project Build Path > Configure Build Path
- in Library Path click on the classath section then click on add JAR on the right (not external jar so paths stay relative)


## Compiling BaRatinAGE

The recommended way to compile BaRatinAGE is using [Ant](https://ant.apache.org/).
You'll need to download this tool and install it (see the documentation [here](https://ant.apache.org/manual/index.html): at the time of writing it is basically an archive to download and unzip, and addining a folder to the PATH env variable system on Windows).
Also make sure you have a working version of jpackage (which comes with recent version of Java JDK).

You can then use the build.xml file to build the runnable jar file using the command:

```shell
ant
```

or if that doesn't work for some reason:

```shell
ant -buildfile .\build.xml
```

This will:
- create the compiled classes in a `build` folder
- create a runnable jar file at the project root.
- create a distributable version in a `dist` folder nammed `${name}_${version}` both as a folder and a .zip file.

To cuztomize the build, modify the property variables at the begining of the `build.xml` file.
At least the properties named `name` and `version` should be modified appropriatly.
In case you modify external libraries or other aspects of the BaRatinAGE, you may need to modify other aspect of the `build.xml` file.
