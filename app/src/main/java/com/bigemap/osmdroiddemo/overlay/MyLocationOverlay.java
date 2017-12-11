package com.bigemap.osmdroiddemo.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.WindowManager;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.utils.PositionUtils;

import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.IOverlayMenuProvider;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.compass.IOrientationConsumer;
import org.osmdroid.views.overlay.compass.IOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.LinkedList;

/**
 * for google map
 * Created by Think on 2017/9/8.
 */

public class MyLocationOverlay extends Overlay implements IMyLocationConsumer,
        IOverlayMenuProvider, Overlay.Snappable, IOrientationConsumer {
    private static final String TAG = "MyLocationOverlay";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    private Paint mCirclePaint = new Paint();

    private final float mScale;

    private Bitmap mDirectionArrowBitmap;

    private MapView mMapView;

    private IMapController mMapController;
    private IMyLocationProvider mMyLocationProvider;

    private final LinkedList<Runnable> mRunOnFirstFix = new LinkedList<Runnable>();
    private final Point mMapCoordsProjected = new Point();
    private final Point mMapCoordsTranslated = new Point();
    private Handler mHandler;
    private Object mHandlerToken = new Object();

    /**
     * if true, when the user pans the map, follow my location will automatically disable
     * if false, when the user pans the map, the map will continue to follow current location
     */
    private boolean enableAutoStop = true;
    private Location mLocation;
    private final GeoPoint mGeoPoint = new GeoPoint(0.0, 0.0); // for reuse
    private boolean mIsLocationEnabled = false;
    private boolean mIsFollowing = false; // follow location updates
    private boolean mDrawAccuracyEnabled = true;

    private float mDirectionArrowCenterX;
    private float mDirectionArrowCenterY;

    private static final int MENU_MY_LOCATION = getSafeMenuId();

    private boolean mOptionsMenuEnabled = true;

    // to avoid allocations during onDraw
    private final float[] mMatrixValues = new float[9];
    private Matrix mMatrix = new Matrix();
    private Rect mMyLocationRect = new Rect();
    private Rect mMyLocationPreviousRect = new Rect();

    //地图源
    private int mTileSource = 0;

    //Compass
    private Paint sSmoothPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Display mDisplay;
    private IOrientationProvider mOrientationProvider;
    private Bitmap mCompassFrameBitmap;
    private boolean mIsCompassEnabled;
    private float mAzimuth = Float.NaN;

    private float mCompassFrameCenterX;
    private float mCompassFrameCenterY;

    private static final int MENU_COMPASS = getSafeMenuId() + 1;

    // ===========================================================
    // Constructors
    // ===========================================================

    public MyLocationOverlay(MapView mapView) {
        this(new GpsMyLocationProvider(mapView.getContext()), mapView);
    }

    public MyLocationOverlay(IMyLocationProvider myLocationProvider, MapView mapView) {
        super();
        mScale = mapView.getContext().getResources().getDisplayMetrics().density;

        mMapView = mapView;
        mMapController = mapView.getController();
        mCirclePaint.setARGB(0, 100, 100, 255);
        mCirclePaint.setAntiAlias(true);

        final WindowManager windowManager = (WindowManager) mapView.getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        mDisplay = windowManager.getDefaultDisplay();

        setCompass(getBitmapFromVectorDrawable(mapView.getContext(), R.drawable.ic_vector_compass));
        setDirectionArrow(BitmapFactory.decodeResource(mapView.getResources(), R.drawable.img_point));

        mHandler = new Handler(Looper.getMainLooper());
        setMyLocationProvider(myLocationProvider);
//        setOrientationProvider(new InternalCompassOrientationProvider(mapView.getContext()));
    }

    /**
     * fix for https://github.com/osmdroid/osmdroid/issues/249
     *
     * @param directionArrowBitmap 定位图标
     */
    public void setDirectionArrow(final Bitmap directionArrowBitmap) {
        this.mDirectionArrowBitmap = directionArrowBitmap;

        mDirectionArrowCenterX = mDirectionArrowBitmap.getWidth() / 2.0f - 0.5f;
        mDirectionArrowCenterY = mDirectionArrowBitmap.getHeight() / 2.0f - 0.5f;

    }

    private void setCompass(Bitmap compassBitmap) {
        this.mCompassFrameBitmap = compassBitmap;
        mCompassFrameCenterX = mCompassFrameBitmap.getWidth() / 2 - 0.5f;
        mCompassFrameCenterY = mCompassFrameBitmap.getHeight() / 2 - 0.5f;
    }

    public int getTileSource() {
        return mTileSource;
    }

    public void setTileSource(int mTileSource) {
        this.mTileSource = mTileSource;
    }

    @Override
    public void onDetach(MapView mapView) {
        Log.d(TAG, "onDetach: ");
        this.disableMyLocation();
        /*if (mPersonBitmap != null) {
            mPersonBitmap.recycle();
		}
		if (mDirectionArrowBitmap != null) {
			mDirectionArrowBitmap.recycle();
		}*/
        this.mMapView = null;
        this.mMapController = null;
        mHandler = null;
        mMatrix = null;
        mCirclePaint = null;
        //mPersonBitmap = null;
        //mDirectionArrowBitmap = null;
        mHandlerToken = null;
        mLocation = null;
        mMapController = null;
        mMyLocationPreviousRect = null;
        if (mMyLocationProvider != null)
            mMyLocationProvider.destroy();

        mMyLocationProvider = null;
        sSmoothPaint = null;
        this.disableCompass();
        super.onDetach(mapView);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    /**
     * If enabled, an accuracy circle will be drawn around your current position.
     *
     * @param drawAccuracyEnabled whether the accuracy circle will be enabled
     */
    public void setDrawAccuracyEnabled(final boolean drawAccuracyEnabled) {
        mDrawAccuracyEnabled = drawAccuracyEnabled;
    }

    /**
     * If enabled, an accuracy circle will be drawn around your current position.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isDrawAccuracyEnabled() {
        return mDrawAccuracyEnabled;
    }

    public IMyLocationProvider getMyLocationProvider() {
        return mMyLocationProvider;
    }

    private void setMyLocationProvider(IMyLocationProvider myLocationProvider) {
        if (myLocationProvider == null)
            throw new RuntimeException(
                    "You must pass an IMyLocationProvider to setMyLocationProvider()");

        if (isMyLocationEnabled())
            stopLocationProvider();

        mMyLocationProvider = myLocationProvider;
    }

    private void drawMyLocation(final Canvas canvas, final MapView mapView, final Location lastFix, final float bearing) {
        final Projection pj = mapView.getProjection();
        pj.toPixelsFromProjected(mMapCoordsProjected, mMapCoordsTranslated);

        if (mDrawAccuracyEnabled) {
            final float radius = lastFix.getAccuracy()
                    / (float) TileSystem.GroundResolution(lastFix.getLatitude(),
                    mapView.getZoomLevel());

            mCirclePaint.setAlpha(50);
            mCirclePaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mMapCoordsTranslated.x, mMapCoordsTranslated.y, radius, mCirclePaint);

            mCirclePaint.setAlpha(150);
            mCirclePaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(mMapCoordsTranslated.x, mMapCoordsTranslated.y, radius, mCirclePaint);
        }

        canvas.getMatrix(mMatrix);
        mMatrix.getValues(mMatrixValues);

        // Calculate real scale including accounting for rotation
        float scaleX = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_X]
                * mMatrixValues[Matrix.MSCALE_X] + mMatrixValues[Matrix.MSKEW_Y]
                * mMatrixValues[Matrix.MSKEW_Y]);
        float scaleY = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_Y]
                * mMatrixValues[Matrix.MSCALE_Y] + mMatrixValues[Matrix.MSKEW_X]
                * mMatrixValues[Matrix.MSKEW_X]);

        canvas.save();
        // Rotate the icon if we have a GPS fix, take into account if the map is already rotated
        float mapRotation = lastFix.getBearing();
        if (mapRotation >= 360.0f)
            mapRotation = mapRotation - 360f;
        canvas.rotate(mapRotation, mMapCoordsTranslated.x, mMapCoordsTranslated.y);
        // Counteract any scaling that may be happening so the icon stays the same size
        canvas.scale(1 / scaleX, 1 / scaleY, mMapCoordsTranslated.x, mMapCoordsTranslated.y);
        // Draw the bitmap
        canvas.drawBitmap(mDirectionArrowBitmap, mMapCoordsTranslated.x - mDirectionArrowCenterX,
                mMapCoordsTranslated.y - mDirectionArrowCenterY, sSmoothPaint);
        canvas.restore();

        if (isCompassEnabled()) {
            canvas.save();
            canvas.rotate(-bearing, mMapCoordsTranslated.x, mMapCoordsTranslated.y);
            canvas.scale(1 / scaleX, 1 / scaleY, mMapCoordsTranslated.x, mMapCoordsTranslated.y);
            canvas.drawBitmap(mCompassFrameBitmap, mMapCoordsTranslated.x - mCompassFrameCenterX,
                    mMapCoordsTranslated.y - mCompassFrameCenterY, sSmoothPaint);
            canvas.restore();
        }
    }

    private Rect getMyLocationDrawingBounds(int zoomLevel, Location lastFix, Rect reuse) {
        if (reuse == null)
            reuse = new Rect();

        final Projection pj = mMapView.getProjection();
        pj.toPixelsFromProjected(mMapCoordsProjected, mMapCoordsTranslated);

        // Start with the bitmap bounds
        if (lastFix.hasBearing()) {
            // Get a square bounding box around the object, and expand by the length of the diagonal
            // so as to allow for extra space for rotating
            int widestEdge = (int) Math.ceil(Math.max(mDirectionArrowBitmap.getWidth(),
                    mDirectionArrowBitmap.getHeight()) * Math.sqrt(2));
            reuse.set(mMapCoordsTranslated.x, mMapCoordsTranslated.y, mMapCoordsTranslated.x
                    + widestEdge, mMapCoordsTranslated.y + widestEdge);
            reuse.offset(-widestEdge / 2, -widestEdge / 2);
        }

        // Add in the accuracy circle if enabled
        if (mDrawAccuracyEnabled) {
            final int radius = (int) Math.ceil(lastFix.getAccuracy()
                    / (float) TileSystem.GroundResolution(lastFix.getLatitude(), zoomLevel));
            reuse.union(mMapCoordsTranslated.x - radius, mMapCoordsTranslated.y - radius,
                    mMapCoordsTranslated.x + radius, mMapCoordsTranslated.y + radius);
            final int strokeWidth = (int) Math.ceil(mCirclePaint.getStrokeWidth() == 0 ? 1
                    : mCirclePaint.getStrokeWidth());
            reuse.inset(-strokeWidth, -strokeWidth);
        }

        return reuse;
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public void draw(Canvas c, MapView mapView, boolean shadow) {
        if (shadow)
            return;

        if (mLocation != null && isMyLocationEnabled()) {
            drawMyLocation(c, mapView, mLocation, mAzimuth + getDisplayOrientation());
        }

    }

    @Override
    public boolean onSnapToItem(final int x, final int y, final Point snapPoint,
                                final IMapView mapView) {
        if (this.mLocation != null) {
            Projection pj = mMapView.getProjection();
            pj.toPixelsFromProjected(mMapCoordsProjected, mMapCoordsTranslated);
            snapPoint.x = mMapCoordsTranslated.x;
            snapPoint.y = mMapCoordsTranslated.y;
            final double xDiff = x - mMapCoordsTranslated.x;
            final double yDiff = y - mMapCoordsTranslated.y;
            boolean snap = xDiff * xDiff + yDiff * yDiff < 64;
            if (Configuration.getInstance().isDebugMode()) {
                Log.d(IMapView.LOGTAG, "snap=" + snap);
            }
            return snap;
        } else {
            return false;
        }
    }

    public void setEnableAutoStop(boolean value) {
        this.enableAutoStop = value;
    }

    public boolean getEnableAutoStop() {
        return this.enableAutoStop;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (enableAutoStop)
                this.disableFollowLocation();
            else
                return true;//prevent the pan
        }

        return super.onTouchEvent(event, mapView);
    }

    // ===========================================================
    // Menu handling methods
    // ===========================================================

    @Override
    public void setOptionsMenuEnabled(final boolean pOptionsMenuEnabled) {
        this.mOptionsMenuEnabled = pOptionsMenuEnabled;
    }

    @Override
    public boolean isOptionsMenuEnabled() {
        return this.mOptionsMenuEnabled;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
                                       final MapView pMapView) {
        pMenu.add(0, MENU_MY_LOCATION + pMenuIdOffset, Menu.NONE,
                pMapView.getContext().getResources().getString(org.osmdroid.library.R.string.my_location))
                .setIcon(
                        pMapView.getContext().getResources().getDrawable(org.osmdroid.library.R.drawable.ic_menu_mylocation)
                )
                .setCheckable(true);

        pMenu.add(0, MENU_COMPASS + pMenuIdOffset, Menu.NONE,
                pMapView.getContext().getResources().getString(org.osmdroid.library.R.string.compass))
                .setIcon(ContextCompat.getDrawable(pMapView.getContext(), R.drawable.img_compass))
                .setCheckable(true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
                                        final MapView pMapView) {
        pMenu.findItem(MENU_MY_LOCATION + pMenuIdOffset).setChecked(this.isMyLocationEnabled());
        pMenu.findItem(MENU_COMPASS + pMenuIdOffset).setChecked(this.isCompassEnabled());
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem pItem, final int pMenuIdOffset,
                                         final MapView pMapView) {
        final int menuId = pItem.getItemId() - pMenuIdOffset;
        if (menuId == MENU_MY_LOCATION) {
            if (this.isMyLocationEnabled()) {
                this.disableFollowLocation();
                this.disableMyLocation();
            } else {
                this.enableFollowLocation();
                this.enableMyLocation();
            }
            return true;
        } else if (menuId == MENU_COMPASS) {
            if (this.isCompassEnabled()) {
                this.disableCompass();
            } else {
                this.enableCompass();
            }
            return true;
        } else {
            return false;
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * Return a GeoPoint of the last known location, or null if not known.
     */
    public GeoPoint getMyLocation() {
        if (mLocation == null) {
            return null;
        } else {
            return new GeoPoint(mLocation);
        }
    }

    public Location getLastFix() {
        return mLocation;
    }

    /**
     * Enables "follow" functionality. The map will center on your current location and
     * automatically scroll as you move. Scrolling the map in the UI will disable.
     */
    public void enableFollowLocation() {
        mIsFollowing = true;

        // set initial location when enabled
        if (isMyLocationEnabled()) {
            Location location = mMyLocationProvider.getLastKnownLocation();
            if (location != null) {
                setLocation(location);
            }
        }

        // Update the screen to see changes take effect
        if (mMapView != null) {
            mMapView.postInvalidate();
        }
    }

    /**
     * Disables "follow" functionality.
     */
    public void disableFollowLocation() {
        mIsFollowing = false;
    }

    /**
     * If enabled, the map will center on your current location and automatically scroll as you
     * move. Scrolling the map in the UI will disable.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isFollowLocationEnabled() {
        return mIsFollowing;
    }

    @Override
    public void onLocationChanged(final Location location, IMyLocationProvider source) {

        if (location != null && mHandler != null) {
            // These location updates can come in from different threads
            mHandler.postAtTime(new Runnable() {
                @Override
                public void run() {
                    setLocation(location);

                    for (final Runnable runnable : mRunOnFirstFix) {
                        new Thread(runnable).start();
                    }
                    mRunOnFirstFix.clear();
                }
            }, mHandlerToken, 0);
        }
    }

    protected void setLocation(Location location) {
        // If we had a previous location, let's get those bounds

        Location oldLocation = mLocation;
        if (oldLocation != null) {
            this.getMyLocationDrawingBounds(mMapView.getZoomLevel(), oldLocation,
                    mMyLocationPreviousRect);
        }

        mLocation = convertLocation(location);

        // Cache location point
        mMapView.getProjection().toProjectedPixels(mLocation.getLatitude(),
                mLocation.getLongitude(), mMapCoordsProjected);

        if (mIsFollowing) {
            mGeoPoint.setLatitude(mLocation.getLatitude());
            mGeoPoint.setLongitude(mLocation.getLongitude());
            mMapController.animateTo(mGeoPoint);
        } else {
            // Get new drawing bounds
            this.getMyLocationDrawingBounds(mMapView.getZoomLevel(), mLocation, mMyLocationRect);

            // If we had a previous location, merge in those bounds too
            if (oldLocation != null) {
                mMyLocationRect.union(mMyLocationPreviousRect);
            }

            final int left = mMyLocationRect.left;
            final int top = mMyLocationRect.top;
            final int right = mMyLocationRect.right;
            final int bottom = mMyLocationRect.bottom;

            // Invalidate the bounds
            mMapView.invalidateMapCoordinates(left, top, right, bottom);
        }
    }

    public boolean enableMyLocation(IMyLocationProvider myLocationProvider) {
        // Set the location provider. This will call stopLocationProvider().
        setMyLocationProvider(myLocationProvider);

        boolean success = mMyLocationProvider.startLocationProvider(this);
        mIsLocationEnabled = success;

        // set initial location when enabled
        if (success) {
            Location location = mMyLocationProvider.getLastKnownLocation();
            if (location != null) {
                setLocation(location);
            }
        }

        // Update the screen to see changes take effect
        if (mMapView != null) {
            mMapView.postInvalidate();
        }
        return success;
    }

    /**
     * 坐标系转换
     *
     * @param gpsLocation gps坐标
     * @return
     */
    private Location convertLocation(Location gpsLocation) {
        Location convertedLocation = null;
        switch (mTileSource) {
            case Constant.GOOGLE_MAP:
                convertedLocation = PositionUtils.gps_To_Gcj02(gpsLocation);
                break;
            case Constant.OSM:
                convertedLocation = gpsLocation;
                break;
        }
        return convertedLocation;
    }

    /**
     * Enable receiving location updates from the provided IMyLocationProvider and show your
     * location on the maps. You will likely want to call enableMyLocation() from your Activity's
     * Activity.onResume() method, to enable the features of this overlay. Remember to call the
     * corresponding disableMyLocation() in your Activity's Activity.onPause() method to turn off
     * updates when in the background.
     */
    public boolean enableMyLocation() {
        return enableMyLocation(mMyLocationProvider);
    }

    /**
     * Disable location updates
     */
    public void disableMyLocation() {
        mIsLocationEnabled = false;

        stopLocationProvider();

        // Update the screen to see changes take effect
        if (mMapView != null) {
            mMapView.postInvalidate();
        }
    }

    public boolean enableCompass() {
        return enableCompass(mOrientationProvider);
    }

    private boolean enableCompass(IOrientationProvider orientationProvider) {
        // Set the orientation provider. This will call stopOrientationProvider().
        setOrientationProvider(orientationProvider);

        boolean success = mOrientationProvider.startOrientationProvider(this);
        mIsCompassEnabled = success;
        Log.d(TAG, "enableCompass: isCompass="+success);

        // Update the screen to see changes take effect
        if (mMapView != null) {
            this.invalidateCompass();
        }

        return success;
    }

    public void disableCompass() {
        mIsCompassEnabled = false;

        if (mOrientationProvider != null) {
            mOrientationProvider.stopOrientationProvider();
        }
        mOrientationProvider = null;

        // Reset values
        mAzimuth = Float.NaN;

        // Update the screen to see changes take effect
        if (mMapView != null) {
            this.invalidateCompass();
        }
    }

    private void stopLocationProvider() {
        if (mMyLocationProvider != null) {
            mMyLocationProvider.stopLocationProvider();
        }
        if (mHandler != null && mHandlerToken != null)
            mHandler.removeCallbacksAndMessages(mHandlerToken);
    }

    /**
     * If enabled, the map is receiving location updates and drawing your location on the map.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isMyLocationEnabled() {
        return mIsLocationEnabled;
    }

    /**
     * Queues a runnable to be executed as soon as we have a location fix. If we already have a fix,
     * we'll execute the runnable immediately and return true. If not, we'll hang on to the runnable
     * and return false; as soon as we get a location fix, we'll run it in in a new thread.
     */
    public boolean runOnFirstFix(final Runnable runnable) {
        if (mMyLocationProvider != null && mLocation != null) {
            new Thread(runnable).start();
            return true;
        } else {
            mRunOnFirstFix.addLast(runnable);
            return false;
        }
    }

    public boolean isCompassEnabled() {
        return mIsCompassEnabled;
    }

    public IOrientationProvider getOrientationProvider() {
        return mOrientationProvider;
    }

    public void setOrientationProvider(IOrientationProvider orientationProvider) throws RuntimeException {
        if (orientationProvider == null)
            throw new RuntimeException(
                    "You must pass an IOrientationProvider to setOrientationProvider()");

        if (isCompassEnabled())
            mOrientationProvider.stopOrientationProvider();

        mOrientationProvider = orientationProvider;
    }

    @Override
    public void onOrientationChanged(float orientation, IOrientationProvider source) {
        mAzimuth = orientation;
        this.invalidateCompass();
    }

    private void invalidateCompass() {
        Rect screenRect = mMapView.getProjection().getScreenRect();
        int left = screenRect.left + (int) Math.ceil(mCompassFrameCenterX * mScale);
        int top = screenRect.top + (int) Math.ceil(mCompassFrameCenterY * mScale);

        // Expand by 2 to cover stroke width
        mMapView.postInvalidateMapCoordinates(left - 2, top - 2, left + 2, top + 2);
    }

    private int getDisplayOrientation() {
        switch (mDisplay.getRotation()) {
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_0:
            default:
                return 0;
        }
    }

    /**
     * 矢量bitmap
     *
     * @param context
     * @param drawableId
     * @return
     */
    private Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
