package io.github.dxf4j;

import io.github.dxf4j.core.*;
import io.github.dxf4j.handle.HandleAllocator;
import io.github.dxf4j.object.DxfDictionary;
import java.io.*;
import java.util.*;

/**
 * The main entry point for reading, modifying and writing DXF files.
 * <p>
 * Usage:
 * <pre>
 * DxfDocument doc = DxfDocument.read(new File("template.dxf"));
 * // ... modify document ...
 * doc.write(new File("output.dxf"));
 * </pre>
 */
public class DxfDocument {

    private final Map<String, String> headerVars;
    private final List<DxfGroupCode> headerRaw;
    private final List<DxfSection> sections;
    private final String nl;

    public DxfDocument(Map<String, String> headerVars, List<DxfGroupCode> headerRaw,
                       List<DxfSection> sections, String nl) {
        this.headerVars = headerVars;
        this.headerRaw = headerRaw;
        this.sections = sections;
        this.nl = nl;
    }

    /** Read a DXF file. */
    public static DxfDocument read(File file) throws IOException {
        byte[] bytes = readBytes(file);
        String content = new String(bytes, "UTF-8");
        DxfReader reader = new DxfReader(content);
        return reader.parse();
    }

    /** Read from an input stream. */
    public static DxfDocument read(InputStream is) throws IOException {
        byte[] bytes = readAllBytes(is);
        String content = new String(bytes, "UTF-8");
        DxfReader reader = new DxfReader(content);
        return reader.parse();
    }

    /** Parse from a string. */
    public static DxfDocument parse(String content) {
        DxfReader reader = new DxfReader(content);
        return reader.parse();
    }

    /** Write the document to a file. */
    public void write(File file) throws IOException {
        DxfWriter writer = new DxfWriter(this);
        String output = writer.write();
        file.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(output.getBytes("UTF-8"));
        }
    }

    /** Serialize to string. */
    public String serialize() {
        return new DxfWriter(this).write();
    }

    // --- Header access ---

    /** Get a header variable value (e.g. "$HANDSEED", "$DWGCODEPAGE"). */
    public String getHeaderVar(String varName) {
        return headerVars.get(varName);
    }

    /** Set a header variable value. Updates the raw group codes too. */
    public void setHeaderVar(String varName, String newValue) {
        String oldValue = headerVars.get(varName);
        headerVars.put(varName, newValue);

        // Update raw group codes
        for (int i = 0; i < headerRaw.size() - 1; i++) {
            DxfGroupCode gc = headerRaw.get(i);
            if (gc.getCodeInt() == 9 && varName.equals(gc.getValueTrimmed())) {
                DxfGroupCode oldValueGc = headerRaw.get(i + 1);
                headerRaw.set(i + 1, new DxfGroupCode(oldValueGc.getCode(), newValue));
                break;
            }
        }
    }

    /** Get the $HANDSEED value as a long. */
    public long getHandSeed() {
        String val = getHeaderVar("$HANDSEED");
        if (val == null) return 0xFFF0;
        return Long.parseLong(val.trim(), 16);
    }

    /** Update $HANDSEED. */
    public void setHandSeed(long value) {
        setHeaderVar("$HANDSEED", Long.toHexString(value).toUpperCase());
    }

    /** Create a HandleAllocator starting from $HANDSEED. */
    public HandleAllocator createHandleAllocator() {
        return new HandleAllocator(getHandSeed());
    }

    /** Get the declared codepage ($DWGCODEPAGE). */
    public String getCodepage() {
        String cp = getHeaderVar("$DWGCODEPAGE");
        return cp != null ? cp : "ANSI_1252";
    }

    // --- Section access ---

    public List<DxfSection> getSections() {
        return sections;
    }

    public List<DxfGroupCode> getHeaderRaw() {
        return headerRaw;
    }

    public Map<String, String> getHeaderVars() {
        return headerVars;
    }

    public String getNewline() {
        return nl;
    }

    /** Get a section by type. */
    public DxfSection getSection(DxfSectionType type) {
        for (DxfSection s : sections) {
            if (s.getType() == type) return s;
        }
        return null;
    }

    /** Get the ENTITIES section. */
    public DxfSection getEntitiesSection() {
        return getSection(DxfSectionType.ENTITIES);
    }

    /** Get the OBJECTS section. */
    public DxfSection getObjectsSection() {
        return getSection(DxfSectionType.OBJECTS);
    }

    /** Find a DICTIONARY in the OBJECTS section by handle. */
    public DxfDictionary findDictionary(String handle) {
        DxfSection objects = getObjectsSection();
        if (objects == null) return null;
        DxfEntity e = objects.findByHandle(handle);
        return (e instanceof DxfDictionary) ? (DxfDictionary) e : null;
    }

    /**
     * Find the ACAD_IMAGE_DICT dictionary handle.
     * Searches the root dictionary for the "ACAD_IMAGE_DICT" entry.
     */
    public String findImageDictHandle() {
        DxfSection objects = getObjectsSection();
        if (objects == null) return null;
        for (DxfEntity e : objects.getEntities()) {
            if (e instanceof DxfDictionary) {
                DxfDictionary dict = (DxfDictionary) e;
                Map<String, String> entries = dict.getEntries();
                if (entries.containsKey("ACAD_IMAGE_DICT")) {
                    return entries.get("ACAD_IMAGE_DICT");
                }
            }
        }
        return null;
    }

    // --- Utilities ---

    private static byte[] readBytes(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            int offset = 0;
            while (offset < bytes.length) {
                int read = fis.read(bytes, offset, bytes.length - offset);
                if (read < 0) break;
                offset += read;
            }
        }
        return bytes;
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) >= 0) {
            bos.write(buf, 0, n);
        }
        return bos.toByteArray();
    }
}
