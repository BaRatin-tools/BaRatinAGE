$NAME = "BaRatinAGE"

$VERSION = "3.0.0-alpha5";

$JPACKAGE_VERSION = "3"

$CONSOLE = $True

$ICON_PATH = "resources/icons/icon.ico"

$DIRS_TO_CREATE = @(
    "log", "exe/bam_workspace"
)

$DIRS_TO_COPY = @(
    "resources", "example"
)

$EXE_TO_COPY = @(
    "exe/BaM", "exe/distribution"
)


#####################################################################
# Script start
"*********************************************************************"

#  setting up variables
$IS_WINDOWS = [System.Environment]::OSVersion.Platform -eq "Win32NT"
$IS_UNIX = [System.Environment]::OSVersion.Platform -eq "Unix"

$NAME_VERSION = "$($NAME)-$($VERSION)"
$TARGER_DIR = "target"
$TARGET_PACKAGE_DIR = "target-packaged"
$TARGET_PACKAGE_DIR_FULL = "$($TARGET_PACKAGE_DIR)/$($NAME_VERSION)"
$RESOURCES_DIR = $TARGET_PACKAGE_DIR_FULL
if ($IS_UNIX) {
    $RESOURCES_DIR = "$($TARGET_PACKAGE_DIR_FULL)/bin"
}
if ($IS_WINDOWS) {
    "Adding exe..."
    for ($i = 0; $i -lt $EXE_TO_COPY.Length; ++$i) {
        $EXE_TO_COPY[$i] = "$($EXE_TO_COPY[$i]).exe"
    }
}

# cleaning up targer dir
if (Test-Path "$($TARGER_DIR)"  -PathType Container) {
    "Removing folder '$($TARGER_DIR)'..."
    Remove-Item -Path "$($TARGER_DIR)" -Recurse -Force
}

# preparing package app dir
if (-Not(Test-Path $TARGET_PACKAGE_DIR -PathType Container)) {
    New-Item -ItemType "directory" -Path "$($TARGET_PACKAGE_DIR)" | Out-Null
}

if (Test-Path $TARGET_PACKAGE_DIR_FULL -PathType Container) {
    "Removing existing packaged app target directory '$($TARGET_PACKAGE_DIR_FULL)'..."
    Remove-Item -Path "$($TARGET_PACKAGE_DIR_FULL)" -Recurse  -Force
}

# updating pom.xml version
"Updading app version in pom.xml..."
mvn versions:set -DnewVersion="$($VERSION)"

# creating compiled/packaged jar file
"Creating jar file..."
mvn clean package

# creating packaged app
"Creating packaged app..."

$JPACKAGE_CMD = "jpackage"
$JPACKAGE_CMD = "$($JPACKAGE_CMD) --type app-image"
$JPACKAGE_CMD = "$($JPACKAGE_CMD) --app-version $($JPACKAGE_VERSION)"
$JPACKAGE_CMD = "$($JPACKAGE_CMD) --name $($NAME_VERSION)"
$JPACKAGE_CMD = "$($JPACKAGE_CMD) --dest $($TARGET_PACKAGE_DIR)"
$JPACKAGE_CMD = "$($JPACKAGE_CMD) --input $($TARGER_DIR)"
$JPACKAGE_CMD = "$($JPACKAGE_CMD) --main-jar $($NAME_VERSION).jar"
$JPACKAGE_CMD = "$($JPACKAGE_CMD) --icon $($ICON_PATH)"
    
if ($CONSOLE -And $IS_WINDOWS) {
    $JPACKAGE_CMD = "$($JPACKAGE_CMD) --win-console"
} 

$JPACKAGE_CMD 

Invoke-Expression $JPACKAGE_CMD 

# creating necessary forlder
foreach ( $DIR in $DIRS_TO_CREATE ) {
    if (-Not (Test-Path "$($RESOURCES_DIR)/$($DIR)" -PathType Container)) {
        "Creating folder '$($DIR)'..."
        New-Item -ItemType "directory" -Path "$($RESOURCES_DIR)/$($DIR)" | Out-Null
    }
}

# copying necessary folder
foreach ( $DIR in $DIRS_TO_COPY ) {
    "Copying folder '$($DIR)'..."
    Copy-item -Force -Recurse  $DIR -Destination "$($RESOURCES_DIR)/$($DIR)"
}

# copying necessary exe
foreach ( $EXE in $EXE_TO_COPY ) {
    "Copying file '$($EXE)'..."
    Copy-item -Force $EXE -Destination "$($RESOURCES_DIR)/$($EXE)"
}

