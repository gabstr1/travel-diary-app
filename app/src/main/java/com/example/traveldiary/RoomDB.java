package com.example.traveldiary;

import android.content.Context;
import android.media.Image;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Item.class}, version = 2, exportSchema = false)
public abstract class RoomDB extends RoomDatabase {
    //Create database instance
    private static RoomDB database;
    //Define database name
    private static String DATABASE_NAME = "database";

    public synchronized static  RoomDB getInstance(Context context){
        //check condition
        if(database == null){
            //when database is null, initialize database
            database = Room.databaseBuilder(context.getApplicationContext(),
                    RoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    //Create Dao
    public abstract MyDao myDao();
}
