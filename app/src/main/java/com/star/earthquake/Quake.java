package com.star.earthquake;


import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Quake {

    private Date date;
    private String details;
    private Location location;
    private double magnitude;
    private String link;

    public Date getDate() {
        return date;
    }

    public String getDetails() {
        return details;
    }

    public Location getLocation() {
        return location;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public String getLink() {
        return link;
    }

    public Quake(Date date, String details, Location location, double magnitude, String link) {
        this.date = date;
        this.details = details;
        this.location = location;
        this.magnitude = magnitude;
        this.link = link;
    }

    @Override
    public String toString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH.mm");

        String dateString = simpleDateFormat.format(date);

        return dateString + ": " + magnitude + " " + details;
    }
}
