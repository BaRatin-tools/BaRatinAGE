# Setting up the development environment

To have the development environment ready for BaRatinAGE V3, you'll need the following:
- java JDK (Java Development Kit)
- [Maven](https://maven.apache.org/)
- the BaRatinAGE source code
- a compiled version of BaM for you system 
- optionnally: git, to clone the BaRatinAGE repository and get the source code

## Getting Java

For your development environment, you'll need a recent enough [version of Java Standard Edition](https://en.wikipedia.org/wiki/Java_version_history).
It is recommended to have the most recent Long Term Support (LTS) release.
You need at least the Java SE 10 but we recommand you use Java 19.

Also note that you need the JDK (the JRE isn't enough) since some of the necessary tools only come with the JDK.
For example, on linux, if you're using OpenJDK, make sure you install the jdk by running this command (adapt to the desired version):
```sh
apt install openjdk-19-jdk 
```
... and not `apt install openjdk-19-jre`

## Getting Maven

_Note: that if you're using Eclipse as an IDE, you may not need to install Maven since Eclipse comes with its own embedded version of Maven._

Maven can be downloaded from the [Maven Official Website](https://maven.apache.org/).
As stated in the [Installing Apache Maven]() page

> The installation of Apache Maven is a simple process of extracting the archive and adding the `bin` directory with the `mvn` command to the `PATH`.

Below a detailed approach you can follow on windows is suggested but feel free to install Maven anyt way you see fit!

1. downloaded the zip file from https://maven.apache.org/index.html
2. unzip it in `C:\Program Files\Java` folder (administrator privilege requrired). _Note: any folder would do! I give an example using the Java folder because this makes the most sense to me but feel free to use any directory you like._
3. update the `PATH` environment variable by adding the path to the maven bin folder; for example: `C:\Program Files\Java\apache-maven-3.8.6\bin`. _Note: see this [www.howtogeek.com article](https://www.howtogeek.com/787217/how-to-edit-environment-variables-on-windows-10-or-11/) to learn to modify an environment variable on windows_
4. restart the terminal/console. The command `mvn -version` should return the version of Maven you're working with.


## Getting the BaRatinAGE source code source code

TO get the source code of BaRatinAGE V3 simply clone the [GitHub BaRatinAGE repository](https://github.com/BaRatin-tools/BaRatinAGE) and switch to the **v3** branch.
Open a terminal in the directory where BaRatinAGE source code will be downloaded:

```
git clone https://github.com/BaRatin-tools/BaRatinAGE.git
git checkout v3
```

Alternatively, simply download the source code from GitHub as a `.zip` archive file that you unzip where you see fit.
Click the green `<> Code` button on the [v3 branch BaRatinAGE repo](https://github.com/BaRatin-tools/BaRatinAGE/tree/v3) and then click `Download ZIP`.
Or here is the direct link: [https://github.com/BaRatin-tools/BaRatinAGE/archive/refs/heads/v3.zip](https://github.com/BaRatin-tools/BaRatinAGE/archive/refs/heads/v3.zip)

## Getting BaM executable

To get BaM executable, refere to the instructions in the readme of the [GitHub BaM repository](https://github.com/BaM-tools/BaM).
_Note: that you need a version that doesn't wait for a user input to exit at the end of the execution!_

It can be compiled from source (provided you have access to all the dependencies) as described below.

### Setting up Windows for compiling BaM

On windows, you need to install a Fortran compiler (see [https://fortran-lang.org/en/compilers/](https://fortran-lang.org/en/compilers/)).
We recommend [GFortran](https://gcc.gnu.org/fortran/).
Binaries for this compiler can be found [here](https://gcc.gnu.org/wiki/GFortranBinaries).

On windows, you can do the following:

- install msys: [https://www.msys2.org/](https://www.msys2.org/)
- run msys2 and install the following packages using pacman:
  - [gcc-fortran](https://packages.msys2.org/package/gcc-fortran)
  - [make](https://packages.msys2.org/package/make)
- add `C:\msys64\usr\bin` (or wherever you install msys2) to you path environment variable

### Compiling BaM using the makefile

In a folder, clone the 3 required repositories:

```shell
git clone https://github.com/BaM-tools/BaM.git
git clone https://github.com/benRenard/BMSL.git
git clone https://github.com/benRenard/miniDMSL.git
```

To build, BaM, navigate with the console to `BaM/makefile` and run: `make`.
This will build the executable file, named `BaM` (`BaM.exe` on windows).

## BaRatinAGE directory final setup

After getting the BaRatinAGE source code, you need to add a directory named `exe` at the root of the project where you copy/past the BaM executable.

## IDE setup

Use any IDE you like for Java development.
Here are the steps to have you [Eclipse](https://www.eclipse.org/downloads/) and [Visual Studio Code](https://code.visualstudio.com/) configured.

### Eclipse

1. Click `File > Import`
2. Choose `Maven > Existing Maven Project`
3. Select the root directory of BaRatinAGE (**make sure you're on the v3 branch first**)

Done!

### Visual Studio Code (VS Code)

[Visual Studio Code](https://code.visualstudio.com/) needs to be setup correctly for Java.
To do so, please refere to this [Getting Started](https://code.visualstudio.com/docs/java/java-tutorial).
Basically, you need to install the [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack).

The [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) can be found by searching the extensions in the extension panel of VS Code.
If you use [VS Codium](https://vscodium.com/), you may need to download the `vsix` file for the [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) (on the right panel, click download) and install it manually.

