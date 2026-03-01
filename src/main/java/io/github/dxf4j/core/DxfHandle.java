package io.github.dxf4j.core;

/**
 * Value type for a DXF hexadecimal handle.
 * Handles are unique identifiers for entities and objects in a DXF file.
 */
public class DxfHandle {

    private final long value;

    public DxfHandle(long value) {
        this.value = value;
    }

    public DxfHandle(String hex) {
        this.value = Long.parseLong(hex.trim(), 16);
    }

    public long toLong() {
        return value;
    }

    /** Returns the uppercase hex string (e.g. "1A3"). */
    public String toHex() {
        return Long.toHexString(value).toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DxfHandle)) return false;
        return value == ((DxfHandle) o).value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public String toString() {
        return toHex();
    }
}
