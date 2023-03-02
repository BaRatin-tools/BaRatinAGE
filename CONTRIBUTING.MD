## Getting started using Visual Studio Code as IDE

The IDE used for development is Visual Studio Code (VS Code).
It needs to be setup correctly for Java.
To do so, please refere to this [Getting Started](https://code.visualstudio.com/docs/java/java-tutorial).
Feel free to use any IDE you like but know that it hasn't been tested on our side.

Here, a few necessary steps to get a development environment ready using VS code are described.

### Install the Extension Pack for Java

The [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) can be found by searching the extensions in the extension panel of VS Code.
If you use VS Codium, youn may need to download the vsix file for the [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) (on the right panel, click download) and install it manually.

### Cloning the repo and opening it in VS Code

Import/clone the BaRatinAge repository somewhere on your computer.

To clone the repo, in the console, navigate to where you want to clone the repo and run:

```shell
git clone https://github.com/BaRatin-tools/BaRatinAGE
```

then, navigate to the created folder "BaRatinAGE"

```shell
cd ./BaRatinAGE/
```

and then switch to the v3 branch:

```shell
git checkout v3
```

Now you can open VS Code from the console with

```shell
code .
```

(or `codium .`, if using VS Codium)

Once VS Code is running, if you open any of the Java source file (.java file in the src folder), you'll see the Java extension starting running.

A small tweak that will make you life easier when developping is to turn on autocomplete for the `awt` package.
Follow the appraoch suggested [here](https://stackoverflow.com/a/72851417):

- Open command pallete: `Ctrl + Shift + P`
- Select `Java: Help center`
- Go to `Students` tab
- Click on `Enable AWT Development`

Probably that you'll get some errors since there are some external libraries you need to install.

### Installing the external dependencies

The external dependencies of BaRatinAGE are located in a folder named `lib` at the root.
You need to download (and for some also compile) them.
The required librairies are:

- JFreeChart
- JFreeSVG
- juniversalchardet

Compilation is required for JFreeChart.
You need to have Maven install for that.

### JFreeChart

Browse to the [releases of JFreeChart](https://github.com/jfree/jfreechart/releases) in the GitHub repo of JFreeChart.
Download the source code of the last release and extract it in folder named "lib" at the root of the BaRatinAGE project.
Open a console in `./lib/jfreechart-1.5.4` (adapt to your version) to run the compilation command indicated on the readme page of the [JFreeChart Github Repo](https://github.com/jfree/jfreechart/):

```shell
mvn clean install
```

This command will create the needed `.jar` file to use the JFreeChart library, located in `./lib/jfreechart-1.5.4/target/jfreechart-1.5.4.jar`.

VS Code should automatically take into account this external dependency and you should no longer see import error messages related to JFreeChart.

### JFreeSVG

This library is only used to export a JFreeChart as an SVG.
The compile Jar file can be downloaded directly from the [JFreeSVG](https://www.jfree.org/jfreesvg/) website.
However, I recommmend you compile it from source after download/extracting the latest release from [https://github.com/jfree/jfreesvg/releases](https://github.com/jfree/jfreesvg/releases).

Commpile it using the command:

```shell
mvn clean verify
```

### universalchardet

The `universalchardet` is used to auto detect the text formatting files.
Download the latest release from [https://github.com/albfernandez/juniversalchardet/releases](https://github.com/albfernandez/juniversalchardet/releases).
Add the `.jar` file in the `lib` folder.

## Running/debugging

After installing the required dependencies, you're almoste ready to run the application.
You only need to configure VS code, so it knowns how to start the project.
You'll need to create a folder named `.vscode` at the root of the project.
In this folder, two files are required: `settings.json` and `launch.json`.

Content of the settings.json should be:

```json
{
  "java.project.sourcePaths": ["src"],
  "java.project.outputPath": "bin",
  "java.project.referencedLibraries": ["lib/**/*.jar"]
}
```

Then, in the left side of VS code, open the `Run and Debug` panel (Ctrl+Shift+D).
Then click `create a launch.json file` and choose `java` when prompted to choose a debugger.
This will create the `launch.json` file with the appropriate configuration.

Now you should be able to run the App.
However, you won't be able to run BaM yet!
The BaM executable needs to be added to the project first.

## BaM executable

To get BaM executable, refere to the instructions in the readme of the [BaM repository](https://github.com/BaM-tools/BaM) on Github.

It can be compiled from source (provided you have access to all the dependencies) as described below.

You need to install a Fortran compiler (see [https://fortran-lang.org/en/compilers/](https://fortran-lang.org/en/compilers/)).
We recommend [GFortran](https://gcc.gnu.org/fortran/).
Binaries for this compiler can be found [here](https://gcc.gnu.org/wiki/GFortranBinaries).

On windows, you can do the following:

- install msys: [https://www.msys2.org/](https://www.msys2.org/)
- run msys2 and install the following packages using pacman:
  - [gcc-fortran](https://packages.msys2.org/package/gcc-fortran)
  - [make](https://packages.msys2.org/package/make)
- add `C:\msys64\usr\bin` (or wherever you install msys2) to you path environment variable

In a folder, clone the 3 required repositories:

```shell
git clone https://github.com/BaM-tools/BaM.git
git clone https://github.com/benRenard/BMSL.git
git clone https://github.com/benRenard/miniDMSL.git
```

To build, BaM, navigate with the console to `BaM/makefile` and run: `make`.
This will build the executable file, named `BaM` (`BaM.exe` on windows).
Copy/past it in the `BaRatinAGE` folder in a new directory named `exe` at the root of the project.
