package it.cammino.risuscito.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

import it.cammino.risuscito.R;

/**
 * Created by marcello.battain on 12/02/2015.
 */
public class ThemeUtils {

    public ThemeUtils(Activity context) {
        mContext = context;
        isChanged(false); // invalidate stored booleans
    }

    private Context mContext;
    private boolean mDarkMode;
    private boolean mTrueBlack;
    private int mLastPrimaryColor;
    private int mLastAccentColor;
    private boolean mLastColoredNav;
    private boolean mDirectoryCount;

    public int getPopupTheme() {
        if (mDarkMode || mTrueBlack) {
            return R.style.ThemeOverlay_AppCompat_Dark;
        } else {
            return R.style.ThemeOverlay_AppCompat_Light;
        }
    }

    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("dark_mode", false);
    }

    public static boolean isTrueBlack(Context context) {
        if (!isDarkMode(context)) return false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("true_black", false);
    }

    public static boolean isDirectoryCount(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("directory_count", false);
    }

    public int primaryColor() {
        final int defaultColor = mContext.getResources().getColor(R.color.theme_primary);
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt("primary_color", defaultColor);
    }

    public void primaryColor(int newColor) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("primary_color", newColor).commit();
    }

    public int primaryColorDark() {
        return shiftColorDown(primaryColor());
    }

    public int accentColor() {
        final int defaultColor = mContext.getResources().getColor(R.color.theme_accent);
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt("accent_color", defaultColor);
    }

    public int accentColorLight() {
//        return shiftColorUp(accentColor());
        return lighter(accentColor(),0.5f);

    }

    public int accentColorDark() {
        return shiftColorDown(accentColor());
    }

    public void accentColor(int newColor) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("accent_color", newColor).commit();
    }

    public boolean isColoredNavBar() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("colored_navbar", true);
    }

    public boolean isChanged(boolean checkForChanged) {
        final boolean darkTheme = isDarkMode(mContext);
        final boolean blackTheme = isTrueBlack(mContext);
        final int primaryColor = primaryColor();
        final int accentColor = accentColor();
        final boolean coloredNav = isColoredNavBar();
        final boolean directoryCount = isDirectoryCount(mContext);

        boolean changed = false;
        if (checkForChanged) {
            changed = mDarkMode != darkTheme || mTrueBlack != blackTheme ||
                    mLastPrimaryColor != primaryColor || mLastAccentColor != accentColor ||
                    coloredNav != mLastColoredNav ||
                    directoryCount != mDirectoryCount;
        }

        mDarkMode = darkTheme;
        mTrueBlack = blackTheme;
        mLastPrimaryColor = primaryColor;
        mLastAccentColor = accentColor;
        mLastColoredNav = coloredNav;
        mDirectoryCount = directoryCount;

        return changed;
    }

    public int getCurrent() {
//        if (hasNavDrawer) {
//            if (accentColor() == mContext.getResources().getColor(R.color.blue_dark))
//                return R.style.RisuscitoTheme_WithNavDrawer_BlueDark;
//            if (accentColor() == mContext.getResources().getColor(R.color.grey))
//                return R.style.RisuscitoTheme_WithNavDrawer_Grey;
//            if (accentColor() == mContext.getResources().getColor(R.color.blue_grey))
//                return R.style.RisuscitoTheme_WithNavDrawer_BlueGrey;
//            if (accentColor() == mContext.getResources().getColor(R.color.black))
//                return R.style.RisuscitoTheme_WithNavDrawer_Black;
//            if (accentColor() == mContext.getResources().getColor(R.color.brown))
//                return R.style.RisuscitoTheme_WithNavDrawer_Brown;
//            if (accentColor() == mContext.getResources().getColor(R.color.red))
//                return R.style.RisuscitoTheme_WithNavDrawer_Red;
//            if (accentColor() == mContext.getResources().getColor(R.color.pink))
//                return R.style.RisuscitoTheme_WithNavDrawer_Pink;
//            if (accentColor() == mContext.getResources().getColor(R.color.purple))
//                return R.style.RisuscitoTheme_WithNavDrawer_Purple;
//            if (accentColor() == mContext.getResources().getColor(R.color.violet))
//                return R.style.RisuscitoTheme_WithNavDrawer_Violet;
//            if (accentColor() == mContext.getResources().getColor(R.color.blue))
//                return R.style.RisuscitoTheme_WithNavDrawer_Blue;
//            if (accentColor() == mContext.getResources().getColor(R.color.blue_light))
//                return R.style.RisuscitoTheme_WithNavDrawer_BlueLight;
//            if (accentColor() == mContext.getResources().getColor(R.color.torqouise))
//                return R.style.RisuscitoTheme_WithNavDrawer_Torqouise;
//            if (accentColor() == mContext.getResources().getColor(R.color.green_water))
//                return R.style.RisuscitoTheme_WithNavDrawer_GreenWater;
//            if (accentColor() == mContext.getResources().getColor(R.color.green))
//                return R.style.RisuscitoTheme_WithNavDrawer_Green;
//            if (accentColor() == mContext.getResources().getColor(R.color.green_light))
//                return R.style.RisuscitoTheme_WithNavDrawer_GreenLight;
//            if (accentColor() == mContext.getResources().getColor(R.color.green_bean))
//                return R.style.RisuscitoTheme_WithNavDrawer_GreenBean;
//            if (accentColor() == mContext.getResources().getColor(R.color.yellow))
//                return R.style.RisuscitoTheme_WithNavDrawer_Yellow;
//            if (accentColor() == mContext.getResources().getColor(R.color.orange_light))
//                return R.style.RisuscitoTheme_WithNavDrawer_OrangeLight;
//            if (accentColor() == mContext.getResources().getColor(R.color.orange))
//                return R.style.RisuscitoTheme_WithNavDrawer_Orange;
//            if (accentColor() == mContext.getResources().getColor(R.color.red_light))
//                return R.style.RisuscitoTheme_WithNavDrawer_RedLight;
//            else
//                return R.style.RisuscitoTheme;
//        }
//        else {
        if (accentColor() == mContext.getResources().getColor(R.color.blue_dark))
            return R.style.RisuscitoTheme_BlueDark;
        if (accentColor() == mContext.getResources().getColor(R.color.grey))
            return R.style.RisuscitoTheme_Grey;
        if (accentColor() == mContext.getResources().getColor(R.color.blue_grey))
            return R.style.RisuscitoTheme_BlueGrey;
        if (accentColor() == mContext.getResources().getColor(R.color.black))
            return R.style.RisuscitoTheme_Black;
        if (accentColor() == mContext.getResources().getColor(R.color.brown))
            return R.style.RisuscitoTheme_Brown;
        if (accentColor() == mContext.getResources().getColor(R.color.red))
            return R.style.RisuscitoTheme_Red;
        if (accentColor() == mContext.getResources().getColor(R.color.pink))
            return R.style.RisuscitoTheme_Pink;
        if (accentColor() == mContext.getResources().getColor(R.color.purple))
            return R.style.RisuscitoTheme_Purple;
        if (accentColor() == mContext.getResources().getColor(R.color.violet))
            return R.style.RisuscitoTheme_Violet;
        if (accentColor() == mContext.getResources().getColor(R.color.blue))
            return R.style.RisuscitoTheme_Blue;
        if (accentColor() == mContext.getResources().getColor(R.color.blue_light))
            return R.style.RisuscitoTheme_BlueLight;
        if (accentColor() == mContext.getResources().getColor(R.color.torqouise))
            return R.style.RisuscitoTheme_Torqouise;
        if (accentColor() == mContext.getResources().getColor(R.color.green_water))
            return R.style.RisuscitoTheme_GreenWater;
        if (accentColor() == mContext.getResources().getColor(R.color.green))
            return R.style.RisuscitoTheme_Green;
        if (accentColor() == mContext.getResources().getColor(R.color.green_light))
            return R.style.RisuscitoTheme_GreenLight;
        if (accentColor() == mContext.getResources().getColor(R.color.green_bean))
            return R.style.RisuscitoTheme_GreenBean;
        if (accentColor() == mContext.getResources().getColor(R.color.yellow))
            return R.style.RisuscitoTheme_Yellow;
        if (accentColor() == mContext.getResources().getColor(R.color.orange_light))
            return R.style.RisuscitoTheme_OrangeLight;
        if (accentColor() == mContext.getResources().getColor(R.color.orange))
            return R.style.RisuscitoTheme_Orange;
        if (accentColor() == mContext.getResources().getColor(R.color.red_light))
            return R.style.RisuscitoTheme_RedLight;
        else
            return R.style.RisuscitoTheme;
//        }
    }

    public static int shiftColorDown(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    public static int shiftColorUp(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
//        hsv[2] *= 1.2f; // value component
        hsv[2] = 0.2f + 0.8f * hsv[2];
        return Color.HSVToColor(hsv);
    }

    /**
     * Lightens a color by a given factor.
     *
     * @param color
     *            The color to lighten
     * @param factor
     *            The factor to lighten the color. 0 will make the color unchanged. 1 will make the
     *            color white.
     * @return lighter version of the specified color.
     */
    public static int lighter(int color, float factor) {
        int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
        int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
        int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
        return Color.argb(Color.alpha(color), red, green, blue);
    }


//    public int getCurrent(boolean hasNavDrawer) {
//        if (hasNavDrawer) {
//            if (mTrueBlack) {
//                return R.style.Theme_CabinetTrueBlack_WithNavDrawer;
//            } else if (mDarkMode) {
//                return R.style.Theme_CabinetDark_WithNavDrawer;
//            } else {
//                return R.style.Theme_Cabinet_WithNavDrawer;
//            }
//        } else {
//            if (mTrueBlack) {
//                return R.style.Theme_CabinetTrueBlack;
//            } else if (mDarkMode) {
//                return R.style.Theme_CabinetDark;
//            } else {
//                return R.style.Theme_Cabinet;
//            }
//        }
//    }
}

