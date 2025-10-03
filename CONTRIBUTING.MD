# Setting up the development environment

To have the development environment ready for BaRatinAGE V3, you'll need the following:

- java JDK (Java Development Kit)
- [Maven](https://maven.apache.org/)
- the BaRatinAGE source code
- a compiled version of BaM for you system
- a compiled version of [distribution BMSL cli](https://github.com/benRenard/BMSL/tree/main/cli/distribution)
- optionnally: git, to clone the BaRatinAGE repository and get the source code

## Getting Java

For your development environment, you'll need a recent enough [version of Java Standard Edition](https://en.wikipedia.org/wiki/Java_version_history).
It is recommended to have the most recent Long Term Support (LTS) release.
The minimum version required is 21.

Also note that you need the JDK (the JRE isn't enough) since some of the necessary tools only come with the JDK.
For example, on linux, if you're using OpenJDK, make sure you install the jdk by running this command (adapt to the desired version):

```sh
apt install openjdk-21-jdk
```

... and not `apt install openjdk-21-jre`

## Getting Maven

_Note: if you're using Eclipse, you may not need to install Maven since Eclipse comes with its own embedded version of Maven._

Maven can be downloaded from the [Maven Official Website](https://maven.apache.org/).
As stated in the [Installing Apache Maven](https://maven.apache.org/install.html) page

> The installation of Apache Maven is a simple process of extracting the archive and adding the `bin` directory with the `mvn` command to the `PATH`.

Below a detailed approach you can follow on windows is suggested but feel free to install Maven any way you see fit!

1. downloaded the zip file from https://maven.apache.org/index.html
2. unzip it in `C:\Program Files\Java` folder (administrator privilege requrired). _Note: any folder would do! I give an example using the Java folder because this makes the most sense to me but feel free to use any directory you like._
3. update the `PATH` environment variable by adding the path to the maven bin folder; for example: `C:\Program Files\Java\apache-maven-3.8.6\bin`. _Note: see this [www.howtogeek.com article](https://www.howtogeek.com/787217/how-to-edit-environment-variables-on-windows-10-or-11/) to learn to modify an environment variable on windows_
4. restart the terminal/console. The command `mvn -version` should return the version of Maven you're working with.

## Getting the BaRatinAGE source code source code

To get the source code of **BaRatinAGE v3** simply clone the [GitHub BaRatinAGE repository](https://github.com/BaRatin-tools/BaRatinAGE).
In other words, open a terminal in the directory where BaRatinAGE source code will be downloaded and run the following commands:

```
git clone https://github.com/BaRatin-tools/BaRatinAGE.git
cd BaRatinAGE
```

Alternatively, simply download the source code from GitHub as a `.zip` archive file that you unzip in a directory of your choice.

## Getting BaM executable and the distribution CLI executables

Both executable can be found in the release sections of their respective repository:
- [BaM](https://github.com/BaM-tools/BaM).
- [distribution CLI](https://github.com/benRenard/BMSL).

In case you need to build BaM on your local environment, here are instruction for Windows

### Prerequisit for compiling BaM on Windows

On Windows, we recommand using MSYS2:
- Go to [msys2.org](https://www.msys2.org/), download the installer and run it to install MSYS2.
- In the MSYS2 shell, run `pacman -Syu` to update the package list and upgrade everything to the latest version. You may need to restart the shell and rerun the command a few times.
- install the mingw-w64 gofortran compiler : `pacman -S mingw-w64-x86_64-gcc-fortran`
- install the make package to make the make command available: `pacman -S make` 
- to be able to use the powershell (instead of the MSYS2 shell), add the folders where both `gfortran` and `make` are located to you PATH environment variable. You can find this out by running `where gfortran` and `where make` in the MSYS2 shell.

### Compiling BaM using the makefile

In a folder, clone the 3 required repositories:

```
git clone https://github.com/BaM-tools/BaM.git
git clone https://github.com/benRenard/BMSL.git
git clone https://github.com/benRenard/miniDMSL.git
```

To build, BaM, navigate with the console to `BaM/makefile` and run: `make`.
This will build the executable file, named `BaM` (`BaM.exe` on windows).


## BaRatinAGE directory final setup

After getting the BaRatinAGE source code, you need to add a directory named `exe` at the root of the project where you copy/past the BaM and distribution CLI executables.

## IDE setup

Use any IDE you like for Java development.
Here are the steps for [Eclipse](https://www.eclipse.org/downloads/) and [Visual Studio Code](https://code.visualstudio.com/).

### Eclipse

1. Click `File > Import`
2. Choose `Maven > Existing Maven Project`
3. Select the root directory of BaRatinAGE

Done!

### Visual Studio Code (VS Code)

[Visual Studio Code](https://code.visualstudio.com/) needs to be setup correctly for Java.
To do so, please refere to this [Getting Started with Java in VS Code](https://code.visualstudio.com/docs/java/java-tutorial).
Basically, you need to install the [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack).
The [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) can be found by searching the extensions in the extension panel of VS Code.
If you're using [VS Codium](https://vscodium.com/), you may need to download the `vsix` file for the [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) (on the right panel, click download) and install it manually.

# Packaging BaRatinAGE

Packaging BaRatinAGE refers to both creating the executable `jar` file and using `jpackage` to create a standalone, platform specific, executable.

There are two options: 
- using the github action dedicated to building and releasing the app
- use the `build.ps1` powershell script (On Linux, you probably need to install powershell to run the script)

The `build.ps1` script takes one mandatory arguments, the version number.
Optionnaly, you can use it to also create the final archive (`*.zip` or `*.tar.gz` file) using the `-archive` flag.
A `-quiet` flag can also be used to prevent printing too much information to the console.

For example, to build the version **v3.0.3**, you can run the following in a powershell console:

```ps
.\build.ps1 "3.0.3" -archive
```
