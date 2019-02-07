package io.left.ripple.views;

import static io.left.rightmesh.mesh.MeshManager.ADDED;
import static io.left.rightmesh.mesh.MeshManager.REMOVED;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshManager.PeerChangedEvent;
import io.left.rightmesh.mesh.MeshManager.RightMeshEvent;
import io.left.ripple.MeshIdAdapter;
import io.left.ripple.R;

/**
 * Fragment that keeps track of connected peers when registered to listen to PEER_CHANGED events,
 * and allows the user to select one of these peers as a message recipient.
 */
public class CustomViewRightMeshRecipient extends ConstraintLayout
        implements AdapterView.OnItemSelectedListener {

    // Keeps track of the most recently tracked recipient, in case it disconnects and is removed
    // from the list.
    private MeshId recipientId;

    // UI Elements
    private Spinner spinnerRecipient;
    private TextView tvDeviceStatusLabel;
    private TextView tvNetworkStatusLabel;

    // Keeps track of peers and populates the spinnerRecipient.
    private MeshIdAdapter adapterRecipient;

    private RecipientChangedListener onRecipientChangedListener = null;

    /**
     * Used when instantiating Views programmatically.
     *
     * @param context View context
     */
    public CustomViewRightMeshRecipient(Context context) {
        super(context);
        init(context);
    }

    /**
     * {@link CustomViewRightMeshRecipient} constructor<br>.
     *
     * Trigger in xml declaration.
     *
     * @param context View context
     * @param attrs attribute
     */
    public CustomViewRightMeshRecipient(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * {@link CustomViewRightMeshRecipient} constructor<br>.
     *
     * Trigger in xml declaration with android:style attribute
     *
     * @param context View context
     * @param attrs Attribute
     * @param defStyleAttr applied style
     */
    public CustomViewRightMeshRecipient(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * Init {@link CustomViewRightMeshRecipient}.
     * @param context View context.
     */
    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.customview_component_rightmesh, this, true);

        this.recipientId = null;

        spinnerRecipient = findViewById(R.id.spinner_recipient);
        spinnerRecipient.setOnItemSelectedListener(this);

        tvDeviceStatusLabel = findViewById(R.id.tv_device_status);
        tvNetworkStatusLabel = findViewById(R.id.tv_network_status);

        adapterRecipient = new MeshIdAdapter(getContext());
        spinnerRecipient.setAdapter(adapterRecipient);
    }

    public MeshId getRecipientId() {
        return recipientId;
    }

    /**
     * Sets the contents of the status label (runs on the activity's UI thread).
     *
     * @param status new contents of the status label
     */
    public void setStatus(String status) {
        tvDeviceStatusLabel.setText(status);
    }

    /**
     * Set a listener for updates to the value of the selected recipient.
     *
     * @param listener listener to be notified
     */
    public void setOnRecipientChangedListener(RecipientChangedListener listener) {
        onRecipientChangedListener = listener;
    }

    /**
     * When the selected item in the recipient selection spinnerRecipient changes,
     * update the local variable and notify the listener.
     *
     * @param parent   the spinnerRecipient
     * @param view     view of the selected peer
     * @param position position of the selected peer in the list, used to get the actual object
     * @param id       passed by Android
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        recipientId = adapterRecipient.getItem(position);
        onRecipientChangedListener.onRecipientChanged(recipientId);
    }

    /**
     * If nothing is selected in the recipient selection spinnerRecipient,
     * and there are peers aside from this device to select,
     * arbitrarily select the first such peer in the list.
     *
     * @param parent the spinnerRecipient
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        if (adapterRecipient.getCount() > 1) {
            parent.setSelection(1);
        }
    }

    /**
     * Update the mesh peers available in the recipient selection spinnerRecipient when
     * mesh peers are discovered or change state.
     *
     * @param rme event passed from RightMesh
     */
    public void updatePeersList(RightMeshEvent rme) {
        PeerChangedEvent pce = (PeerChangedEvent) rme;
        MeshId peer = rme.peerUuid;

        if (pce.state == ADDED && !adapterRecipient.contains(peer)) {
            // Add the peer to the list if it is new.
            adapterRecipient.add(peer);

            if (adapterRecipient.getCount() == 2) {
                // If this is the first peer in the list, automatically select it.
                adapterRecipient.notifyDataSetChanged();
                spinnerRecipient.setSelection(1);
            }
        } else if (pce.state == REMOVED) {
            // Remove a peer when it disconnects.
            adapterRecipient.remove(peer);

            // Toast if the recipient has been disconnected.
            if (peer.equals(recipientId)) {
                Toast.makeText(getContext(),
                        "Recipient has disconnected.", Toast.LENGTH_SHORT).show();
            }
        }

        // Update the connected devices label if there are other devices connected.
        if (adapterRecipient.getCount() > 1) {
            // Get string resource with number of connected devices.
            int numConnectedDevices = adapterRecipient.getCount() - 1;
            String newText = getResources().getQuantityString(
                    R.plurals.number_of_connected_devices,
                    numConnectedDevices, numConnectedDevices);

            tvNetworkStatusLabel.setText(newText);
        } else {
            tvNetworkStatusLabel.setText("");
        }
    }

    /**
     * Add and display new connected device in spinner.
     * @param newMeshId MeshId
     */
    public void addNewDevice(MeshId newMeshId) {
        adapterRecipient.add(newMeshId);
        adapterRecipient.setDeviceId(newMeshId);
        adapterRecipient.notifyDataSetChanged();
    }

    /**
     * Get spinner adapter.
     * @return Current {@link MeshIdAdapter}.
     */
    public MeshIdAdapter getAdapter() {
        return adapterRecipient;
    }

    public interface RecipientChangedListener {
        /**
         * Change and add the recipient to spinner.
         * @param recipient Connected device.
         */
        void onRecipientChanged(MeshId recipient);
    }
}