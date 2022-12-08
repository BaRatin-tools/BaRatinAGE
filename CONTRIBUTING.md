# Development environment

To have a fully working environment for developping and modifying BaRatinAGE you'll need:
- an IDE that you've set up correctly for Java
- a recent enough version of Java JDK
- BaRatin executable (can be found in [released versions](https://github.com/BaRatin-tools/BaRatinAGE/releases) or recompiled from [sources](https://github.com/BaRatin-tools/BaRatin)
- Download and compile external dependencies

Here we detail some of these aspects.

## Java version

For your development environment, you'll need a recent enough [version of Java Standard Edition](https://en.wikipedia.org/wiki/Java_version_history).
It is recommended to have the most recent Long Term Support (LTS) release.
At the time of writing (15/11/2022), you need at least the Java SE 10. 


## External libraries

At the time of writing, BaRatinAGE depends on 4 libraries you need to download and install.
You can follow the steps described below. 

### Downloads

- opencsv jar file was downloaded from sourceforge at https://sourceforge.net/projects/opencsv/files/opencsv/
- juniversalchardet jar file was downloaded from github at https://github.com/albfernandez/juniversalchardet/releases/
- jfreechart source code was downloaded from github at https://github.com/jfree/jfreechart/releases/
- orsonpdf source code was downloaded from github at https://github.com/jfree/orsonpdf/releases/

jfreechart and orsonpdf required compiling following the method described below
 
### Compiling

- prerequesite: have maven installed and its bin path added to PATH. For example (on windows):
   - downloaded the zip file from https://maven.apache.org/index.html
   - unzip it in `C:\Program Files\Java` folder (administrator privilege requrired) 
   - update the env variable by adding the path `C:\Program Files\Java\apache-maven-3.8.6\bin`
   - restart the console. The command `mvn -version` should return the version of Maven you're working with.
- download latest source file from github
- unzip in externalLib folder
- install by running the command `mvn clean verify` or `mvn clean install` (see the github `README.md` file for the recommanded way to build the lib)
- the jar file resulting from the build will typically be found in the target folder
 
 
### Eclipse IDE: updating the build path
Updating the build path in eclipe may be a bit confusing, so as a memo:
- right click on the externalLib folder and refresh it
- update the build path by rightclicking the project Build Path > Configure Build Path
- in Library Path click on the classath section then click on add JAR on the right (not external jar so paths stay relative)


## Compiling BaRatinAGE

The recommended way to compile BaRatinAGE is to use [Ant](https://ant.apache.org/).
You'll need to download this tool and install it (see the documentation [here](https://ant.apache.org/manual/index.html): at the time of writing it basically requires downloading and unzipping an archive, and adding a folder to the PATH env variable system on Windows).
Also make sure that you have a working version of jpackage (which comes with recent version of Java JDK).

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

To customize the build, modify the property variables at the beginning of the `build.xml` file.
At least the properties named `name` and `version` should be modified appropriately.
In case you modify external libraries or other aspects of BaRatinAGE, you may need to modify other aspects of the `build.xml` file.
