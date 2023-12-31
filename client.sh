#!/bin/bash
source javacard-env

GREEN="\e[32m"
CYAN="\e[36m"
MAGENTA="\e[93m"
BOLDGREEN="\e[4;32m"
RED="\e[31m"
ENDCOLOR="\e[0m"
UNDERLINED="\e[4;97m"
PREFIX="${BOLDGREEN}Brita >${ENDCOLOR}"


function compile(){
	javac -source 1.2 -target 1.1 -g -cp "$JC_HOME_TOOLS/bin/api.jar" applet/src/applet/Main.java
	java -classpath "/home/kali/oracle_javacard_sdks/jc211_kit/bin/converter.jar:/home/kali/oracle_javacard_sdks/jc211_kit/javacard-rsa/applet/src/" com.sun.javacard.converter.Converter -verbose -exportpath "$JC_HOME_TOOLS/api_export_files:Main" -classdir applet/src -applet 0xa0:0x0:0x0:0x0:0x62:0x3:0x1:0xc:0x6:0x1:0x2 applet.Main applet 0x0a:0x0:0x0:0x0:0x62:0x3:0x1:0xc:0x6:0x1 1.0
}

function upload(){
	gpshell delete_cap
	gpshell install_cap
	gpshell get_status
}

sendPIN ()
{
    read pin
    pin=`echo $pin | sed 's/./& /g'`
    pin=$(printf "%02d %02d %02d %02d" $pin)
    sed "s/PIN/${pin}/" script.scriptor.tpl > script.scriptor
}

function getkey(){
	mkdir -p key
	keybytes=$(echo "$1" | grep -A5 "> 10 03" | tail -n5 | sed 's/< //' | sed 's/ //g' | sed ':a;N;$!ba;s/\n//g' | tail -c+6 | head -c-23)
	exponent=$(echo "$keybytes" | head -c+5)
	pubkey=$(echo "$keybytes" | tail -c+10)
	sed -e "s/PUBKEY/0x$pubkey/" -e "s/EXPONENT/0x$exponent/" template.asn1 > key/def.asn1
	openssl asn1parse -genconf key/def.asn1 -out key/pubkey.der -noout
	openssl rsa -in key/pubkey.der -inform der -pubin -out key/pubkey.pem
	openssl rsa -pubin -inform PEM -text -noout -in key/pubkey.pem
}

function getsig(){
	mkdir -p sig
	echo "$1" | grep -A7 "10 04" | tail -n+6 | sed -e 's/< //' -e 's/ //g' | sed ':a;N;$!ba;s/\n//g' | xxd -r -p > sig/signature.bin
    echo -e "${PREFIX} Size of the signature: $(ls -l sig/signature.bin | awk '{print $5}')bytes"
}

function getdata(){
	mkdir -p sig
	echo "$1" | grep -m1 "10 04" | tail -c+16 | sed 's/ //g' | xxd -r -p > sig/data.bin
	echo -en "${PREFIX} "
	xxd -g1 sig/data.bin | sed 's/00000000/Message sent to be signed/'
}

function verify(){
	echo -en "${PREFIX} Enter card PIN (tips. 2603): "

	while
		sendPIN
		appletout="$(scriptor script.scriptor 2>/dev/null)"
        echo "$appletout"
    	[[ "$appletout" == *"69 82"* ]] && echo -en "${PREFIX} ${MAGENTA}Wrong PIN! ${ENDCOLOR}Enter again: "
	do true; done


	getkey "$appletout"
	getsig "$appletout"
	getdata "$appletout"
	openssl dgst -sha1 -verify key/pubkey.pem -signature sig/signature.bin sig/data.bin &> /dev/null
    if [[ $? -ne "0" ]]; then
        echo -e "${PREFIX} ${RED}ERROR: ${ENDCOLOR}The signature is not correct."
    else
        echo -e "${PREFIX} ${GREEN}SUCCESS: ${ENDCOLOR}The signature is OK!"
    fi
}

start ()
{
echo -e "${CYAN}╔════════════════════════════════════╗"
echo -e "║                                    ║"
echo -e "║${MAGENTA}        Welcome in BritaCard ${CYAN}       ║"
echo -e "║                                    ║"
echo -e "╚════════════════════════════════════╝${ENDCOLOR}"
command="hello"
}

help ()
{
    echo -e "${PREFIX} How to interact with the JavaCard:"
    echo -e "> ${UNDERLINED}check${ENDCOLOR} check if the JavaCard is connected."
    echo -e "> ${UNDERLINED}compile${ENDCOLOR} compile the applet."
    echo -e "> ${UNDERLINED}upload${ENDCOLOR} upload the applet to the card."
    echo -e "> ${UNDERLINED}cu${ENDCOLOR} compile and upload the applet to the card."
    echo -e "> ${UNDERLINED}sendN${ENDCOLOR} function which asks the applet to sign some data and verifies it with the public key of the JavaCard."
}

start

while [ -n "$command" ]; do
    echo -en "${PREFIX} Enter a command: "
	read  command

	case "$command" in
		compile)
			compile
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
		sendN|v)
			verify
			;;

		"")
		    echo -n
			;;

		*)
            help
			;;
	esac

done