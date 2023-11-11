#!/bin/bash
source javacard-env

GREEN="32"
CYAN="\e[36m"
MAGENTA="\e[93m"
BOLDGREEN="\e[4;${GREEN}m"
RED="\e[31m"
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

sendPIN ()
{
    echo -en "${PREFIX} Enter card PIN (tips. 2603): "
    read pin
    pin=`echo $pin | sed 's/./& /g'`
    pin=$(printf "%02d %02d %02d %02d" $pin)
    sed "s/PIN/${pin}/" script.scriptor
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
    echo -e "${PREFIX} Size of the signature: $(ls -l sig/signature.bin | awk '{print $5}')"
}

function getdata(){
	mkdir -p sig
	echo "$1" | grep -m1 "10 03" | tail -c+7 | sed 's/ //g' | xxd -r -p > sig/data.bin
	xxd -g1 sig/data.bin
}

function verify(){
	appletout="$(sendPIN | scriptor 2>/dev/null)"
	getkey "$appletout"
	getsig "$appletout"
	getdata "$appletout"
	openssl dgst -sha1 -verify key/pubkey.pem -signature sig/signature.bin sig/data.bin &> /dev/null
    if [[ $? -ne "0" ]]; then
        echo -e "${PREFIX} ${RED}ERROR: ${ENDCOLOR}The signature is not correct."
    else
        echo -e "${PREFIX} ${GREEN}SUCCESS: ${ENDCOLOR}The signature is OK !"
    fi
}

start ()
{
echo -e "${CYAN}╔════════════════════════════════════╗"
echo -e "║                                    ║"
echo -e "║${MAGENTA}       Welcome in RoudinerCard ${CYAN}     ║"
echo -e "║                                    ║"
echo -e "╚════════════════════════════════════╝${ENDCOLOR}"
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

		"")
			;;

		*)
            		help
			;;
	esac

done
