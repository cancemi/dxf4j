package io.github.dxf4j.entity;

import io.github.dxf4j.DxfGroupCode;
import io.github.dxf4j.core.DxfEntity;
import io.github.dxf4j.core.DxfHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * An IMAGE entity (AcDbRasterImage) in the DXF ENTITIES section.
 */
public class DxfImage extends DxfEntity {

    public DxfImage(List<DxfGroupCode> groupCodes) {
        super("IMAGE", groupCodes);
    }

    /** The referenced IMAGEDEF handle (group code 340). */
    public String getImageDefHandle() {
        return getGroupValue("340");
    }

    /** The IMAGEDEF_REACTOR handle (group code 360). */
    public String getReactorHandle() {
        return getGroupValue("360");
    }

    /**
     * Build an IMAGE entity from parameters.
     *
     * @param imageHandle  handle for this IMAGE entity
     * @param ownerHandle  handle of the owner (usually the model space block record)
     * @param layer        layer name
     * @param posX         insertion point X
     * @param posY         insertion point Y
     * @param pixW         image width in pixels
     * @param pixH         image height in pixels
     * @param displayWidth desired display width in DXF units
     * @param imgDefHandle handle of the IMAGEDEF
     * @param reactorHandle handle of the IMAGEDEF_REACTOR
     * @return a new DxfImage entity
     */
    public static DxfImage build(DxfHandle imageHandle, String ownerHandle, String layer,
                                  double posX, double posY,
                                  int pixW, int pixH, double displayWidth,
                                  DxfHandle imgDefHandle, DxfHandle reactorHandle) {
        double displayH = displayWidth * pixH / pixW;
        double uPx = displayWidth / pixW;
        double vPy = displayH / pixH;

        List<DxfGroupCode> codes = new ArrayList<>();
        codes.add(new DxfGroupCode("  5", imageHandle.toHex()));
        codes.add(new DxfGroupCode("330", ownerHandle));
        codes.add(new DxfGroupCode("100", "AcDbEntity"));
        codes.add(new DxfGroupCode("  8", layer));
        codes.add(new DxfGroupCode(" 62", "   254"));
        codes.add(new DxfGroupCode("100", "AcDbRasterImage"));
        codes.add(new DxfGroupCode(" 90", "        0"));
        codes.add(new DxfGroupCode(" 10", String.format(Locale.US, "%.6f", posX)));
        codes.add(new DxfGroupCode(" 20", String.format(Locale.US, "%.6f", posY)));
        codes.add(new DxfGroupCode(" 30", "0.0"));
        codes.add(new DxfGroupCode(" 11", String.format(Locale.US, "%.15f", uPx)));
        codes.add(new DxfGroupCode(" 21", "0.0"));
        codes.add(new DxfGroupCode(" 31", "0.0"));
        codes.add(new DxfGroupCode(" 12", "0.0000000000000001"));
        codes.add(new DxfGroupCode(" 22", String.format(Locale.US, "%.15f", vPy)));
        codes.add(new DxfGroupCode(" 32", "0.0"));
        codes.add(new DxfGroupCode(" 13", String.format(Locale.US, "%.1f", (double) pixW)));
        codes.add(new DxfGroupCode(" 23", String.format(Locale.US, "%.1f", (double) pixH)));
        codes.add(new DxfGroupCode("340", imgDefHandle.toHex()));
        codes.add(new DxfGroupCode(" 70", "     7"));
        codes.add(new DxfGroupCode("280", "     0"));
        codes.add(new DxfGroupCode("281", "    50"));
        codes.add(new DxfGroupCode("282", "    50"));
        codes.add(new DxfGroupCode("283", "     0"));
        codes.add(new DxfGroupCode("290", "     0"));
        codes.add(new DxfGroupCode("360", reactorHandle.toHex()));
        codes.add(new DxfGroupCode(" 71", "     1"));
        codes.add(new DxfGroupCode(" 91", "        2"));
        codes.add(new DxfGroupCode(" 14", "-0.5"));
        codes.add(new DxfGroupCode(" 24", "-0.5"));
        codes.add(new DxfGroupCode(" 14", String.format(Locale.US, "%.1f", pixW - 0.5)));
        codes.add(new DxfGroupCode(" 24", String.format(Locale.US, "%.1f", pixH - 0.5)));

        return new DxfImage(codes);
    }
}
