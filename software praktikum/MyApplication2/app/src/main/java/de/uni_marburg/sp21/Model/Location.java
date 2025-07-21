package de.uni_marburg.sp21.Model;

public class Location {
    private double lat;
    private double lon;

    public Location (double lat,double lon){
        this.lat=lat;
        this.lon=lon;
    }

    //lat
    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLat(){
        return lat;
    }

    //lon
    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLon(){
        return lon;
    }
}
