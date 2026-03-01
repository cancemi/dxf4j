package io.github.dxf4j.object;

import io.github.dxf4j.DxfGroupCode;
import io.github.dxf4j.core.DxfEntity;
import io.github.dxf4j.core.DxfHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * An IMAGEDEF object in the OBJECTS section.
 * Defines the image file path and pixel dimensions referenced by IMAGE entities.
 */
public class DxfImageDef extends DxfEntity {

    public DxfImageDef(List<DxfGroupCode> groupCodes) {
        super("IMAGEDEF", groupCodes);
    }

    /** The image file path (group code 1). */
    public String getFilePath() {
        return getGroupValue("  1");
    }

    /**
     * Build an IMAGEDEF from parameters.
     *
     * @param imgDefHandle  handle for this IMAGEDEF
     * @param dictHandle    handle of ACAD_IMAGE_DICT (owner)
     * @param reactorHandle handle of the IMAGEDEF_REACTOR
     * @param filePath      absolute path to the image file
     * @param pixW          image width in pixels
     * @param pixH          image height in pixels
     * @return a new DxfImageDef
     */
    public static DxfImageDef build(DxfHandle imgDefHandle, String dictHandle,
                                     DxfHandle reactorHandle,
                                     String filePath, int pixW, int pixH) {
        List<DxfGroupCode> codes = new ArrayList<>();
        codes.add(new DxfGroupCode("  5", imgDefHandle.toHex()));
        codes.add(new DxfGroupCode("102", "{ACAD_REACTORS"));
        codes.add(new DxfGroupCode("330", dictHandle));
        codes.add(new DxfGroupCode("330", reactorHandle.toHex()));
        codes.add(new DxfGroupCode("102", "}"));
        codes.add(new DxfGroupCode("330", dictHandle));
        codes.add(new DxfGroupCode("100", "AcDbRasterImageDef"));
        codes.add(new DxfGroupCode(" 90", "        0"));
        codes.add(new DxfGroupCode("  1", filePath));
        codes.add(new DxfGroupCode(" 10", String.format(Locale.US, "%.1f", (double) pixW)));
        codes.add(new DxfGroupCode(" 20", String.format(Locale.US, "%.1f", (double) pixH)));
        codes.add(new DxfGroupCode(" 11", String.format(Locale.US, "%.16f", 1.0 / pixW)));
        codes.add(new DxfGroupCode(" 21", String.format(Locale.US, "%.16f", 1.0 / pixH)));
        codes.add(new DxfGroupCode("280", "     1"));
        codes.add(new DxfGroupCode("281", "     0"));
        return new DxfImageDef(codes);
    }
}
