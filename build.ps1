param (
    [parameter(Mandatory = $true)][string]$version,
    [switch]$archive = $false,
    [switch]$quiet = $false
)

$ErrorActionPreference = "Stop"

$VERSION = $version

$VERBOSE = !$quiet 

$CREATE_ARCHIVE = $archive

#####################################################################

$NAME = "BaRatinAGE"

$ICON_PATH = "resources/icons/icon"

$DIRS_TO_CREATE = @(
    "log", "exe/bam_workspace"
)

$DIRS_TO_COPY = @(
    "resources/fonts", "resources/i18n", "resources/icons", "example"
)
$FILES_TO_COPY = @(
    "resources/credits.csv", "resources/baratin_qfh_presets.json"
)

# no extension!
$EXE_TO_COPY = @(
    "exe/BaM", "exe/distribution"
)

#####################################################################

#  setting up variables

$VERSTION_SPLIT = $VERSION.Split("-");

$CONSOLE = $False
if ($VERSTION_SPLIT.Length -eq 2) {
    $CONSOLE = $True
}

$IS_WINDOWS = [System.Environment]::OSVersion.Platform -eq "Win32NT"
$IS_UNIX = [System.Environment]::OSVersion.Platform -eq "Unix"

$JPACKAGE_VERSION = $VERSTION_SPLIT[0];

$NAME_VERSION = "$($NAME)-$($VERSION)"
$TARGER_DIR = "target"
$TARGET_PACKAGE_DIR = "target-packaged"
$TARGET_PACKAGE_DIR_FULL = "$($TARGET_PACKAGE_DIR)/$($NAME_VERSION)"

$PLATFORM = "Windows"
if ($IS_UNIX) {
    $PLATFORM = "Linux"
}



$RESOURCES_DIR = $TARGET_PACKAGE_DIR_FULL
if ($IS_UNIX) {
    $RESOURCES_DIR = "$($TARGET_PACKAGE_DIR_FULL)/bin"
}

if ($IS_WINDOWS) {
    for ($i = 0; $i -lt $EXE_TO_COPY.Length; ++$i) {
        $EXE_TO_COPY[$i] = "$($EXE_TO_COPY[$i]).exe"
    }
}

if ($IS_WINDOWS) {
    $ARCHIVE_FILE_PATH = "$($TARGET_PACKAGE_DIR)/$($NAME_VERSION)_$($PLATFORM).zip"
    $ICON_PATH = "$($ICON_PATH).ico"
}
elseif ($IS_UNIX) {
    $ARCHIVE_FILE_PATH = "$($TARGET_PACKAGE_DIR)/$($NAME_VERSION)_$($PLATFORM).tar.gz"
    $ICON_PATH = "$($ICON_PATH).png"
}

""
"*********************************************************************"
""

####################################################################


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

""
"*********************************************************************"
""

# updating pom.xml version
"Updading app version in pom.xml..."
if ($VERBOSE) {
    mvn versions:set -DnewVersion="$($VERSION)"
}
else {
    mvn versions:set -DnewVersion="$($VERSION)" -q
}

if (-Not ($LASTEXITCODE -eq 0)) {
    throw 'ERROR: Updading app version in pom.xml failed!'
}

""
"*********************************************************************"
""

# creating compiled/packaged jar file
"Creating jar file..."
if ($VERBOSE) {
    mvn clean package
}
else {
    mvn clean package -q
}

if (-Not ($LASTEXITCODE -eq 0)) {
    throw 'ERROR: Creating jar file failed!'
}

""
"*********************************************************************"
""

# renaming main jar
Rename-Item -Path "$($TARGER_DIR)/$($NAME_VERSION).jar" -NewName "$($NAME).jar"

# creating packaged app
"Creating packaged app..."

$JPACKAGE_CMD = "jpackage"
$JPACKAGE_CMD = "$($JPACKAGE_CMD) --type app-image"
$JPACKAGE_CMD = "$($JPACKAGE_CMD) --app-version $($JPACKAGE_VERSION)"
$JPACKAGE_CMD = "$($JPACKAGE_CMD) --name $($NAME)"
$JPACKAGE_CMD = "$($JPACKAGE_CMD) --dest $($TARGET_PACKAGE_DIR_FULL)"
$JPACKAGE_CMD = "$($JPACKAGE_CMD) --input $($TARGER_DIR)"
$JPACKAGE_CMD = "$($JPACKAGE_CMD) --main-jar $($NAME).jar"
$JPACKAGE_CMD = "$($JPACKAGE_CMD) --icon $($ICON_PATH)"
    
if ($CONSOLE -And $IS_WINDOWS) {
    $JPACKAGE_CMD = "$($JPACKAGE_CMD) --win-console"
} 

$JPACKAGE_CMD 

Invoke-Expression $JPACKAGE_CMD 

# re-organize files
Move-Item -Path "$($TARGET_PACKAGE_DIR_FULL)/$($NAME)/" -Destination  "$($($TARGET_PACKAGE_DIR))/tmp/" -force
Remove-Item -Path "$($TARGET_PACKAGE_DIR_FULL)/" -Recurse -Force
Move-Item -Path "$($($TARGET_PACKAGE_DIR))/tmp/" -Destination "$($TARGET_PACKAGE_DIR_FULL)/" -force

""
"*********************************************************************"
""

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

# copying necessary folder
foreach ( $FILE in $FILES_TO_COPY ) {
    "Copying file '$($FILE)'..."
    Copy-item -Force $FILE -Destination "$($RESOURCES_DIR)/$($FILE)"
}

# copying necessary exe
foreach ( $EXE in $EXE_TO_COPY ) {
    "Copying file '$($EXE)'..."
    Copy-item -Force $EXE -Destination "$($RESOURCES_DIR)/$($EXE)"
}

""
"*********************************************************************"
""
if ($CREATE_ARCHIVE) {

    "Creating archive '$($ARCHIVE_FILE_PATH)'..."
    if (Test-Path  $ARCHIVE_FILE_PATH -PathType Leaf) {
        Remove-Item $ARCHIVE_FILE_PATH -Force
    }

    if ($IS_WINDOWS) {
        Compress-Archive -Path $TARGET_PACKAGE_DIR_FULL -DestinationPath $ARCHIVE_FILE_PATH
    }
    else {
        # -c: Create a new archive.
        # -z: Compress the archive using gzip.
        # -v: Verbosely list the files processed.
        # -f: Use archive file or device ARCHIVE.
        if ($VERBOSE) {
            $FLAGS = "-czvf"
        }
        else {
            $FLAGS = "-czf"
        }

        $TAR_CMD = "tar $FLAGS $ARCHIVE_FILE_PATH -C $TARGET_PACKAGE_DIR $NAME_VERSION"
        $TAR_CMD 
        
        Invoke-Expression $TAR_CMD
    }

    ""
    "*********************************************************************"
    ""

}

"All done!"
