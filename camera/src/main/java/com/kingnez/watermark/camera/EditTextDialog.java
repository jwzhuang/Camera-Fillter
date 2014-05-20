package com.kingnez.watermark.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Created by kingnez on 5/20/14.
 */
public class EditTextDialog extends DialogFragment {

    public interface EditTextDialogListener {
        public void onDialogPositiveClick(String tag, String content);
    }

    EditTextDialogListener mListener;
    EditText mEditText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_input, null);
        mEditText = (EditText) layout.findViewById(R.id.content);
        builder.setView(layout)
                .setTitle(getTag())
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogPositiveClick(getTag(), mEditText.getText().toString());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (EditTextDialogListener) activity;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }
}
