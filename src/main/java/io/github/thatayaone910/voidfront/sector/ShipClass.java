package io.github.thatayaone910.voidfront.sector;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Provides the supported starship classes and their starting combat values. */
public class ShipClass {
    private static final Map<String, Map<String, Integer>> starshipClasses = Map.of(
            "sniper", Map.of("shield", 1, "shots", 5));

    /**
     * Retrieves the starting attributes for a supported starship class.
     *
     * @param shipClass the class to look up, such as {@code sniper}
     * @return A list containing the class name, shield strength, and shots.
     * @throws IllegalArgumentException if the specified class is not supported
     * */
    public static List<Object> getShipClass(String shipClass){
        if (!isSupportedShipClass(shipClass)) {
            throw new IllegalArgumentException(shipClass == null
                    ? "Ship class cannot be null."
                    : "Unknown ship class: " + shipClass);
        }
        Map<String, Integer> details = starshipClasses.get(shipClass.toLowerCase(Locale.ROOT));

        return List.of(shipClass.toLowerCase(Locale.ROOT),
                details.get("shield"),
                details.get("shots"));
    }

    public static boolean isSupportedShipClass(String shipClass) {
        if (shipClass == null) {
            return false;
        }
        return starshipClasses.containsKey(shipClass.toLowerCase(Locale.ROOT));
    }

    public static List<String> getShipClasses() {
        return starshipClasses.keySet().stream().sorted().toList();
    }
}
