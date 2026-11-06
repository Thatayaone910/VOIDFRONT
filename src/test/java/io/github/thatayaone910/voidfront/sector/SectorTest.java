package io.github.thatayaone910.voidfront.sector;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import io.github.thatayaone910.voidfront.sector.hazard.SpaceHazard;
import io.github.thatayaone910.voidfront.sector.hazard.Asteroid;

import static org.junit.jupiter.api.Assertions.*;

public class SectorTest {
    private Sector sector;
    private JsonNode arguments;

    @BeforeEach
    void setUp() throws Exception {


        //create a sector object once instead of repetition
        sector = new Sector(100, 100, 10, 5, 3);
        sector.clearSector();


        ObjectMapper mapper = new ObjectMapper();
        arguments = mapper.readTree("[\"sniper\", 10, 10]");
    }

    @Test
    void shouldCreateSectorWithValidDimensions(){
        //this checks if i create the sector,using valid value,the code doesnt crash
        assertNotNull(sector);
    }

    @Test
    void shouldSetCorrectBoundaries(){

        //Top_Left is always (0,0)
        assertEquals(0, sector.getSectorBounds()[0].getX());
        assertEquals(0, sector.getSectorBounds()[0].getY());

        //Bottom_Right is (WIDTH-1, HEIGHT-1)
        assertEquals(99, sector.getSectorBounds()[1].getX());
        assertEquals(99, sector.getSectorBounds()[1].getY());
    }

    @Test
    void shouldSetCorrectCentre(){
        assertEquals(50, sector.getCentre().getX());
        assertEquals(50, sector.getCentre().getY());
    }

    @Test
    void shouldRerotateCorrectVisibility(){
        assertEquals(10, sector.getScanRange());
    }
    @Test
    void shouldRerotateRepairTime(){
        assertEquals(5,sector.getRepairDuration());
    }

    @Test
    void shouldRerotateReloadTime(){
        assertEquals(3, sector.getRearmDuration());
    }

    @Test
    /**THIS STILL NEEDS MORE WORK ShouldntAddSameStarshipTwice()
     * shouldntAddNullStarship(), callsignReturnsCorrectNames() or
     * returnsEmptyListWhenEmpt() still need to find  a way to not be redundant*/
    void shouldAddStarshipToSector() throws Exception {
        Starship callsign = new Starship("HAL", arguments);

        sector.addStarship(callsign);

        List<String> names = sector.callsigns();
        assertTrue(names.contains("HAL"));
    }

    @Test

    void shouldRerotateEmptyForNoStarshipsAdded(){
        List<String> names = sector.callsigns();

        assertTrue(names.isEmpty() );

    }
//    @Test
//    void

    @Test
    void dumpHasStarshipsAndSpaceHazards()throws Exception {
        Starship callsign = new Starship("HAL", arguments);
        SpaceHazard mountain = new Asteroid(10, 20);

        sector.addStarship(callsign);
        sector.addSpaceHazard(mountain);

        List<Object> dumped = sector.dump();

        assertEquals(2, dumped.size());
        assertTrue(dumped.contains(callsign));
        assertTrue(dumped.contains(mountain));
    }

    @Test
    void sectorStateIsIsolatedBetweenInstances() throws Exception {
        Sector otherSector = new Sector(100, 100, 10, 5, 3);
        Starship starship = new Starship("HAL", arguments);
        SpaceHazard asteroid = new Asteroid(10, 20);

        sector.addStarship(starship);
        sector.addSpaceHazard(asteroid);

        assertSame(starship, sector.findStarship("HAL"));
        assertNull(otherSector.findStarship("HAL"));
        assertEquals(1, sector.getSpaceHazards().size());
        assertTrue(otherSector.getSpaceHazards().isEmpty());
    }

}
