//import com.sun.javacard.apduio.*;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.TerminalFactory;
import java.util.Arrays;

public class Main {
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
            // Connect card
            System.out.println(Arrays.toString(TerminalFactory.getDefault().terminals().list().get(0).connect("*").getBasicChannel().transmit(new CommandAPDU(0x10, 0x40, 0X00, 0X00)).getData()));
        } catch (CardException e) {
            throw new RuntimeException(e);
        }

    }
}
