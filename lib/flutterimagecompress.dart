import 'dart:async';

import 'package:flutter/services.dart';

//调用原生压缩图像
class Flutterimagecompress {
  //图像
  static const MethodChannel _channel =
      const MethodChannel('flutterimagecompress');

  //调用系统压缩图片
  static Future<String> compressImage(String path,
      String savePath,
      int quality,
      int maxWidth,
      int maxHeight) async {
    //剔除file部分
    String nowerPath = path;
    //地址
    if (nowerPath.startsWith("file://")) {
      nowerPath = nowerPath.replaceAll("file://", "");
    }
    //调用原生压缩图像
    final String ret = await _channel.invokeMethod('compressImage', {
      "path": nowerPath,
      "savePath": savePath,
      "quality": quality.toString(),
      "maxWidth": maxWidth.toString(),
      "maxHeight": maxHeight.toString()
    });
    return ret;
  }

  //调用原生压缩图像
  static Future<String>  getDefaultDirPath() async {
    final String ret = await _channel.invokeMethod('getDefaultDirPath', {});
    return ret;
  }

}
