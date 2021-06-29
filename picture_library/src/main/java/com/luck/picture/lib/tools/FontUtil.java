package com.luck.picture.lib.tools;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.TextView;

import androidx.annotation.StringDef;
import androidx.core.content.res.ResourcesCompat;

import com.luck.picture.lib.R;

import java.io.File;
import java.util.HashMap;

/**
 * @author hufeiyang
 * @data 2021/6/24
 * @Description:
 */
public class FontUtil {

    /**
     * 缓存TypeFace字体Map
     */
    public static HashMap<String, Typeface> cacheTypeFace = new HashMap<>();
    public static HashMap<Integer, Typeface> europeCacheTypeFace = new HashMap<>();

    /**
     * 给textView设置NotoSansUI-Bold.ttf字体
     * @param textView textView
     * @param activity
     */
    public static void setNotosansuiBold(TextView textView, Activity activity) {
        setFont(textView, NOTOSANSUI_BOLD, activity);
    }

    /**
     * 给textView设置NotoSansUI-Medium.ttf字体
     * @param textView textView
     * @param activity
     */
    public static void setNotosansuiMedium(TextView textView, Activity activity) {
        setFont(textView, NOTOSANSUI_MEDIUM, activity);
    }

    /**
     * 给textView设置NotoSansUI-Regular.ttf字体
     * @param textView textView
     * @param activity
     */
    public static void setNotosansuiRegular(TextView textView, Activity activity) {
        setFont(textView, NOTOSANSUI_REGULAR, activity);
    }

    /**
     * 给textView设置字体
     *  @param textView textView
     * @param font 字体类型
     * @param activity
     */
    private static void setFont(TextView textView, String font, Activity activity) {
        if(textView == null)return;
        try {
            Typeface typeface = getTypeface(font,activity);
            textView.setTypeface(typeface);
        } catch (Throwable e) {
            e.printStackTrace();
            textView.setTypeface(Typeface.DEFAULT);
        }
    }

    /**
     * 给textView设置字体
     *  @param textView textView
     * @param fontResName 字体资源名称
     * @param activity
     */
    private static void setFont(TextView textView, int fontResName, Activity activity) {
        if(textView == null)return;
        try {
            Typeface typeface = getTypeface(fontResName,activity);
            textView.setTypeface(typeface);
        } catch (Throwable e) {
            e.printStackTrace();
            textView.setTypeface(Typeface.DEFAULT);
        }

    }

    /**
     * 获取自定义字体库
     *
     * @param fontName 带后缀，比如Roboto-Medium.ttf
     * @return 如果没有对应的字体库则返回null
     */
    public static Typeface getTypeface(@FontName String fontName, Activity activity) {
        Typeface cacheTypeface = cacheTypeFace.get(fontName);
        if (cacheTypeface != null) {
            return cacheTypeface;
        }
        Typeface typeface;
        try {
            //从assets/font目录下取字体
            typeface = Typeface.createFromAsset(activity.getApplication().getAssets(), "akufont" + File.separator + fontName);
            cacheTypeFace.put(fontName, typeface);
        } catch (Exception e) {
            e.printStackTrace();
            //若从从assets/font目录下取字体为null或者报错 则给一个默认字体
            typeface = Typeface.DEFAULT;
        }
        return typeface;
    }

    /**
     * 获取自定义字体库
     *
     * @param fontResName 字体资源名称
     * @return 如果没有对应的字体库则返回null
     */
    public static Typeface getTypeface(int fontResName, Activity activity) {
        Typeface cacheTypeface = europeCacheTypeFace.get(fontResName);
        if (cacheTypeface != null) {
            return cacheTypeface;
        }
        Typeface typeface;
        try {
            //从res的font目录下去拿
            typeface = ResourcesCompat.getFont(activity.getApplication(), fontResName);
            europeCacheTypeFace.put(fontResName, typeface);
        } catch (Exception e) {
            e.printStackTrace();
            //若获取的字体为null或者报错 则给一个默认字体
            typeface = Typeface.DEFAULT;
        }
        return typeface;
    }

    /**
     * 设置下划线
     *
     * @param textView textView
     */
    @SuppressWarnings("unused")
    public static void setUnderLine(TextView textView) {
        textView.getPaint().setAntiAlias(true);
        textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
    }

    /**
     * 设置删除线
     * @param textView textView
     */
    @SuppressWarnings("unused")
    public static void setThroughLine(TextView textView) {
        textView.getPaint().setAntiAlias(true);
        textView.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
    }


    @StringDef({DIN_BOLD, ROBOTO_BOLD, ROBOTO_LIGHT, ROBOTO_MEDIUM})
    public @interface FontName {}
    public final static String DIN_BOLD = "din_bold.otf";
    public final static String ROBOTO_BOLD = "Roboto-Bold.ttf";
    public final static String ROBOTO_LIGHT = "Roboto-Light.ttf";
    public final static String ROBOTO_MEDIUM = "Roboto-Medium.ttf";


//    public final static int GILROY_BLACK_6 = R.font.gilroy_black_6;
    public final static int NOTOSANSUI_BOLD = R.font.noto_sans_ui_bold;
//    public final static int NOTOSANSUI_EXTRABOLD = R.font.noto_sans_ui_extrabold;
    public final static int NOTOSANSUI_MEDIUM = R.font.noto_sans_ui_medium;
    public final static int NOTOSANSUI_REGULAR = R.font.noto_sans_ui_regular;
}
