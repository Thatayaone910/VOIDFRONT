package io.github.thatayaone910.voidfront.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Bloom;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import org.controlsfx.control.Notifications;
import io.github.thatayaone910.voidfront.protocol.Requests;
import io.github.thatayaone910.voidfront.sector.*;
import io.github.thatayaone910.voidfront.sector.hazard.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**Controller class for the Graphical User Interface*/
public class VoidfrontController implements Initializable {

    // Network
    private Client client;
    private JsonNode response;
    // Configurations
    private final int MIN_ZOOM = 120, MAX_ZOOM = 50;
    private final int CELL_SIZE = 80;
    private final double IMAGE_SIZE = CELL_SIZE - ((double) (CELL_SIZE * 33) / 100);
    private final double LABEL_SIZE = IMAGE_SIZE - ((IMAGE_SIZE * 80) / 100);
    private final int BULLET_RANGE = 5;
    private final boolean IS_DAY = false;
    // Sector configs
    private int SECTOR_WIDTH;
    private int SECTOR_HEIGHT;
    private int SCAN_RANGE;
    private int REARMING_TIME;
    private int REPAIRING_TIME;
    // Game state
    private String myCallsign;
    private final Map<String, SectorObject> starships = new HashMap<>();


    @FXML
    private Label lblTime;
    @FXML
    private GridPane grdSector;
    @FXML
    private Label lblShots;
    @FXML
    private Label lblShield;
    @FXML
    private ScrollPane scrSectorContainer;
    @FXML
    private Label lblStatus;
    @FXML
    private Label lblKills;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Remove focus from the ScrollPane
        scrSectorContainer.setFocusTraversable(false);

        initializeSectorMap(1, 1);
    }

    /**
     * Initialises the dynamic sector size from sectorConfigs.
     * @param width  The width of the sector (number of columns)
     * @param height The height of the sector (number of rows)
     */
    private void initializeSectorMap(int width, int height) {
        grdSector.getColumnConstraints().clear();
        grdSector.getRowConstraints().clear();
        grdSector.getChildren().clear();

        // Sector configs from the server
        String CELL_BG = IS_DAY ? "smoke" : "#071225";

        // Dynamically add columns using 'width'
        for (int i = 0; i < width; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPrefWidth( CELL_SIZE ); // Establish standard responsive bounding boxes for elements
            col.setHalignment( HPos.CENTER );
            grdSector.getColumnConstraints().add( col );
        }

        // Dynamically add rows using 'height'
        for (int j = 0; j < height; j++) {
            RowConstraints row = new RowConstraints();
            row.setPrefHeight( CELL_SIZE );
            row.setValignment( VPos.CENTER );
            grdSector.getRowConstraints().add( row );
        }

        // Populate cells with starship/hazard containers (visible gridlines)
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                VBox cellWrapper = new VBox();
                cellWrapper.setAlignment(Pos.CENTER);
                cellWrapper.setPadding(new Insets(5));

                // Show cell boarders by CSS
                cellWrapper.setStyle("-fx-border-color: #1B2F4A; -fx-border-width: 0 1px 1px 0; -fx-background-color: " + CELL_BG);

                grdSector.add(cellWrapper, x, y);
            }
        }
    }

    /**<h2>This method must be called during the {@code Stage} setup</h2>
     * It configures the game to listen and respond to the game-specific keys,
     * and also initiates the {@code deploy} process.
     *
     * @param args CLI arguments provided during startup.
     * @param stage The primary stage.*/
    public void configureEnvironment(String[] args, Stage stage){
        // Bind the Key Listener method to the Scene
        setGameKeys( stage.getScene() );

        // Execute 'Quit' when 'X' is pressed
        stage.setOnCloseRequest(event -> {
            event.consume();    // prevents the window from closing instantly

            quit();
        });

        // Start the 'deploy' process
        deploySelf( args );
    }

    /**This method sets a global keyEvent listener to the GUI (Scene). This
     * allows the client to play the game using the keyboard. It must be run
     * once on startup/initialization.
     *
     * @param scene The Scene of the sector's Stage.*/
    private void setGameKeys(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            // Evaluate heading
            switch ( keyEvent.getCode() ){
                // Movement Keys
                case UP -> move(myCallsign , "thrust");
                case DOWN -> move(myCallsign, "back");
                case LEFT -> rotate(myCallsign, "left");
                case RIGHT -> rotate(myCallsign,"right");
                // Combat Keys
                case SPACE -> fire( myCallsign );
                case R -> rearm( myCallsign );
                case P -> repair( myCallsign );
                // Utility Keys
                case F1 -> showHelpDialog();
                case ESCAPE -> quit();
            }
            // To stop after evaluation
            keyEvent.consume();

            // FIND A BETTER LOGIC/PLACE FOR LOOK
            scan();
        });
    }

    /**Pops up the Help menu showing all possible keys
     * to use.*/
    public void showHelpDialog() {
        Dialog<Void> helpDialog = new Dialog<>();
        helpDialog.setTitle("Help Manual");

        // Build the help menu layout text
        Label lblHelpText = new Label(
                """
                        =========== HELP ===========
                        Navigation:
                          ↑ ↓    -   To Move 'thrust' or 'back'
                          ← →    -   To Turn 'left' or 'right'

                        Combat:
                          Space  -   To Fire
                          R      -   To Reload your shots
                          P      -   To Repair your shield

                        Utility:
                          F1     -   Shows this help box
                          ESC    -   To Quit the game

                        =========================="""
        );

        // Style
        lblHelpText.setStyle("-fx-font-size: 16; -fx-text-fill: black;");

        VBox container = new VBox(lblHelpText);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20));

        // Add content to the dialog
        helpDialog.getDialogPane().setContent(container);

        // The 'Done' button
        ButtonType doneButtonType = new ButtonType("Done", ButtonBar.ButtonData.OK_DONE);
        helpDialog.getDialogPane().getButtonTypes().add(doneButtonType);

        helpDialog.showAndWait();
    }

    /**
     * Creates the "Starship Setup" dialog box that allows the user to enter the
     * starship's Name and Kind.
     *
     * @return An {@link Optional} object containing values from the TextField and ComboBox.*/
    private Optional<Pair<String, String>> showStarshipSetUp() {
        Dialog<Pair<String, String>> setupDialog = new Dialog<>();
        setupDialog.setTitle("Starship Setup");
        setupDialog.setHeaderText("Enter your callsign and ship class");

        // Input fields
        TextField callsignField = new TextField(){{setPromptText("Enter your callsign...");}};
        ComboBox<String> starshipKindField = new ComboBox<>() {{
            setItems(FXCollections.observableArrayList(ShipClass.getShipClasses()));
            setValue(getItems().getFirst());
        }};

        setupDialog.getDialogPane().setContent(
                new VBox(){{
                    setAlignment( Pos.CENTER );
                    setSpacing( 10 );
                    setFillWidth( true );
                    getChildren().addAll(callsignField, starshipKindField);
                }}
        );

        // Button
        setupDialog.getDialogPane().getButtonTypes().add( new ButtonType("Launch", ButtonBar.ButtonData.OK_DONE) );
        setupDialog.setResultConverter(buttonType -> {
            if( buttonType != null ){
                if( buttonType.getText().equals( "Launch" ) )
                    return new Pair<>(callsignField.getText(), starshipKindField.getValue());
            }
            else
                shutdownProgram();

            return null;
        });

        return setupDialog.showAndWait();
    }

    /**Safely kills the UI thread and Force-kills any
     * background threads*/
    private void shutdownProgram() {
        javafx.application.Platform.exit(); // kill the UI thread
        System.exit(0); // kill threads
    }

    // =====|| UI UTILITIES
    /**Gets a specific cell/block/position in the grid (sector) which can then be used to place
     * an object (starship or hazard) inside it.
     *
     * @param position The Position object pointing to the cell.
     * @return {VBox} The container which you can put the object in.*/
    private VBox getCellAt(Position position) {
        for (Node node: grdSector.getChildren()) {
            if (GridPane.getColumnIndex(node) == position.getX()
                    && GridPane.getRowIndex(node) == gridRowFor(position)) {
                return (VBox) node;
            }
        }
        return null;
    }

    private int gridRowFor(Position position) {
        return SECTOR_HEIGHT - position.getY();
    }

    /**Clears a specific cell in the grid (sector). It will be left
     * empty if it was previously occupied.
     *
     * @param position The cell's {@link Position} in the grid.*/
    private void clearCellAt(Position position){
        Objects.requireNonNull(getCellAt( position ))
                .getChildren().clear();
    }

    /**
     * Centers the pannable ScrollPane directly over a specific grid coordinate.
     * @param position The {@link Position} of the cell to center at.
     */
    public void centerAt(Position position) {
        //===== PIXEL VALUES EXTRACTION
        // Get pixel dimensions of the entire scrollPane
        double gridWidth = grdSector.getBoundsInLocal().getWidth();
        double gridHeight = grdSector.getBoundsInLocal().getHeight();

        // Get pixel dimensions of the visible window (the viewport)
        double viewportWidth = scrSectorContainer.getViewportBounds().getWidth();
        double viewportHeight = scrSectorContainer.getViewportBounds().getHeight();

        //===== CALCULATIONS
        // Find the exact center pixel of the cell
        double cellX = (position.getX() * CELL_SIZE) + (CELL_SIZE / 2.0);
        double cellY = (gridRowFor(position) * CELL_SIZE) + (CELL_SIZE / 2.0);

        // Using Viewport as starting point, calculate the ScrollPane normalized ratio (0.0 to 1.0)
        double hTarget = (cellX - (viewportWidth / 2.0)) / (gridWidth - viewportWidth);
        double vTarget = (cellY - (viewportHeight / 2.0)) / (gridHeight - viewportHeight);

        // Apply clamped values to ScrollPane
        scrSectorContainer.setHvalue( Math.clamp(hTarget, 0.0, 1.0) );
        scrSectorContainer.setVvalue( Math.clamp(vTarget, 0.0, 1.0) );
    }

    /**
     * Adds a {@link Starship} to a specific cell according to its Position.
     *
     * @param newStarship The instance of the Starship to be added.*/
    private void addStarship(Starship newStarship){
        // Check if cell not occupied
        if( !Objects.requireNonNull(getCellAt(newStarship.getPosition())).getChildren().isEmpty() )
            return;

        // Name label
        Label objectName = new Label( newStarship.getCallsign() );
        objectName.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #00A6FF;" +
                "-fx-font-size: " + LABEL_SIZE
        );

        // Image
        ImageView shape = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/icons/entities/starship.png"))));
        shape.setFitHeight(IMAGE_SIZE);
        shape.setFitWidth(IMAGE_SIZE);
        shape.setRotate(
                // Orientation
                switch ( newStarship.getHeading() ){
                    case NORTH -> 0;
                    case EAST -> 90;
                    case SOUTH -> 180;
                    case WEST -> 270;
                }
        );
        // Tooltip to see the position
        Tooltip.install(
                shape,
                new Tooltip(newStarship.getCallsign() + " at "+newStarship.getPosition()){{
                    setStyle("-fx-text-size: 16; -fx-font-weight: bold;" +
                            (IS_DAY ? "": "-fx-text-fill: black; -fx-background-color: white"));
                }});

        // Place in the respective cell by position
        Objects.requireNonNull( getCellAt( newStarship.getPosition() ) )
                .getChildren()
                .addAll(shape, objectName);

        // Append to starships hashmap (or replace if exists)
        starships.put(newStarship.getCallsign(), new SectorObject(newStarship, shape));
    }

    /**
     * Adds an {@link SpaceHazard} to a specific cell according to its Position.
     *
     * @param newSpaceHazard The instance of the SpaceHazard to be added.*/
    private void addSpaceHazard(SpaceHazard newSpaceHazard){
        // Check if cell not occupied
        if( !Objects.requireNonNull(getCellAt(newSpaceHazard.getPosition())).getChildren().isEmpty() )
            return;

        // Name label
        Label objectName = new Label( newSpaceHazard.getType() );
        objectName.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + (IS_DAY ? "black;" : "white;") +
                "-fx-font-size: " + LABEL_SIZE
        );

        // Image
        String icon = switch (newSpaceHazard.getType()) {
            case "Asteroid" -> "asteroid.png";
            case "Nebula" -> "nebula.png";
            case "VoidRift" -> "void-rift.png";
            default -> throw new IllegalArgumentException("Unsupported space hazard: " + newSpaceHazard.getType());
        };
        ImageView shape = new ImageView(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/icons/hazards/" + icon))));
        shape.setFitHeight(IMAGE_SIZE);
        shape.setFitWidth(IMAGE_SIZE);

        // Tooltip to see the position
        Tooltip.install(
                shape,
                new Tooltip(newSpaceHazard.getType() + " at "+newSpaceHazard.getPosition()){{
                    setStyle("-fx-text-size: 16; -fx-font-weight: bold;" +
                            (IS_DAY ? "": "-fx-text-fill: black; -fx-background-color: white"));
                }});

        // Place in the respective cell by position
        Objects.requireNonNull( getCellAt( newSpaceHazard.getPosition() ) )
                .getChildren()
                .addAll(shape, objectName);
    }

    /**
     * Updates the 'State' of the provided starship. If the starship instance
     * is for the current player (names matching), the UI components will
     * be updated by updateUI().
     *
     * @param starship The starship to update.
     * @param newState The new state directly from the server response*/
    private void updateState(Starship starship, JsonNode newState) {
        starship.setPosition(
                new Position(
                        newState.get("position").get(0).asInt(),
                        newState.get("position").get(1).asInt()
                )
        );
        starship.setHeading( Heading.valueOf(newState.get("heading").asText()) );
        starship.setShield( newState.get("shields").asInt() );
        starship.setShots( newState.get("shots").asInt() );
        starship.setStatus( newState.get("status").asText() );
        starship.setKillCount( newState.get("killCount").asInt() );

        if( starship.getCallsign().equals( myCallsign ) ) updateUI();
    }

    /**Updates all the components of the UI that shows the
     * game's/player's state.*/
    private void updateUI() {
        Starship me = (Starship) starships.get( myCallsign ).instance();
        lblShots.setText("" + me.getShots());
        lblShield.setText("" + me.getShieldStrength());
        lblKills.setText("" + me.getKillCount());
    }

    /**
     * Pops up an error notification message received from
     * the server.
     *
     * @param message The message string to be displayed*/
    private void showErrorMessage(String message){
        Notifications.create()
                .title("Error")
                .text(message)
                .position(Pos.CENTER)
                .hideAfter(Duration.seconds(5))
                .darkStyle()
                .hideCloseButton()
                .show();
    }

    // =====|| PROTOCOLS
    /**
     * This method handles the 'deploy' process when the game starts. It
     * will Show the UI that allows for starship configuration, then 'deploy'
     * it to the Sector.
     *
     * @param args The cli arguments provided when the program is run.*/
    private void deploySelf(String[] args){
        // Starship 'Name' and 'Kind' setup
        showStarshipSetUp().ifPresent(pair -> {
            try {
                // Establish the connection
                String IP = args[0];
                int PORT = Integer.parseInt(args[1]);
                client = new Client(IP, PORT);
                client.connect();

                // Send deploy request
                client.sendMessage( Requests.deployRequest(pair.getKey(), pair.getValue()) );

                // Receive response
                response = client.receiveMessage();

                if( response.get("result").asText().equals("OK") ){
                    // Initialize the sector
                    JsonNode sectorBounds = response.get("data").withArray("sectorBounds");
                    SECTOR_WIDTH = sectorBounds.get(1).get("x").asInt() - sectorBounds.get(0).get("x").asInt();
                    SECTOR_HEIGHT = sectorBounds.get(1).get("y").asInt() - sectorBounds.get(0).get("y").asInt();
                    SCAN_RANGE = response.get("data").get("visibility").asInt();
                    REARMING_TIME = response.get("data").get("rearm").asInt();
                    REPAIRING_TIME = response.get("data").get("repair").asInt();
                    initializeSectorMap( SECTOR_WIDTH + 1, SECTOR_HEIGHT + 1);

                    // Initialize the starship (self)
                    Starship starship = new Starship(pair.getKey().toUpperCase(), pair.getValue(), response.get("state"));
                    myCallsign = starship.getCallsign();
                    addStarship( starship );

                    // Update the UI
                    animateStatusUpdate( starship.getStatus() );
                    updateUI();

                    centerAt( starship.getPosition() );
                }
                else
                    showErrorMessage( response.get("data").get("message").asText() );
            } catch (IOException e) {
                System.err.println("\n## VOIDFRONT Error: " + e.getMessage() + "\n");
                showErrorMessage( e.getMessage() );
                shutdownProgram();
            }
        });
    }

    /**
     * Turns a specific Starship to a specific Heading by facing either {@code Left} or {@code Right}.
     *
     * <li>Right -> Turns the starship 90 degrees <b>clockwise</b></li>
     * <li>Left -> Turns the starship 90 degrees <b>anti-clockwise</b></li>
     *
     * @param callsign The starship to rotate.
     * @param heading Where to face (left or right).
     * @throws IllegalArgumentException If the {@code heading} is neither of the two.*/
    private void rotate(String callsign, String heading){
        if(!heading.equalsIgnoreCase("left") && !heading.equalsIgnoreCase("right"))
            throw new IllegalArgumentException("Must either be 'Left' or 'Right'.");

        // Query and update
        try {
            client.sendMessage( Requests.rotateRequest(callsign, heading.toLowerCase()) );

            response = client.receiveMessage();

            if( response.get("result").asText().equals("OK") ){
                starships.forEach((name, uiObject) -> {
                    if( name.equals( callsign ) ){
                        // Update local instance
                        updateState(((Starship)uiObject.instance()), response.get("state") );

                        uiObject.image().setRotate(
                                uiObject.image().getRotate() +
                                        (heading.equalsIgnoreCase("right") ? 90 : -90 ));
                    }
                });
            }
            else
                showErrorMessage( response.get("data").get("message").asText() );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**This method updates the sector grid. It populates it with hazards and starships,
     * effectively updating what you see.*/
    private void scan(){
        try {
            // Send the 'scan' query
            client.sendMessage( Requests.scanRequest( myCallsign ) );

            // Receive response
            response = client.receiveMessage();

            if( response.get("result").asText().equals("OK") ){
                // TODO: Populate/update objects
                Starship myStarship = (Starship) starships.get( myCallsign ).instance();

                response.get("data").withArray("objects").forEach(obj -> {
                    if( obj.get("type").asText().equals("HAZARD") ){
                        // Find coordinates by heading and distance
                        int x, y;


                        y = switch (Heading.valueOf(obj.get("heading").asText())) {
                            case NORTH -> {
                                x = myStarship.getPosition().getX();
                                yield myStarship.getPosition().getY() + obj.get("distance").asInt();
                            }
                            case EAST -> {
                                x = myStarship.getPosition().getX() + obj.get("distance").asInt();
                                yield myStarship.getPosition().getY();
                            }
                            case SOUTH -> {
                                x = myStarship.getPosition().getX();
                                yield myStarship.getPosition().getY() - obj.get("distance").asInt();
                            }
                            case WEST -> {
                                x = myStarship.getPosition().getX() - obj.get("distance").asInt();
                                yield myStarship.getPosition().getY();
                            }
                        };

                        addSpaceHazard(switch (obj.get("name").asText().toLowerCase(Locale.ROOT)) {
                            case "voidrift" -> new VoidRift(x, y);
                            case "nebula" -> new Nebula(x, y);
                            case "asteroid" -> new Asteroid(x, y);
                            default -> throw new IllegalArgumentException("Unknown scanned hazard: " + obj.get("name").asText());
                        });
                    }
                    else if( obj.get("type").asText().equals("STARSHIP") ){
                        // Find coordinates by heading and distance
                        try {
                            String  coord = switch ( Heading.valueOf( obj.get("heading").asText() ) ){
                                case NORTH -> "["+ myStarship.getPosition().getX()+","+ (myStarship.getPosition().getY() + obj.get("distance").asInt())+"]";// Increment Y
                                case EAST -> "["+ (myStarship.getPosition().getX() + obj.get("distance").asInt())+","+ myStarship.getPosition().getY()+"]";// Increment X
                                case SOUTH -> "["+ myStarship.getPosition().getX()+","+ (myStarship.getPosition().getY() - obj.get("distance").asInt())+"]";// Decrement Y
                                case WEST -> "["+ (myStarship.getPosition().getX() - obj.get("distance").asInt()) +","+ myStarship.getPosition().getY()+"]";// Decrement X
                            };

                            addStarship( new Starship(
                                    obj.get("name").asText(),
                                    "sniper",
                                    new ObjectMapper().readTree("{\n" +
                                            "    \"position\": " + coord + ",\n" +
                                            "    \"heading\": \""+obj.get("heading").asText()+"\",\n" +
                                            "    \"shields\": 1,\n" +
                                            "    \"shots\": 5,\n" +
                                            "    \"status\": \"NORMAL\"\n" +
                                            "   }"
                                    )
                            ) );
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                // Update my current state
                updateState( myStarship, response.get("state") );
            }
            else
                showErrorMessage( response.get("data").get("message").asText() );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method processes the {@code Move} command on the starship that executes it.
     * It moves the starship according to the second argument.
     *
     * @param callsign The name of the moving starship.
     * @param dir The heading its moving towards. (Either {@code thrust} or {@code back})
     * @throws IllegalArgumentException If the {@code dir} is neither of the two.*/
    private void move(String callsign, String dir){
        if(!dir.equalsIgnoreCase("thrust") && !dir.equalsIgnoreCase("back"))
            throw new IllegalArgumentException("Must either be 'thrust' or 'back'.");

        // Query and update
        try {
            Requests.CommandRequest requests;

            if( dir.equalsIgnoreCase("thrust") )
                requests = Requests.thrustRequest(callsign, 1);
            else
                requests = Requests.reverseRequest(callsign, 1);

            client.sendMessage( requests );

            response = client.receiveMessage();

            if( response.get("result").asText().equals("OK") && response.get("data").get("message").asText().equals("Done") ){
                starships.forEach((name, uiObject) -> {
                    if( name.equals( callsign ) ){
                        Starship mover = (Starship) uiObject.instance();

                        // Clear the previously occupied cell
                        clearCellAt( mover.getPosition() );

                        // Update mover's position
                        updateState(mover, response.get("state") );

                        // Update the UI
                        addStarship( mover );

                        // Centre the screen to my new location
                        centerAt( mover.getPosition() );
                    }
                });
            }
            else if( !response.get("data").get("message").asText().equals("Obstructed") )
                showErrorMessage( response.get("data").get("message").asText() );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method handles the Quit process whereby a popup will show
     * asking the user to confirm. If 'Yes', the following happens:
     * <li>The Client disconnects from the Server</li>
     * <li>The user interface is closed</li>
     * <li>Any running threads will be killed immediately</li>*/
    private void quit() {
        Alert alert = new Alert( Alert.AlertType.CONFIRMATION );
        alert.setTitle("Quit");
        alert.setHeaderText("Are you sure?");

        alert.showAndWait().ifPresent(buttonType -> {
            if( buttonType.getText().equals("OK") ){
                // Send 'Quit' request
                try {
                    client.sendMessage( Requests.quitRequest( myCallsign ) );

                    response = client.receiveMessage();

                    if( response.get("result").asText().equals("OK") ){
                        client.disconnect();    // Disconnect client
                        shutdownProgram();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * This method processes the {@code Fire} command on the starship that executes it.
     * Whether it misses or not, the starship will fire for as long as its number
     * of shot is above zero.
     *
     * @param nameOfShooter The name of the shooting starship.*/
    private void fire(String nameOfShooter){
        Starship shooter = (Starship) starships.get( nameOfShooter ).instance();

        if( shooter.getShots() > 0 ){
            try {
                client.sendMessage( Requests.fireRequest( myCallsign ) );

                response = client.receiveMessage();

                if( response.get("result").asText().equals("OK") ){
                    shooter.decreaseShots();
                    updateUI();

                    animateLaserFire( shooter );    // Cool :)

                    // Decrease the shield of the other starship if 'Hit'
                    if( response.get("data").get("message").asText().equals("Hit") ){
                        Starship target = ((Starship) starships.get( response.get("data").get("callsign").asText() ).instance());

                        if( response.get("data").get("state").get("status").asText().equals("DEAD") )
                            killStarship( target );
                        else
                            target.decreaseShieldStrength();
                    }
                }
                else
                    showErrorMessage( response.get("data").get("message").asText() );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**This method handles the termination of a starship when it dies.
     *
     * @param target The starship instance that was killed.*/
    private void killStarship(Starship target) {
        animateExplosion( target );

        // Remove from the list
        starships.remove( target.getCallsign() );
    }

    /**
     * This method processes the {@code Reload} command on the starship that executes it.
     *
     * @param callsign The name of the rearming starship.
     * */
    private void rearm(String callsign){

        try {
            client.sendMessage( Requests.rearmRequest( myCallsign ) );

            response = client.receiveMessage();

            if( response.get("result").asText().equals("OK") ){
                // Animate the rearm process
                Starship rearmer = (Starship) starships.get( callsign ).instance();
                animateReload( rearmer );

                // Update state
                updateState( rearmer, response.get("state") );
            }
            else
                showErrorMessage( response.get("data").get("message").asText() );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method processes the {@code Repair} command on the starship that executes it.
     *
     * @param callsign The name of the repairing starship.*/
    private void repair(String callsign){
        try {
            client.sendMessage( Requests.repairRequest( myCallsign ) );

            response = client.receiveMessage();

            if( response.get("result").asText().equals("OK") ){
                // Animate the repair process
                Starship rearmer = (Starship) starships.get( callsign ).instance();
                animateRepair( rearmer );

                // Update state
                updateState( rearmer, response.get("state") );
            }
            else
                showErrorMessage( response.get("data").get("message").asText() );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ANIMATIONS
    /**
     * Animates the {@code Fire} process in the UI.
     *
     * @param shooter The starship instance that executes the command.*/
    public void animateLaserFire(Starship shooter) {
        // Measure physical pixel sizes dynamically for perfect responsiveness
        double cellWidth = grdSector.getBoundsInLocal().getWidth() / grdSector.getColumnCount();
        double cellHeight = grdSector.getBoundsInLocal().getHeight() / grdSector.getRowCount();

        // Create the bullet
        Rectangle bullet = new Rectangle(4, 20, Color.RED);
        if( shooter.getHeading().equals( Heading.EAST ) ||  shooter.getHeading().equals( Heading.WEST ))
            bullet.setRotate( 90 );
        bullet.setEffect(new Bloom(0.5)); // Glowing energy beam effect :)

        double targetX = 0;
        double targetY = 0;

        switch ( shooter.getHeading() ) {
            case NORTH:   targetY = -(BULLET_RANGE * cellHeight); break; // Up
            case EAST:  targetX = BULLET_RANGE * cellWidth; break; // Right
            case SOUTH: targetY = BULLET_RANGE * cellHeight; break; // Down
            case WEST: targetX = -(BULLET_RANGE * cellWidth); break; // Left
        }

        // Place the bullet at the starship's current position
        grdSector.add(bullet, shooter.getPosition().getX(), gridRowFor(shooter.getPosition()));

        // Create the flight path
        TranslateTransition flight = new TranslateTransition( Duration.millis(400), bullet );
        flight.setToX(targetX);
        flight.setToY(targetY);
        flight.setInterpolator( Interpolator.LINEAR );

        // Remove the bullet once finished
        flight.setOnFinished(_ -> grdSector.getChildren().remove(bullet));
        flight.play();  // start the translation
    }

    /**
     * Animates the {@code Reload} process in the UI.
     *
     * @param rearmer The starship instance that executes the command.*/
    public void animateReload(Starship rearmer) {
        // The circle to animate a pulse
        Circle pulseRing = new Circle(20);
        pulseRing.setFill(Color.TRANSPARENT);
        pulseRing.setStroke(Color.GREEN);
        pulseRing.setStrokeWidth(4);

        // Place the circle at the starship's current position
        grdSector.add(pulseRing, rearmer.getPosition().getX(), gridRowFor(rearmer.getPosition()));

        // To scale outwards
        ScaleTransition scale = new ScaleTransition(Duration.seconds((double) REARMING_TIME / 10), pulseRing);
        scale.setToX(2.5);
        scale.setToY(2.5);

        // To fade out
        FadeTransition fade = new FadeTransition(Duration.seconds((double) REARMING_TIME / 10), pulseRing);
        fade.setToValue(0.0);

        // To run Scale and Fade in parallel
        ParallelTransition pulseEffect = new ParallelTransition(scale, fade);
        pulseEffect.setCycleCount(6);   // 6 => repeat 3 pulses

        // Remove it so the cell remains clean
        pulseEffect.setOnFinished(_ -> grdSector.getChildren().remove(pulseRing));
        pulseEffect.play();

        animateStatusUpdate( "REARMING" ); // Set status on UI
    }

    /**
     * Animates the {@code Repair} process in the UI.
     *
     * @param repairer The starship instance that executes the command.*/
    public void animateRepair(Starship repairer) {
        // Create a protective overlay
        Rectangle shieldWall = new Rectangle(CELL_SIZE, CELL_SIZE);
        shieldWall.setStroke(Color.GREEN);     // Border
        shieldWall.setFill(Color.LIGHTGREEN); // Inside
        shieldWall.setStrokeWidth(3);

        grdSector.add(shieldWall, repairer.getPosition().getX(), gridRowFor(repairer.getPosition()));

        // Create the flicker (opacity rapidly shifting)
        FadeTransition flicker = new FadeTransition(Duration.seconds((double) REPAIRING_TIME / 10), shieldWall);
        flicker.setFromValue(0.2);
        flicker.setToValue(1.0);
        flicker.setAutoReverse(true);
        flicker.setCycleCount(4); // 4 quick flashes

        // Scale the wall inward
        ScaleTransition implode = new ScaleTransition(Duration.seconds((double) REPAIRING_TIME / 10), shieldWall);
        implode.setToX(0.9);
        implode.setToY(0.9);

        // Flash first, then vanish sequentially
        SequentialTransition matrixSequence = new SequentialTransition(flicker, implode);

        // Remove the node instantly on completion
        matrixSequence.setOnFinished(_ -> grdSector.getChildren().remove(shieldWall));
        matrixSequence.play();

        animateStatusUpdate( "REPAIRING" ); // Set status on UI
    }

    /**
     * Animates the explosion for a starship when its status rotates to {@code DEAD}.
     *
     * @param target The starship instance that is killed.*/
    private void animateExplosion(Starship target) {
        ImageView targetsImage = starships.get( target.getCallsign() ).image();

        // Set image to blast
        targetsImage.setImage(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/icons/effects/blast.png"))));

        ScaleTransition scale = new ScaleTransition( Duration.seconds(1), targetsImage );
        scale.setToX(2.5);
        scale.setToY(2.5);

        FadeTransition fade = new FadeTransition(Duration.seconds(1), targetsImage);

        // Scale first, then vanish sequentially
        SequentialTransition matrixSequence = new SequentialTransition(scale, fade);

        // Remove the node instantly on completion
        matrixSequence.setOnFinished(_ -> {
            // Remove starship from the grid
            Objects.requireNonNull(getCellAt(target.getPosition())).getChildren().clear();
        });
        matrixSequence.play();
    }

    /**
     * Temporarily animates the blinking of the 'STATUS' component on the UI
     * when a non-NORMAL status is provided.
     *
     * @param status The non-NORMAL status to animate. */
    private void animateStatusUpdate(String status) {
        int cycleCount = 6;
        String style = "-fx-font-size: 20;-fx-font-weight: bold;-fx-text-fill: ";
        lblStatus.setText(status);
        lblStatus.setStyle(style + (status.equals("DEAD") ? "tomato" : "orange"));

        // Blink on/off
        FadeTransition blink = new FadeTransition(
                Duration.seconds( (double) (status.equals("REARMING") ? REARMING_TIME : REPAIRING_TIME)  / cycleCount ),
                lblStatus);
        blink.setFromValue(1.0);
        blink.setToValue(0.0);
        blink.setCycleCount( cycleCount );
        blink.setAutoReverse( true );

        blink.setOnFinished(_ -> {
            if( !status.equals("DEAD") ){
                lblStatus.setText("NORMAL");
                lblStatus.setStyle(style + "lime");
            }
        });
        blink.play();
    }
}
