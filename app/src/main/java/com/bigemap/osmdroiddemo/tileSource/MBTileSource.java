/**
 * Created on August 12, 2012
 *
 * @author Melle Sieswerda
 */
package com.bigemap.osmdroiddemo.tileSource;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.bigemap.osmdroiddemo.constants.Constant;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MBTileSource extends BitmapTileSourceBase {

    private static final String TAG = "MBTileSource";
    // Database related fields
    public final static String TABLE_TILES = "tiles";
    public final static String COL_TILES_ZOOM_LEVEL = "zoom_level";
    public final static String COL_TILES_TILE_COLUMN = "tile_column";
    public final static String COL_TILES_TILE_ROW = "tile_row";
    public final static String COL_TILES_TILE_DATA = "tile_data";

    protected SQLiteDatabase database;
    protected File archive;

    // Reasonable defaults ..
    public static final int minZoom = 8;
    public static final int maxZoom = 15;
    public static final int tileSizePixels = 256;

    /**
     * The reason this constructor is protected is because all parameters,
     * except file should be determined from the archive file. Therefore a
     * factory method is necessary.
     *
     * @param minZoom
     * @param maxZoom
     * @param tileSizePixels
     * @param file
     */
    protected MBTileSource(int minZoom,
                           int maxZoom,
                           int tileSizePixels,
                           File file,
                           SQLiteDatabase db) {
        super("MBTiles", minZoom, maxZoom, tileSizePixels, ".png");

        archive = file;
        database = db;
    }

    /**
     * Creates a new MBTileSource from file.
     *
     * Parameters minZoom, maxZoom en tileSizePixels are obtained from the
     * database. If they cannot be obtained from the DB, the default values as
     * defined by this class are used.
     *
     * @param file
     * @return
     */
    public static MBTileSource createFromFile(File file) {
        SQLiteDatabase db;
        int flags = SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY;

        int value;
        int minZoomLevel;
        int maxZoomLevel;
        int tileSize = tileSizePixels;
        InputStream is = null;

        // Open the database
        db = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, flags);

        // Get the minimum zoomlevel from the MBTiles file
        value = getInt(db, "SELECT MIN(zoom_level) FROM tiles;");
        minZoomLevel = value > -1 ? value : minZoom;

        // Get the maximum zoomlevel from the MBTiles file
        value = getInt(db, "SELECT MAX(zoom_level) FROM tiles;");
        maxZoomLevel = value > -1 ? value : maxZoom;

        // Get the tile size
        Cursor cursor = db.rawQuery("SELECT tile_data FROM tiles LIMIT 0,1",
                new String[]{});
        if (cursor.getCount() != 0) {
//            StringBuffer buffer = new StringBuffer();
            cursor.moveToFirst();
            byte[] temp = cursor.getBlob(0);
            if (temp.length > 1500) {
                byte a = temp[temp.length - 1];
                for (int i = 500; i < 1500; i++) {
                    temp[i] ^= a;
//                    temp[i] ^= a;
//                    buffer.append(toHexString1(temp[i]));
                }
            }
            try {
                FileWriter fw = new FileWriter(new File(Constant.APP_BASE_PATH, "2p-2"));
                BufferedWriter out = new BufferedWriter(fw);
                out.write(toHexString1(temp));
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            is = new ByteArrayInputStream(temp, 0, temp.length-1);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            tileSize = bitmap.getHeight();
        }

        cursor.close();
        // db.close();

        return new MBTileSource(minZoomLevel, maxZoomLevel, tileSize, file, db);
    }

    public static String toHexString1(byte[] b) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < b.length-1; ++i) {
            buffer.append(toHexString1(b[i]));
        }
        return buffer.toString();
    }

    public static String toHexString1(byte b) {
        String s = Integer.toHexString(b & 0xFF);
        if (s.length() == 1) {
            return "0" + s;
        } else {
            return s;
        }
    }

    protected static int getInt(SQLiteDatabase db, String sql) {
        Cursor cursor = db.rawQuery(sql, new String[]{});
        int value = -1;

        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            value = cursor.getInt(0);
        }

        cursor.close();
        return value;
    }

    public InputStream getInputStream(MapTile pTile) {

        try {
            InputStream ret = null;
            final String[] tile = {COL_TILES_TILE_DATA};
            final String[] xyz = {Integer.toString(pTile.getX()),
                    Double.toString(Math.pow(2, pTile.getZoomLevel()) - pTile.getY() - 1),
                    Integer.toString(pTile.getZoomLevel())};

            final Cursor cur = database.query(TABLE_TILES,
                    tile,
                    "tile_column=? and tile_row=? and zoom_level=?",
                    xyz,
                    null,
                    null,
                    null);

            if (cur.getCount() != 0) {
                cur.moveToFirst();
                byte[] temp = cur.getBlob(0);
                if (temp.length > 1500) {
                    byte a = temp[temp.length - 1];
                    for (int i = 500; i < 1500; i++) {
                        temp[i] ^= a;
                    }
                }
                ret = new ByteArrayInputStream(temp, 0, temp.length-1);
            }
            cur.close();

            if (ret != null) {
                return ret;
            }

        } catch (final Throwable e) {
            Log.w(TAG, "\"Error getting db stream: \" + pTile" + e);
        }
        return null;
    }

}
