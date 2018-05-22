package com.trackdealer.helpersUI;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.squareup.picasso.Transformation;

import timber.log.Timber;

/**
 * Трансформация картинки
 * <p>
 * Преобразуем в масштаб экрана и обрезаем справа и снизу
 */
public class BitmapTransform implements Transformation {

    private final String TAG = "BitmapTransform";

    private final int maxWidth;
    private final int maxHeight;
    private final int cropHeightBot;
    private final int cropWidthLeft;

    public BitmapTransform(int maxWidth, int maxHeight, int cropWidthLeft, int cropHeightBot) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.cropHeightBot = cropHeightBot;
        this.cropWidthLeft = cropWidthLeft;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int targetWidth, targetHeight;
        double aspectRatio;

        targetHeight = maxHeight;
        aspectRatio = (double) maxWidth / (double) maxHeight;
        targetWidth = (int) (targetHeight * aspectRatio);

        Timber.d(TAG + " sourceWidth = " + source.getWidth() + " sourceHeight = " + source.getHeight());
        Timber.d(TAG + " targetWidth = " + targetWidth + " targetHeight = " + targetHeight);

        Bitmap result = Bitmap.createScaledBitmap(source, source.getWidth()*maxHeight/source.getHeight(), source.getHeight()*maxHeight/source.getHeight(), false);

        Rect rect = new Rect(0, 0, targetWidth, targetHeight);
        Bitmap resultBmp = Bitmap.createBitmap(rect.right, rect.bottom, Bitmap.Config.ARGB_8888);
        new Canvas(resultBmp).drawBitmap(result, 0, 0, null);
        if (resultBmp != source) {
            source.recycle();
        }
        return resultBmp;
    }

    @Override
    public String key() {
        return maxWidth + "x" + maxHeight;
    }

}