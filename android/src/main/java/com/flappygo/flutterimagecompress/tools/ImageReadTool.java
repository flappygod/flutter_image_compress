package com.flappygo.flutterimagecompress.tools;

import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.graphics.Bitmap;
import android.graphics.Matrix;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

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
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        if (setting != null && setting.getInPreferredConfig() != null) {
            options.inPreferredConfig = setting.getInPreferredConfig();
        }
        //最大高度宽度
        int inSampleSize = 1;
        if (imageWidth != 0 && imageHeight != 0 && setting != null) {
            inSampleSize = computeSampleSize(
                    options,
                    -1,
                    setting.getMaxHeight() * setting.getMaxWidth()
            );
        }
        ///大小
        double sizeData = FileSizeUtil.getFileOrFilesSize(path, FileSizeUtil.SIZETYPE_KB);
        if (setting != null && setting.getMaxKbSize() != 0) {
            double scaleData = sizeData / setting.getMaxKbSize();
            if (scaleData > 1.0) {
                scaleData = Math.sqrt(scaleData);
            }
            inSampleSize = Math.min(inSampleSize, (int) scaleData);
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return options;
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

    //compute size
    public static int computeSampleSize(Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    //compute size
    private static int computeInitialSampleSize(Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

}
