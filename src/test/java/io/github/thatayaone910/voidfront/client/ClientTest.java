package io.github.thatayaone910.voidfront.client;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.*;



public class ClientTest {
    /*This class tests client connections made by Client class
     * POSSIBLE TEST CASE SUGGESTIONS:
     * Test client connects successfully to a valid host & port
     * Test that client handles failed connection gracefully (no funny error messages shown in terminal)
     * Test receives responses from server correctly
     * Test handles bad repsonses correctly
     * Test disconnects socket properly
     */
    @Test
    void connectWhenServerRunning() throws IOException {
    //create test server
        ServerSocket testServer = new ServerSocket(5000);

        // create testClient
        Client testClient = new Client("localhost",5000);
        //attempt to connect clinet to server
        testClient.connect();

        assertTrue(true);
        testServer.close();
    }

    @Test
    void failConnectionWhenServerNotRunning(){
        Client testClient = new Client("localhost",5000);
        assertThrows(IOException.class, testClient::connect);
    }

    @Test
    void failConnectWhenServerIncorrect(){
        Client testClient = new Client("localhost",9000);
        assertThrows(IOException.class, testClient::connect);
    }

    @Test
    void clientDisconnect() throws IOException{
        ServerSocket testServer= new ServerSocket(5000);
        Client testClient = new Client("localhost",5000);
        testClient.connect();

        testClient.disconnect();
        assertTrue(true);

        testServer.close();
    }
}
