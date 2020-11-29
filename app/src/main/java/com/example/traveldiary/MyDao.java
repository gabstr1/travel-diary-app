package com.example.traveldiary;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import static androidx.room.OnConflictStrategy.REPLACE;

import java.util.List;

@Dao
public interface MyDao {
    @Insert(onConflict = REPLACE)
    void addItem(Item item);

    @Delete
    void delete(Item item);

    //Update query
    @Query("UPDATE listItems SET itemDescription = :sText WHERE itemId = :sID")
    void update(int sID, String sText);

    // Get all data query
    @Query("SELECT * from listItems")
    List<Item> getAllItems();

    @Query("SELECT * from listItems WHERE itemId = :sID")
    int getItem(int sID);

    @Query("SELECT latitude, longitude FROM listItems")
    List<Coordinates> getAllCoordinates();

}
