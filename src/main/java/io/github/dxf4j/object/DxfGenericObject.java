package io.github.dxf4j.object;

import io.github.dxf4j.DxfGroupCode;
import io.github.dxf4j.core.DxfEntity;
import java.util.List;

/**
 * A generic passthrough object for unknown object types in the OBJECTS section.
 * Preserves all group codes as-is for lossless round-trip.
 */
public class DxfGenericObject extends DxfEntity {

    public DxfGenericObject(String objectType, List<DxfGroupCode> groupCodes) {
        super(objectType, groupCodes);
    }
}
