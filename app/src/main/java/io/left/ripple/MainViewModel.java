package io.left.ripple;

import static io.left.ripple.Colour.RED;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshManager;
import io.left.rightmesh.util.RightMeshException;

/**
 * De-coupling business logic from Mainactivity to MainViewModel.
 * <p>
 * Reason to extend AndroidViewModel
 * Allow data to survive configuration changes (Eg: screen rotation, locale changing)
 * Activity lifecycle consciousness
 */
public class MainViewModel extends AndroidViewModel {
    private static final String TAG = MainViewModel.class.getCanonicalName();

    // Update this to your assigned mesh port.
    private static final int MESH_PORT = 9001;

    private RightMeshConnector rmConnector;

    // Current background colour
    MutableLiveData<Colour> colour = new MutableLiveData<>();
    MutableLiveData<MeshManager.RightMeshEvent> peerChangedEvent = new MutableLiveData<>();
    // Stores the MeshId of this device so that it doesn't need to be retrieved with a service call.
    MutableLiveData<MeshId> myMeshId = new MutableLiveData<>();

    private MeshId currentTargetMeshId = null;

    /**
     * Viewmodel constructor.
     *
     * @param application Application context
     */
    public MainViewModel(@NonNull Application application) {
        super(application);

        colour.setValue(RED);
        rmConnector = new RightMeshConnector(MESH_PORT);
    }

    /**
     * Init {@link MainViewModel}.
     */
    void init() {
        // Initialize the RightMesh library with the SSID pattern "Ripple".
        rmConnector.connect(getApplication());

        rmConnector.setOnDataReceiveListener(event -> receiveColourMessage(event));
        rmConnector.setOnPeerChangedListener(event -> peerChangedEvent.postValue(event));
        rmConnector.setOnMyMeshIdReceivingListener(meshId -> myMeshId.setValue(meshId));
    }

    /**
     * {@link RightMeshConnector} Setter.
     * (using for testing)
     *
     * @param rmConnector {@link RightMeshConnector}
     */
    public void setRMConnector(RightMeshConnector rmConnector) {
        this.rmConnector = rmConnector;
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
     * Set {@link MeshId} that will receive msg.
     *
     * @param targetMeshId Target MeshId
     */
    void setRecipient(MeshId targetMeshId) {
        currentTargetMeshId = targetMeshId;
    }

    /**
     * Open Rightmesh Setting page.
     */
    void toRightMeshWalletActivty() {
        try {
            rmConnector.toRightMeshWalletActivty();
        } catch (RightMeshException e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * Send Color to target device.
     *
     * @param targetMeshId MeshId will receive this msg.
     * @param msgColor     Message color.
     */
    void sendColorMsg(MeshId targetMeshId, Colour msgColor) {
        try {
            if (targetMeshId != null) {
                String payload = targetMeshId.toString() + ":" + msgColor.toString();
                rmConnector.sentDataReliable(targetMeshId, payload);
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
        if (!recipient.equals(myMeshId.getValue())) {
            sendColorMsg(recipient, Colour.valueOf(
                    dataString.substring(separatorIndex + 1)));
        }

        // Change the colour of this phone to illustrate the path of the data.
        setColour(Colour.valueOf(dataString.substring(separatorIndex + 1)));
    }

    /**
     * Close RightMesh connection when activity is destroyed.
     */
    @Override
    protected void onCleared() {
        try {
            rmConnector.stop();
        } catch (RightMeshException.RightMeshServiceDisconnectedException e) {
            Log.e(TAG, "Service disconnected before stopping AndroidMeshManager with message"
                    + e.getMessage());
        }
    }
}
