/**
 * Created on August 12, 2012
 * 
 * @author Melle Sieswerda
 * 
 */
package com.bigemap.osmdroiddemo.tileSource;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.MapTileFileStorageProviderBase;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.util.StreamUtils;

import java.io.File;
import java.io.InputStream;

public class MBTileModuleProvider extends MapTileFileStorageProviderBase {
    private static final String TAG = "MBTileModuleProvider";
    protected MBTileSource tileSource;

    /**
     * Constructor
     * 
     * @param file
     * @param tileSource
     */
    public MBTileModuleProvider(IRegisterReceiver receiverRegistrar,
                                File file,
                                MBTileSource tileSource) {

        // Call the super constructor
        super(receiverRegistrar,
                Configuration.getInstance().getTileFileSystemThreads(),
              Configuration.getInstance().getTileFileSystemMaxQueueSize());

        // Initialize fields
        this.tileSource = tileSource;

    }

    @Override
    protected String getName() {
        return "MBTiles File Archive Provider";
    }

    @Override
    protected String getThreadGroupName() {
        return "mbtilesarchive";
    }

    @Override
    protected Runnable getTileLoader() {
        return new TileLoader();
    }

    @Override
    public boolean getUsesDataConnection() {
        return false;
    }

    @Override
    public int getMinimumZoomLevel() {
        return tileSource.getMinimumZoomLevel();
    }

    @Override
    public int getMaximumZoomLevel() {
        return tileSource.getMaximumZoomLevel();
    }

    @Override
    public void setTileSource(ITileSource tileSource) {
        Log.w(TAG, "*** Warning: someone's trying to reassign MBTileModuleProvider's tileSource!");
        if (tileSource instanceof MBTileSource) {
            this.tileSource = (MBTileSource) tileSource;
        } else {
            Log.w(TAG, "*** Warning: and it wasn't even an MBTileSource! That's just rude!");
            
        }
    }

    private class TileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        public Drawable loadTile(final MapTileRequestState pState) {

            // if there's no sdcard then don't do anything
            if (!isSdCardAvailable()) {
                return null;
            }

            MapTile pTile = pState.getMapTile();
            InputStream inputStream = null;

            try {
                inputStream = tileSource.getInputStream(pTile);
                
                if (inputStream != null) {
                    Drawable drawable = tileSource.getDrawable(inputStream);

                    // Note that the finally clause will be called before
                    // the value is returned!
                    return drawable;
                }

            } catch (Throwable e) {
                Log.e(TAG, "loadTile: Error loading tile", e);

            } finally {
                if (inputStream != null) {
                    StreamUtils.closeStream(inputStream);
                }
            }

            return null;
        }
    }

}
