package io.github.dxf4j.core;

import io.github.dxf4j.DxfGroupCode;
import java.util.ArrayList;
import java.util.List;

/**
 * A DXF section container. Sections that are fully parsed (ENTITIES, OBJECTS)
 * contain typed entities. Other sections (CLASSES, TABLES, BLOCKS, ACDSDATA)
 * store raw group codes for lossless round-trip.
 */
public class DxfSection {

    private final DxfSectionType type;
    private final String name;
    private final List<DxfEntity> entities;
    private final List<DxfGroupCode> rawGroupCodes;
    private final boolean parsed;

    /** Create a parsed section (entities are typed). */
    public DxfSection(DxfSectionType type, String name, List<DxfEntity> entities) {
        this.type = type;
        this.name = name;
        this.entities = new ArrayList<>(entities);
        this.rawGroupCodes = null;
        this.parsed = true;
    }

    /** Create a raw section (group codes preserved as-is). */
    public DxfSection(DxfSectionType type, String name, List<DxfGroupCode> rawGroupCodes, boolean raw) {
        this.type = type;
        this.name = name;
        this.entities = null;
        this.rawGroupCodes = new ArrayList<>(rawGroupCodes);
        this.parsed = false;
    }

    public DxfSectionType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isParsed() {
        return parsed;
    }

    /** Entities in this section (only for parsed sections). */
    public List<DxfEntity> getEntities() {
        if (!parsed) throw new IllegalStateException("Section " + name + " is raw, not parsed into entities");
        return entities;
    }

    /** Raw group codes (only for unparsed sections). */
    public List<DxfGroupCode> getRawGroupCodes() {
        if (parsed) throw new IllegalStateException("Section " + name + " is parsed, not raw");
        return rawGroupCodes;
    }

    /** Find entity by handle. */
    public DxfEntity findByHandle(String handle) {
        if (!parsed) return null;
        for (DxfEntity e : entities) {
            if (handle.equals(e.getHandle())) {
                return e;
            }
        }
        return null;
    }

    /** Find all entities of a given type. */
    public List<DxfEntity> findByType(String entityType) {
        List<DxfEntity> result = new ArrayList<>();
        if (!parsed) return result;
        for (DxfEntity e : entities) {
            if (entityType.equals(e.getEntityType())) {
                result.add(e);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        if (parsed) {
            return "Section " + name + " [" + entities.size() + " entities]";
        } else {
            return "Section " + name + " [raw, " + rawGroupCodes.size() + " group codes]";
        }
    }
}
