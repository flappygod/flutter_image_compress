package com.flappygo.flutterimagecompress.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/********
 * @author flappygo
 */
public class ImageReadTool {

    private static final String TAG = "ImageReadTool";

    /************
     * is exist
     *
     * @param path
     * @return
     */
    public static boolean isFileExsits(String path) {
        try {
            File file = new File(path);
            return file.exists();
        } catch (Exception e) {
            return false;
        }
    }

    //is file exist and not dic
    public static boolean isFileExsitsAntNotDic(String path) {
        try {
            File file = new File(path);
            return file.exists() && !file.isDirectory();
        } catch (Exception e) {
            return false;
        }
    }

    //get width and height
    public static LXImageWH getImageWH(String filePath) {
        // option
        Options options = new Options();
        // true
        options.inJustDecodeBounds = true;
        // get option
        BitmapFactory.decodeFile(filePath, options);
        LXImageWH ret = new LXImageWH();
        ret.setWidth(options.outWidth);
        ret.setHeight(options.outHeight);
        return ret;
    }


    //read file from drawable
    public synchronized static Drawable readFileDrawable(Context context,
                                                         String path,
                                                         LXImageReadOption setting) {

        // input stream
        FileInputStream fin = null;
        try {
            // is null
            if (path == null) {
                LogTool.w(TAG, "the path is a null");
                return null;
            }
            // null
            if (context == null) {
                LogTool.w(TAG, "the context is a null");
                return null;
            }
            // input stream
            fin = new FileInputStream(path);
            // option
            Options option = getOption(path, setting);
            // decode
            Bitmap bm = BitmapFactory.decodeStream(fin, null, option);
            // if setting success
            if (setting != null) {
                if (setting.isScaleFill()) {
                    bm = imageScale(bm, setting);
                } else {
                    bm = imageScaleMax(bm, setting);
                }
            }
            //decode
            if (setting != null && setting.getRadiusOption() != null) {
                bm = BitmapRadiusTool.toRoundCorner(bm, setting.getRadiusOption());
            }
            // to drawable
            BitmapDrawable drawable = new BitmapDrawable(
                    context.getResources(), bm);
            // return
            return drawable;
        } catch (FileNotFoundException e) {
            LogTool.e(TAG, e.getMessage());
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    LogTool.e(TAG, e.getMessage());
                }
            }
        }

        return null;
    }

    //read file bitmap
    public synchronized static Bitmap readFileBitmap(String path,
                                                     LXImageReadOption setting) throws Exception {
        // InputStream
        FileInputStream fin = null;
        try {
            File file = new File(path);
            if (path == null) {
                throw new Exception("the path is a null");
            }
            if (file.isDirectory()) {
                throw new Exception("the bitmapfile is a dictionary");
            }
            fin = new FileInputStream(path);
            Options option = getOption(path, setting);
            Bitmap bm = BitmapFactory.decodeStream(fin, null, option);
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
            return bm;
        } catch (FileNotFoundException e) {
            throw e;
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    LogTool.e(TAG, e.getMessage());
                }
            }
        }
    }

    //get path bitmap
    public synchronized static Bitmap readFileBitmap(String path) throws Exception {
        FileInputStream fin = null;
        try {
            if (path == null) {
                throw new Exception("the path is a null");
            }
            fin = new FileInputStream(path);
            Bitmap bm = BitmapFactory.decodeStream(fin);
            return bm;
        } catch (FileNotFoundException e) {
            throw e;
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    LogTool.e(TAG, e.getMessage());
                }
            }
        }
    }


    //get image size
    public synchronized static LXImageWH getImageSize(String path) {
        if (isFileExsitsAntNotDic(path)) {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            return new LXImageWH(imageWidth, imageHeight);
        } else {
            return null;
        }
    }


    //get option
    public synchronized static Options getOption(String path,
                                                 LXImageReadOption setting) {
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
        options.inSampleSize = 1;
        if (imageWidth != 0 && imageHeight != 0 && setting != null) {
            int sampleSize = computeSampleSize(options, -1,
                    setting.getMaxHeight() * setting.getMaxWidth());
            options.inSampleSize = sampleSize;
        }
        options.inJustDecodeBounds = false;
        return options;
    }

    //scale the image

    public synchronized static Bitmap imageScaleMax(Bitmap bitmap,
                                                    LXImageReadOption setting) {
        int dst_w = setting.getMaxWidth();
        int dst_h = setting.getMaxHeight();
        if (dst_w <= 0 || dst_h <= 0)
            return bitmap;
        float src_w = bitmap.getWidth();
        float src_h = bitmap.getHeight();

        float scale_w;
        float scale_h;

        if (src_w / src_h > dst_w / dst_h) {
            scale_w = ((float) dst_w) / src_w;
            scale_h = ((float) dst_w) / src_w;
        } else {
            scale_w = ((float) dst_h) / src_h;
            scale_h = ((float) dst_h) / src_h;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, (int) src_w,
                (int) src_h, matrix, true);

        return dstbmp;
    }

    //scale

    public synchronized static Bitmap imageScale(Bitmap bitmap,
                                                 LXImageReadOption setting) {
        int dst_w = setting.getMaxWidth();
        int dst_h = setting.getMaxHeight();
        if (dst_w <= 0 || dst_h <= 0)
            return bitmap;
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        float scale_w = ((float) dst_w) / src_w;
        float scale_h = ((float) dst_h) / src_h;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,
                true);
        return dstbmp;
    }

    //compute size
    public static int computeSampleSize(Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);
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

    private static int computeInitialSampleSize(Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
                Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
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
