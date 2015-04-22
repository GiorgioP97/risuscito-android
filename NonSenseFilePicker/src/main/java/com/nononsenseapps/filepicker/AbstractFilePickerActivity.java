/*
 * Copyright (c) 2014 Jonas Kalderstam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nononsenseapps.filepicker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;



/**
 * An abstract base activity that handles all the fluff you don't care about.
 * <p/>
 * Usage: To start a child activity you could either use an intent starting the
 * activity directly, or you could use an implicit intent with GET_CONTENT, if
 * it
 * is also defined in your manifest. It is defined to be handled here in case
 * you
 * want the user to be able to use other file pickers on the system.
 * <p/>
 * That means using an intent with action GET_CONTENT
 * If you want to be able to select multiple items, include EXTRA_ALLOW_MULTIPLE
 * (default false).
 * <p/>
 * Two non-standard extra arguments are supported as well: EXTRA_ONLY_DIRS
 * (defaults to false)
 * allows only directories to be selected.
 * And EXTRA_START_PATH (default null), which should specify the starting path.
 * <p/>
 * The result of the user's action is returned in onActivityResult intent,
 * access it using getUri.
 * In case of multiple choices, these can be accessed with getClipData
 * containing Uri objects.
 * If running earlier than JellyBean you can access them with
 * getStringArrayListExtra(EXTRA_PATHS)
 *
 * @param <T>
 */
public abstract class AbstractFilePickerActivity<T> extends AppCompatActivity
        implements AbstractFilePickerFragment.OnFilePickedListener {
    public static final String EXTRA_START_PATH =
            "nononsense.intent" + ".START_PATH";
    public static final String EXTRA_MODE = "nononsense.intent.MODE";
    public static final String EXTRA_ALLOW_CREATE_DIR = "nononsense.intent" + ".ALLOW_CREATE_DIR";
    // For compatibility
    public static final String EXTRA_ALLOW_MULTIPLE =
            "android.intent.extra" + ".ALLOW_MULTIPLE";
    public static final String EXTRA_PATHS = "nononsense.intent.PATHS";
    public static final int MODE_FILE = AbstractFilePickerFragment.MODE_FILE;
    public static final int MODE_FILE_AND_DIR =
            AbstractFilePickerFragment.MODE_FILE_AND_DIR;
    public static final int MODE_DIR = AbstractFilePickerFragment.MODE_DIR;
    public static final String PRIMARY_COLOR = AbstractFilePickerFragment.KEY_PRIMARY_COLOR;
    public static final String ACCENT_COLOR = AbstractFilePickerFragment.KEY_ACCENT_COLOR;
    protected static final String TAG = "filepicker_fragment";
    protected String startPath = null;
    protected int mode = AbstractFilePickerFragment.MODE_FILE;
    protected boolean allowCreateDir = false;
    protected boolean allowMultiple = false;
    protected int primaryColor = 0;
    protected int accentColor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setupFauxDialog();
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            startPath = intent.getStringExtra(EXTRA_START_PATH);
            mode = intent.getIntExtra(EXTRA_MODE, mode);
            allowCreateDir = intent.getBooleanExtra(EXTRA_ALLOW_CREATE_DIR,
                    allowCreateDir);
            allowMultiple =
                    intent.getBooleanExtra(EXTRA_ALLOW_MULTIPLE, allowMultiple);
            primaryColor = intent.getIntExtra(PRIMARY_COLOR, primaryColor);
            accentColor = intent.getIntExtra(ACCENT_COLOR, accentColor);

        }
        if (accentColor != 0)
            setTheme(getCurrentTheme(accentColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(shiftColorDown(primaryColor));
        setContentView(R.layout.activity_filepicker);
        setupActionBar();

        FragmentManager fm = getSupportFragmentManager();
        AbstractFilePickerFragment<T> fragment =
                (AbstractFilePickerFragment<T>) fm.findFragmentByTag(TAG);

        if (fragment == null) {
            fragment =
                    getFragment(startPath, mode, allowMultiple, allowCreateDir, primaryColor);
        }

        if (fragment != null) {
            fm.beginTransaction().replace(R.id.fragment, fragment, TAG)
                    .commit();
        }

        // Default to cancelled
        setResult(Activity.RESULT_CANCELED);
    }

    protected void setupFauxDialog() {
        // Check if this should be a dialog
        TypedValue tv = new TypedValue();
        if (!getTheme().resolveAttribute(R.attr.isDialog, tv, true) ||
                tv.data == 0) {
            return;
        }

        // Should be a dialog; set up the window parameters.
        DisplayMetrics dm = getResources().getDisplayMetrics();

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = getResources()
                .getDimensionPixelSize(R.dimen.configure_dialog_width);
        params.height = Math.min(getResources()
                        .getDimensionPixelSize(R.dimen.configure_dialog_max_height),
                dm.heightPixels * 3 / 4);
        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        getWindow().setAttributes(params);
    }

    protected void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.risuscito_toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getWindowTitle());
        if (primaryColor != 0)
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(primaryColor));
    }

    protected abstract AbstractFilePickerFragment<T> getFragment(
            final String startPath, final int mode, final boolean allowMultiple,
            final boolean allowCreateDir, final int color);

    /**
     * @return the title to apply to the window
     */
    protected String getWindowTitle() {
        final int res;
        switch (mode) {
            case AbstractFilePickerFragment.MODE_DIR:
                res = R.plurals.select_dir;
                break;
            case AbstractFilePickerFragment.MODE_FILE_AND_DIR:
                res = R.plurals.select_dir_or_file;
                break;
            case AbstractFilePickerFragment.MODE_FILE:
            default:
                res = R.plurals.select_file;
                break;
        }

        final int count;
        if (allowMultiple) {
            count = 99;
        } else {
            count = 1;
        }

        return getResources().getQuantityString(res, count);
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
    }

    @Override
    public void onFilePicked(final Uri file) {
        Intent i = new Intent();
        i.setData(file);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onFilesPicked(final List<Uri> files) {
        Intent i = new Intent();
        i.putExtra(EXTRA_ALLOW_MULTIPLE, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ClipData clip = null;
            for (Uri file : files) {
                if (clip == null) {
                    clip = new ClipData("Paths", new String[]{},
                            new ClipData.Item(file));
                } else {
                    clip.addItem(new ClipData.Item(file));
                }
            }
            i.setClipData(clip);
        } else {
            ArrayList<String> paths = new ArrayList<String>();
            for (Uri file : files) {
                paths.add(file.toString());
            }
            i.putStringArrayListExtra(EXTRA_PATHS, paths);
        }

        setResult(Activity.RESULT_OK, i);
        finish();
    }

    @Override
    public void onCancelled() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onCancelled();
                return true;
            default:
                return false;
        }
    }

    public int getCurrentTheme(int color) {
        if (color == getResources().getColor(R.color.blue_dark))
            return R.style.FilePicker_Theme_BlueDark;
        if (color == getResources().getColor(R.color.grey))
            return R.style.FilePicker_Theme_Grey;
        if (color == getResources().getColor(R.color.blue_grey))
            return R.style.FilePicker_Theme_BlueGrey;
        if (color == getResources().getColor(R.color.black))
            return R.style.FilePicker_Theme_Black;
        if (color == getResources().getColor(R.color.brown))
            return R.style.FilePicker_Theme_Brown;
        if (color == getResources().getColor(R.color.red))
            return R.style.FilePicker_Theme_Red;
        if (color == getResources().getColor(R.color.pink))
            return R.style.FilePicker_Theme_Pink;
        if (color == getResources().getColor(R.color.purple))
            return R.style.FilePicker_Theme_Purple;
        if (color == getResources().getColor(R.color.violet))
            return R.style.FilePicker_Theme_Violet;
        if (color == getResources().getColor(R.color.blue))
            return R.style.FilePicker_Theme_Blue;
        if (color == getResources().getColor(R.color.blue_light))
            return R.style.FilePicker_Theme_BlueLight;
        if (color == getResources().getColor(R.color.torqouise))
            return R.style.FilePicker_Theme_Torqouise;
        if (color == getResources().getColor(R.color.green_water))
            return R.style.FilePicker_Theme_GreenWater;
        if (color == getResources().getColor(R.color.green))
            return R.style.FilePicker_Theme_Green;
        if (color == getResources().getColor(R.color.green_light))
            return R.style.FilePicker_Theme_GreenLight;
        if (color == getResources().getColor(R.color.green_bean))
            return R.style.FilePicker_Theme_GreenBean;
        if (color == getResources().getColor(R.color.yellow))
            return R.style.FilePicker_Theme_Yellow;
        if (color == getResources().getColor(R.color.orange_light))
            return R.style.FilePicker_Theme_OrangeLight;
        if (color == getResources().getColor(R.color.orange))
            return R.style.FilePicker_Theme_Orange;
        if (color == getResources().getColor(R.color.red_light))
            return R.style.FilePicker_Theme_RedLight;
        else
            return R.style.FilePicker_Theme;
    }

    public static int shiftColorDown(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f; // value component
        return Color.HSVToColor(hsv);
    }

}
