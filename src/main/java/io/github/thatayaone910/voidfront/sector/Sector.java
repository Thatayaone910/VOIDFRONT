package io.github.thatayaone910.voidfront.sector;

import io.github.thatayaone910.voidfront.sector.hazard.Nebula;
import io.github.thatayaone910.voidfront.sector.hazard.Asteroid;
import io.github.thatayaone910.voidfront.sector.hazard.SpaceHazard;
import io.github.thatayaone910.voidfront.sector.hazard.VoidRift;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents the environment in which starships exist and interact.
 * The Sector manages the boundaries, configuration settings (visibility, timing),
 * and the collection of active starships and hazards.
 */
public class Sector {

    /** The coordinate representing the top-left corner of the sector. */
    private final Position topLeft;
    /** The coordinate representing the bottom-right corner of the sector. */
    private final Position bottomRight;
    /** The coordinate representing the exact center of the sector. */
    private final Position centre;

    private final int scanRange;
    private final int repairDuration;
    private final int rearmDuration;
    private final int left , right, top, bottom;

    private final List<Starship> starships = new ArrayList<>();
    private final List<SpaceHazard> spaceHazards = new ArrayList<>();

    /**
     * Constructs a Sector using explicit dimension and timing values.
     *
     * @param WIDTH       The total width of the sector.
     * @param HEIGHT      The total height of the sector.
     * @param SCAN_RANGE  The distance a starship can see.
     * @param REPAIRING_TIME The time required for a starship to repair shields.
     * @param REARMING_TIME The time required for a starship to rearm weapons.
     * @throws IllegalArgumentException if dimensions are <= 0 or timing/visibility values are negative.
     */
    public Sector(int width, int height, int scanRange, int repairDuration, int rearmDuration) {
        //Input validation
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Sector dimensions must be greater than 0.");
        }

        if (scanRange < 0 || repairDuration < 0 || rearmDuration < 0) {
            throw new IllegalArgumentException("Scan range and service durations cannot be negative.");
        }

        this.topLeft = new Position(0, 0);
        this.bottomRight = new Position(width - 1, height - 1);
        this.centre = new Position(width / 2, height / 2);
        this.scanRange = scanRange;
        this.repairDuration = repairDuration;
        this.rearmDuration = rearmDuration;
        this.left = 0;
        this.right = width - 1;
        this.top = height - 1;
        this.bottom = 0;
    }

    /**
     * Constructs a Sector using a {@link SectorConfig} object.
     * This constructor also triggers the automatic population of hazards.
     *
     * @param config The configuration object containing sector parameters.
     * @throws IllegalArgumentException if the provided config is null.
     */
    public Sector(SectorConfig config) {
        this(requireConfig(config).getWidth(),
                requireConfig(config).getHeight(),
                requireConfig(config).getScanRange(),
                requireConfig(config).getRepairDuration(),
                requireConfig(config).getRearmDuration()
        );
        populateSpaceHazards();
    }

    /** @return The rearm time configured for this sector. */
    public int getRearmDuration() {
        return this.rearmDuration;
    }

    /** @return The repair time configured for this sector. */
    public int getRepairDuration() {
        return this.repairDuration;
    }

    /** @return The visibility range configured for this sector. */
    public int getScanRange() {
        return this.scanRange;
    }

    /**
     * Returns the minimum and maximum sector bounds.
     * @return An array where index 0 is TOP_LEFT and index 1 is BOTTOM_RIGHT.
     */
    public Position[] getSectorBounds() {
        return new Position[]{topLeft, bottomRight};
    }


    /**
     * Fills the sector with a specific number of unique hazards.
     * Currently capped at: 1 Nebula, 1 VoidRift, and 3 Asteroids.
     *
     * @param width  The width boundary for random placement.
     * @param height The height boundary for random placement.
     */
    private void populateSpaceHazards() {
        List<Position> available = availablePositions();
        Random random = new Random();
        int hazardCount = Math.min(5, Math.max(0, available.size() - 1));

        for (int index = 0; index < hazardCount; index++) {
            Position position = available.remove(random.nextInt(available.size()));
            SpaceHazard hazard = switch (index) {
                case 0 -> new Nebula(position.getX(), position.getY());
                case 1 -> new VoidRift(position.getX(), position.getY());
                default -> new Asteroid(position.getX(), position.getY());
            };
            spaceHazards.add(hazard);
        }
    }

    private List<Position> availablePositions() {
        List<Position> available = new ArrayList<>();
        for (int x = left; x <= right; x++) {
            for (int y = bottom; y <= top; y++) {
                available.add(new Position(x, y));
            }
        }
        return available;
    }

    private static SectorConfig requireConfig(SectorConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("SectorConfig cannot be null.");
        }
        return config;
    }

    /**
     * Registers a starship into the sector's active playing list.
     *
     * @param starship The starship to add.
     * @return The starship that was added.
     * @throws IllegalArgumentException if the starship is null.
     * @throws IllegalStateException if the starship is already registered in the sector.
     */
    public synchronized Starship addStarship(Starship starship) {
        if (starship == null) {
            throw new IllegalArgumentException("Starship cannot be null.");
        }
        if (starship.getCallsign() == null || starship.getCallsign().isBlank()) {
            throw new IllegalArgumentException("Callsign cannot be empty.");
        }
        if (findStarship(starship.getCallsign()) != null) {
            throw new IllegalStateException("Callsign is already in use.");
        }
        if (starship.getPosition() == null || !starship.getPosition().isIn(topLeft, bottomRight)
                || spaceHazards.stream().anyMatch(hazard -> hazard.blocksPosition(starship.getPosition()))
                || starships.stream().anyMatch(existing -> existing.blocksPosition(starship.getPosition()))) {
            throw new IllegalArgumentException("Starship spawn position is unavailable.");
        }

        starships.add(starship);
        return starship;
    }

    /** @return A copy of the list containing all starships currently in the sector. */
    public synchronized List<Starship> getAllStarships() {
        return new ArrayList<>(starships);
    }

    public synchronized void clearSector() {
        starships.clear();
        spaceHazards.clear();
    }

    /** @return A list containing all hazards.*/
    public synchronized List<SpaceHazard> getSpaceHazards() {
        return new ArrayList<>(spaceHazards);
    }

    public synchronized Position randomAvailablePosition() {
        List<Position> available = availablePositions();
        available.removeIf(position ->
                spaceHazards.stream().anyMatch(hazard -> hazard.blocksPosition(position))
                        || starships.stream().anyMatch(starship -> starship.blocksPosition(position)));
        if (available.isEmpty()) {
            throw new IllegalStateException("No available spawn positions remain in the sector.");
        }
        return available.get(ThreadLocalRandom.current().nextInt(available.size()));
    }

    /**
     * Retrieves a list of names of all starships currently in the sector.
     * @return A list of starship callsigns.
     */
    public synchronized List<String> callsigns() {
        List<String> callsigns = new ArrayList<>();
        for (Starship starship : starships) {
            if (starship != null && starship.getCallsign() != null) {
                callsigns.add(starship.getCallsign());
            }
        }
        return callsigns;
    }

    /**
     * Manually adds a specific hazard to the sector.
     * @param hazard The hazard to add.
     * @throws IllegalArgumentException if the hazard is null.
     */
    public synchronized void addSpaceHazard(SpaceHazard hazard) {
        if (hazard == null) {
            throw new IllegalArgumentException("SpaceHazard cannot be null.");
        }
        if (!hazard.getPosition().isIn(topLeft, bottomRight)) {
            throw new IllegalArgumentException("Hazard position is outside the sector.");
        }
        if (spaceHazards.stream().anyMatch(existing -> existing.getPosition().equals(hazard.getPosition()))
                || starships.stream().anyMatch(starship -> starship.blocksPosition(hazard.getPosition()))) {
            throw new IllegalArgumentException("Hazard position is already occupied.");
        }
        spaceHazards.add(hazard);
    }

    /**
     * Returns a combined list of all entities (starships and hazards) in the sector.
     * @return A list of Objects containing all active simulation entities.
     */
    public synchronized List<Object> dump() {
        List<Object> allObjects = new ArrayList<>();
        for (Starship starship : starships) {
            if (starship != null) {
                allObjects.add(starship);
            }
        }
        for (SpaceHazard hazard : spaceHazards) {
            if (hazard != null) {
                allObjects.add(hazard);
            }
        }
        return allObjects;
    }

    /**
     * Finds and return a starship in the sector using its name
     * @param callsign The nam of the starship we're scaning for
     * @return The matching starship object or null if not found
     */
    public synchronized Starship findStarship(String callsign){
        //get all current starships in the sector and check if querying starship exists and is alive
        List<Starship> currentStarships = getAllStarships();

        //loop through all the starships to find the querying starship to return a starship object
        for(Starship starship: currentStarships){
            if(starship.getCallsign().equalsIgnoreCase(callsign)){
                return starship;
            }
        }
        //if starship is not found or is dead
        return null;
    }


    /**
     * Computes the distance between two Starships in the Sector.Used in scan command
     * <br>
     * @param starship1 The starting point Starship
     * @param starship2 The ending point Starship
     * @return {int} The geometrical distance between them.
     * */
    public int distanceBetween( Starship starship1, Starship starship2 ){
        return Math.abs(starship1.getPosition().getX()-starship2.getPosition().getX()) +
                Math.abs(starship1.getPosition().getY()-starship2.getPosition().getY());
    }

    /**
     * Computes the distance between a Starship and an SpaceHazard in the Sector. Used in scan command
     * <br>
     * @param starship The starting point Starship
     * @param hazard The ending point SpaceHazard
     * @return {int} The geometrical distance between them.
     * */
    public int distanceToSpaceHazard(Starship starship, SpaceHazard hazard){
        int deltaX = hazard.getPosition().getX() - starship.getPosition().getX();
        int deltaY = hazard.getPosition().getY() - starship.getPosition().getY();

        return (int) Math.sqrt( Math.pow(deltaX, 2) + Math.pow(deltaY, 2) );
    }

    /**
     * Removes a starship from the sector's active players
     *
     * @param starship The starship to rmove from the sector
     */
    public synchronized void removeStarship(Starship starship){
        if(starships.contains(starship)){
            starships.remove(starship);
        }
        else throw new IllegalArgumentException("Starship not found in sector");
    }

    /**
     * Helper function for testing conditions
     * Removes all hazards from the sector
     */
    public void removeSpaceHazards(){
        spaceHazards.clear();
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }

    public Position getCentre() {
        return centre;
    }

    /**
     * Determines the relative heading of an object from a starship
     *
     * @param starship The querying starship
     * @param object The target object position
     * @return THe heading of the object relative to teh starship or null if diagonal
     */
    public Heading validHeading(Starship starship, Position object){
        int starshipx= starship.getPosition().getX();
        int objX = object.getX();

        int starshipY = starship.getPosition().getY();
        int objY = object.getY();
        //check if they are in the same row
        if(starshipx ==objX){
            if(objY > starshipY) return Heading.NORTH;
            if(objY < starshipY) return Heading.SOUTH;
        }
        //check if tehy are in the same colonm
        if(starshipY==objY){
            if (objX > starshipx) return Heading.EAST;
            if(objX<starshipx) return Heading.WEST;
        }

        return null;
    }

    /**
     * Calculates the distance form the starship to the nearest edge ina specifc diection
     *
     * @param starship The querying starship
     * @param heading The heading to check
     * @return The distance to the sector edge
     */

    public int distanceToEdge(Starship starship, Heading heading){
        int x=starship.getPosition().getX();
        int y = starship.getPosition().getY();

        return switch(heading){
            case NORTH -> Math.abs(getTop()-y);
            case SOUTH -> Math.abs(y-getBottom());
            case EAST -> Math.abs(getRight()-x);
            case WEST -> Math.abs(x-getLeft());
        };
    }

    /**
     *Calculates a position directly in front of a starship based on its fcaing heading
     *
     * @param starship THe starship whose durection is being used
     * @param distance the distance in fornt of the starship
     * @return The calculated position in front of teh starship
     * */
    public Position positionInFront(Starship starship, int distance){
        int x = starship.getPosition().getX() + starship.getHeading().xOffset() * distance;
        int y = starship.getPosition().getY() + starship.getHeading().yOffset() * distance;
        return new Position(x,y);
    }
}