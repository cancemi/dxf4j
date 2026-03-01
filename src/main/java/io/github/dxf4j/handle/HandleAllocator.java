package io.github.dxf4j.handle;

import io.github.dxf4j.core.DxfHandle;

/**
 * Manages $HANDSEED allocation for new DXF entities and objects.
 * Each call to {@link #allocate()} returns the next available handle
 * and advances the seed.
 */
public class HandleAllocator {

    private long nextHandle;

    public HandleAllocator(long handSeed) {
        this.nextHandle = handSeed;
    }

    public HandleAllocator(DxfHandle handSeed) {
        this.nextHandle = handSeed.toLong();
    }

    /** Allocate and return the next available handle. */
    public DxfHandle allocate() {
        DxfHandle h = new DxfHandle(nextHandle);
        nextHandle++;
        return h;
    }

    /** Allocate N consecutive handles and return them as an array. */
    public DxfHandle[] allocate(int count) {
        DxfHandle[] handles = new DxfHandle[count];
        for (int i = 0; i < count; i++) {
            handles[i] = allocate();
        }
        return handles;
    }

    /** The current $HANDSEED value (next handle to be allocated). */
    public DxfHandle getCurrentSeed() {
        return new DxfHandle(nextHandle);
    }
}
