#!/bin/bash
source javacard-env

function compile(){
	javac -source 1.2 -target 1.1 -g -cp "$JC_HOME_TOOLS/bin/api.jar" applet/src/applet/Main.java
	java -classpath "/home/kali/oracle_javacard_sdks/jc211_kit/bin/converter.jar:/home/kali/oracle_javacard_sdks/jc211_kit/javacard-rsa/applet/src/" com.sun.javacard.converter.Converter -verbose -exportpath "$JC_HOME_TOOLS/api_export_files:Main" -classdir applet/src -applet 0xa0:0x0:0x0:0x0:0x62:0x3:0x1:0xc:0x6:0x1:0x2 applet.Main applet 0x0a:0x0:0x0:0x0:0x62:0x3:0x1:0xc:0x6:0x1 1.0
}

function upload(){
	gpshell delete_cap
	gpshell install_cap
	gpshell get_status
}

command="hello"

while [ -n "$command" ]; do
	read -p "Enter a command: " command
	
	case "$command" in
		compile)
			compile
			;;
			
		ctest)
			javac -source 1.2 -target 1.1 -g -cp "$JC_HOME_TOOLS/bin/api.jar" applet/src/applet/Test.java
			java -classpath "/home/kali/oracle_javacard_sdks/jc211_kit/bin/converter.jar:/home/kali/oracle_javacard_sdks/jc211_kit/javacard-rsa/applet/src/" com.sun.javacard.converter.Converter -verbose -exportpath "$JC_HOME_TOOLS/api_export_files:Main" -classdir applet/src -applet 0xa0:0x0:0x0:0x0:0x62:0x3:0x1:0xc:0x6:0x1:0x2 applet.Test applet 0x0a:0x0:0x0:0x0:0x62:0x3:0x1:0xc:0x6:0x1 1.0
			;;

		upload)
			upload
			;;

		cu)
			compile && upload
			;;
		check)
			pcsc_scan -r
			;;

		*)
			echo "Try again"
			;;
	esac

done
