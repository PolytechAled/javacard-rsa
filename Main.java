import com.sun.javacard.apduio.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.print("Entrer le code PIN de la carte: ");
        Scanner in = new Scanner(System.in);
        String in_code = in.nextLine();
    }
}
