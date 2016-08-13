package com.example.will.sfclippy;

/**
 * Created by will on 27/07/2016.
 */
public class AppSingleton {
    private static AppSingleton ourInstance = new AppSingleton();

    public static AppSingleton getInstance() {
        return ourInstance;
    }

    private DataProvider dataProvider;

    private AppSingleton() {
        this.dataProvider = null;
    }

    public void setDataProvider( DataProvider dataProvider ) {
        this.dataProvider = dataProvider;
    }

    public DataProvider getDataProvider( ) {
        return dataProvider;
    }
}
