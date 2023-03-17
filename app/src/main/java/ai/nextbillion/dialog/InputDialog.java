package ai.nextbillion.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import ai.nextbillion.R;

public class InputDialog {
    InputTextCallback callback;
    public InputDialog(InputTextCallback textCallback){
        callback = textCallback;
    }
    public void showDialog(Context context){
        FrameLayout rootView = new FrameLayout(context);
        View view = View.inflate(context,R.layout.input_location_layout,rootView);
        EditText editTextLat = view.findViewById(R.id.lat_et);
        EditText editTextLot= view.findViewById(R.id.lnt_et);
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle("Input moving location")
                .setView(rootView)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String lat = editTextLat.getText().toString();
                        String lot = editTextLot.getText().toString();
                        callback.onInput(lat,lot);
                        Toast.makeText(context, "Moving to " + lat + "," +  lot, Toast.LENGTH_LONG).show();
                    }
                });
        builder.create().show();
    }

    public  interface InputTextCallback {
       void onInput(String lat,String lot);
    }

}
