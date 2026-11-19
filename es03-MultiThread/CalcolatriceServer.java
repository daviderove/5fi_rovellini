import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class CalcolatriceServer {
    private static final int PORTA = 5010;
    private static int operazioniRiuscite = 0;
    private static boolean shouldStopServer = false;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Server avviato sulla porta " + PORTA);

            while (!shouldStopServer) {
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();
                    System.out.println("Client connesso: " + clientSocket.getRemoteSocketAddress());
                    gestisciClient(clientSocket);
                } catch (IOException e) {
                    if (!shouldStopServer) {
                        System.err.println("Errore di I/O nella gestione del client: " + e.getMessage());
                    }
                } finally {
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        try {
                            clientSocket.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }

            System.out.println("Server arrestato correttamente.");
        } catch (IOException e) {
            System.err.println("Impossibile avviare il server sulla porta " + PORTA + ": " + e.getMessage());
        }
    }

    private static void gestisciClient(Socket clientSocket) {
        try (
            Socket sock = clientSocket;
            BufferedReader in = new BufferedReader(
                new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8)
            );
            PrintWriter out = new PrintWriter(
                new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8),
                true
            )
        ) {
            String richiesta;
            while ((richiesta = in.readLine()) != null) {
                String trimmed = richiesta.trim();

                if (trimmed.equalsIgnoreCase("QUIT")) {
                    out.println("CHIUSURA: Operazioni eseguite: " + operazioniRiuscite + ". Arrivederci!");
                    out.flush();
                    shouldStopServer = true;
                    break;
                }

                String risposta = elaboraRichiesta(trimmed);
                out.println(risposta);
                out.flush();
            }

            if (!shouldStopServer) {
                System.out.println("Client disconnesso senza QUIT. In attesa di nuove connessioni...");
            }
        } catch (IOException e) {
            System.err.println("Connessione interrotta con il client: " + e.getMessage());
        }
    }

    private static String elaboraRichiesta(String richiesta) {
        if (richiesta.isEmpty()) {
            return "ERRORE: Formato non valido. Usa: NUMERO OPERAZIONE NUMERO";
        }

        String[] parti = richiesta.split("\\s+");
        if (parti.length != 3) {
            return "ERRORE: Formato non valido. Usa: NUMERO OPERAZIONE NUMERO";
        }

        try {
            double num1 = Double.parseDouble(parti[0]);
            String operazione = parti[1];
            double num2 = Double.parseDouble(parti[2]);

            if (!Double.isFinite(num1) || !Double.isFinite(num2)) {
                return "ERRORE: Sono ammessi soltanto numeri finiti";
            }

            double risultato = calcola(num1, operazione, num2);
            if (!Double.isFinite(risultato)) {
                return "ERRORE: Il risultato non è un numero finito";
            }

            operazioniRiuscite++;
            return "RISULTATO: " + risultato;
        } catch (NumberFormatException e) {
            return "ERRORE: Formato non valido. Usa: NUMERO OPERAZIONE NUMERO";
        } catch (ArithmeticException e) {
            return "ERRORE: Divisione per zero non consentita";
        } catch (IllegalArgumentException e) {
            return "ERRORE: Operazione non supportata. Usa +, -, *, /";
        }
    }

    private static double calcola(double num1, String operazione, double num2) {
        switch (operazione) {
            case "+":
                return num1 + num2;
            case "-":
                return num1 - num2;
            case "*":
                return num1 * num2;
            case "/":
                if (num2 == 0.0) {
                    throw new ArithmeticException("Divisione per zero");
                }
                return num1 / num2;
            default:
                throw new IllegalArgumentException("Operazione non supportata");
        }
    }
}
