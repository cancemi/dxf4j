package io.github.dxf4j.core;

/**
 * Enum of DXF section types.
 */
public enum DxfSectionType {
    HEADER,
    CLASSES,
    TABLES,
    BLOCKS,
    ENTITIES,
    OBJECTS,
    ACDSDATA,
    UNKNOWN;

    /** Parse section type from the section name string (e.g. "ENTITIES"). */
    public static DxfSectionType fromName(String name) {
        try {
            return valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
