package io.github.dxf4j.entity;

import io.github.dxf4j.DxfGroupCode;
import io.github.dxf4j.core.DxfEntity;
import java.util.List;

/**
 * A TEXT entity in the DXF ENTITIES section.
 */
public class DxfText extends DxfEntity {

    public DxfText(List<DxfGroupCode> groupCodes) {
        super("TEXT", groupCodes);
    }

    /** The text content (group code 1). */
    public String getText() {
        return getGroupValue("  1");
    }

    /** X position (group code 10). */
    public double getX() {
        String v = getGroupValue(" 10");
        return v != null ? Double.parseDouble(v) : 0;
    }

    /** Y position (group code 20). */
    public double getY() {
        String v = getGroupValue(" 20");
        return v != null ? Double.parseDouble(v) : 0;
    }

    /** Z position (group code 30). */
    public double getZ() {
        String v = getGroupValue(" 30");
        return v != null ? Double.parseDouble(v) : 0;
    }

    /** Text height (group code 40). */
    public double getHeight() {
        String v = getGroupValue(" 40");
        return v != null ? Double.parseDouble(v) : 0;
    }
}
