package com.bigemap.osmdroiddemo.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.WindowManager;

import com.bigemap.osmdroiddemo.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.IOverlayMenuProvider;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.compass.IOrientationConsumer;
import org.osmdroid.views.overlay.compass.IOrientationProvider;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

public class MyCompassOverlay extends Overlay implements IOverlayMenuProvider, IOrientationConsumer{
    private Paint sSmoothPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private MapView mMapView;
    private final Display mDisplay;

    private IOrientationProvider mOrientationProvider;

    private Bitmap mCompassFrameBitmap;
    private final Matrix mCompassMatrix = new Matrix();
    private boolean mIsCompassEnabled;

    /**
     * The bearing, in degrees east of north, or NaN if none has been set.
     */
    private float mAzimuth = Float.NaN;

    private float mCompassCenterX = 35.0f;
    private float mCompassCenterY = 35.0f;

    private final float mCompassFrameCenterX;
    private final float mCompassFrameCenterY;

    private static final int MENU_COMPASS = getSafeMenuId();

    private boolean mOptionsMenuEnabled = true;

    private final float mScale;
    // ===========================================================
    // Constructors
    // ===========================================================

    public MyCompassOverlay(Context context, MapView mapView) {
        this(context, new InternalCompassOrientationProvider(context), mapView);
    }


    public MyCompassOverlay(Context context, IOrientationProvider orientationProvider,
                          MapView mapView) {
        super();
        mScale = context.getResources().getDisplayMetrics().density;

        mMapView = mapView;
        final WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        mDisplay = windowManager.getDefaultDisplay();

        mCompassFrameBitmap=getBitmapFromVectorDrawable(context, R.drawable.ic_vector_compass);
        mCompassFrameCenterX = mCompassFrameBitmap.getWidth() / 2 - 0.5f;
        mCompassFrameCenterY = mCompassFrameBitmap.getHeight() / 2 - 0.5f;

        setOrientationProvider(orientationProvider);
    }

    @Override
    public void onDetach(MapView mapView) {
        this.mMapView=null;
        sSmoothPaint=null;
        this.disableCompass();
        mCompassFrameBitmap.recycle();
        super.onDetach(mapView);
    }

    private void invalidateCompass() {
        Rect screenRect = mMapView.getProjection().getScreenRect();
        final int frameLeft = screenRect.left
                + (int) Math.ceil((mCompassCenterX - mCompassFrameCenterX) * mScale);
        final int frameTop = screenRect.top
                + (int) Math.ceil((mCompassCenterY - mCompassFrameCenterY) * mScale);
        final int frameRight = screenRect.left
                + (int) Math.ceil((mCompassCenterX + mCompassFrameCenterX) * mScale);
        final int frameBottom = screenRect.top
                + (int) Math.ceil((mCompassCenterY + mCompassFrameCenterY) * mScale);

        // Expand by 2 to cover stroke width
        mMapView.postInvalidateMapCoordinates(frameLeft - 2, frameTop - 2, frameRight + 2,
                frameBottom + 2);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public void setCompassCenter(final float x, final float y) {
        mCompassCenterX = x;
        mCompassCenterY = y;
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

    private void drawCompass(final Canvas canvas, final float bearing, final Rect screenRect) {
        final Projection proj = mMapView.getProjection();
        final float centerX = mCompassCenterX * mScale;
        final float centerY = mCompassCenterY * mScale;

//        mCompassMatrix.setTranslate(-mCompassFrameCenterX, -mCompassFrameCenterY);
        mCompassMatrix.setRotate(-bearing, mCompassFrameCenterX, mCompassFrameCenterY);
        mCompassMatrix.postTranslate(-mCompassFrameCenterX, -mCompassFrameCenterY);
        mCompassMatrix.postTranslate(centerX, centerY);

        canvas.save();
        canvas.concat(proj.getInvertedScaleRotateCanvasMatrix());
        canvas.concat(mCompassMatrix);
        canvas.drawBitmap(mCompassFrameBitmap, 0, 0, sSmoothPaint);
        canvas.restore();

    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public void draw(Canvas c, MapView mapView, boolean shadow) {
        if (shadow) {
            return;
        }

        if (isCompassEnabled() && !Float.isNaN(mAzimuth)) {
            drawCompass(c, mAzimuth + getDisplayOrientation(), mapView.getProjection()
                    .getScreenRect());
        }
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
        pMenu.add(0, MENU_COMPASS + pMenuIdOffset, Menu.NONE,
                pMapView.getContext().getResources().getString(org.osmdroid.library.R.string.compass))

                .setIcon(ContextCompat.getDrawable(pMapView.getContext(),org.osmdroid.library.R.drawable.ic_menu_compass))
                .setCheckable(true);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
                                        final MapView pMapView) {
        pMenu.findItem(MENU_COMPASS + pMenuIdOffset).setChecked(this.isCompassEnabled());
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem pItem, final int pMenuIdOffset,
                                         final MapView pMapView) {
        final int menuId = pItem.getItemId() - pMenuIdOffset;
        if (menuId == MENU_COMPASS) {
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

    @Override
    public void onOrientationChanged(float orientation, IOrientationProvider source) {
        mAzimuth = orientation;
        this.invalidateCompass();
    }

    public boolean enableCompass(IOrientationProvider orientationProvider) {
        // Set the orientation provider. This will call stopOrientationProvider().
        setOrientationProvider(orientationProvider);

        boolean success = mOrientationProvider.startOrientationProvider(this);
        mIsCompassEnabled = success;

        // Update the screen to see changes take effect
        if (mMapView != null) {
            this.invalidateCompass();
        }

        return success;
    }

    /**
     * Enable receiving orientation updates from the provided IOrientationProvider and show a
     * compass on the map. You will likely want to call enableCompass() from your Activity's
     * Activity.onResume() method, to enable the features of this overlay. Remember to call the
     * corresponding disableCompass() in your Activity's Activity.onPause() method to turn off
     * updates when in the background.
     */
    public boolean enableCompass() {
        return enableCompass(mOrientationProvider);
    }

    /**
     * Disable orientation updates
     */
    public void disableCompass() {
        mIsCompassEnabled = false;

        if (mOrientationProvider != null) {
            mOrientationProvider.stopOrientationProvider();
        }
        mOrientationProvider=null;

        // Reset values
        mAzimuth = Float.NaN;

        // Update the screen to see changes take effect
        if (mMapView != null) {
            this.invalidateCompass();
        }
    }

    /**
     * If enabled, the map is receiving orientation updates and drawing your location on the map.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isCompassEnabled() {
        return mIsCompassEnabled;
    }

    public float getOrientation() {
        return mAzimuth;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    private int getDisplayOrientation() {
        switch (mDisplay.getRotation()) {
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
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
