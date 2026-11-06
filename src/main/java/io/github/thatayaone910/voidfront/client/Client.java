package io.github.thatayaone910.voidfront.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.Socket;

/**
 * Handles the network connection and JSON communication between a client and game server*/
public class Client {

    private Socket socket;
    private DataOutputStream out;
    private  DataInputStream in;
    private final ObjectMapper mapper = new ObjectMapper();
    String IP;
    int Port;

    /**
    * Initializes a new Client
     *
     * @param IP The server IP address (eg. "localhost" or "127.0.0.1")
     * @param Port The server's port to listen to client connections
     * */
    public Client(String IP, int Port) {
        this.IP = IP;
        this.Port = Port;
    }

    /**
     * Establishes a connection to the server and initializes input & output streams
     *
     * @throws IOException if the connection fails or the server is unreachable
     * */
    public void connect() throws IOException {
        this.socket = new Socket(IP, Port);
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());

    }

    /**
     * Serializes an object to JSON and sends it to the server
     *
     * @param message is the JSON request that the user sends to the server */
    public void sendMessage(Object message) throws IOException {
        String jsonString = mapper.writeValueAsString(message);
        out.writeUTF(jsonString);
        out.flush();
    }

    /**
     * Listens for & parses the next JSON response from the server
     *
     * @return A {@link JsonNode} containing the server's response
     * @throws  IOException If the connection is lost or the server is unreachable
     * */
    public JsonNode receiveMessage() throws IOException {
        String response = in.readUTF();
        return mapper.readTree(response);
    }

    /**
     * Gracefully closes the network socket and associated streams
     *
     * @throws IOException If an error occurs while closing the connection
     * */
    public void disconnect() throws IOException{
        if (socket != null && !socket.isClosed()) {
            if (in !=null) in.close();
            if (out !=null) out.close();
            if (socket !=null)socket.close();
        }
    }

}
