package io.github.dxf4j.template;

import io.github.dxf4j.DxfDocument;
import io.github.dxf4j.DxfGroupCode;
import io.github.dxf4j.core.DxfEntity;
import io.github.dxf4j.core.DxfSection;
import java.util.Map;

/**
 * Replaces placeholder text values in TEXT entity group codes throughout a DXF document.
 * <p>
 * This operates on parsed group code values, replacing exact matches of placeholder
 * strings with their replacement values. Values are converted to uppercase for DXF.
 * <p>
 * Usage:
 * <pre>
 * DxfDocument doc = DxfDocument.read(new File("template.dxf"));
 * DxfTemplateEngine engine = new DxfTemplateEngine(doc);
 * Map&lt;String, String&gt; fields = new HashMap&lt;&gt;();
 * fields.put("CLIENTE_NOME", "Mario Rossi");
 * fields.put("POTENZA_KW", "6.0");
 * engine.replaceAll(fields);
 * doc.write(new File("output.dxf"));
 * </pre>
 */
public class DxfTemplateEngine {

    private final DxfDocument doc;

    public DxfTemplateEngine(DxfDocument doc) {
        this.doc = doc;
    }

    /**
     * Replace all placeholders in TEXT entity values throughout the document.
     * Replacement values are converted to uppercase (DXF convention).
     *
     * @param fields map of placeholder name to replacement value
     * @return number of replacements made
     */
    public int replaceAll(Map<String, String> fields) {
        return replaceAll(fields, true);
    }

    /**
     * Replace all placeholders in TEXT entity values throughout the document.
     *
     * @param fields    map of placeholder name to replacement value
     * @param uppercase if true, convert replacement values to uppercase
     * @return number of replacements made
     */
    public int replaceAll(Map<String, String> fields, boolean uppercase) {
        int count = 0;
        for (DxfSection section : doc.getSections()) {
            if (!section.isParsed()) continue;
            for (DxfEntity entity : section.getEntities()) {
                count += replaceInEntity(entity, fields, uppercase);
            }
        }
        // Also replace in header raw group codes
        count += replaceInRawCodes(doc.getHeaderRaw(), fields, uppercase);
        return count;
    }

    private int replaceInEntity(DxfEntity entity, Map<String, String> fields, boolean uppercase) {
        int count = 0;
        java.util.List<DxfGroupCode> codes = entity.getGroupCodes();
        for (int i = 0; i < codes.size(); i++) {
            DxfGroupCode gc = codes.get(i);
            String value = gc.getValue();
            String newValue = value;
            boolean changed = false;

            for (Map.Entry<String, String> entry : fields.entrySet()) {
                if (newValue.contains(entry.getKey())) {
                    String replacement = entry.getValue();
                    if (replacement == null || " ".equals(replacement)) {
                        replacement = "";
                    }
                    if (uppercase) {
                        replacement = replacement.toUpperCase();
                    }
                    newValue = newValue.replace(entry.getKey(), replacement);
                    changed = true;
                }
            }

            if (changed) {
                codes.set(i, new DxfGroupCode(gc.getCode(), newValue));
                count++;
            }
        }
        return count;
    }

    private int replaceInRawCodes(java.util.List<DxfGroupCode> rawCodes, Map<String, String> fields, boolean uppercase) {
        int count = 0;
        for (int i = 0; i < rawCodes.size(); i++) {
            DxfGroupCode gc = rawCodes.get(i);
            String value = gc.getValue();
            String newValue = value;
            boolean changed = false;

            for (Map.Entry<String, String> entry : fields.entrySet()) {
                if (newValue.contains(entry.getKey())) {
                    String replacement = entry.getValue();
                    if (replacement == null || " ".equals(replacement)) {
                        replacement = "";
                    }
                    if (uppercase) {
                        replacement = replacement.toUpperCase();
                    }
                    newValue = newValue.replace(entry.getKey(), replacement);
                    changed = true;
                }
            }

            if (changed) {
                rawCodes.set(i, new DxfGroupCode(gc.getCode(), newValue));
                count++;
            }
        }
        return count;
    }
}
