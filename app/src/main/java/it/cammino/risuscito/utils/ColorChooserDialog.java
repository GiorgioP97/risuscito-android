package it.cammino.risuscito.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;

import it.cammino.risuscito.R;
import it.cammino.risuscito.Utility;
import it.cammino.utilities.colorpicker.ColorPickerDialog;
import it.cammino.utilities.colorpicker.ColorPickerPalette;
import it.cammino.utilities.colorpicker.ColorPickerSwatch.OnColorSelectedListener;

/**
 * Created by marcello.battain on 19/02/2015.
 */
public class ColorChooserDialog extends ColorPickerDialog implements OnColorSelectedListener {

    private ColorCallback mCallback;
    private int prevOrientation;
    private AppCompatActivity mActivity;
//    private AlertDialogPro dialog;
    private MaterialDialog dialog;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (ColorCallback) activity;
        mActivity = (AppCompatActivity) activity;
    }

    public static interface ColorCallback {
        void onColorSelection(int title, int color);
    }

    public static ColorChooserDialog newInstance(int titleResId, int[] colors, int selectedColor,
                                                int columns, int size) {
        ColorChooserDialog ret = new ColorChooserDialog();
        ret.initialize(titleResId, colors, selectedColor, columns, size);
        return ret;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        prevOrientation = mActivity.getRequestedOrientation();
//        Utility.blockOrientation(mActivity);
//        AlertDialogPro.Builder builder = new AlertDialogPro.Builder(mActivity);
//        dialog = builder.setTitle(getString(mTitleResId))
//                .setView(R.layout.color_picker_dialog)
//                .setPositiveButton(R.string.single_choice_ok, new ButtonClickedListener(Utility.CHANGE_COLOR))
////                .setNeutralButton(R.string.defaultStr, new ButtonClickedListener(Utility.RESET_COLOR))
//                .setNegativeButton(R.string.cancel, new ButtonClickedListener(Utility.DISMISS))
//                .setCancelable(false)
//                .show();

        dialog = new MaterialDialog.Builder(mActivity)
                .title(mTitleResId)
                .customView(R.layout.color_picker_dialog, true)
//                .positiveText(R.string.single_choice_ok)
                .negativeText(R.string.cancel)
//                .callback(new MaterialDialog.ButtonCallback() {
//                    @Override
//                    public void onPositive(MaterialDialog dialog) {
//                        dismiss();
//                        mCallback.onColorSelection(mTitleResId, mSelectedColor);
//                    }
//                })
                .show();

        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    arg0.dismiss();
//                    mActivity.setRequestedOrientation(prevOrientation);
                    return true;
                }
                return false;
            }
        });
//        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
//        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
//                getResources().getColor(R.color.btn_disabled_text));

        setmProgress((ProgressBar) dialog.getCustomView().findViewById(android.R.id.progress));
        setmPalette((ColorPickerPalette) dialog.getCustomView().findViewById(R.id.color_picker));
        getmPalette().init(mSize, mColumns, this);
        if (mColors != null) {
            showPaletteView();
        }
        return dialog;
    }

    @Override
    public void onColorSelected(int color) {
        if (mListener != null) {
            mListener.onColorSelected(color);
        }
        if (getTargetFragment() instanceof OnColorSelectedListener) {
            final OnColorSelectedListener listener =
                    (OnColorSelectedListener) getTargetFragment();
            listener.onColorSelected(color);
        }
        if (color != mSelectedColor) {
            mSelectedColor = color;
            // Redraw palette to show checkmark on newly selected color before dismissing.
            getmPalette().drawPalette(mColors, mSelectedColor);
        }
        dismiss();
        mCallback.onColorSelection(mTitleResId, mSelectedColor);
//        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
//        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
//                ((ThemeableActivity) mActivity).getThemeUtils().accentColor());
    }

    private class ButtonClickedListener implements DialogInterface.OnClickListener {
        private int clickedCode;

        public ButtonClickedListener(int code) {
            clickedCode = code;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (clickedCode) {
                case Utility.DISMISS:
//                    mActivity.setRequestedOrientation(prevOrientation);
                    break;
                case Utility.CHANGE_COLOR:
//                    mActivity.setRequestedOrientation(prevOrientation);
                    dismiss();
                    mCallback.onColorSelection(mTitleResId, mSelectedColor);
                    break;
                default:
//                    mActivity.setRequestedOrientation(prevOrientation);
                    break;
            }
        }
    }
}
