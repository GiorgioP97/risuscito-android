package it.cammino.risuscito;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.getbase.floatingactionbutton.FloatingActionButton;

import it.cammino.risuscito.ui.ThemeableActivity;

public class PaginaRenderFullScreen extends ThemeableActivity {
    
	private DatabaseCanti listaCanti;
	private static String urlCanto;
    public static int speedValue;
    public static boolean scrollPlaying;
    public static int idCanto;
	
	private WebView pageView;
	private int defaultZoomLevel = 0;
	private int defaultScrollX = 0;
	private int defaultScrollY = 0;
	
	private Handler mHandler = new Handler();
	final Runnable mScrollDown = new Runnable()
	{
	    public void run()
	    {
	    	try {
	    		pageView.scrollBy(0, speedValue);
	    	}
	    	catch (NumberFormatException e) {
	    		pageView.scrollBy(0, 0);
	    	}
	    	
	        mHandler.postDelayed(this, 700);
	    }
	};

    private LUtils mLUtils;
//    private ThemeUtils mThemeUtils;

	@Override
    public void onCreate(Bundle savedInstanceState) {
//        super.hasNavDrawer = false;
//        super.alsoLollipop = true;
        mLUtils = LUtils.getInstance(PaginaRenderFullScreen.this);
        mLUtils.goFullscreen();
        super.onCreate(savedInstanceState);
//        mThemeUtils = new ThemeUtils(this);
//        setTheme(mThemeUtils.getCurrent(false));
        setContentView(R.layout.activity_pagina_render_fullscreen);
        
        listaCanti = new DatabaseCanti(this);

//        mThemeUtils = new ThemeUtils(this);

        // recupera il numero della pagina da visualizzare dal parametro passato dalla chiamata
        Bundle bundle = this.getIntent().getExtras();
        urlCanto = bundle.getString(Utility.URL_CANTO);
        speedValue = bundle.getInt(Utility.SPEED_VALUE);
        scrollPlaying = bundle.getBoolean(Utility.SCROLL_PLAYING);
        idCanto = bundle.getInt(Utility.ID_CANTO);
//        Log.i(getClass().toString(), "urlCanto: " + urlCanto);
//        Log.i(getClass().toString(), "speedValue: " + speedValue);
//        Log.i(getClass().toString(), "scrollPlaying: " + scrollPlaying);
//        Log.i(getClass().toString(), "idCanto: " + idCanto);

        getSavedZoom();
        
		pageView = (WebView) findViewById(R.id.cantoView);
        ViewCompat.setTransitionName(pageView, Utility.TAG_TRANSIZIONE);

        FloatingActionButton fabFullscreen = (FloatingActionButton) findViewById(R.id.fab_fullscreen_off);
        fabFullscreen.setColorNormal(getThemeUtils().accentColor());
        fabFullscreen.setColorPressed(getThemeUtils().accentColorDark());
//        fabFullscreen.setColorRipple(getThemeUtils().accentColorLight());
        fabFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveZoom();
                mLUtils.closeActivityWithFadeOut();
            }
        });

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            finish();
            mLUtils.closeActivityWithFadeOut();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
    
    @Override
    public void onResume() {
    	super.onResume();

//        checkScreenAwake();

        pageView.loadUrl(urlCanto);
//	    Log.i(this.getClass().toString(), "scrollPlaying? " + scrollPlaying);
	    if (scrollPlaying) {
			mScrollDown.run();
	    }

		WebSettings webSettings = pageView.getSettings();
		webSettings.setUseWideViewPort(true);
		webSettings.setSupportZoom(true);
		webSettings.setLoadWithOverviewMode(true);
//		webSettings.setBuiltInZoomControls(true);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            webSettings.setBuiltInZoomControls(false);
        else {
            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);
        }

		if (defaultZoomLevel > 0)
            pageView.setInitialScale(defaultZoomLevel);
        pageView.setWebViewClient(new MyWebViewClient());
        
    }
    
	@Override
	public void onDestroy() {
//		saveZoom();
		if (listaCanti != null)
			listaCanti.close();
		super.onDestroy();
	}
    
    //recupera e setta lo zoom
    private void getSavedZoom() {
    	
    	SQLiteDatabase db = listaCanti.getReadableDatabase();
    	
	    String query = "SELECT zoom, scroll_x , scroll_y" +
	      		"  FROM ELENCO" +
	      		"  WHERE _id =  " + idCanto;   
	    Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

	    defaultZoomLevel = cursor.getInt(0);
	    defaultScrollX = cursor.getInt(1);
	    defaultScrollY = cursor.getInt(2);
	    
	    cursor.close();
	    db.close();
	    		
    }


    @SuppressWarnings("deprecation")
    private void saveZoom(){
        defaultZoomLevel = (int) (pageView.getScale() *100);
        defaultScrollX = pageView.getScrollX();
        defaultScrollY = pageView.getScrollY();

        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String sql = "UPDATE ELENCO" +
                "  SET zoom = " + defaultZoomLevel + " " +
                ", scroll_x = " + defaultScrollX + " " +
                ", scroll_y = " + defaultScrollY + " " +
                "  WHERE _id =  " + idCanto;
        db.execSQL(sql);
        db.close();
    }
    
   	private class MyWebViewClient extends WebViewClient {
	    @Override
	    public void onPageFinished(WebView view, String url) {
	    	view.postDelayed(new Runnable() {
	    		@Override
	    		public void run() {
	    			if (defaultScrollX > 0 || defaultScrollY > 0)
	    				pageView.scrollTo(defaultScrollX, defaultScrollY);
	    		}
	    		// Delay the scrollTo to make it work
	    	}, 500);
	    	super.onPageFinished(view, url);
	    }
	}

    //controlla se l'app deve mantenere lo schermo acceso
//    public void checkScreenAwake() {
//        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(this);
//        boolean screenOn = pref.getBoolean(Utility.SCREEN_ON, false);
//        if (screenOn)
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        else
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//    }

}