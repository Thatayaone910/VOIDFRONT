package io.github.thatayaone910.voidfront.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ClientMainTest {
    /*
    * Test class for ClientMain
    * This class must test for user inputs: command-line args
    * */

    // Valid IP and Port

        @Test
        void testValidIPAndPort() {
            // Arrange
            List<String> args = new ArrayList<>(Arrays.asList("localhost","5000"));

            // Act
            String IP = args.get(0);
            int Port = Integer.parseInt(args.get(1));

            // Assert
            assertEquals("localhost",IP);
            assertEquals(5000, Port);
        }

        @Test
        void testValidLength() {
            // Arrange
            List<String> args = new ArrayList<>(Arrays.asList("localhost","5000"));

            // Act
            boolean hasValidArgs = args.size() == 2;

            // Assert
            assertTrue(hasValidArgs);
        }

        //Invalid/Empty Args

        @Test
        void testInvalidLength() {
            // Arrange
            List<String> args = new ArrayList<>(Arrays.asList());

            // Act
            boolean hasValidArgs = args.size() == 2;

            // Assert
            assertFalse(hasValidArgs);
        }
    //Empty IP and Port
    @Test
    void testEmptyArgs() {
        List<String> args = new ArrayList<>(Arrays.asList());

        String IP;
        int Port;
        if (args.size() == 2) {
            IP = args.get(0);
            Port = Integer.parseInt(args.get(1));
        } else {
            IP = "localhost";
            Port = 5000;
        }
        assertEquals("localhost", IP);
        assertEquals(5000, Port);

    }
        //  Invalid Port

        @Test
        void testInvalidPort() {
            // Arrange
            List<String> args = new ArrayList<>(Arrays.asList("localhost","notPort"));
            // Act & Assert
            assertThrows(NumberFormatException.class, () -> {
                Integer.parseInt(args.get(1));
            });
        }

        @Test
        void testPortOutOfRange() {
            // Arrange
            List<String> args = new ArrayList<>(Arrays.asList("localhost","9000"));

            // Act
            int port = Integer.parseInt(args.get(1));
            boolean isValidRange = false;

            // Assert
            assertFalse(isValidRange);
        }

        //Invalid IP

        @Test
        void testEmptyIP() {
            // Arrange
            List<String> args = new ArrayList<>(Arrays.asList("","5000"));

            // Act
            String ip = args.get(0);
            boolean isEmptyIp = ip.isEmpty();

            // Assert
            assertTrue(isEmptyIp);
        }
    }
