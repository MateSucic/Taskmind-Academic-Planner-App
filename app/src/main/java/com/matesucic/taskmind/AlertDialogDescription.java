package com.matesucic.taskmind;

import android.app.AlertDialog;
import android.content.Context;

public class AlertDialogDescription {
    public static void showAlertDialog(Context context, String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Description");
        builder.setMessage(description);

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
