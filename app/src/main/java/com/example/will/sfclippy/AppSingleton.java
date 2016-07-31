package com.example.will.sfclippy;

/**
 * Created by will on 27/07/2016.
 */
public class AppSingleton {
    private static AppSingleton ourInstance = new AppSingleton();

    public static AppSingleton getInstance() {
        return ourInstance;
    }

    private StorageService storageService;

    private AppSingleton() {
        this.storageService = null;
    }

    public void setStorageService( StorageService service ) {
        this.storageService = service;
    }

    public StorageService getStorageService( ) {
        return storageService;
    }
}
