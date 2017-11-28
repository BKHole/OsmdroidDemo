package com.bigemap.osmdroiddemo.kml;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.thoughtworks.xstream.XStream;

public class KMLXStream {

	XStream xstream;

	public KMLXStream() {
		
		xstream = new XStream();
	
		xstream.alias("kml", Kml.class);
		xstream.useAttributeFor(Kml.class, "xmlns");
		xstream.useAttributeFor(Kml.class, "hint");
		
		
        xstream.alias("Document", Document.class);
        xstream.useAttributeFor(Document.class, "id");
        xstream.alias("Style", Style.class);

        xstream.alias("Folder", Folder.class);
        xstream.alias("Placemark", Placemark.class);
        xstream.alias("GroundOverlay", GroundOverlay.class);
        xstream.alias("LinearRing", LinearRing.class);
        xstream.alias("Polygon", Polygon.class);
        xstream.alias("outerBoundaryIs", OuterBoundaryIs.class);
        
        xstream.addImplicitCollection(Placemark.class, "styles", Style.class);
        xstream.addImplicitCollection(Folder.class, "placemarks", Placemark.class);
        xstream.addImplicitCollection(Folder.class, "groundoverlays", GroundOverlay.class);
   
	}
	
	public String toKML(Kml kml){
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xstream.toXML(kml);
	}
	
	public Kml fromKML(String raw){
		return null;
	}
	
	public void toKMLFile(Kml kmlFile, String filename, boolean kml, boolean kmz) throws IOException{
		
		if (kml){
			FileWriter fr = new FileWriter(filename);
			fr.append(this.toKML(kmlFile));
			fr.flush();
		}
		
		if (kmz){
			String data = this.toKML(kmlFile);
			byte[] buf = new byte[1024];
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(filename + ".kmz"));
			ZipEntry ze= new ZipEntry(filename + ".kml");
			zos.putNextEntry(ze);
			
			InputStream in = new ByteArrayInputStream(data.getBytes("UTF-8"));
			int len;
	        while ((len = in.read(buf)) > 0) {
	        	zos.write(buf, 0, len);
	        }
			zos.closeEntry();
			zos.finish();
			zos.flush();
			zos.close();
			// end writing kmz
		}
	}
}
