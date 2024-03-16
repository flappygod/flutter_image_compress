package com.example.flutter_img_compress.tools;

import android.util.Log;

/**********
 * @author flappygo
 */
public class LogTool {
	
	//log show
	public static boolean  showErrorLog=false;
	
	
	public static void d(String tag, String msg){
		if(!showErrorLog){
			return ;
		}
		Log.d(tag, msg);
	}
	
	public static void d(String tag, String msg, Throwable tr){
		if(!showErrorLog){
			return ;
		}
		Log.d(tag, msg, tr);
	}
	
	public static void e(String tag, String msg){
		if(!showErrorLog){
			return ;
		}
		Log.e(tag, msg);
	}
	
	public static void e(String tag, String msg, Throwable tr){
		if(!showErrorLog){
			return ;
		}
		Log.e(tag, msg, tr);
	}
	
	public static void i(String tag, String msg){
		if(!showErrorLog){
			return ;
		}
		Log.i(tag, msg);
	}
	
	public static void i(String tag, String msg, Throwable tr){
		if(!showErrorLog){
			return ;
		}
		Log.i(tag, msg, tr);
	}
	
	public static void w(String tag, String msg){
		if(!showErrorLog){
			return ;
		}
		Log.w(tag, msg);
	}
	
	public static void w(String tag, String msg, Throwable tr){
		if(!showErrorLog){
			return ;
		}
		Log.w(tag, msg, tr);
	}
	
	public static void w(String tag,Throwable tr){
		if(!showErrorLog){
			return ;
		}
		Log.w(tag, tr);
	}


}
