package io.github.dxf4j.core;

import io.github.dxf4j.DxfGroupCode;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for a DXF entity or object, represented as an ordered list of group codes.
 * The first group code is always (0, entityType).
 */
public class DxfEntity {

    private final String entityType;
    private final List<DxfGroupCode> groupCodes;

    public DxfEntity(String entityType) {
        this.entityType = entityType;
        this.groupCodes = new ArrayList<>();
    }

    public DxfEntity(String entityType, List<DxfGroupCode> groupCodes) {
        this.entityType = entityType;
        this.groupCodes = new ArrayList<>(groupCodes);
    }

    /** The entity type name (e.g. "TEXT", "IMAGE", "DICTIONARY"). */
    public String getEntityType() {
        return entityType;
    }

    /** The group codes after the (0, entityType) pair. */
    public List<DxfGroupCode> getGroupCodes() {
        return groupCodes;
    }

    /** Find the first value for the given group code number. */
    public String getGroupValue(int code) {
        String codeStr = formatCode(code);
        for (DxfGroupCode gc : groupCodes) {
            if (gc.getCode().equals(codeStr)) {
                return gc.getValueTrimmed();
            }
        }
        return null;
    }

    /** Find the first value for the given group code string (preserving padding). */
    public String getGroupValue(String codeStr) {
        for (DxfGroupCode gc : groupCodes) {
            if (gc.getCode().equals(codeStr)) {
                return gc.getValueTrimmed();
            }
        }
        return null;
    }

    /** The entity handle (group code 5). */
    public String getHandle() {
        return getGroupValue("  5");
    }

    /** The owner handle (group code 330). */
    public String getOwnerHandle() {
        return getGroupValue("330");
    }

    /** The layer name (group code 8). */
    public String getLayer() {
        return getGroupValue("  8");
    }

    /** Format a code integer as the standard 3-character padded string. */
    public static String formatCode(int code) {
        return String.format("%3d", code).replace('0', ' ').replace(" 0", "  ");
    }

    /**
     * Format a code integer matching DXF conventions:
     * codes 0-9: 2 leading spaces, codes 10-99: 1 leading space, codes 100+: no leading space.
     */
    public static String formatCodePadded(int code) {
        if (code < 10) {
            return "  " + code;
        } else if (code < 100) {
            return " " + code;
        } else {
            return String.valueOf(code);
        }
    }

    @Override
    public String toString() {
        return entityType + " [handle=" + getHandle() + ", " + groupCodes.size() + " group codes]";
    }
}
