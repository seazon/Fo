package com.seazon.fo;

import android.content.Context;
import android.util.DisplayMetrics;

/** 
 * 计算公式 pixels = dips * (density / 160) 
 *  
 * @version 1.0.1 2010-12-11 
 *  
 * @author 
 */  
public class DensityUtil {  
      
    // 当前屏幕的densityDpi  
    private static float dmDensityDpi = 0.0f;  
    private static DisplayMetrics dm;  
    private static float scale = 0.0f;  
  
    /** 
     *  
     * 根据构造函数获得当前手机的屏幕系数 
     *  
     * */  
    public DensityUtil(Context context) {  
        // 获取当前屏幕  
        dm = new DisplayMetrics();  
        dm = context.getApplicationContext().getResources().getDisplayMetrics();  
        // 设置DensityDpi  
        setDmDensityDpi(dm.densityDpi);  
        // 密度因子  
        scale = getDmDensityDpi() / 160;  
    }  
  
    /** 
     * 当前屏幕的density因子 
     *  
     * @param DmDensity 
     * @retrun DmDensity Getter 
     * */  
    public static float getDmDensityDpi() {  
        return dmDensityDpi;  
    }  
  
    /** 
     * 当前屏幕的density因子 
     *  
     * @param DmDensity 
     * @retrun DmDensity Setter 
     * */  
    public static void setDmDensityDpi(float dmDensityDpi) {  
        DensityUtil.dmDensityDpi = dmDensityDpi;  
    }  
  
    /** 
     * 密度转换像素 
     * */  
    public int dip2px(float dipValue) {  
  
        return (int) (dipValue * scale + 0.5f);  
  
    }  
  
    /** 
     * 像素转换密度 
     * */  
    public int px2dip(float pxValue) {  
        return (int) (pxValue / scale + 0.5f);  
    }  
  
    @Override  
    public String toString() {  
        return " dmDensityDpi:" + dmDensityDpi;  
    }  
}  