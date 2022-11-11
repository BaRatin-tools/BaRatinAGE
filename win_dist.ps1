$jarname = "baratinage_0.jar"
$targetname = "BaRatinAGE_v2.2_beta0"
$appname = "BaRatinAGE"

mkdir ./dist/app/
cp ./$jarname ./dist/app/baratinage.jar
cp ./exe ./dist/app/exe
cp ./exe/BaRatin.exe ./dist/app/exe/BaRatin.exe
cp ./help/ ./dist/app/help/ -r
cp ./lang/ ./dist/app/lang/ -r
cp ./options/ ./dist/app/options/ -r

jpackage --input ./dist/app/ --main-jar baratinage.jar --type app-image --name $appname --dest ./dist/$targetname/ --icon BaRatinAGE.ico
	

cp ./dist/$targetname/$appname/app/exe/ ./dist/$targetname/$appname/exe -r
cp ./dist/$targetname/$appname/app/help/ ./dist/$targetname/$appname/help -r
cp ./dist/$targetname/$appname/app/lang/ ./dist/$targetname/$appname/lang -r
cp ./dist/$targetname/$appname/app/options/ ./dist/$targetname/$appname/options -r

rmdir ./dist/$targetname/$appname/app/exe/ -r
rmdir ./dist/$targetname/$appname/app/help/ -r
rmdir ./dist/$targetname/$appname/app/lang/ -r
rmdir ./dist/$targetname/$appname/app/options/ -r
