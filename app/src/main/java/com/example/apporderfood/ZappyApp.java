package com.example.apporderfood;

import android.app.Application;

import com.mikepenz.iconics.Iconics;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;

public class ZappyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Iconics.init(this);
        Iconics.registerFont(FontAwesome.INSTANCE);
    }
}
