package io.github.dxf4j;

/**
 * An atomic (code, value) pair in a DXF file.
 * DXF files are sequences of group codes: a numeric code on one line
 * followed by its value on the next line.
 */
public class DxfGroupCode {

    private final String code;
    private final String value;

    public DxfGroupCode(String code, String value) {
        this.code = code;
        this.value = value;
    }

    /** The group code string, preserving original padding (e.g. "  0", "  5", "100"). */
    public String getCode() {
        return code;
    }

    /** The numeric code value (trimmed). */
    public int getCodeInt() {
        return Integer.parseInt(code.trim());
    }

    /** The value string, preserving original formatting. */
    public String getValue() {
        return value;
    }

    /** The value string, trimmed. */
    public String getValueTrimmed() {
        return value.trim();
    }

    @Override
    public String toString() {
        return code + " = " + value;
    }
}
