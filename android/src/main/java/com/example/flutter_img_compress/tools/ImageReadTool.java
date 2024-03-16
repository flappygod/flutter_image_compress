package com.example.flutter_img_compress.tools;

import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.graphics.Bitmap;
import android.graphics.Matrix;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.math.BigDecimal;

/********
 * @author flappygo
 */
public class ImageReadTool {

    private static final String TAG = "ImageReadTool";


    //read file bitmap
    public synchronized static Bitmap readFileBitmap(String path,
                                                     LXImageReadOption setting) throws Exception {

        ///error files
        File file = new File(path);
        if (!file.exists() || file.isDirectory()) {
            throw new Exception("error file");
        }

        ///get input stream
        int degree = getBitmapDegree(path);

        //get bitmap file
        FileInputStream fin = new FileInputStream(path);
        Options option = getOption(path, setting);
        Bitmap bm;

        ///角度为零
        if (degree == 0) {
            bm = BitmapFactory.decodeStream(fin, null, option);
        }
        ///旋转角度
        else {
            bm = rotateBitmapByDegree(BitmapFactory.decodeStream(fin, null, option), degree);
        }
        if (setting != null) {
            if (setting.isScaleFill()) {
                bm = imageScale(bm, setting);
            } else {
                bm = imageScaleMax(bm, setting);
            }
        }
        if (setting != null && setting.getRadiusOption() != null) {
            bm = BitmapRadiusTool.toRoundCorner(bm, setting.getRadiusOption());
        }
        fin.close();
        return bm;
    }


    ///获取图片旋转角度
    private static int getBitmapDegree(String path) throws IOException {
        int degree = 0;
        // 从指定路径下读取图片，并获取其EXIF信息
        ExifInterface exifInterface = new ExifInterface(path);
        // 获取图片的旋转信息
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
        }
        return degree;
    }

    ///将图片旋转多少度
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
        returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }


    //get option
    public synchronized static Options getOption(String path, LXImageReadOption setting) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        if (setting != null && setting.getInPreferredConfig() != null) {
            options.inPreferredConfig = setting.getInPreferredConfig();
        }
        //最大高度宽度
        int inSampleSize = 1;

        ///set max size
        if (options.outWidth != 0 && options.outHeight != 0 && setting != null && setting.getMaxHeight() != 0 && setting.getMaxWidth() != 0) {
            inSampleSize = calculateInSampleSize(
                    options,
                    setting.getMaxHeight()
                    , setting.getMaxWidth()
            );
        }
        double scaleData;
        double sizeData = FileSizeUtil.getFileOrFilesSize(path, FileSizeUtil.SIZETYPE_KB);
        if (setting != null && setting.getMaxKbSize() != 0) {
            scaleData = sizeData / setting.getMaxKbSize();
            if (scaleData > 1.0) {
                scaleData = Math.sqrt(scaleData);
            } else {
                scaleData = 1;
            }
            int scale = 1;
            while (scaleData > scale) {
                scale <<= 1;
            }
            inSampleSize = Math.max(inSampleSize, scale);
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return options;
    }


    ///calculate in sample size
    public static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    //scale the image
    public synchronized static Bitmap imageScaleMax(Bitmap bitmap,
                                                    LXImageReadOption setting) {
        int dst_w = setting.getMaxWidth();
        int dst_h = setting.getMaxHeight();
        //is zero
        if (dst_w <= 0 || dst_h <= 0)
            return bitmap;

        //no need to transform
        if (dst_w > bitmap.getWidth() && dst_h > bitmap.getHeight())
            return bitmap;

        float src_w = bitmap.getWidth();
        float src_h = bitmap.getHeight();

        float scale_w;
        float scale_h;
        if (src_w * 1.0 / src_h > dst_w * 1.0 / dst_h) {
            scale_w = ((float) dst_w) / src_w;
            scale_h = ((float) dst_w) / src_w;
        } else {
            scale_w = ((float) dst_h) / src_h;
            scale_h = ((float) dst_h) / src_h;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap bitmapNew = Bitmap.createBitmap(bitmap,
                0,
                0,
                (int) src_w,
                (int) src_h,
                matrix,
                true
        );
        if (bitmapNew != bitmap) {
            bitmap.recycle();
        }
        return bitmapNew;
    }

    //scale

    public synchronized static Bitmap imageScale(Bitmap bitmap,
                                                 LXImageReadOption setting) {
        int dst_w = setting.getMaxWidth();
        int dst_h = setting.getMaxHeight();
        //zero
        if (dst_w <= 0 || dst_h <= 0)
            return bitmap;
        //no need to transform
        if (dst_w > bitmap.getWidth() && dst_h > bitmap.getHeight())
            return bitmap;
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        float scale_w = ((float) dst_w) / src_w;
        float scale_h = ((float) dst_h) / src_h;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap bitmapNew = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix, true);
        if (bitmapNew != bitmap) {
            bitmap.recycle();
        }
        return bitmapNew;
    }


}
