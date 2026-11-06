package io.github.thatayaone910.voidfront.sector.hazard;

import javafx.scene.image.ImageView;
import io.github.thatayaone910.voidfront.sector.Starship;

/**
 * This {@link Record} (Value-Object) serves as a UI model for
 * every object that gets added to the sector's grid. This object
 * can either be a {@link Starship} or an {@link SpaceHazard}.
 *
 * @param instance The java object represented by this model.
 * @param image The image to be shown in the UI.*/
public record SectorObject(Object instance, ImageView image) {
}
