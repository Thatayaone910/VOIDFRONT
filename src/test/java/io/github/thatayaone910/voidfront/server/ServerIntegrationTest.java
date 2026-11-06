package io.github.thatayaone910.voidfront.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ServerIntegrationTest {

    @Test
    void connectionsShareSectorAndServerClosesCleanly() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }

        Server server = new Server(port, "sector.properties");
        assertEquals(29, server.getSector().getRight());
        assertEquals(29, server.getSector().getTop());
        AtomicReference<Throwable> serverFailure = new AtomicReference<>();
        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Throwable failure) {
                serverFailure.set(failure);
            }
        });
        serverThread.start();

        try (Socket firstClient = connect(port);
             Socket secondClient = connect(port)) {
            firstClient.setSoTimeout(2000);
            secondClient.setSoTimeout(2000);
            JsonNode invalidRequest = send(firstClient,
                    "{\"callsign\":\"ALPHA\",\"command\":\"rotate\",\"arguments\":[\"north\"]}");
            assertEquals("ERROR", invalidRequest.get("result").asText());
            JsonNode unexpectedArguments = send(firstClient,
                    "{\"callsign\":\"ALPHA\",\"command\":\"state\",\"arguments\":[\"unexpected\"]}");
            assertEquals("ERROR", unexpectedArguments.get("result").asText());
            assertEquals("Could not parse arguments",
                    unexpectedArguments.get("data").get("message").asText());
            assertEquals("OK", deploy(firstClient, "ALPHA"));
            assertEquals("ERROR", deploy(secondClient, "ALPHA"));
        } finally {
            server.close();
            serverThread.join(2000);
        }

        assertFalse(serverThread.isAlive(), "Server thread should stop after close()");
        assertNull(serverFailure.get(), "Server should stop without an uncaught failure");
    }

    private Socket connect(int port) throws Exception {
        long deadline = System.nanoTime() + 2_000_000_000L;
        IOException lastFailure = null;
        while (System.nanoTime() < deadline) {
            Socket socket = new Socket();
            try {
                socket.connect(new java.net.InetSocketAddress("127.0.0.1", port), 200);
                return socket;
            } catch (SocketTimeoutException | java.net.ConnectException e) {
                lastFailure = e;
                socket.close();
                Thread.sleep(10);
            }
        }
        throw new IOException("Server did not accept connections before the timeout.", lastFailure);
    }

    private String deploy(Socket socket, String callsign) throws IOException {
        JsonNode response = send(socket, "{\"callsign\":\"" + callsign
                + "\",\"command\":\"deploy\",\"arguments\":[\"sniper\",1,5]}");
        return response.get("result").asText();
    }

    private JsonNode send(Socket socket, String request) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        DataInputStream input = new DataInputStream(socket.getInputStream());
        output.writeUTF(request);
        output.flush();
        return mapper.readTree(input.readUTF());
    }
}
