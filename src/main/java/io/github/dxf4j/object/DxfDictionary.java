package io.github.dxf4j.object;

import io.github.dxf4j.DxfGroupCode;
import io.github.dxf4j.core.DxfEntity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A DICTIONARY object in the OBJECTS section.
 * Contains name-to-handle entries (group codes 3 + 350).
 */
public class DxfDictionary extends DxfEntity {

    public DxfDictionary(List<DxfGroupCode> groupCodes) {
        super("DICTIONARY", groupCodes);
    }

    /**
     * Get all dictionary entries as name->handle map.
     * Entries are pairs of (group 3 = name, group 350 = handle).
     */
    public Map<String, String> getEntries() {
        Map<String, String> entries = new LinkedHashMap<>();
        List<DxfGroupCode> codes = getGroupCodes();
        for (int i = 0; i < codes.size() - 1; i++) {
            DxfGroupCode gc = codes.get(i);
            if ("  3".equals(gc.getCode())) {
                DxfGroupCode next = codes.get(i + 1);
                if ("350".equals(next.getCode())) {
                    entries.put(gc.getValueTrimmed(), next.getValueTrimmed());
                }
            }
        }
        return entries;
    }

    /**
     * Add a new entry to this dictionary.
     * Appends group codes (3, name) and (350, handle) at the end.
     */
    public void addEntry(String name, String handle) {
        getGroupCodes().add(new DxfGroupCode("  3", name));
        getGroupCodes().add(new DxfGroupCode("350", handle));
    }
}
