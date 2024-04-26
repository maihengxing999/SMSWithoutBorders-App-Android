package com.example.sw0b_001.Data.Platforms;

import android.content.Context;
import android.content.Intent;

import androidx.room.Room;

import com.example.sw0b_001.Database.Datastore;
import com.example.sw0b_001.EmailComposeActivity;
import com.example.sw0b_001.MessageComposeActivity;
import com.example.sw0b_001.R;
import com.example.sw0b_001.TextComposeActivity;

public class PlatformsHandler {
    static public Intent getIntent(Context context, String platform_name, String type) {
        Intent intent = null;
        switch(type) {
            case "email": {
                intent = new Intent(context, EmailComposeActivity.class);
                break;
            }

            case "text": {
                intent = new Intent(context, TextComposeActivity.class);
                break;
            }

            case "messaging": {
                intent = new Intent(context, MessageComposeActivity.class);
                break;
            }
            // TODO: put a default here
        }
        if(intent != null ) {
            intent.putExtra("platform_name", platform_name);
        }
        return intent;
    }

    public static int hardGetLogoByName(Context context, String name) {
        int logo = -1;
        if(name.equals("gmail"))
            logo = R.drawable.gmail;

        else if(name.equals("twitter"))
            logo = R.drawable.twitter;

        else if(name.equals("telegram"))
            logo = R.drawable.telegram;

        return logo;
    }

    private static Platforms fetchPlatform(Context context, long platformID) throws Throwable {
        final Platforms[] platforms = new Platforms[1];
        Thread fetchPlatformThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Datastore databaseConnection = Room.databaseBuilder(context,
                        Datastore.class, Datastore.databaseName)
                        .fallbackToDestructiveMigration()
                        .build();

                PlatformDao platformDao = databaseConnection.platformDao();
                platforms[0] = platformDao.get(platformID);
            }
        });

        try {
            fetchPlatformThread.start();
            fetchPlatformThread.join();
        } catch (InterruptedException e) {
            throw e.fillInStackTrace();
        }

        return platforms[0];
    }

    private static Platforms fetchPlatform(Context context, String platformName) throws Throwable {
        final Platforms[] platforms = new Platforms[1];
        Thread fetchPlatformThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Datastore databaseConnection = Room.databaseBuilder(context,
                        Datastore.class, Datastore.databaseName)
                        .fallbackToDestructiveMigration()
                        .build();

                PlatformDao platformDao = databaseConnection.platformDao();
                platforms[0] = platformDao.get(platformName);
            }
        });

        try {
            fetchPlatformThread.start();
            fetchPlatformThread.join();
        } catch (InterruptedException e) {
            throw e.fillInStackTrace();
        }

        return platforms[0];
    }

    public static Platforms getPlatform(Context context, long platformId) {
        Platforms platforms = new Platforms();
        try {
            platforms = fetchPlatform(context, platformId);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return platforms;
    }

    public static Platforms getPlatform(Context context, String platformName) {
        Platforms platforms = new Platforms();
        try {
            platforms = fetchPlatform(context, platformName);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return platforms;
    }

}
