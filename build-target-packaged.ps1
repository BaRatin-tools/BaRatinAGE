$NAME = "BaRatinAGE"

$VERSION = "3.0.0-alpha4";

$JPACKAGE_VERSION = "3"

$CONSOLE = $True

$ICON_PATH = "resources/icons/icon.ico"

$DIRS_TO_CREATE = @(
   "log", "exe/bam_workspace"
)

$DIRS_TO_COPY = @(
   "resources", "example"
)

$FILES_TO_COPY = @(
    "exe/BaM.exe", "exe/distribution.exe"
)


#####################################################################
# Script start
"*********************************************************************"

#  setting up variables
$NAME_VERSION = "$($NAME)-$($VERSION)"
$TARGER_DIR = "target"
$TARGET_PACKAGE_DIR = "target-packaged"
$TARGET_PACKAGE_DIR_FULL = "$($TARGET_PACKAGE_DIR)/$($NAME_VERSION)"


# cleaning up targer dir
"Deleting content of build target directory..."
rm "$($TARGER_DIR)/*" -R -FORCE

# preparing package app dir
if (-Not (Test-Path $TARGET_PACKAGE_DIR -PathType Container)) {
    "Creating packaged app target directory..."
    mkdir $TARGET_PACKAGE_DIR | Out-Null
} else {
    "Emptying packaged app target directory..."
    rm "$($TARGET_PACKAGE_DIR)/*" -R -FORCE
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
    
if ($CONSOLE) {
    $JPACKAGE_CMD = "$($JPACKAGE_CMD) --win-console"
} 

$JPACKAGE_CMD 

iex $JPACKAGE_CMD 

# creating necessary forlder
foreach ( $DIR in $DIRS_TO_CREATE )
{
    "Creating folder '$($DIR)'..."
    mkdir "$($TARGET_PACKAGE_DIR_FULL)/$($DIR)" | Out-Null
}

# copying necessary folder
foreach ( $DIR in $DIRS_TO_COPY )
{
    "Copying folder '$($DIR)'..."
    Copy-item -Force -Recurse  $DIR -Destination "$($TARGET_PACKAGE_DIR_FULL)/$($DIR)"
}

# copying necessary files
foreach ( $FILE in $FILES_TO_COPY )
{
    "Copying file '$($FILE)'..."
    Copy-item -Force $FILE -Destination "$($TARGET_PACKAGE_DIR_FULL)/$($FILE)"
}