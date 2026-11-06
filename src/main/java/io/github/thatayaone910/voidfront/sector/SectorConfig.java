package io.github.thatayaone910.voidfront.sector;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Manages the configuration settings for the Starship Sector.
 * This class handles sector dimensions, scan range, and starship service durations using defaults
 * or by loading them from an external properties file.
 */
public class SectorConfig {

    private int width;
    private int height;
    private int scanRange;
    private int repairDuration;
    private int rearmDuration;

    /**
     * Default constructor.
     * Initializes the sector settings using internal hard-coded default values.
     */
    public SectorConfig() {
        setDefaultValues();
    }

    /**
     * Constructor that attempts to load configuration settings from a specified file.
     * If the file is missing or contains invalid data, the class falls back to
     * internal default values.
     *
     * @param fileName The path to the .properties file containing configuration.
     */
    public SectorConfig(String fileName) {
        setDefaultValues(); // Initialize with defaults first
        if (!load(fileName)) {
            System.out.println("Falling back to default configuration.");
        }
    }

    /**
     * Sets all configuration fields to their standard default values.
     * Default: 100x100 sector, scan range 5, repair/rearm durations of 3.
     */
    private void setDefaultValues() {
        this.width = 100;
        this.height = 100;
        this.scanRange = 5;
        this.repairDuration = 3;
        this.rearmDuration = 3;
    }

    /**
     * Reads and parses configuration values from a properties file.
     * Expected keys: sector.width, sector.height, sector.scanRange,
     * sector.repairDuration, sector.rearmDuration.
     *
     * @param fileName Path to the properties file.
     * @return true if the file was loaded and parsed successfully; false otherwise.
     */
    private boolean load(String fileName) {
        Properties prop = new Properties();

        InputStream resource = getClass().getClassLoader().getResourceAsStream(fileName);
        try (InputStream input = resource != null ? resource : new FileInputStream(fileName)) {
            prop.load(input);

            int width = Integer.parseInt(prop.getProperty("sector.width", "100"));
            int height = Integer.parseInt(prop.getProperty("sector.height", "100"));
            int scanRange = Integer.parseInt(prop.getProperty("sector.scanRange", "5"));
            int repairDuration = Integer.parseInt(prop.getProperty("sector.repairDuration", "3"));
            int rearmDuration = Integer.parseInt(prop.getProperty("sector.rearmDuration", "3"));
            if (width <= 0 || height <= 0 || scanRange < 0 || repairDuration < 0 || rearmDuration < 0) {
                return false;
            }

            this.width = width;
            this.height = height;
            this.scanRange = scanRange;
            this.repairDuration = repairDuration;
            this.rearmDuration = rearmDuration;
            return true;

        } catch (IOException ex) {
            return false;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Gets the configured width of the sector.
     * @return the sector width as an integer.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the configured height of the sector.
     * @return the sector height as an integer.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the starship's sensor visibility range.
     * @return the distance a starship can see.
     */
    public int getScanRange() {
        return scanRange;
    }

    /**
     * Gets the time required for a starship to complete a repair.
     * @return the repair time in seconds or cycles.
     */
    public int getRepairDuration() {
        return repairDuration;
    }

    /**
     * Gets the time required for a starship to rearm its weapons.
     * @return the rearm time in seconds or cycles.
     */
    public int getRearmDuration() {
        return rearmDuration;
    }

    /**
     * Validates if a specific set of coordinates falls within the configured
     * sector boundaries.
     *
     * @param x The horizontal coordinate to check.
     * @param y The vertical coordinate to check.
     * @return true if the position is within [0, width) and [0, height); false otherwise.
     */
    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}