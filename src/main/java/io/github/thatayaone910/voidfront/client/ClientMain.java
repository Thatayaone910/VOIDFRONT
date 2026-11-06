package io.github.thatayaone910.voidfront.client;

import com.fasterxml.jackson.databind.*;
import io.github.thatayaone910.voidfront.protocol.Requests;

import java.io.EOFException;
import java.io.IOException;
import java.util.Scanner;


public class ClientMain {
    private static Client client;
    static boolean isDeployed = false;
    private static String callsign = "";

    public static void printState(JsonNode response) {
        if (response.get("result").asText().equals("ERROR")) {
            System.err.println(response.get("data").get("message").asText());
            return;
        }

        System.out.println("------------------------------");
        JsonNode state = response.get("state");

        System.out.println("Position: [" + state.get("position").get(0).asInt() + "," +
                state.get("position").get(1).asInt() + "]");
        System.out.println("Heading: " + state.get("heading").asText());
        System.out.println("Shields: " + state.get("shields").asText() + "/1");
        System.out.println("Ammunition: " + state.get("shots").asText() + "/5");
        System.out.println("Status: " + state.get("status").asText());
        System.out.println("Kills: " + state.get("killCount").asInt());
        System.out.println("------------------------------");
    }

    public static void printHeading(JsonNode response) {
        if (response.get("result").asText().equals("ERROR")) {
            System.err.println(response.get("data").get("message").asText());
            return;
        }
        System.out.println(response.toPrettyString());
    }

    public static void printLook(JsonNode response) {
        if (response.get("result").asText().equals("ERROR")) {
            System.err.println(response.get("data").get("message").asText());
            return;
        }
        System.out.println("Result: " + response.get("result").asText());
        JsonNode objects = response.get("data").get("objects");

        if (objects.isEmpty()) {
            System.out.println("Nothing within range");
            return;
        }
        for (JsonNode object : objects) {
            System.out.println("\nContact: " + object.get("type").asText());
            System.out.println("Heading: " + object.get("heading").asText());
            System.out.println("Distance: " + object.get("distance").asInt());
        }
    }

    static void printFire(JsonNode response) {
        if (response.get("result").asText().equals("ERROR")) {
            System.err.println(response.get("data").get("message").asText());
            return;
        }
        System.out.println("Result: " + response.get("data").get("message").asText());

        //if hit
        if (response.get("data").get("message").asText().equals("Hit")) {
            System.out.println("Hit: " + response.get("data").get("callsign").asText());
            System.out.println("Distance: " + response.get("data").get("distance").asInt());
            System.out.println("Hit starship's state: ");

            JsonNode targetState = response.get("data").get("state");

            System.out.println("Position: [" + targetState.get("position").get(0).asInt() + "," +
                    targetState.get("position").get(1).asInt() + "]");
            System.out.println("Heading: " + targetState.get("heading").asText());
            System.out.println("Shields: " + targetState.get("shields").asText() + "/1");
            System.out.println("Shots: " + targetState.get("shots").asText() + "/5");
            System.out.println("Status: " + targetState.get("status").asText());

            System.out.println("Kills: " + response.get("state").get("killCount").asInt());
        }
        //if missed
        if (response.has("state")) {
            JsonNode state = response.get("state");
            System.out.println("Shots: " + state.get("shots").asText() + "/5");
        }
        System.out.println("------------------------------");
}



    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String command, shipClass, argument = "";
        String[] commandArgs;

        System.out.println("\n===============| Welcome to VOIDFRONT |=============== " +
                "\nPLAYER COMMANDS:" +
                "\ndeploy       - deploy your starship into the sector" +
                "\nthrust <n>  - move thrust n steps" +
                "\nreverse <n>  - reverse n steps" +
                "\nrotate <left/right> - rotate your starship" +
                "\nscan         - scan around" +
                "\nstate        - get your starship's state" +
                "\nheading  - get your starship's heading" +
                "\nfire         - fire your weapon" +
                "\nrearm       - rearm your weapon" +
                "\nrepair       - repair your shields" +
                "\nquit         - disconnect from the game");
        if(args.length<2){
            System.out.println("Missing server details"
                    +"\nRun: mvn exec:java -Dexec.mainClass=\"io.github.thatayaone910.voidfront.client.ClientMain\" -Dexec.args=\"<SERVER_IP_ADDRESS> <PORT>\"");
            System.exit(1);
            return;
        }
        try {
            client = new Client(args[0], Integer.parseInt(args[1]));
            //connect client
            client.connect();
        } catch (Exception e) {
            System.err.println("Could not connect to game server. Exiting program...");
            System.exit(1);
            return;
        }

        try {
            // 3. Loop to send messages from terminal to server
            while (true) {
                if (isDeployed) {
                    System.out.print("\n" + callsign.toUpperCase() + "'s command> ");
                } else {
                    System.out.println("\nDeploy here >");
                }

                String commandInput = scanner.nextLine().toLowerCase().trim();

                if (commandInput.isEmpty()) continue;

                commandArgs = commandInput.split("\\s+");
                command = commandArgs[0].toLowerCase();

            if(commandArgs.length==2){
                argument= commandArgs[1];
            }
            if((!command.equals("deploy")  && !command.equals("help")) && !isDeployed){
                System.err.println("## PLEASE DEPLOY INTO THE SECTOR FIRST!\n");
                continue;
            }
            if(command.equals("deploy") && isDeployed ){
                System.err.println("## YOUR STARSHIP IS ALREADY DEPLOYED!\n");
                continue;}

                switch (command) {
                    case "deploy" -> {
                        if (commandArgs.length != 3) {
                            System.err.println("Incorrect deploy command. " +
                                    "Usage: deploy <ship class> <callsign>");
                            continue;
                        }
                        shipClass = commandArgs[1].toLowerCase();
                        if(!shipClass.equals("sniper")){
                            System.err.println("Ship class must be sniper");
                            continue;
                        }
                        callsign = commandArgs[2].toUpperCase();

                        client.sendMessage(Requests.deployRequest(callsign, shipClass));
                        JsonNode response = client.receiveMessage();

                        if (response.get("result").asText().equals("OK")) {
                            isDeployed = true;
//                            System.out.println(callsign + " deployed successfully!");
                            printState(response);
                            continue;
                        } else {
                            System.err.println(response.get("result").asText() + ": " + response.get("data").get("message").asText());
                            System.err.println("Perhaps try a different name?");
                            continue;
                        }
                    }
                    case "state" -> {
                        client.sendMessage(Requests.stateRequest(callsign));
                        printState(client.receiveMessage());
//                        continue;
                    }
                    case "scan" -> {
                        client.sendMessage(Requests.scanRequest(callsign));
                        JsonNode response= client.receiveMessage();
                        printLook(response);
//                        continue;
                    }
                    case "heading" -> {
                        client.sendMessage(Requests.headingRequest(callsign));
                        JsonNode response= client.receiveMessage();
                        printHeading(response);
                        continue;
                    }
                    case "quit" -> {
                        client.sendMessage(Requests.quitRequest(callsign));
                        JsonNode quitResponse = client.receiveMessage();
                        printState(quitResponse);

                        if (quitResponse.get("result").asText().equals("OK")){
                            client.disconnect();
                            System.out.println("Player disconnected");
                            System.exit(0);
                            return;
                        }
                    }
                    case "rotate" -> {
                        if (argument.isEmpty() || commandArgs.length == 1) {
                            System.err.println("Usage: rotate <left/right>");
                            continue;
                        }
                        if (argument.equalsIgnoreCase("left") ||
                                argument.equalsIgnoreCase("right")) {
                            client.sendMessage(Requests.rotateRequest(callsign, argument));
                            JsonNode response= client.receiveMessage();
                            printHeading(response);
                            continue;

                        } else {
                            System.err.println("Invalid heading parsed. \nUsage: rotate <left/right>");
                            continue;
                        }
                    }
                    case "thrust" -> {
                        if (argument.isEmpty() || commandArgs.length == 1) {
                            System.err.println("Usage: thrust <num. of steps>");
                            continue;
                        }
                        try {
                            int steps = Integer.parseInt(argument);
                            if (steps > 0) {
                                client.sendMessage(Requests.thrustRequest(
                                        callsign, Integer.parseInt(argument)));
                                JsonNode response= client.receiveMessage();
                                printState(response);
                                continue;
                            } else {
                                System.err.println("Number of steps cannot be smaller than 1");
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid steps requested");
                        }
                    }
                    case "reverse" -> {
                        if (argument.isEmpty() || commandArgs.length == 1) {
                            System.err.println("Usage: reverse <num. of steps>");
                            continue;
                        }
                        try {
                            int steps = Integer.parseInt(argument);
                            if (steps > 0) {
                                client.sendMessage(Requests.reverseRequest(
                                        callsign, Integer.parseInt(argument)));
                                JsonNode response= client.receiveMessage();
                                printState(response);
                                continue;
                            } else {
                                System.err.println("Number of steps cannot be smaller than 1");
                                continue;
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid steps requested");
                        }
                    }
                    case "fire" -> {
                        client.sendMessage(Requests.fireRequest(callsign));
                        JsonNode response= client.receiveMessage();
                        printFire(response);
                        continue;
                    }
                    case "rearm" -> {
                        client.sendMessage(Requests.rearmRequest(callsign));

                        JsonNode response= client.receiveMessage();
                        printState(response);
                        continue;
                    }
                    case "repair" -> {
                        client.sendMessage(Requests.repairRequest(callsign));
                        JsonNode response= client.receiveMessage();
                        printState(response);
                        continue;
                    }
                    case "help" -> {
                        System.out.println("Available commands:");
                        System.out.println("  deploy       - deploy your starship into the sector");
                        System.out.println("  thrust <n>  - move thrust n steps");
                        System.out.println("  reverse <n>  - reverse n steps");
                        System.out.println("  rotate <left/right> - rotate your starship");
                        System.out.println("  scan         - scan around");
                        System.out.println("  state        - get your starship's state");
                        System.out.println("  heading  - get your starship's heading");
                        System.out.println("  fire         - fire your weapon");
                        System.out.println("  rearm       - rearm your weapon");
                        System.out.println("  repair       - repair your shields");
                        System.out.println("  quit         - disconnect from the game");
                        continue;
                    }
                    default -> {
                        System.out.println("Unsupported command");
                        continue;
                    }
                }
            }
        } catch (EOFException eof) {
            if (!isDeployed) {
                System.out.println("Exiting game...");
                System.exit(1);
            }

            client.sendMessage(Requests.quitRequest(callsign));
            JsonNode quitResponse = client.receiveMessage();

            if (quitResponse.isNull() || quitResponse.has("null")) {
                try{
                client.disconnect();
                System.out.println("Game ended unexpectedly :( . Exiting...");
                System.exit(1);

            }catch(Exception ignored){}
                System.exit(1);}
        }
    }
}
