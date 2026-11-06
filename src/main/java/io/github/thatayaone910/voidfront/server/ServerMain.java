package io.github.thatayaone910.voidfront.server;

import io.github.thatayaone910.voidfront.sector.Starship;
import io.github.thatayaone910.voidfront.sector.hazard.SpaceHazard;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;

public class ServerMain {

    private static final int PORT = 5000;
    private static final String CONFIG = "sector.properties";

    public static void main(String[] args) {
        Server server = new Server(PORT, CONFIG);

        // Start server thread
        Thread serverThread = new Thread(server::start);
        serverThread.setDaemon(true);
        serverThread.start();

        // Admin console
        runAdminConsole(server);
    }

    private static void runAdminConsole(Server server) {
        System.out.println("Admin console ready. Commands: dump | starships | quit");

        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
            String command;

            while ((command = console.readLine()) != null) {
                switch (command.strip().toLowerCase()) {
                    case "dump":
                        System.out.println("\n~~~ SECTOR DUMP ~~~");
                        for (Object obj : server.getSector().dump()) {
                            if (obj instanceof Starship starship) {
                                System.out.println("Starship: " + starship.getCallsign());
                            } else if (obj instanceof SpaceHazard hazard) {
                                System.out.println(
                                        hazard.getClass().getSimpleName() +
                                                " at (" +
                                                hazard.getPosition().getX() + ", " +
                                                hazard.getPosition().getY() + ")"
                                );
                            }
                        }
                        System.out.println("~~~~~~~~~~~\n");
                        break;

                    case "starships":
                        List<Starship> starships = server.getSector().getAllStarships();
                        if (starships.isEmpty()) {
                            System.out.println("No starships currently in the sector.");
                            break;
                        }
                        System.out.println("\n~~~ STARSHIPS ~~~");
                        for (Starship starship : starships) {
                            System.out.println("Starship: " + starship.getCallsign());
                            System.out.println("  Ship class: " + starship.getShipClass());

                            System.out.println("  Position: (" +
                                    starship.getPosition().getX() + ", " +
                                    starship.getPosition().getY() + ")");

                            System.out.println("  Heading: " + starship.getHeading());

                            System.out.println("  Shields: " +
                                    starship.getShieldStrength() + "/" +
                                    starship.getMaxShieldStrength());

                            System.out.println("  Shots: " +
                                    starship.getShots() + "/" +
                                    starship.getMaxShots());

                            System.out.println("  Status: " + starship.getStatus());
                        }
                        System.out.println("~~~~~~~~~~~\n");

                        break;


                    case "quit" :
                    {
                        server.close();
                        System.out.println("Server shutting down.");
                        return;
                    }
                    default : System.out.println("Unknown command: '" + command + "'. Try: dump | starships| quit");
                }
            }
        } catch (IOException e) {
            System.err.println("Console error: " + e.getMessage());
        } finally {
            server.close();
        }
    }
}