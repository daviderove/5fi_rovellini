import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class CalcolatriceClient {
    private static final String HOST = "localhost";
    private static final int PORTA = 5010;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(HOST, PORTA);
            PrintWriter out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                true
            );
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)
        ) {
            System.out.println("Connesso al server " + HOST + ":" + PORTA);
            stampaMenu();

            while (true) {
                System.out.print("Calcolo > ");
                if (!scanner.hasNextLine()) {
                    System.out.println("\nInput terminato (EOF). Chiusura client.");
                    break;
                }

                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("Inserisci un'operazione (es. 10 + 5) oppure 'quit'.");
                    continue;
                }

                if (input.equalsIgnoreCase("quit")) {
                    out.println("QUIT");
                    out.flush();
                    String chiusura = in.readLine();
                    System.out.println(chiusura == null ? "Server disconnesso" : chiusura);
                    break;
                }

                out.println(input);
                out.flush();

                String risposta = in.readLine();
                if (risposta == null) {
                    System.out.println("Server disconnesso");
                    break;
                }
                System.out.println(risposta);
            }
        } catch (ConnectException e) {
            System.err.println("Impossibile connettersi a " + HOST + ":" + PORTA + ". Verifica che il server sia attivo.");
        } catch (IOException e) {
            System.err.println("Errore di comunicazione con il server: " + e.getMessage());
        }
    }

    private static void stampaMenu() {
        System.out.println("=== CALCOLATRICE REMOTA ===");
        System.out.println("Formato: NUMERO OPERAZIONE NUMERO");
        System.out.println("Operazioni: + - * /");
        System.out.println("Scrivi 'quit' per uscire");
    }
}
