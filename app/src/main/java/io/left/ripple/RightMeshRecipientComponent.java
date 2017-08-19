package io.left.ripple;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import io.left.rightmesh.id.MeshID;
import io.left.rightmesh.mesh.MeshManager.PeerChangedEvent;
import io.left.rightmesh.mesh.MeshManager.RightMeshEvent;

import static io.left.rightmesh.mesh.PeerListener.ADDED;
import static io.left.rightmesh.mesh.PeerListener.REMOVED;

/**
 * Fragment that keeps track of connected peers when registered to listen to PEER_CHANGED events,
 * and allows the user to select one of these peers as a message recipient.
 */
public class RightMeshRecipientComponent extends Fragment
        implements AdapterView.OnItemSelectedListener {

    // Keeps track of the most recently tracked recipient, in case it disconnects and is removed
    // from the list.
    private MeshID recipientID = null;

    // UI Elements
    private Spinner spinner;
    private TextView deviceStatusLabel;
    private TextView networkStatusLabel;

    // Keeps track of peers and populates the spinner.
    private MeshIDAdapter spinnerAdapter;

    public void setSpinnerAdapter(MeshIDAdapter spinnerAdapter) {
        this.spinnerAdapter = spinnerAdapter;
        spinner.setAdapter(spinnerAdapter);
    }

    /**
     * Sets the contents of the status label (runs on the activity's UI thread).
     *
     * @param status new contents of the status label
     */
    public void setStatus(String status) {
        getActivity().runOnUiThread(() -> deviceStatusLabel.setText(status));
    }


    //
    // EXTERNAL EVENT HANDLING
    //

    /**
     * Interface for communicating with whatever party is interested in hearing about the output
     * of this fragment.
     */
    interface RecipientChangedListener {
        void onChange(MeshID recipient);
    }

    private RecipientChangedListener onRecipientChangedListener = null;

    /**
     * Set a listener for updates to the value of the selected recipient.
     *
     * @param listener listener to be notified
     */
    public void setOnRecipientChangedListener(RecipientChangedListener listener) {
        onRecipientChangedListener = listener;
    }


    //
    // ANDROID EVENT HANDLING
    //

    /**
     * When the view is created, inflate the layout, save references to all of the elements, and
     * set the spinner event listener.
     *
     * @param inflater to inflate the layout
     * @param container to inflate the layout
     * @param savedInstanceState passed by Android
     * @return the inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.component_rightmesh, container);

        spinner = (Spinner) rootView.findViewById(R.id.spinner_recipient);
        spinner.setOnItemSelectedListener(this);

        deviceStatusLabel = (TextView) rootView.findViewById(R.id.textView_deviceStatus);
        networkStatusLabel = (TextView) rootView.findViewById(R.id.textView_networkStatus);

        return rootView;
    }

    /**
     * When the selected item in the recipient selection spinner changes, update the local variable
     * and notify the listener.
     *
     * @param parent the spinner
     * @param view view of the selected peer
     * @param position position of the selected peer in the list, used to get the actual object
     * @param id passed by Android
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        recipientID = spinnerAdapter.getItem(position);
        onRecipientChangedListener.onChange(recipientID);
    }

    /**
     * If nothing is selected in the recipient selection spinner, and there are peers aside from
     * this device to select, arbitrarily select the first such peer in the list.
     *
     * @param parent the spinner
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        if (spinnerAdapter.getCount() > 1) {
            parent.setSelection(1);
        }
    }


    //
    // MESH EVENT HANDLING
    //

    /**
     * Update the mesh peers available in the recipient selection spinner when mesh peers are
     * discovered or change state.
     *
     * @param rme event passed from RightMesh
     */
    public void updatePeersList(RightMeshEvent rme) {
        PeerChangedEvent pce = (PeerChangedEvent) rme;
        MeshID peer = rme.peerUuid;

        if (pce.state == ADDED && !spinnerAdapter.contains(peer)) {
            // Add the peer to the list if it is new.
            spinnerAdapter.add(peer);

            if (spinnerAdapter.getCount() == 2) {
                // If this is the first peer in the list, automatically select it.
                spinnerAdapter.notifyDataSetChanged();
                spinner.setSelection(1);
            }
        } else if (pce.state == REMOVED) {
            // Remove a peer when it disconnects.
            spinnerAdapter.remove(peer);

            // Toast if the recipient has been disconnected.
            if (peer.equals(recipientID)) {
                Toast.makeText(getActivity().getApplicationContext(),
                        "Recipient has disconnected.", Toast.LENGTH_SHORT).show();
            }
        }

        // Update the connected devices label if there are other devices connected.
        Activity parentActivity = getActivity();
        if (parentActivity != null) {
            if (spinnerAdapter.getCount() > 1) {
                // Get string resource with number of connected devices.
                int numConnectedDevices = spinnerAdapter.getCount()-1;
                String newText = getResources().getQuantityString(
                        R.plurals.number_of_connected_devices,
                        numConnectedDevices, numConnectedDevices);

                parentActivity.runOnUiThread(() -> networkStatusLabel.setText(newText));
            } else {
                parentActivity.runOnUiThread(() -> networkStatusLabel.setText(""));
            }
        }
    }


    //
    // HELPER FUNCTIONS
    //

    /**
     * Truncates MeshIDs to 8 characters long.
     *
     * @param id to get string of
     * @return truncated string
     */
    static String shortenMeshID(MeshID id) {
        return id.toString().substring(0, 10) + "...";
    }
}