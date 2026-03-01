package io.github.dxf4j.entity;

import io.github.dxf4j.DxfGroupCode;
import io.github.dxf4j.core.DxfEntity;
import java.util.List;

/**
 * A generic passthrough entity for unknown or unparsed entity types.
 * Preserves all group codes as-is for lossless round-trip.
 */
public class DxfGenericEntity extends DxfEntity {

    public DxfGenericEntity(String entityType, List<DxfGroupCode> groupCodes) {
        super(entityType, groupCodes);
    }
}
