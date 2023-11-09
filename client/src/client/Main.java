package client;//import com.sun.javacard.apduio.*;

import javax.smartcardio.*;
import java.util.Arrays;
import java.util.List;


public class Main {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void main(String[] args) {
        byte[] pin = new byte[4];
        launchCMD();

        /*System.out.print("Entrer le code PIN de la carte: ");
        try{
            System.in.readNBytes(pin, 0, pin.length);
        }catch (Exception e){
            e.printStackTrace();
        }
        for (int i = 0; i < pin.length; i++){
            if(pin[i] >= '0' && pin[i] <= '9')
                pin[i] = (byte) (pin[i]-'0');
            else{
                System.out.println("Bad format");
                System.exit(1);
            }
        }*/
    }

    //200000006203010c060102
    public static void launchCMD(){
        /*Apdu apdu = new Apdu();

        apdu.command[Apdu.CLA] = 0x10;
        apdu.command[Apdu.INS] = 0x40;
        apdu.command[Apdu.P1] = 0x00;
        apdu.command[Apdu.P2] = 0x00;
        byte[] appletAID = { 0x20, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x0C, 0x06, 0x01, 0x02 };
        apdu.setDataIn(appletAID);
        cad.exchangeApdu(apdu);
        if (apdu.getStatus() != 0x9000) {
            System.out.println("Erreur lors de la sélection de l'applet");
            System.exit(1);
        }*/

        try {
            TerminalFactory terminalFactory = TerminalFactory.getInstance("PC/SC", null);

            List<CardTerminal> terminals = terminalFactory.terminals().list();

            if (!terminals.isEmpty()) {
                System.out.println("Available terminals: " + terminals);

                CardTerminal chosenTerminal = terminals.get(0);

                System.out.println("Chosen terminal: " + chosenTerminal);

                System.out.println("Connecting...");
                Card card = chosenTerminal.connect("*");

                System.out.println("Connected to card " + card);

                CardChannel channel = card.openLogicalChannel();

                                                                                      // aid
//                CommandAPDU selectAppletCommand = new CommandAPDU(0x0, 0xA4, 0x04, 0x0, new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, (byte) 0x18, 0x43, (byte) 0x4D, 0x00});
                CommandAPDU selectAppletCommand = new CommandAPDU(0x0, 0xA4, 0x04, 0x0, new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x0C, 0x06, 0x01});
                System.out.println("Sending command " + selectAppletCommand + " " + bytesToHex(selectAppletCommand.getBytes()));

                ResponseAPDU response = channel.transmit(selectAppletCommand);

                CommandAPDU helloCommand = new CommandAPDU(0x10, 0x01, 0X00, 0X00, 0x00);

                // Connect card
                System.out.println("Received: " + response + " " + Arrays.toString(response.getData()));

                response = channel.transmit(helloCommand);

                System.out.println("Sending command " + helloCommand + " " + bytesToHex(helloCommand.getBytes()));

                System.out.println("Received: " + response + " " + Arrays.toString(response.getData()));

                card.disconnect(true);
            } else {
                System.out.println("No terminal found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
