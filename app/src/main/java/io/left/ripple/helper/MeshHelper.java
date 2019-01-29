package io.left.ripple.helper;

import io.left.rightmesh.id.MeshId;

public class MeshHelper {

    private static MeshHelper instance = null;

    private MeshHelper() {
    }

    public static MeshHelper getInstance(){
        if(instance == null)
            instance = new MeshHelper();

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
