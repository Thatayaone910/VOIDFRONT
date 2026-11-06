package io.github.thatayaone910.voidfront.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.thatayaone910.voidfront.service.*;
import io.github.thatayaone910.voidfront.sector.Sector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Locale;
import java.util.Objects;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Sector sector;
    private DataInputStream in;
    private DataOutputStream out;
    private static final ObjectMapper mapper = new ObjectMapper();
    private final Runnable onDisconnect;

    public ClientHandler(Socket socket, Sector sector) throws IOException {
        this(socket, sector, () -> {});
    }

    public ClientHandler(Socket socket, Sector sector, Runnable onDisconnect) throws IOException {
        this.socket = socket;
        this.sector = sector;
        this.onDisconnect = Objects.requireNonNull(onDisconnect, "onDisconnect");
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            CombatService combatService = new CombatService( sector );

            while (!socket.isClosed()) {
                String strCommand;
                try {
                    strCommand = in.readUTF();
                } catch (EOFException e) {
                    break;
                }

                JsonNode request;
                try {
                    request = mapper.readTree(strCommand);
                } catch (JsonProcessingException e) {
                    out.writeUTF(unsupportedArgumentsResponse());
                    continue;
                }

                if (!hasValidEnvelope(request)) {
                    out.writeUTF(unsupportedArgumentsResponse());
                    continue;
                }

                String callsign = request.get("callsign").asText();
                String command = request.get("command").asText();
                JsonNode arguments = request.get("arguments");
                if (!hasValidArguments(command, arguments)) {
                    out.writeUTF(unsupportedArgumentsResponse());
                    continue;
                }

                System.out.println("# Client[" + callsign + "]: " + strCommand);
                switch (command) {
                            case "deploy":
                                DeployService toLaunch = new DeployService(sector);
                                Object DeployResponse = toLaunch.deployStarship(callsign,arguments);
                                out.writeUTF(mapper.writeValueAsString(DeployResponse));
                                break;

                            case "state":
                                //create a QueryService for the state query
                                QueryService stateQuery = new QueryService(sector);
                                // get the response
                                Object stateResponse = stateQuery.getState(callsign);
                                out.writeUTF(mapper.writeValueAsString(stateResponse)); //write reponse to client
                                break;

                            case "scan":
                                QueryService scanQuery = new QueryService(sector);
                                Object scanResponse = scanQuery.getScan(callsign);

                                if (scanResponse==null){
                                    out.writeUTF(mapper.writeValueAsString(QueryService.badMessage()));
                                    break;
                                }

                                //convert scan response into string to be built for response
                                String jsonLook = mapper.writeValueAsString(scanResponse);
                                //send json
                                out.writeUTF(jsonLook);
                                break;

                            case "thrust", "reverse":
                                NavigationService moveService = new NavigationService(sector);
                                Object moveResponse = moveService.move(callsign,arguments.get(0).asInt(), command);
                                if (moveResponse== null){
                                    out.writeUTF(mapper.writeValueAsString(QueryService.badMessage()));
                                    break;

                                }
                                String jsonMove = mapper.writeValueAsString(moveResponse);
                                out.writeUTF(jsonMove);
                                break;

                            case "rotate":
                                NavigationService rotateService = new NavigationService(sector);
                                Object rotateResponse = rotateService.getTurn(
                                        callsign, arguments.get(0).asText().toLowerCase(Locale.ROOT));

                                if (rotateResponse == null) {
                                    out.writeUTF(mapper.writeValueAsString(QueryService.badMessage()));
                                    break;
                                }
                                String jsonTurn = mapper.writeValueAsString(rotateResponse);
                                out.writeUTF(jsonTurn);
                                break;


                            case "repair":
                                String repairResult = combatService.repair(callsign);
                                out.writeUTF(repairResult != null ? repairResult : unsupportedArgumentsResponse());
                                break;

                                case "rearm":
                                String rearmResult = combatService.rearm(callsign);
                                out.writeUTF(rearmResult != null ? rearmResult : unsupportedArgumentsResponse());
                                break;

                            case "fire":
                                String result = combatService.fire( callsign );

                                out.writeUTF( result != null ? result : mapper.writeValueAsString(QueryService.badMessage()));;

                                break;

                            case "heading":
                                //create a QueryService for the state query
                                QueryService headingQuery = new QueryService(sector);
                                // get the response
                                Object headingResponse = headingQuery.getHeading(callsign);
                                out.writeUTF(mapper.writeValueAsString(headingResponse)); //write reponse to client
                                break;

                            case "quit":
                                QuitService quitRequest = new QuitService(sector);
                                Object quitResponse =quitRequest.getQuit(callsign);
                                out.writeUTF(mapper.writeValueAsString(quitResponse)); //write response to client
                                return;

                            default:
                                // Handle Unsupported command
                                out.writeUTF(unsupportedCommandResponse());
                                break;
                }
            }
        } catch (IOException e) {
            if (!socket.isClosed()) {
                System.err.println("Client handler I/O failure: " + e.getMessage());
            }
        } finally {
            close();
            onDisconnect.run();
        }
    }

    public void close() {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Unable to close client connection: " + e.getMessage());
        }
    }

    private boolean hasValidEnvelope(JsonNode request) {
        return request != null
                && request.isObject()
                && request.hasNonNull("callsign")
                && request.get("callsign").isTextual()
                && request.hasNonNull("command")
                && request.get("command").isTextual()
                && request.hasNonNull("arguments")
                && request.get("arguments").isArray();
    }

    private boolean hasValidArguments(String command, JsonNode arguments) {
        return switch (command) {
            case "deploy" -> arguments.size() == 3
                    && arguments.get(0).isTextual()
                    && arguments.get(1).isIntegralNumber()
                    && arguments.get(1).canConvertToInt()
                    && arguments.get(1).asInt() >= 0
                    && arguments.get(2).isIntegralNumber()
                    && arguments.get(2).canConvertToInt()
                    && arguments.get(2).asInt() >= 0;
            case "thrust", "reverse" -> arguments.size() == 1
                    && arguments.get(0).isIntegralNumber()
                    && arguments.get(0).canConvertToInt()
                    && arguments.get(0).asInt() > 0;
            case "rotate" -> arguments.size() == 1
                    && arguments.get(0).isTextual()
                    && ("left".equalsIgnoreCase(arguments.get(0).asText())
                    || "right".equalsIgnoreCase(arguments.get(0).asText()));
            case "state", "scan", "heading", "fire", "rearm", "repair", "quit" -> arguments.isEmpty();
            default -> true;
        };
    }

    /**
     * JSON formatted string for all unsupported commands.
     *
     * @return {String} Unsupported command response as JSON
     *
     */

    private String unsupportedCommandResponse() {
        return """
                {
                  "result": "ERROR",
                  "data": {
                    "message": "Unsupported command"
                  }
                }""";
    }
    /**
     * JSON formatted string for arguments that cannot be parsed.
     *
     * @return {String} Unsupported arguments response as JSON
     *
     */
    private String unsupportedArgumentsResponse() {
        return """
                {
                   "result": "ERROR",
                   "data": {
                     "message": "Could not parse arguments"
                   }
                }""";
    }
}
