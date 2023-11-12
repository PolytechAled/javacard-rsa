package applet;

import javacard.framework.*;
import javacard.security.*;

public class Main extends Applet {
    private static final byte KEYPAIR_ALGO = KeyPair.ALG_RSA;
    private static final short KEYPAIR_LENGTH = KeyBuilder.LENGTH_RSA_512;

    private static final byte PIN_MAX_RETRY = 3;
    private static final byte PIN_LENGTH = 4;

    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;

    private static OwnerPIN ownerPIN;
    private static final byte[] pinValue = {2, 6, 0, 3};

    private static final byte[] RESPONSE_KAY = {0x0, 0x6, 0x9};
    private static final byte[] RESPONSE_NOK = {0x4, 0x2, 0x0};

    private static final byte MY_CLA = 0x10;

    private static final byte INS_HELLO = 0x01;
    private static final byte INS_LOGIN = 0x02;
    private static final byte INS_GETPUBKEY = 0x03;
    private static final byte INS_SIGNMSG = 0x04;

    private final static byte[] hello = {0x64, 0x65, 0x61, 0x64, 0x62, 0x65, 0x65, 0x66};

    private static boolean isLoggedIn(){
        return ownerPIN.isValidated();
    }

    public static void install(byte[] buffer, short offset, byte length){
        // GP-compliant JavaCard applet registration
        new Main().register();

        ownerPIN = new OwnerPIN(PIN_MAX_RETRY, PIN_LENGTH);

        ownerPIN.update(pinValue, (short) 0, PIN_LENGTH);

        KeyPair kpg = new KeyPair(KEYPAIR_ALGO, KEYPAIR_LENGTH);

        kpg.genKeyPair();

        publicKey = (RSAPublicKey) kpg.getPublic();
        privateKey = (RSAPrivateKey) kpg.getPrivate();
    }

    public void process(APDU apdu){
        // Good practice: Return 9000 on SELECT
        if (selectingApplet()){
            return;
        }

        // Read buffer
        byte[] apduBuffer = apdu.getBuffer();

        // Applet selection
        if (apduBuffer[ISO7816.OFFSET_CLA] == (byte) 0x00 && apduBuffer[ISO7816.OFFSET_INS] == (byte) 0xA4){
            return;
        }

        if (apduBuffer[ISO7816.OFFSET_CLA] != MY_CLA){
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
            return;
        }

        // ensure the entirety of the buffer is available for the processing
        short bytesLeft = Util.makeShort((byte) 0x00, apduBuffer[ISO7816.OFFSET_LC]);

        if (bytesLeft != apdu.setIncomingAndReceive()){
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        switch (apduBuffer[ISO7816.OFFSET_INS]){
            case INS_HELLO:
                sendAPDU(apdu, hello);

                return;

            case INS_LOGIN:
                if (!isLoggedIn()){
                    if (apduBuffer[ISO7816.OFFSET_LC] == PIN_LENGTH){
                        if (ownerPIN.check(apduBuffer, ISO7816.OFFSET_CDATA, PIN_LENGTH)){
                            sendAPDU(apdu, RESPONSE_KAY);
                            return;
                        }
                    }

                    ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
                    sendAPDU(apdu, RESPONSE_NOK);

                } else {
                    sendAPDU(apdu, RESPONSE_KAY);
                }

                return;

            case INS_GETPUBKEY:
                if (!isLoggedIn()){
                    ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
                    sendAPDU(apdu, RESPONSE_NOK);
                    return;
                }

                byte[] buffer = apdu.getBuffer();
                short offset = ISO7816.OFFSET_CDATA;
                short expLen = publicKey.getExponent(buffer, (short) (offset + 2));
                Util.setShort(buffer, offset, expLen);
                short modLen = publicKey.getModulus(buffer, (short) (offset + 4 + expLen));
                Util.setShort(buffer, (short) (offset + 2 + expLen), modLen);
                apdu.setOutgoingAndSend(offset, (short) (4 + expLen + modLen));

                return;

            case INS_SIGNMSG:
                if (!isLoggedIn()){
                    ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
                    sendAPDU(apdu, RESPONSE_NOK);
                    return;
                }

                Signature sig = Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1, false);

                sig.init(privateKey, Signature.MODE_SIGN);

                byte[] textToSign = new byte[apduBuffer[ISO7816.OFFSET_LC]];
                Util.arrayCopy(apduBuffer, ISO7816.OFFSET_CDATA, textToSign, (short) 0, (short) textToSign.length);
                byte[] signature = new byte[sig.getLength()];
                short sigLen = sig.sign(textToSign, (short) 0, (short) textToSign.length, signature, (short) 0);

                sendAPDU(apdu, signature);

                return;

            default:
                // Good practice: If you don't know the INStruction, say so:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);

        }
    }

    private static void sendAPDU(APDU apdu, byte[] data){
//        byte[] buf = apdu.getBuffer();
//        Util.arrayCopy(data, (short) 0, buf, ISO7816.OFFSET_CDATA, (short) buf.length);
//        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (short) data.length);

        byte[] apduBuffer = apdu.getBuffer();
        Util.arrayCopy(data, (short) 0, apduBuffer, ISO7816.OFFSET_CDATA,
                (short) data.length);
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (short) data.length);
    }
}