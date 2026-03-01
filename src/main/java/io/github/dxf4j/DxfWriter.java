package io.github.dxf4j;

import io.github.dxf4j.core.DxfEntity;
import io.github.dxf4j.core.DxfSection;
import java.util.List;

/**
 * Serializes a {@link DxfDocument} back to DXF text format.
 */
public class DxfWriter {

    private final DxfDocument doc;

    public DxfWriter(DxfDocument doc) {
        this.doc = doc;
    }

    /** Serialize the document to a DXF string. */
    public String write() {
        String nl = doc.getNewline();
        StringBuilder sb = new StringBuilder();

        // Write HEADER section
        sb.append("  0").append(nl).append("SECTION").append(nl);
        sb.append("  2").append(nl).append("HEADER").append(nl);
        for (DxfGroupCode gc : doc.getHeaderRaw()) {
            sb.append(gc.getCode()).append(nl).append(gc.getValue()).append(nl);
        }
        sb.append("  0").append(nl).append("ENDSEC").append(nl);

        // Write other sections in order
        for (DxfSection section : doc.getSections()) {
            sb.append("  0").append(nl).append("SECTION").append(nl);
            sb.append("  2").append(nl).append(section.getName()).append(nl);

            if (section.isParsed()) {
                for (DxfEntity entity : section.getEntities()) {
                    sb.append("  0").append(nl).append(entity.getEntityType()).append(nl);
                    for (DxfGroupCode gc : entity.getGroupCodes()) {
                        sb.append(gc.getCode()).append(nl).append(gc.getValue()).append(nl);
                    }
                }
            } else {
                for (DxfGroupCode gc : section.getRawGroupCodes()) {
                    sb.append(gc.getCode()).append(nl).append(gc.getValue()).append(nl);
                }
            }

            sb.append("  0").append(nl).append("ENDSEC").append(nl);
        }

        // EOF
        sb.append("  0").append(nl).append("EOF").append(nl);

        return sb.toString();
    }
}
