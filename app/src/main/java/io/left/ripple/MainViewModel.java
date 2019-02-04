package io.left.ripple;

import static io.left.rightmesh.mesh.MeshManager.DATA_RECEIVED;
import static io.left.rightmesh.mesh.MeshManager.PEER_CHANGED;
import static io.left.ripple.Colour.RED;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import io.left.rightmesh.android.AndroidMeshManager;
import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshManager;
import io.left.rightmesh.mesh.MeshStateListener;
import io.left.rightmesh.util.RightMeshException;

/**
 * De-coupling business logic from Mainactivity to MainViewModel.
 *
 * Reason to extend AndroidViewModel
 *      Allow data to survive configuration changes (Eg: screen rotation, locale changing)
 *      Activity lifecycle consciousness
 */
public class MainViewModel extends AndroidViewModel implements MeshStateListener {
    private static final String TAG = MainViewModel.class.getCanonicalName();

    // Interface object for the RightMesh library.
    private AndroidMeshManager androidMeshManager;

    // Update this to your assigned mesh port.
    private static final int MESH_PORT = 9001;

    // Current background colour
    MutableLiveData<Colour> colour = new MutableLiveData<>();
    MutableLiveData<MeshManager.RightMeshEvent> peerChangedEvent = new MutableLiveData<>();
    // Stores the MeshId of this device so that it doesn't need to be retrieved with a service call.
    MutableLiveData<MeshId> deviceId = new MutableLiveData<>();

    private MeshId currentTargetMeshId = null;

    /**
     * Viewmodel constructor.
     * @param application Application context
     */
    public MainViewModel(@NonNull Application application) {
        super(application);

        colour.setValue(RED);
    }

    void init() {
        // Initialize the RightMesh library with the SSID pattern "Ripple".
        androidMeshManager = AndroidMeshManager.getInstance(getApplication(), this);
    }

    /**
     * Resume RightMesh connection on activity resume.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        try {
            androidMeshManager.resume();
        } catch (RightMeshException.RightMeshServiceDisconnectedException e) {
            Log.e(TAG, "Service disconnected before resuming AndroidMeshManager with message"
                    + e.getMessage());
        }
    }

    /**
     * Close RightMesh connection when activity is destroyed.
     */
    @Override
    protected void onCleared() {
        try {
            androidMeshManager.stop();
        } catch (RightMeshException.RightMeshServiceDisconnectedException e) {
            Log.e(TAG, "Service disconnected before stopping AndroidMeshManager with message"
                    + e.getMessage());
        }
    }

    void showSettingsActivity() {
        try {
            androidMeshManager.showSettingsActivity();
        } catch (RightMeshException ignored) { /* Meh. */ }
    }

    void sendColorMsg(MeshId targetMeshId, Colour msgColor) {
        try {
            if (targetMeshId != null) {
                String payload = targetMeshId.toString() + ":" + msgColor.toString();
                androidMeshManager.sendDataReliable(androidMeshManager.getNextHopPeer(targetMeshId),
                        MESH_PORT, payload.getBytes());
            }
        } catch (RightMeshException.RightMeshServiceDisconnectedException sde) {
            Log.e(TAG, "Service disconnected while sending data, with message: "
                    + sde.getMessage());
        } catch (RightMeshException rme) {
            Log.e(TAG, "Unable to find next hop to peer, with message: " + rme.getMessage());
        }
    }

    /**
     * Send current selected colour to currentTargetMeshId.
     */
    void sendColorMsg() {
        sendColorMsg(currentTargetMeshId,
                colour.getValue());
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
                androidMeshManager.bind(MESH_PORT);

                // Update the peers list.
                deviceId.setValue(meshId);

                // Bind RightMesh event handlers.
                androidMeshManager.on(DATA_RECEIVED, this::receiveColourMessage);
                androidMeshManager.on(PEER_CHANGED, event -> peerChangedEvent.postValue(event));
            } catch (RightMeshException.RightMeshServiceDisconnectedException sde) {
                Log.e(TAG, "Service disconnected while binding, with message: "
                        + sde.getMessage());
            } catch (RightMeshException rme) {
                Log.e(TAG, "MeshPort already bound, with message: " + rme.getMessage());
            }
        }
    }


    /**
     * Changes the background to the supplied colour, if valid.
     *
     * @param colour colour to change to
     */
    void setColour(Colour colour) {
        this.colour.postValue(colour);
    }

    /**
     * Handles an incoming message by changing the screen colour and passing along the message.
     *
     * @param rme generic event passed by RightMesh
     */
    private void receiveColourMessage(MeshManager.RightMeshEvent rme) {
        // Retrieve data from event.
        MeshManager.DataReceivedEvent dre = (MeshManager.DataReceivedEvent) rme;
        String dataString = new String(dre.data);
        int separatorIndex = dataString.indexOf(':');

        // Transmit the message forward if this device is not the intended final recipient.
        MeshId recipient;
        try {
            recipient = MeshId.fromString(dataString.substring(0, separatorIndex));
        } catch (RightMeshException e) {
            Log.e(TAG, "error creating meshId " + e.getMessage());
            e.printStackTrace();
            return;
        }
        if (!recipient.equals(deviceId.getValue())) {
            sendColorMsg(recipient, Colour.valueOf(
                    dataString.substring(separatorIndex + 1)));
        }

        // Change the colour of this phone to illustrate the path of the data.
        setColour(Colour.valueOf(dataString.substring(separatorIndex + 1)));
    }

    void setRecipient(MeshId meshId) {
        currentTargetMeshId = meshId;
    }
}
