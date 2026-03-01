package io.github.dxf4j;

import io.github.dxf4j.core.*;
import io.github.dxf4j.entity.*;
import io.github.dxf4j.object.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser that converts raw DXF text content into a {@link DxfDocument}.
 * <p>
 * Parsing strategy:
 * <ul>
 *   <li>HEADER: parsed into header variables ($HANDSEED, $DWGCODEPAGE, etc.)</li>
 *   <li>ENTITIES: parsed into typed entity objects (DxfText, DxfImage, etc.)</li>
 *   <li>OBJECTS: parsed into typed objects (DxfDictionary, DxfImageDef, etc.)</li>
 *   <li>CLASSES, TABLES, BLOCKS, ACDSDATA: stored as raw group codes (lossless passthrough)</li>
 * </ul>
 */
public class DxfReader {

    private final String content;
    private final String nl;
    private int pos;

    public DxfReader(String content) {
        this.content = content;
        this.nl = content.contains("\r\n") ? "\r\n" : "\n";
        this.pos = 0;
    }

    /** Parse the DXF content into a DxfDocument. */
    public DxfDocument parse() {
        Map<String, String> headerVars = new LinkedHashMap<>();
        List<DxfGroupCode> headerRaw = new ArrayList<>();
        List<DxfSection> sections = new ArrayList<>();

        while (pos < content.length()) {
            DxfGroupCode gc = readGroupCode();
            if (gc == null) break;

            if (gc.getCodeInt() == 0 && "SECTION".equals(gc.getValueTrimmed())) {
                DxfGroupCode nameGc = readGroupCode();
                if (nameGc == null) break;
                String sectionName = nameGc.getValueTrimmed();
                DxfSectionType type = DxfSectionType.fromName(sectionName);

                if (type == DxfSectionType.HEADER) {
                    headerRaw = readRawUntilEndSec();
                    headerVars = parseHeaderVars(headerRaw);
                } else if (type == DxfSectionType.ENTITIES) {
                    List<DxfEntity> entities = readEntities();
                    sections.add(new DxfSection(type, sectionName, entities));
                } else if (type == DxfSectionType.OBJECTS) {
                    List<DxfEntity> objects = readObjects();
                    sections.add(new DxfSection(type, sectionName, objects));
                } else {
                    // Raw passthrough
                    List<DxfGroupCode> raw = readRawUntilEndSec();
                    sections.add(new DxfSection(type, sectionName, raw, true));
                }
            } else if (gc.getCodeInt() == 0 && "EOF".equals(gc.getValueTrimmed())) {
                break;
            }
        }

        return new DxfDocument(headerVars, headerRaw, sections, nl);
    }

    /** Read group codes until ENDSEC, returning all entities found. */
    private List<DxfEntity> readEntities() {
        List<DxfEntity> entities = new ArrayList<>();
        List<DxfGroupCode> currentCodes = new ArrayList<>();
        String currentType = null;

        while (pos < content.length()) {
            DxfGroupCode gc = readGroupCode();
            if (gc == null) break;

            if (gc.getCodeInt() == 0) {
                // Flush previous entity
                if (currentType != null) {
                    entities.add(createEntity(currentType, currentCodes));
                }

                String typeName = gc.getValueTrimmed();
                if ("ENDSEC".equals(typeName)) {
                    break;
                }

                currentType = typeName;
                currentCodes = new ArrayList<>();
            } else {
                currentCodes.add(gc);
            }
        }

        return entities;
    }

    /** Read group codes until ENDSEC, returning all objects found. */
    private List<DxfEntity> readObjects() {
        List<DxfEntity> objects = new ArrayList<>();
        List<DxfGroupCode> currentCodes = new ArrayList<>();
        String currentType = null;

        while (pos < content.length()) {
            DxfGroupCode gc = readGroupCode();
            if (gc == null) break;

            if (gc.getCodeInt() == 0) {
                if (currentType != null) {
                    objects.add(createObject(currentType, currentCodes));
                }

                String typeName = gc.getValueTrimmed();
                if ("ENDSEC".equals(typeName)) {
                    break;
                }

                currentType = typeName;
                currentCodes = new ArrayList<>();
            } else {
                currentCodes.add(gc);
            }
        }

        return objects;
    }

    /** Read raw group codes until ENDSEC. */
    private List<DxfGroupCode> readRawUntilEndSec() {
        List<DxfGroupCode> raw = new ArrayList<>();

        while (pos < content.length()) {
            DxfGroupCode gc = readGroupCode();
            if (gc == null) break;

            if (gc.getCodeInt() == 0 && "ENDSEC".equals(gc.getValueTrimmed())) {
                break;
            }

            raw.add(gc);
        }

        return raw;
    }

    /** Read one group code (code line + value line) from the current position. */
    private DxfGroupCode readGroupCode() {
        if (pos >= content.length()) return null;

        int codeEnd = content.indexOf(nl, pos);
        if (codeEnd < 0) return null;
        String code = content.substring(pos, codeEnd);
        pos = codeEnd + nl.length();

        if (pos >= content.length()) return null;
        int valueEnd = content.indexOf(nl, pos);
        String value;
        if (valueEnd < 0) {
            value = content.substring(pos);
            pos = content.length();
        } else {
            value = content.substring(pos, valueEnd);
            pos = valueEnd + nl.length();
        }

        return new DxfGroupCode(code, value);
    }

    /** Create a typed entity from the entity type name and group codes. */
    private DxfEntity createEntity(String type, List<DxfGroupCode> codes) {
        switch (type) {
            case "TEXT":
            case "MTEXT":
                return new DxfText(codes);
            case "IMAGE":
                return new DxfImage(codes);
            default:
                return new DxfGenericEntity(type, codes);
        }
    }

    /** Create a typed object from the object type name and group codes. */
    private DxfEntity createObject(String type, List<DxfGroupCode> codes) {
        switch (type) {
            case "DICTIONARY":
                return new DxfDictionary(codes);
            case "IMAGEDEF":
                return new DxfImageDef(codes);
            case "IMAGEDEF_REACTOR":
                return new DxfImageDefReactor(codes);
            default:
                return new DxfGenericObject(type, codes);
        }
    }

    /** Parse header variables from raw group codes. */
    private Map<String, String> parseHeaderVars(List<DxfGroupCode> raw) {
        Map<String, String> vars = new LinkedHashMap<>();
        for (int i = 0; i < raw.size() - 1; i++) {
            DxfGroupCode gc = raw.get(i);
            if (gc.getCodeInt() == 9) {
                String varName = gc.getValueTrimmed();
                DxfGroupCode valueGc = raw.get(i + 1);
                vars.put(varName, valueGc.getValueTrimmed());
            }
        }
        return vars;
    }
}
