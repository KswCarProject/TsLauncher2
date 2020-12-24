package com.android.launcher2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.view.ViewCompat;
import com.android.launcher.R;
import java.util.Random;

final class Utilities {
    private static final String TAG = "Launcher.Utilities";
    private static final Paint sBlurPaint = new Paint();
    private static final Canvas sCanvas = new Canvas();
    static int sColorIndex = 0;
    static int[] sColors = {SupportMenu.CATEGORY_MASK, -16711936, -16776961};
    private static final Paint sDisabledPaint = new Paint();
    private static final Paint sGlowColorFocusedPaint = new Paint();
    private static final Paint sGlowColorPressedPaint = new Paint();
    private static int sIconHeight = -1;
    private static int sIconTextureHeight = -1;
    private static int sIconTextureWidth = -1;
    private static int sIconWidth = -1;
    private static final Rect sOldBounds = new Rect();

    Utilities() {
    }

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
    }

    static Bitmap createIconBitmap(Bitmap icon, Context context) {
        return (icon.getWidth() == sIconTextureWidth && icon.getHeight() == sIconTextureHeight) ? icon : createIconBitmap((Drawable) new BitmapDrawable(context.getResources(), icon), context);
    }

    static Bitmap createIconBitmap(Drawable icon, Context context) {
        return createIconBitmap(icon, context, (Bitmap) null);
    }

    static Bitmap createIconBitmap(Drawable icon, Context context, Bitmap iconBack) {
        Bitmap bitmap;
        Bitmap icon_back;
        synchronized (sCanvas) {
            if (sIconWidth == -1) {
                initStatics(context);
            }
            int width = sIconWidth;
            int height = sIconHeight;
            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                if (bitmapDrawable.getBitmap().getDensity() == 0) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0 && !(width == sourceWidth && height == sourceHeight)) {
                float ratio = ((float) sourceWidth) / ((float) sourceHeight);
                if (sourceWidth > sourceHeight) {
                    height = (int) (((float) width) / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (((float) height) * ratio);
                }
            }
            int textureWidth = sIconTextureWidth;
            int textureHeight = sIconTextureHeight;
            Canvas canvas = sCanvas;
            if (context.getResources().getInteger(R.integer.myiconback) == 0) {
                if (iconBack != null) {
                    icon_back = iconBack;
                } else {
                    icon_back = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_back);
                }
                textureWidth = icon_back.getWidth();
                textureHeight = icon_back.getHeight();
                bitmap = Bitmap.createBitmap(textureWidth, textureHeight, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(bitmap);
                if (sourceWidth == textureWidth && sourceHeight == textureHeight) {
                    width = textureWidth;
                    height = textureHeight;
                } else {
                    canvas.drawBitmap(icon_back, 0.0f, 0.0f, new Paint());
                }
            } else {
                bitmap = Bitmap.createBitmap(textureWidth, textureHeight, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(bitmap);
            }
            int left = (textureWidth - width) / 2;
            int top = (textureHeight - height) / 2;
            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left + width, top + height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
            canvas.setBitmap((Bitmap) null);
        }
        return bitmap;
    }

    static void drawSelectedAllAppsBitmap(Canvas dest, int destWidth, int destHeight, boolean pressed, Bitmap src) {
        synchronized (sCanvas) {
            if (sIconWidth == -1) {
                throw new RuntimeException("Assertion failed: Utilities not initialized");
            }
            dest.drawColor(0, PorterDuff.Mode.CLEAR);
            int[] xy = new int[2];
            Bitmap mask = src.extractAlpha(sBlurPaint, xy);
            dest.drawBitmap(mask, ((float) ((destWidth - src.getWidth()) / 2)) + ((float) xy[0]), ((float) ((destHeight - src.getHeight()) / 2)) + ((float) xy[1]), pressed ? sGlowColorPressedPaint : sGlowColorFocusedPaint);
            mask.recycle();
        }
    }

    static Bitmap resampleIconBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) {
            if (sIconWidth == -1) {
                initStatics(context);
            }
            if (bitmap.getWidth() != sIconWidth || bitmap.getHeight() != sIconHeight) {
                bitmap = createIconBitmap((Drawable) new BitmapDrawable(context.getResources(), bitmap), context);
            }
        }
        return bitmap;
    }

    static Bitmap drawDisabledBitmap(Bitmap bitmap, Context context) {
        Bitmap disabled;
        synchronized (sCanvas) {
            if (sIconWidth == -1) {
                initStatics(context);
            }
            disabled = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = sCanvas;
            canvas.setBitmap(disabled);
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, sDisabledPaint);
            canvas.setBitmap((Bitmap) null);
        }
        return disabled;
    }

    private static void initStatics(Context context) {
        Resources resources = context.getResources();
        float density = resources.getDisplayMetrics().density;
        int dimension = (int) resources.getDimension(R.dimen.app_icon_size);
        sIconHeight = dimension;
        sIconWidth = dimension;
        int i = sIconWidth;
        sIconTextureHeight = i;
        sIconTextureWidth = i;
        sBlurPaint.setMaskFilter(new BlurMaskFilter(5.0f * density, BlurMaskFilter.Blur.NORMAL));
        sGlowColorPressedPaint.setColor(-15616);
        sGlowColorFocusedPaint.setColor(-29184);
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.2f);
        sDisabledPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        sDisabledPaint.setAlpha(136);
    }

    static int roundToPow2(int n) {
        int orig = n;
        int n2 = n >> 1;
        int mask = 134217728;
        while (mask != 0 && (n2 & mask) == 0) {
            mask >>= 1;
        }
        while (mask != 0) {
            n2 |= mask;
            mask >>= 1;
        }
        int n3 = n2 + 1;
        if (n3 != orig) {
            return n3 << 1;
        }
        return n3;
    }

    static int generateRandomId() {
        return new Random(System.currentTimeMillis()).nextInt(ViewCompat.MEASURED_STATE_TOO_SMALL);
    }
}
