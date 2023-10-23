import com.sun.javacard.apduio.*;

public class Main {
    public static void main(String[] args) {
        byte[] pin = new byte[4];
        System.out.print("Entrer le code PIN de la carte: ");
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
        }
    }
}
