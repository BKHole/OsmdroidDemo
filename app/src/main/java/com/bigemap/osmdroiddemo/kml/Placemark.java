package com.bigemap.osmdroiddemo.kml;

import org.osmdroid.util.GeoPoint;

public class Placemark {

    private String name;
    private String description;
    private GeoPoint point;

    public Placemark() {

    }

    public Placemark(String name, GeoPoint point, String description) {
        this.name = name;
        this.point = point;
        this.description = description;
    }


    public static class PlacemarkBuilder {
        private String name;
        private GeoPoint point;
        private String description;

        public PlacemarkBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public PlacemarkBuilder setPoint(GeoPoint point) {
            this.point = point;
            return this;
        }

        public PlacemarkBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Placemark createPlacemark() {
            return new Placemark(name, point, description);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoPoint getPoint() {
        return point;
    }

    public void setPoint(GeoPoint point) {
        this.point = point;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
