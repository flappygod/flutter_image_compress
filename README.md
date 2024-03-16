# flutter_img_compress

Compress your image to a expected size.


How to use?

#file path
String? filePath = await FlutterImageCompress.compressImage(
path,
maxSize: size,
maxWidth: maxWidth,
maxHeight: maxHeight,
quality: quality,
);

or just

FlutterImageCompress.compressImage(path,quality,maxWidth,maxHeight);

#which will save to path

FlutterImageCompress.getCompressDefaultPath();


