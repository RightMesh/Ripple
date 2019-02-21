package io.left.ripple.helper;

import io.left.rightmesh.id.MeshId;

public final class MeshHelper {

    private static MeshHelper instance = null;

    private MeshHelper() {
    }

    /**
     * Get Singleton instance.
     *
     * Avoid using double check locking pattern
     * http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
     *
     * @return Meshhelper
     */
    public static MeshHelper getInstance() {
        synchronized (MeshHelper.class) {
            if (instance == null) {
                // if instance is null, initialize
                instance = new MeshHelper();
            }
        }

        return instance;
    }

    /**
     * Truncates MeshIds to 8 characters long.
     *
     * @param id to get string of
     * @return truncated string
     */
    public String shortenMeshId(MeshId id) {
        return id.toString().substring(0, 10) + "...";
    }
}
