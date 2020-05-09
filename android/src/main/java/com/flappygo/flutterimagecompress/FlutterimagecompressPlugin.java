package com.flappygo.flutterimagecompress;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.flappygo.flutterimagecompress.tools.ImageReadTool;
import com.flappygo.flutterimagecompress.tools.LXImageReadOption;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterimagecompressPlugin
 */
public class FlutterimagecompressPlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    //上下文
    private Context context;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutterimagecompress");
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
    }

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutterimagecompress");
        //创建插件
        FlutterimagecompressPlugin plugin = new FlutterimagecompressPlugin();
        //赋值上下文
        plugin.context = registrar.activity().getApplicationContext();
        //设置handler
        channel.setMethodCallHandler(plugin);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull final Result result) {
        //压缩图片
        if (call.method.equals("compressImage")) {
            //系统图片路径
            final String path = call.argument("path");
            //压缩后保存的路径
            final String savePath = call.argument("savePath");
            //数据
            final String quality = call.argument("quality");
            //宽度
            final String maxWidth = call.argument("maxWidth");
            //高度
            final String maxHeight = call.argument("maxHeight");
            //handler
            final Handler handler = new Handler() {
                public void handleMessage(Message message) {
                    if (message.what == 1) {
                        result.success(message.obj);
                    } else {
                        result.success(null);
                    }
                }
            };
            new Thread() {
                public void run() {
                    //然后保存来着
                    try {
                        //读取图像
                        Bitmap bitmap = ImageReadTool.readFileBitmap(path,
                                new LXImageReadOption(
                                        Integer.parseInt(maxWidth),
                                        Integer.parseInt(maxHeight),
                                        false));

                        //保存的地址
                        String truePath = savePath;
                        //空的，默认
                        if (truePath == null || truePath.equals("")) {
                            truePath = getCompressDefaultPath(context);
                        }
                        //保存的地址没有斜杠，补足斜杠
                        if (!truePath.endsWith("/")) {
                            truePath = truePath + "/";
                        }
                        //创建文件夹
                        File savePathFile = new File(truePath);
                        //如果不存在
                        if (!savePathFile.exists()) {
                            savePathFile.mkdirs();
                        }
                        //如果不是真实的地址
                        if (!savePathFile.isDirectory()) {
                            truePath = getCompressDefaultPath(context);
                            savePathFile = new File(truePath);
                            if (!savePathFile.exists()) {
                                savePathFile.mkdirs();
                            }
                        }
                        //保存
                        String fileSaveName = UUID.randomUUID().toString().replaceAll("-", "") + ".jpg";
                        //图像名称
                        String retPath = truePath + fileSaveName;
                        //返回地址
                        File file = new File(retPath);
                        //读取
                        FileOutputStream out = new FileOutputStream(file);
                        //压缩
                        bitmap.compress(Bitmap.CompressFormat.JPEG, Integer.parseInt(quality), out);
                        //刷入
                        out.flush();
                        //关闭
                        out.close();
                        //成功
                        Message message = handler.obtainMessage(1, retPath);
                        //发送消息
                        handler.sendMessageDelayed(message, 200);
                    } catch (Exception e) {
                        //失败
                        Message message = handler.obtainMessage(0, null);
                        handler.sendMessage(message);
                    }
                }
            }.start();
        }
        //返回默认压缩地址
        else if (call.method.equals("getCompressDefaultPath")) {
            //获取压缩缓存地址
            result.success(getCompressDefaultPath(context));
        }
        //返回默认的缓存地址
        else if (call.method.equals("getCacheDefaultPath")) {
            //默认缓存地址
            result.success(getCacheDefaultPath(context));
        } else {
            result.notImplemented();
        }
    }

    public static String getCompressDefaultPath(Context context) {
        String compressPath = getCacheDefaultPath(context) + "imagecache/";
        return compressPath;
    }

    /*********
     * 获取默认的保存图片地址
     *
     * @param context 上下文
     * @return
     */
    public static String getCacheDefaultPath(Context context) {
        String cachePath = null;
        try {
            if (context.getExternalCacheDir() != null) {
                cachePath = context.getExternalCacheDir().getPath() + "/";
            } else if (context.getCacheDir() != null) {
                cachePath = context.getCacheDir().getPath() + "/";
            }
        } catch (Exception e) {
            cachePath = "/";
        }
        return cachePath;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}
