package io.left.ripple;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import io.left.rightmesh.android.AndroidMeshManager;
import io.left.rightmesh.id.MeshID;
import io.left.rightmesh.mesh.MeshManager;
import io.left.rightmesh.mesh.MeshManager.DataReceivedEvent;
import io.left.rightmesh.mesh.MeshManager.RightMeshEvent;
import io.left.rightmesh.mesh.MeshStateListener;
import io.left.rightmesh.util.RightMeshException;

import static io.left.rightmesh.android.MeshService.ServiceDisconnectedException;
import static io.left.rightmesh.mesh.MeshManager.DATA_RECEIVED;
import static io.left.rightmesh.mesh.MeshManager.PEER_CHANGED;
import static io.left.ripple.RightMeshRecipientComponent.shortenMeshID;

/**
 * A simple activity to demonstrate the movement of data through a RightMesh mesh network.
 *
 * <p>
 *     Initializes the RightMesh library, allows users to change the background colour of the app,
 *     then propagate that new background colour out to another peer on the mesh, changing the
 *     background colour of the peers that transmit the message along the way.
 * </p>
 */
public class MainActivity extends AppCompatActivity implements MeshStateListener,
        RightMeshRecipientComponent.RecipientChangedListener {
    private static final String TAG = MainActivity.class.getCanonicalName();

    // Interface object for the RightMesh library.
    AndroidMeshManager mm;

    // Current background colour.
    String colour = "RED";

    // MeshID of the peer to send to when the send button is pressed.
    MeshID target = null;

    // Stores the MeshID of this device so that it doesn't need to be retrieved with a service call.
    MeshID deviceID = null;

    // Responsible for allowing the user to select the ping recipient.
    RightMeshRecipientComponent component = null;

    // Adapter for tracking views and the spinner it feeds, both mostly powered by `component`.
    MeshIDAdapter peersListAdapter = null;

    //
    // ANDROID LIFECYCLE MANAGEMENT
    //

    /**
     * Set Android UI event handlers and connect to the RightMesh library when the
     * activity initializes.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View buttonSend = findViewById(R.id.button_send);
        buttonSend.setOnClickListener(v -> sendMessage(target, colour));

        View buttonSendAll = findViewById(R.id.button_sendAll);
        buttonSendAll.setOnClickListener(v -> sendAll());

        // Display the RightMesh settings activity when the send button is tapped and held.
        buttonSend.setLongClickable(true);
        buttonSend.setOnLongClickListener(v -> {
            try {
                mm.showSettingsActivity();
            } catch (RightMeshException ignored) { /* Meh. */ }
            return true;
        });

        // Change the background colour when the respective colour buttons are pressed.
        findViewById(R.id.button_red).setOnClickListener(v -> setColour("RED"));
        findViewById(R.id.button_green).setOnClickListener(v -> setColour("GREEN"));
        findViewById(R.id.button_blue).setOnClickListener(v -> setColour("BLUE"));

        // Set up the recipient selection spinner.
        peersListAdapter = new MeshIDAdapter(this);
        component = (RightMeshRecipientComponent) getFragmentManager()
                .findFragmentById(R.id.recipient_component);
        component.setSpinnerAdapter(peersListAdapter);
        component.setOnRecipientChangedListener(this);

        // Initialize the RightMesh library with the SSID pattern "Ripple".
        mm = AndroidMeshManager.getInstance(MainActivity.this, MainActivity.this, "Ripple");
    }

    /**
     * Resume RightMesh connection on activity resume.
     */
    @Override
    protected void onResume() {
        super.onResume();
        try {
            mm.resume();
        } catch (ServiceDisconnectedException e) {
            Log.e(TAG, "Service disconnected before resuming AndroidMeshManager with message"
                    + e.getMessage());
        }
    }

    /**
     * Close RightMesh connection when activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mm.stop();
        } catch (ServiceDisconnectedException e) {
            Log.e(TAG, "Service disconnected before stopping AndroidMeshManager with message"
                    + e.getMessage());
        }
    }


    //
    // ANDROID UI EVENT HANDLERS
    //

    /**
     * Sends a message containing the specified recipient's ID and the colour to change to to the
     * NEXT HOP between this device and the recipient.
     *
     * @param recipient final intended recipient of the message
     * @param messageColour to send
     */
    private void sendMessage(MeshID recipient, String messageColour) {
        try {
            if (recipient != null) {
                String payload = recipient.toString() + ":" + messageColour;
                mm.sendDataReliable(mm.getNextHopPeer(recipient), 1234, payload.getBytes());
            }
        } catch(ServiceDisconnectedException sde) {
            Log.e(TAG, "Service disconnected while sending data, with message: "
                    + sde.getMessage());
        } catch (RightMeshException rme) {
            Log.e(TAG, "Unable to find next hop to peer, with message: " + rme.getMessage());
        }
    }

    /**
     * Sends a message (using {@link MainActivity#sendMessage(MeshID, String)}) to all peers.
     */
    private void sendAll() {
        for (int i = 0; i < peersListAdapter.getCount(); i++) {
            MeshID peer = peersListAdapter.getItem(i);
            sendMessage(peer, colour);
        }
    }

    /**
     * Changes the background to the supplied colour, if valid.
     *
     * @param colour colour to change to
     */
    private void setColour(String colour) {
        final int colourCode = getColourID(colour);

        // Change colour if the code is valid.
        if (colourCode != -1) {
            this.colour = colour;
            runOnUiThread(() -> MainActivity.this.findViewById(R.id.layout_background)
                    .setBackgroundColor(ContextCompat.getColor(this, colourCode)));
        }
    }


    //
    // RIGHTMESH EVENT HANDLERS
    //

    /**
     * Configures event handlers and binds to a port when the RightMesh library is ready.
     *
     * @param meshID ID of this device
     * @param state new state of the RightMesh library
     */
    @Override
    public void meshStateChanged(MeshID meshID, int state) {
        deviceID = meshID;
        if (state == MeshStateListener.SUCCESS) {
            try {
                // Attempt to bind to a port.
                MeshManager.Result r = mm.bind(1234);
                if (r.result == MeshManager.FAILURE) {
                    component.setStatus("Failed to bind.");
                    return;
                } else {
                    component.setStatus("This device's ID is " + shortenMeshID(deviceID));
                }

                // Update the peers list.
                peersListAdapter.add(deviceID);
                peersListAdapter.setDeviceID(deviceID);
                runOnUiThread(() -> peersListAdapter.notifyDataSetChanged());

                // Bind RightMesh event handlers.
                mm.on(DATA_RECEIVED, MainActivity.this::receiveColourMessage);
                mm.on(PEER_CHANGED, e -> runOnUiThread(() -> component.updatePeersList(e)));
            } catch (ServiceDisconnectedException sde) {
                Log.e(TAG, "Service disconnected while binding, with message: "
                        + sde.getMessage());
            } catch (RightMeshException rme) {
                Log.e(TAG, "MeshPort already bound, with message: " + rme.getMessage());
            }
        }
    }

    /**
     * Handles an incoming message by changing the screen colour and passing along the message.
     *
     * @param rme generic event passed by RightMesh
     */
    private void receiveColourMessage(RightMeshEvent rme) {
        // Retrieve data from event.
        DataReceivedEvent dre = (DataReceivedEvent) rme;
        String dataString = new String(dre.data);
        int separatorIndex = dataString.indexOf(':');

        // Transmit the message forward if this device is not the intended final recipient.
        try {
            MeshID recipient = new MeshID(dataString.substring(0, separatorIndex));
            if (!recipient.equals(deviceID)) {
                sendMessage(recipient, dataString.substring(separatorIndex+1));
            }
        } catch (RightMeshException rmex) {
            Log.e(TAG, "Failed to parse MeshID from string, with message: " + rmex.getMessage());
        }

        // Change the colour of this phone to illustrate the path of the data.
        setColour(dataString.substring(separatorIndex+1));
    }

    /**
     * Update the message target when the user selects a new recipient.
     *
     * @param recipient new target
     */
    @Override
    public void onChange(MeshID recipient) {
        target = recipient;
    }


    //
    // HELPER METHODS
    //

    /**
     * Returns the Android resource ID for a colour from its String representation.
     *
     * @param colour either RED, GREEN, or BLUE
     * @return Android resource ID for the desired colour, or -1 if invalid
     */
    private int getColourID(String colour) {
        switch (colour.toUpperCase()) {
            case "RED": return R.color.red;
            case "GREEN": return R.color.green;
            case "BLUE": return R.color.blue;
            default: return -1;
        }
    }
}