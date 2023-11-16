import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;
    private String clientUserName;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUserName = br.readLine();
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUserName + " has entered the chat");
        } catch (IOException e) {
            closeEverything(socket, br, bw);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = br.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, br, bw);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUserName.equals(clientUserName)) {
                    System.out.println(messageToSend);
                    clientHandler.bw.write(messageToSend);
                    clientHandler.bw.newLine();
                    clientHandler.bw.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, br, bw);
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUserName + " has left the chat!");
        return;
    }

    public void closeEverything(Socket s, BufferedReader br, BufferedWriter bw) {
        removeClientHandler();
        try {
            if (br != null) {
                br.close();
            }
            if (bw != null) {
                bw.close();
            }
            if (s != null) {
                s.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
