#import "FlutterimagecompressPlugin.h"

@implementation FlutterimagecompressPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"flutterimagecompress"
                                     binaryMessenger:[registrar messenger]];
    FlutterimagecompressPlugin* instance = [[FlutterimagecompressPlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if([@"compressImage" isEqualToString:call.method]){
        //地址
        NSString* path=(NSString*)call.arguments[@"path"];
        //保存地址
        NSString* savePath=(NSString*)call.arguments[@"savePath"];
        //质量
        NSInteger quality=[(NSString*)call.arguments[@"quality"] integerValue];
        //宽度
        NSInteger maxWidth=[(NSString*)call.arguments[@"maxWidth"] integerValue];
        //高度
        NSInteger maxHeight=[(NSString*)call.arguments[@"maxHeight"] integerValue];
        
        //加载图片
        UIImage* image=[[UIImage alloc]initWithContentsOfFile:path];
        //图像
        UIImage* scaledImage=[self OriginImage:image
                                scaleByMaxsize:CGSizeMake(maxWidth, maxHeight)];
        //压缩率
        double qualityDobule=quality*1.0/100;
        //压缩率
        if(qualityDobule>1.0){
            qualityDobule=1.0;
        }
        //数据
        NSData *imageData = UIImageJPEGRepresentation(scaledImage, qualityDobule);
        //数据
        UIImage* trueImage=[UIImage imageWithData:imageData];
        //数据
        long data=[[NSDate date] timeIntervalSince1970]*1000;
        
        //地址必须以/结尾
        if(![savePath hasSuffix:@"/"]){
            savePath=[NSString stringWithFormat:@"%@/",savePath];
        }
        //判断文件夹是否存在
        NSFileManager *fileManager = [NSFileManager defaultManager];
        BOOL isDir = FALSE;
        BOOL isDirExist = [fileManager fileExistsAtPath:savePath
                                            isDirectory:&isDir];
        //存在
        if(isDirExist){
            //但是不是文件夹
            if(!isDir){
                //使用我们自己的默认地址
                savePath=[self getDefaultPath];
                [fileManager createDirectoryAtPath:savePath
                       withIntermediateDirectories:YES
                                        attributes:nil
                                             error:nil];
                
            }
        }else{
            //不存在创建
            [fileManager createDirectoryAtPath:savePath
                   withIntermediateDirectories:YES
                                    attributes:nil
                                         error:nil];
        }
        
        //真实地址
        NSString* truePath=[NSString stringWithFormat:@"%@%ld%@",savePath,data,@".jpg"];
        
        //保存图片到指定位置
        BOOL flag=[self saveToDocument:trueImage
                          withFilePath:truePath];
        //标志
        if(flag){
            result(truePath);
        }else{
            result(nil);
        }
    } else if([@"getDefaultDirPath" isEqualToString:call.method]){
        //返回默认地址
        result([self getDefaultPath]);
    }else {
        //返回
        result(FlutterMethodNotImplemented);
    }
}


//改变图片
-(UIImage*)OriginImage:(UIImage *)image
        scaleByMaxsize:(CGSize)size{
    if(image.size.width<size.width&&image.size.height<size.height){
        return image;
    }
    //size 为CGSize类型，即你所需要的图片尺寸
    float xy=image.size.width/image.size.height;
    float xy2=size.width/size.height;
    if(xy>xy2){
        CGRect rect=CGRectMake(0, 0, size.width, size.width/xy);
        UIGraphicsBeginImageContextWithOptions(CGSizeMake(rect.size.width, rect.size.height), NO, 1.0);
        [image drawInRect:rect];
    }else{
        CGRect rect=CGRectMake(0, 0, size.height*xy, size.height);
        UIGraphicsBeginImageContextWithOptions(CGSizeMake(rect.size.width, rect.size.height), NO, 1.0);
        [image drawInRect:rect];
    }
    UIImage* scaledImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    //返回的就是已经改变的图片
    
    return scaledImage;
}

//将选取的图片保存到目录文件夹下
-(BOOL)saveToDocument:(UIImage *) image
         withFilePath:(NSString *) filePath{
    
    //返回
    if (image == nil) {
        return NO;
    }
    @try {
        NSData *imageData = nil;
        //获取文件扩展名
        NSString *extention = [filePath pathExtension];
        if ([extention isEqualToString:@"png"]) {
            //返回PNG格式的图片数据
            imageData = UIImagePNGRepresentation(image);
        }else{
            //返回JPG格式的图片数据，第二个参数为压缩质量：0:best 1:lost
            imageData = UIImageJPEGRepresentation(image, 0);
        }
        if (imageData == nil || [imageData length] <= 0) {
            return NO;
        }
        //将图片写入指定路径
        [imageData writeToFile:filePath atomically:YES];
        return  YES;
    }
    @catch (NSException *exception) {
        NSLog(@"保存图片失败");
    }
    return NO;
    
}


//获取缓存地址
-(NSString*)getDefaultPath{
    //获取Caches目录路径
    NSArray * cachesPaths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSString * cachesDirectory = [cachesPaths objectAtIndex:0];
    return [NSString stringWithFormat:@"%@/imageCache/",cachesDirectory];
}


@end
