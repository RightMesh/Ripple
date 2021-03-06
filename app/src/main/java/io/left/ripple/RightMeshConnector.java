package io.left.ripple;

import static io.left.rightmesh.mesh.MeshManager.DATA_RECEIVED;
import static io.left.rightmesh.mesh.MeshManager.PEER_CHANGED;

import android.content.Context;
import android.util.Log;

import io.left.rightmesh.android.AndroidMeshManager;
import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshManager;
import io.left.rightmesh.mesh.MeshStateListener;
import io.left.rightmesh.util.RightMeshException;

import java.nio.charset.Charset;

/**
 * To communicate with the RightMesh service and redirect to RightMesh wallet activity.
 *
 * After connecting, we can register {@link OnDataReceiveListener}, {@link OnPeerChangedListener},
 * {@link OnConnectSuccessListener} to receive the RightMesh event.
 *
 * Always call {@link RightMeshConnector#stop()} if not interest in keeping connection with
 * RightMesh service anymore.
 */
public class RightMeshConnector implements MeshStateListener {
    private static final String TAG = RightMeshConnector.class.getCanonicalName();

    private final int meshPort;

    // Interface object for the RightMesh library.
    private AndroidMeshManager androidMeshManager;

    //listener for data receiving event
    private OnDataReceiveListener dataReceiveListener;
    //listener for peer changing event
    private OnPeerChangedListener peerchangedListener;
    //listener for connecting successful event
    private OnConnectSuccessListener connectSuccessListener;

    /**
     * Constructor.
     *
     * @param meshPort Rightmesh Port
     */
    public RightMeshConnector(int meshPort) {
        this.meshPort = meshPort;
    }

    /**
     * Connect to Rightmesh.
     *
     * @param context Should pass application context
     */
    public void connect(Context context) {
        androidMeshManager = AndroidMeshManager.getInstance(context,
                this);
    }

    /**
     * Configures event handlers and binds to a port when the RightMesh library is ready.
     *
     * @param meshId ID of this device
     * @param state  new state of the RightMesh library
     */
    @Override
    public void meshStateChanged(MeshId meshId, int state) {
        if (state == SUCCESS) {
            try {
                // Attempt to bind to a port.
                androidMeshManager.bind(meshPort);

                // Update the peers list.
                if (connectSuccessListener != null) {
                    connectSuccessListener.onConnectSuccess(meshId);
                }

                // Bind RightMesh event handlers.
                androidMeshManager.on(DATA_RECEIVED, event -> {
                    if (dataReceiveListener != null) {
                        dataReceiveListener.onDataReceive(event);
                    }
                });
                androidMeshManager.on(PEER_CHANGED, event -> {
                    if (peerchangedListener != null) {
                        peerchangedListener.onPeerChange(event);
                    }
                });
            } catch (RightMeshException.RightMeshServiceDisconnectedException sde) {
                Log.e(TAG, "Service disconnected while binding, with message: "
                        + sde.getMessage());
            } catch (RightMeshException rme) {
                Log.e(TAG, "MeshPort already bound, with message: " + rme.getMessage());
            }
        }
    }

    /**
     * Trigger when want to disconnect with Rightmesh.
     *
     * @throws RightMeshException.RightMeshServiceDisconnectedException when cannot disconnect
     *                                                                  Rightmesh.
     */
    public void stop() throws RightMeshException.RightMeshServiceDisconnectedException {
        androidMeshManager.stop();
    }

    /**
     * Set listener for data receive event.
     *
     * @param listener a callback
     */
    public void setOnDataReceiveListener(OnDataReceiveListener listener) {
        this.dataReceiveListener = listener;
    }

    /**
     * Set listener for peer change event.
     *
     * @param listener a callback
     */
    public void setOnPeerChangedListener(OnPeerChangedListener listener) {
        this.peerchangedListener = listener;
    }

    /**
     * Set listener for my MeshId receiving event.
     *
     * @param listener a callback
     */
    public void setOnConnectSuccessListener(OnConnectSuccessListener listener) {
        this.connectSuccessListener = listener;
    }

    /**
     * Navigate to Rightmesh Wallet app.
     *
     * @throws RightMeshException can't navigate
     */
    public void toRightMeshWalletActivty() throws RightMeshException {
        this.androidMeshManager.showSettingsActivity();
    }

    /**
     * Send data to target device.
     *
     * @param targetMeshId Target meshId.
     * @param payload      data need to send.
     * @throws RightMeshException.RightMeshServiceDisconnectedException Service disconnected.
     * @throws RightMeshException                                       Can't find next hop.
     */
    public void sendDataReliable(MeshId targetMeshId, String payload) throws RightMeshException,
            RightMeshException.RightMeshServiceDisconnectedException {
        androidMeshManager.sendDataReliable(androidMeshManager.getNextHopPeer(targetMeshId),
                meshPort, payload.getBytes(Charset.forName("UTF-8")));
    }

    /**
     * {@link AndroidMeshManager} setter used to testing purpose.
     *
     * @param androidMeshManager - should pass Mock object.
     */
    public void setAndroidMeshManager(AndroidMeshManager androidMeshManager) {
        this.androidMeshManager = androidMeshManager;
    }

    /**
     * Data Receive Listener.
     */
    public interface OnDataReceiveListener {
        void onDataReceive(MeshManager.RightMeshEvent event);
    }

    /**
     * On Peer Change Listener.
     */
    public interface OnPeerChangedListener {
        void onPeerChange(MeshManager.RightMeshEvent event);
    }

    /**
     * On my {@link MeshId} receiving listener.
     */
    public interface OnConnectSuccessListener {
        void onConnectSuccess(MeshId meshId);
    }
}
