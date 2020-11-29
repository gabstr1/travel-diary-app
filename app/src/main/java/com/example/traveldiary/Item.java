package com.example.traveldiary;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

@Entity(tableName = "listItems")
public class Item implements Parcelable, Comparable<Item> {

    @PrimaryKey(autoGenerate = true)
    private int itemId;

    @ColumnInfo(name = "image")
    private String image;

    @ColumnInfo(name = "itemDescription")
    private String description;

    @ColumnInfo(name = "itemDetails")
    private String details;

    @ColumnInfo(name = "monthName")
    private String monthName;

    @ColumnInfo(name = "itemMonthDayNumber")
    private String monthDayNumber;

    @ColumnInfo(name = "date")
    private String date;

    @Embedded
    Coordinates coordinates;

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public Item(String image, String description, String details, String monthName, String monthDayNumber, String date, Coordinates coordinates) {
        this.image = image;
        this.description = description;
        this.details = details;
        this.monthName = monthName;
        this.monthDayNumber = monthDayNumber;
        this.date = date;
        this.coordinates = coordinates;
    }

    protected Item(Parcel in) {
        itemId = in.readInt();
        image = in.readString();
        description = in.readString();
        details = in.readString();
        monthName = in.readString();
        monthDayNumber = in.readString();
        date = in.readString();
        coordinates = in.readParcelable(Coordinates.class.getClassLoader());
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };



    public int getItemId() { return itemId; }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getMonthDayNumber() {
        return monthDayNumber;
    }

    public void setMonthDayNumber(String monthDayNumber) {
        this.monthDayNumber = monthDayNumber;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(itemId);
        dest.writeString(image);
        dest.writeString(description);
        dest.writeString(details);
        dest.writeString(monthName);
        dest.writeString(monthDayNumber);
        dest.writeString(date);
        dest.writeParcelable(coordinates, flags);
    }

    @Override
    public int compareTo(Item o) {
        return this.itemId - o.getItemId();
    }

    public static Comparator<Item> byDate = new Comparator<Item>() {
        @Override
        public int compare(Item o1, Item o2) {
            return o2.getDate().compareTo(o1.getDate());
        }
    };
}