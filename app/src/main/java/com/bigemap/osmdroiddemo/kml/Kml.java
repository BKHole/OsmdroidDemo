package com.bigemap.osmdroiddemo.kml;

import java.util.ArrayList;
import java.util.List;

public class Kml {
	public Document Document;
	public Folder Folder;
	String xmlns = "http://www.opengis.net/kml/2.2";
	String hint;
}

class Document {
	public String id;
	public String name;
    public String address;
    public String description;
    public String open;
    public Folder Folder = new Folder();
}

class Folder {
	public List<Placemark> placemarks = new ArrayList<Placemark>();
	public List<GroundOverlay> groundoverlays = new ArrayList<GroundOverlay>();
	public String name;
	public String description;
}

class GroundOverlay {
	public String name;
	public String description;
	public Icon icon;
	public LatLonBox latLonBox;
}

class Icon {
	public String href;	
}

class LatLonBox { 
	public String north;
	public String south;
	public String east;
	public String west;
	public String rotation;
}

class Placemark {
	public String name;
	public String visibility;
	public List<Style> styles = new ArrayList<Style>();
	public Polygon Polygon;
	public LineString LineString;
	public Point Point;
}

class Polygon {
	public String extrude;
	public String altitudeMode;
	public OuterBoundaryIs outerBoundaryIs = new OuterBoundaryIs();
}

class OuterBoundaryIs {
	LinearRing LinearRing = new LinearRing();
}

class LinearRing {
	public String coordinates;
}

class LineString {
	public String extrude;
	public String altitudeMode;
	public String coordinates;
}

class Point{
	public String coordinates;
}
class Style {
	public String id;
    public LineStyle lineStyle = new LineStyle();
	public PolyStyle polyStyle = new PolyStyle();
}

class PolyStyle {
	public String color;
	public String outline;
}

class LineStyle {
	public String width;
}
