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
 * Package Name:com.flappygo.lipo.limagegetter.tools <br/>
 * ClassName: ImageReadTool <br/>
 * Function: 图片读取的工具 <br/>
 * date: 2016-3-9 下午3:27:46 <br/>
 *
 * @author lijunlin
 */
public class ImageReadTool {

    private static final String TAG = "ImageReadTool";

    /************
     * 判断文件是否存在
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

    /*********
     * 判断文件是否存在
     *
     * @param path 判断文件是否存在而且不是文件夹
     * @return
     */
    public static boolean isFileExsitsAntNotDic(String path) {
        try {
            File file = new File(path);
            return file.exists() && !file.isDirectory();
        } catch (Exception e) {
            return false;
        }
    }

    /***************
     * 获取图片的宽高
     *
     * @param filePath 文件路径
     * @return
     */
    public static LXImageWH getImageWH(String filePath) {
        // 创建设置
        Options options = new Options();
        // 设置inJustDecodeBounds为true后，decodeFile并不分配空间，此时计算原始图片的长度和宽度
        options.inJustDecodeBounds = true;
        // 取得参数
        BitmapFactory.decodeFile(filePath, options);
        LXImageWH ret = new LXImageWH();
        ret.setWidth(options.outWidth);
        ret.setHeight(options.outHeight);
        return ret;
    }


    /**********************
     * 获取文件drawable
     *
     * @param context 上下文
     * @param path    文件路径
     * @param setting 读取图片的参数设置
     * @return
     */
    public synchronized static Drawable readFileDrawable(Context context,
                                                         String path,
                                                         LXImageReadOption setting) {

        // 获取InputStream
        FileInputStream fin = null;
        try {
            // 如果为空就返回空的
            if (path == null) {
                LogTool.w(TAG, "the path is a null");
                return null;
            }
            // 上下文为空返回
            if (context == null) {
                LogTool.w(TAG, "the context is a null");
                return null;
            }
            // 获取输入流
            fin = new FileInputStream(path);
            // 通过读取图片的大小设置获取option
            Options option = getOption(path, setting);
            // 真正的解析图片
            Bitmap bm = BitmapFactory.decodeStream(fin, null, option);
            // 如果有设置
            if (setting != null) {
                if (setting.isScaleFill()) {
                    bm = imageScale(bm, setting);
                } else {
                    bm = imageScaleMax(bm, setting);
                }
            }
            //进行转换
            if (setting != null && setting.getRadiusOption() != null) {
                bm = BitmapRadiusTool.toRoundCorner(bm, setting.getRadiusOption());
            }
            // 创建drawable
            BitmapDrawable drawable = new BitmapDrawable(
                    context.getResources(), bm);
            // 返回drawable
            return drawable;
        } catch (FileNotFoundException e) {
            // 文件没找到
            LogTool.e(TAG, e.getMessage());
        } finally {
            // 关闭输入流
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

    /**********************
     * 获取文件Bitmap
     *
     * @param setting 设置
     * @throws IOException
     ***********************/
    public synchronized static Bitmap readFileBitmap(String path,
                                                     LXImageReadOption setting) throws Exception{
        // 获取InputStream
        FileInputStream fin = null;
        try {
            File file = new File(path);
            if (path == null) {
                // 文件没找到
                throw new Exception("the path is a null");
            }
            if (file.isDirectory()) {
                // 文件没找到
                throw new Exception("the bitmapfile is a dictionary");
            }
            // 获取输入流
            fin = new FileInputStream(path);
            // 通过读取图片的大小设置获取option
            Options option = getOption(path, setting);
            // 真正的解析图片
            Bitmap bm = BitmapFactory.decodeStream(fin, null, option);
            // 如果有设置
            if (setting != null) {
                if (setting.isScaleFill()) {
                    bm = imageScale(bm, setting);
                } else {
                    bm = imageScaleMax(bm, setting);
                }
            }
            //进行转换
            if (setting != null && setting.getRadiusOption() != null) {
                bm = BitmapRadiusTool.toRoundCorner(bm, setting.getRadiusOption());
            }
            return bm;
        } catch (FileNotFoundException e) {
            // 文件没找到
            throw e;
        } finally {
            // 关闭输入流
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    LogTool.e(TAG, e.getMessage());
                }
            }
        }
    }

    /*********************
     * 直接读取文件的bitmap
     *
     * @param path 文件地址
     * @return 图片
     */
    public synchronized static Bitmap readFileBitmap(String path) throws Exception{
        FileInputStream fin = null;
        try {
            // 如果为空就返回空的
            if (path == null) {
                // 文件没找到
                throw new Exception("the path is a null");
            }
            fin = new FileInputStream(path);
            Bitmap bm = BitmapFactory.decodeStream(fin);
            return bm;
        } catch (FileNotFoundException e) {
            // 文件没找到
            throw e;
        } finally {
            // 关闭输入流
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    LogTool.e(TAG, e.getMessage());
                }
            }
        }
    }


    /*************
     * 获取图片的大小数据
     *
     * @param path 大小
     * @return
     */
    public synchronized static LXImageWH getImageSize(String path) {
        if (isFileExsitsAntNotDic(path)) {
            // 创建设置
            Options options = new Options();
            // 设置inJustDecodeBounds为true后，decodeFile并不分配空间，此时计算原始图片的长度和宽度
            options.inJustDecodeBounds = true;
            // 取得参数
            BitmapFactory.decodeFile(path, options);
            // 高度
            int imageHeight = options.outHeight;
            // 宽度
            int imageWidth = options.outWidth;

            return new LXImageWH(imageWidth, imageHeight);
        } else {
            return null;
        }
    }


    /**********************
     * 不同文件不同设置防止大图造成outof memmery
     *
     * @param path    输出图片宽度
     * @param setting 设置
     ***********************/
    public synchronized static Options getOption(String path,
                                                 LXImageReadOption setting) {
        // 创建设置
        Options options = new Options();
        // 设置inJustDecodeBounds为true后，decodeFile并不分配空间，此时计算原始图片的长度和宽度
        options.inJustDecodeBounds = true;
        // 取得参数
        BitmapFactory.decodeFile(path, options);
        // 高度
        int imageHeight = options.outHeight;
        // 宽度
        int imageWidth = options.outWidth;
        options.inDither = false;
        // 设置加载图片的颜色数为16bit，默认是RGB_8888，表示24bit颜色和透明通道，但一般用不上
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        // 设置图片的格式
        if (setting != null && setting.getInPreferredConfig() != null) {
            options.inPreferredConfig = setting.getInPreferredConfig();
        }
        // 默认为1
        options.inSampleSize = 1;
        // 根据要求设置缩放大小
        if (imageWidth != 0 && imageHeight != 0 && setting != null) {
            // 获取缩放的比例
            int sampleSize = computeSampleSize(options, -1,
                    setting.getMaxHeight() * setting.getMaxWidth());
            // 设置缩放的比例
            options.inSampleSize = sampleSize;
        }
        options.inJustDecodeBounds = false;
        // 最后把标志复原
        return options;
    }

    /**********************
     * 图片大小转换
     *
     * @param setting 设置
     * @param bitmap  输入位图
     ***********************/

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

        if (src_w / src_h > dst_w / dst_h)
        // 用宽来缩放
        {
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

    /**********************
     * 图片大小转换
     *
     * @param setting 设置
     * @param bitmap  输入位图
     ***********************/

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

    /**************
     * 计算出缩放大小
     *
     * @param options
     * @param minSideLength
     * @param maxNumOfPixels
     * @return
     */
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
