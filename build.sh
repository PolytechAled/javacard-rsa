#!/bin/bash
#source javacard-env

RED="31"
GREEN="32"
CYAN="\e[36m"
MAGENTA="\e[93m"
BOLDGREEN="\e[1;${GREEN}m"
ITALICRED="\e[3;${RED}m"
ENDCOLOR="\e[0m"
UNDERLINED="\e[4;97m"
PREFIX="${BOLDGREEN}Roudiner >${ENDCOLOR}"


function compile(){
	javac -source 1.2 -target 1.1 -g -cp "$JC_HOME_TOOLS/bin/api.jar" applet/src/applet/Main.java
	java -classpath "/home/kali/oracle_javacard_sdks/jc211_kit/bin/converter.jar:/home/kali/oracle_javacard_sdks/jc211_kit/javacard-rsa/applet/src/" com.sun.javacard.converter.Converter -verbose -exportpath "$JC_HOME_TOOLS/api_export_files:Main" -classdir applet/src -applet 0xa0:0x0:0x0:0x0:0x62:0x3:0x1:0xc:0x6:0x1:0x2 applet.Main applet 0x0a:0x0:0x0:0x0:0x62:0x3:0x1:0xc:0x6:0x1 1.0
}

function upload(){
	gpshell delete_cap
	gpshell install_cap
	gpshell get_status
}

function getkey(){
	mkdir -p key
	keybytes=$(echo "$1" | grep -A5 "> 10 03" | tail -n5 | sed 's/< //' | sed 's/ //g' | sed ':a;N;$!ba;s/\n//g' | tail -c+6 | head -c-23)
	#keybytes=$(echo "$1" | grep -A4 "> 10 03" | tail -n4 | sed 's/< //' | sed 's/ //g' | sed ':a;N;$!ba;s/\n//g' | tail -c+6)
	exponent=$(echo "$keybytes" | head -c+5)
	pubkey=$(echo "$keybytes" | tail -c+10)
	sed -e "s/PUBKEY/0x$pubkey/" -e "s/EXPONENT/0x$exponent/" template.asn1 > key/def.asn1
	openssl asn1parse -genconf key/def.asn1 -out key/pubkey.der -noout
	openssl rsa -in key/pubkey.der -inform der -pubin -out key/pubkey.pem
	openssl rsa -pubin -inform PEM -text -noout -in key/pubkey.pem
}

function getsig(){
	mkdir -p sig
	echo "$1" | grep -A7 "10 04" | tail -n+6 | sed -e 's/< //' -e 's/ //g' | sed ':a;N;$!ba;s/\n//g' > sig/signature.bin
	cat sig/signature.bin
}

function getdata(){
	mkdir -p sig
	echo "$1" | grep -m1 "10 03" | tail -c+7 | sed 's/ //g' | xxd -r > sig/data.bin
}

function verify(){
	appletout="$(scriptor script.scriptor 2>/dev/null)"
	getkey "$appletout"
	getsig "$appletout"
	#getdata "$appletout"
	openssl dgst -sha256 -verify key/pubkey.pem -signature sig/signature.bin sig/data.bin
}
start ()
{
echo -e "${CYAN}╔════════════════════════════════════╗"
echo -e "║                                    ║"
echo -e "║${MAGENTA}       Welcome in RoudinerCard ${CYAN}     ║"
echo -e "║                                    ║"
echo -e "╚════════════════════════════════════╝"
command="hello"
}

help ()
{
    echo -e "${PREFIX} How to interact with javacard:"
    echo -e "> ${UNDERLINED}check${ENDCOLOR} check if the javacard is connected."
    echo -e "> ${UNDERLINED}sendN${ENDCOLOR} function which sign the data and verify with the public key of javacard."
}

start

while [ -n "$command" ]; do
    echo -en "${PREFIX} Enter a command: "
	read  command

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
		verify|v)
			verify
			;;

		*)
            help
			;;
	esac

done
