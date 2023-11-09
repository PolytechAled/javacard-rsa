package client;

import javax.smartcardio.*;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        try {
            // Initialize the terminal
            TerminalFactory terminalFactory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = terminalFactory.terminals().list();
            CardTerminal terminal = terminals.get(0);

            // Connect to the card
            Card card = terminal.connect("*");
            CardChannel channel = card.openLogicalChannel();

            // Send APDU
            //CommandAPDU commandAPDU = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x0C, 0x06, 0x01});
            CommandAPDU commandAPDU = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, new byte[]{(byte) 0xa0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x0c, 0x06, 0x01});
            ResponseAPDU responseAPDU = channel.transmit(commandAPDU);

            // Handle response
            byte[] responseData = responseAPDU.getData();
            int sw1 = responseAPDU.getSW1();
            int sw2 = responseAPDU.getSW2();

            // Print response
            System.out.println("Response Data: " + byteArrayToHex(responseData));
            System.out.println("SW1: " + Integer.toHexString(sw1));
            System.out.println("SW2: " + Integer.toHexString(sw2));

            CommandAPDU helloCommand = new CommandAPDU(0x11, 0x01, 0X00, 0X00, 0x00);

            ResponseAPDU helloResponseAPDU = channel.transmit(helloCommand);

            // Handle response
            byte[] helloResponseData = helloResponseAPDU.getData();
            int helloSw1 = helloResponseAPDU.getSW1();
            int helloSw2 = helloResponseAPDU.getSW2();

            // Print response
            System.out.println("Response Data: " + byteArrayToHex(helloResponseData));
            System.out.println("SW1: " + Integer.toHexString(helloSw1));
            System.out.println("SW2: " + Integer.toHexString(helloSw2));

            // Close connection
            card.disconnect(true);

        } catch (CardException e) {
            e.printStackTrace();
        }
    }

    private static String byteArrayToHex(byte[] byteArray) {
        StringBuilder result = new StringBuilder();
        for (byte b : byteArray) {
            result.append(String.format("%02X ", b));
        }
        return result.toString();
    }
}