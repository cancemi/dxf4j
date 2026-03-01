package io.github.dxf4j.object;

import io.github.dxf4j.DxfGroupCode;
import io.github.dxf4j.core.DxfEntity;
import io.github.dxf4j.core.DxfHandle;
import java.util.ArrayList;
import java.util.List;

/**
 * An IMAGEDEF_REACTOR object in the OBJECTS section.
 * Links an IMAGE entity to its IMAGEDEF.
 */
public class DxfImageDefReactor extends DxfEntity {

    public DxfImageDefReactor(List<DxfGroupCode> groupCodes) {
        super("IMAGEDEF_REACTOR", groupCodes);
    }

    /**
     * Build an IMAGEDEF_REACTOR from parameters.
     *
     * @param reactorHandle handle for this reactor
     * @param imageHandle   handle of the IMAGE entity (owner)
     * @return a new DxfImageDefReactor
     */
    public static DxfImageDefReactor build(DxfHandle reactorHandle, DxfHandle imageHandle) {
        List<DxfGroupCode> codes = new ArrayList<>();
        codes.add(new DxfGroupCode("  5", reactorHandle.toHex()));
        codes.add(new DxfGroupCode("330", imageHandle.toHex()));
        codes.add(new DxfGroupCode("100", "AcDbRasterImageDefReactor"));
        codes.add(new DxfGroupCode(" 90", "        2"));
        codes.add(new DxfGroupCode("330", imageHandle.toHex()));
        return new DxfImageDefReactor(codes);
    }
}
