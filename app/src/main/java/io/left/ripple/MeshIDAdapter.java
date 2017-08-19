package io.left.ripple;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import io.left.rightmesh.id.MeshID;

/**
 * A custom adapter to style the MeshIDs a little nicer in the list.
 */
class MeshIDAdapter extends ArrayAdapter<MeshID> {

    /**
     * Inflates the parent {@link ArrayAdapter} and stores the context for use loading colours.
     *
     * @param context app context, need by parent class
     */
    MeshIDAdapter(@NonNull Context context) {
        super(context, android.R.layout.simple_spinner_dropdown_item);
    }

    // ID of the peer to treat as this device (i.e. for styling and naming).
    private MeshID deviceID;

    void setDeviceID(MeshID deviceID) {
        this.deviceID = deviceID;
    }


    //
    // VIEW GENERATION METHODS
    //

    /**
     * Returns default view with colour/text modified by
     * {@link MeshIDAdapter#modifyView(TextView, int)}.
     *
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        return modifyView(view, position);
    }

    /**
     * Returns default view with colour/text modified by
     * {@link MeshIDAdapter#modifyView(TextView, int)}.
     *
     * {@inheritDoc}
     */
    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        return modifyView(view, position);
    }

    /**
     * Update the supplied view with a shortened MeshID string or the special styling for the
     * current device.
     *
     * @param view view to be modified
     * @param position position of the item to be used
     * @return the modified view
     */
    private View modifyView(TextView view, int position) {
        MeshID item = this.getItem(position);
        if (item != null) {
            String text; // Text for the item in the list.
            int colour;  // Colour for the text in the list.

            if (deviceID != null && item.equals(deviceID)) {
                // Change text colour if is the current device's ID.
                text = "This Device";
                colour = R.color.blue;
            } else {
                // Otherwise, simply make the MeshID more readable and use the theme default colour.
                text = RightMeshRecipientComponent.shortenMeshID(item);
                colour = android.R.color.primary_text_light;
            }
            view.setText(text);
            view.setTextColor(ContextCompat.getColor(getContext(), colour));
        }
        return view;
    }


    //
    // HELPER METHODS
    //

    /**
     * Mimic {@link java.util.ArrayList#contains(Object)} behaviour to make this class easier to
     * use as a list.
     *
     * @param item to check the existence of
     * @return true if the provided item is in the array, false otherwise
     */
    boolean contains(MeshID item) {
        return getPosition(item) >= 0; // The position is -1 if it doesn't exist.
    }
}