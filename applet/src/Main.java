import javacard.framework.*;
import javacard.security.KeyBuilder;
import javacard.security.KeyPair;
import javacard.security.RSAPublicKey;
import javacard.security.RSAPrivateKey;

public class Main extends Applet {
    private static final byte KEYPAIR_ALGO = KeyPair.ALG_RSA;
    private static final short KEYPAIR_LENGTH = KeyBuilder.LENGTH_RSA_512;

    private static final byte PIN_MAX_RETRY = 3;
    private static final byte PIN_LENGTH = 4;

    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;

    private static OwnerPIN ownerPIN;
    private static byte[] pinValue = {2, 6, 0, 3};

    private static final byte[] RESPONSE_KAY = {0x0, 0x6, 0x9};
    private static final byte[] RESPONSE_NOK = {0x4, 0x2, 0x0};

    private static final byte MY_CLA = 0x10;

    private static final byte INS_HELLO = 0x40;
    private static final byte INS_LOGIN = 0x41;
    private static final byte INS_GETPUBKEY = 0x42;
    private static final byte INS_SIGNMSG = 0x43;

    private final static byte[] hello = {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x72, 0x6f, 0x62, 0x65, 0x72, 0x74};

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
        byte[] adpuBuffer = apdu.getBuffer();

        if (adpuBuffer[ISO7816.OFFSET_CLA] != MY_CLA){
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        switch (adpuBuffer[ISO7816.OFFSET_INS]){
            case INS_HELLO:
                sendADPU(apdu, hello);
                break;

            case INS_LOGIN:
                if (!isLoggedIn()){
                    if (adpuBuffer[ISO7816.OFFSET_LC] != PIN_LENGTH){
                        if (ownerPIN.check(adpuBuffer, ISO7816.OFFSET_CDATA, PIN_LENGTH)){
                            sendADPU(apdu, RESPONSE_KAY);
                        }

                    } else {
                        sendADPU(apdu, RESPONSE_NOK);
                    }

                } else {
                    sendADPU(apdu, RESPONSE_KAY);
                }

                break;

            case INS_GETPUBKEY:
                isLoggedIn();



                break;

            case INS_SIGNMSG:
                isLoggedIn();



                break;

            default:
                // Good practice: If you don't know the INStruction, say so:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);

        }
    }

    private static void sendADPU(APDU apdu, byte[] buf){
        Util.arrayCopy(buf, (byte) 0, buf, ISO7816.OFFSET_CDATA, (byte) buf.length);
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (byte) buf.length);
    }
}