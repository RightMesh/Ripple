package io.left.ripple;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import io.left.rightmesh.id.MeshId;


/**
 * A simple activity to demonstrate the movement of data through a RightMesh mesh network.
 *
 *
 * Initializes the RightMesh library, allows users to change the background colour of the app,
 * then propagate that new background colour out to another peer on the mesh, changing the
 * background colour of the peers that transmit the message along the way.
 */
public class MainActivity extends AppCompatActivity{
    private static final String TAG = MainActivity.class.getCanonicalName();

    MainViewModel viewModel;

    // Responsible for allowing the user to select the ping recipient.
    CustomViewRightMeshRecipient recipientView;
    FloatingActionButton fabSend, fabSendAll;
    Button btnRed, btnGreen, btnBlue;
    View layoutBackground;

    /**
     * Set Android UI event handlers and connect to the RightMesh library when the
     * activity initializes.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabSend = findViewById(R.id.button_send);
        fabSendAll = findViewById(R.id.button_send_all);
        layoutBackground = findViewById(R.id.layout_background);
        btnRed = findViewById(R.id.button_red);
        btnBlue = findViewById(R.id.button_blue);
        btnGreen = findViewById(R.id.button_green);

        initViewModel(savedInstanceState);
        observeViewModel();

        // Display the RightMesh settings activity when the send button is tapped and held.
        fabSend.setOnLongClickListener(v -> {
            viewModel.showSettingsActivity();
            return true;
        });
        fabSend.setOnClickListener(this::sendSingleMsg);
        fabSendAll.setOnClickListener(this::sendAllRecipients);

        btnRed.setOnClickListener(this::colorButtonClick);
        btnGreen.setOnClickListener(this::colorButtonClick);
        btnBlue.setOnClickListener(this::colorButtonClick);

        // Set up the recipient selection spinner.
        recipientView = findViewById(R.id.rightmesh_recipient);
        recipientView.setOnRecipientChangedListener(recipient -> {
            viewModel.setRecipient(recipient);
        });
    }

    private void colorButtonClick(View view) {
        Colour colour = null;
        if(view == btnGreen)
            colour = Colour.GREEN;
        else if(view == btnRed)
            colour = Colour.RED;
        else
            colour = Colour.BLUE;

        viewModel.setColour(colour);
    }

    private void sendSingleMsg(View view) {
        viewModel.sendColorMsg();
    }

    private void sendAllRecipients(View view) {
        MeshIdAdapter recipientAdapter = recipientView.getAdapter();
        Colour crrColour = viewModel.colour.getValue();

        for (int i = 0; i < recipientAdapter.getCount(); i++) {
            MeshId peer = recipientAdapter.getItem(i);
            viewModel.sendColorMsg(peer, crrColour);
        }
    }

    private void observeViewModel() {
        viewModel.colour.observe(this, colour -> {
            layoutBackground.setBackgroundColor(ContextCompat.getColor(this, colour.getColourId()));
        });
        viewModel.peerChangedEvent.observe(this, peerChangeEvent -> {
            recipientView.updatePeersList(peerChangeEvent);
        });
        viewModel.deviceId.observe(this, newMeshId -> {
            recipientView.addNewDevice(newMeshId);
        });
    }

    private void initViewModel(Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        if(savedInstanceState == null)
            viewModel.init();
    }
}
