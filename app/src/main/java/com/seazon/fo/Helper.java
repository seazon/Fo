package com.seazon.fo;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.seazon.utils.LogUtils;

@SuppressLint({"SimpleDateFormat", "DefaultLocale"})
public class Helper {

    public static String getSamePath(String a, String b) {
        String[] aa = a.split("/");
        String[] bb = b.split("/");

        int length = Math.min(aa.length, bb.length);
        if (length == 0) {
            return "/";
        }

        String cc = "";
        for (int i = 1; i < length; ++i) {
            if (aa[i].equals(bb[i])) {
                cc = cc + "/" + aa[i];
            } else if (aa[i].equals(bb[i]) == false) {
                break;
            }
        }

        if (cc.equals(""))
            return "/";

        return cc;
    }

    public static boolean isBlank(String a) {
        if (a == null || a.trim().equals("")) {
            return true;
        }
        return false;
    }

    public static String formatData(long time) {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(date);
    }

    public static String sdkLevelToString(int level) {
        switch (level) {
            case 1:
                return "1.0";
            case 2:
                return "1.1";
            case 3:
                return "1.5";
            case 4:
                return "1.6";
            case 5:
                return "2.0";
            case 6:
                return "2.0.1";
            case 7:
                return "2.1";
            case 8:
                return "2.2";
            case 9:
                return "2.3";
            case 10:
                return "2.3.3";
            case 11:
                return "3.0";
            case 12:
                return "3.1";
            case 13:
                return "3.2";
            case 14:
                return "4.0";
            case 15:
                return "4.0.3";
            case 16:
                return "4.1";
            case 17:
                return "4.2";
            case 18:
                return "4.3";
            case 19:
                return "4.4";
            case 20:
                return "4.4W";
            case 21:
                return "5.0";
            case 22:
                return "5.1";
            default:
                return "";
        }
    }

    @SuppressWarnings("deprecation")
    public static Drawable bitmap2Drawable(Bitmap bitmap) {
        BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
        return (Drawable) bitmapDrawable;
    }

    public static Bitmap drawable2Bitmap(Drawable d) {
        return ((BitmapDrawable) d).getBitmap();
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 5;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static String getFileNameWithoutExtension(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public static String getExtension(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1).toLowerCase();
            }
        }
        return filename;
    }

    public static String toMd5(byte[] bytes) {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(bytes);
            return toHexString(algorithm.digest(), "");
        } catch (NoSuchAlgorithmException e) {
            LogUtils.error(e);
            throw new RuntimeException(e);
        }
    }

    public static String toHexString(byte[] bytes, String separator) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(Integer.toHexString(0xFF & b)).append(separator);
        }
        return hexString.toString();
    }

    public static String getTypeByExtension(String fileName) {
        String format = fileName.substring(fileName.lastIndexOf(".") + 1)
                .toLowerCase();

        String mimetype = Core.mimeTypeMap.get(format);
        if (mimetype == null)
            mimetype = "*/*";

        return mimetype;
    }

    public static String getDes(long fileLength, long time) {
        return setFileSize(fileLength) + " / " + Helper.getFormatDateTime(new Date(time), "yyyy-MM-dd");
    }

    public static String getDes(long time) {
        return Helper.getFormatDateTime(new Date(time), "yyyy-MM-dd");
    }

    public static String setFileSize(long fileLength) {
        Double fileSize = null;
        DecimalFormat format = new DecimalFormat(".00");
        if (fileLength < 1024) {
            return fileLength + " bytes";
        } else {
            fileSize = Double.valueOf(String.valueOf(fileLength));
            if (fileSize < 1024 * 1024) {
                return format.format(fileSize / 1024) + " KB";
            } else if (fileSize < 1024 * 1024 * 1024) {
                return format.format(fileSize / 1024 / 1024) + " MB";
            } else {
                return format.format(fileSize / 1024 / 1024 / 1024) + " GB";
            }
        }
    }

    public static int setFileIcon(String fileName, boolean isLight) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            if (isLight)
                return R.drawable.format_file;
            else
                return R.drawable.format_file_dark;
        }
        String format = fileName.substring(index + 1);

        format = format.toLowerCase();
        if (format.equals("apk"))
            if (isLight)
                return R.drawable.format_file;
            else
                return R.drawable.format_file_dark;
        else if (format.equals("xls") || format.equals("xlsx"))
            if (isLight)
                return R.drawable.format_text;
            else
                return R.drawable.format_text_dark;
        else if (format.equals("htm") || format.equals("html"))
            if (isLight)
                return R.drawable.format_text;
            else
                return R.drawable.format_text_dark;
        else if (format.equals("mp4") || format.equals("3gp")
                || format.equals("avi") || format.equals("rm")
                || format.equals("rmvb") || format.equals("mpg")
                || format.equals("mov") || format.equals("mkv")
                || format.equals("flv") || format.equals("mpeg")
                || format.equals("m2ts") || format.equals("ts")
                || format.equals("wmv"))
            if (isLight)
                return R.drawable.format_video;
            else
                return R.drawable.format_video_dark;
        else if (format.equals("mp3") || format.equals("wma")
                || format.equals("aac") || format.equals("wav")
                || format.equals("flac") || format.equals("ape")
                || format.equals("ogg") || format.equals("mka")
                || format.equals("m4a") || format.equals("amr")
                || format.equals("midi"))
            if (isLight)
                return R.drawable.format_audio;
            else
                return R.drawable.format_audio_dark;
        else if (format.equals("pdf"))
            if (isLight)
                return R.drawable.format_text;
            else
                return R.drawable.format_text_dark;
        else if (format.equals("jpg") || format.equals("jpeg")
                || format.equals("gif") || format.equals("png")
                || format.equals("bmp"))
            if (isLight)
                return R.drawable.format_image;
            else
                return R.drawable.format_image_dark;
        else if (format.equals("ppt") || format.equals("pptx"))
            if (isLight)
                return R.drawable.format_text;
            else
                return R.drawable.format_text_dark;
        else if (format.equals("txt"))
            if (isLight)
                return R.drawable.format_text;
            else
                return R.drawable.format_text_dark;
        else if (format.equals("doc") || format.equals("docx"))
            if (isLight)
                return R.drawable.format_text;
            else
                return R.drawable.format_text_dark;
        else if (format.equals("zip") || format.equals("rar")
                || format.equals("7z") || format.equals("gz")
                || format.equals("gzip"))
            if (isLight)
                return R.drawable.format_zip;
            else
                return R.drawable.format_zip_dark;
        else if (format.equals("epub") || format.equals("umd"))
            if (isLight)
                return R.drawable.format_book;
            else
                return R.drawable.format_book_dark;
        else if (isLight)
            return R.drawable.format_file;
        else
            return R.drawable.format_file_dark;
    }

    public static String getDescAdd(File file, Context context) {
        int index = file.getName().lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        String format = file.getName().substring(index + 1);

        format = format.toLowerCase();
        if (format.equals("apk")) {
            PackageInfo packageInfo = getPackageInfo(file, context);
            if (packageInfo == null) {
                return "";
            }
            return " / " + packageInfo.versionName;
        } else {
            return "";
        }
    }

    public static PackageInfo getPackageInfo(File file, Context context) {
        String type = Helper.getTypeByExtension(file.getName());
        if (type != null && type.equals("application/vnd.android.package-archive")) {
            return context.getPackageManager().getPackageArchiveInfo(file.getPath(), PackageManager.GET_ACTIVITIES);
        }

        return null;
    }

    public static String rwString(File file) {
        String a = "";
        if (file.isDirectory()) {
            a = "d";
        } else {
            a = "-";
        }
        if (file.canRead()) {
            a += "r";
        } else {
            a += "-";
        }
        if (file.canWrite()) {
            a += "w";
        } else {
            a += "-";
        }
        return a;
    }

    public static String getFormatDateTime(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static boolean deleteFile(File file) {
        boolean ret = true;
        if (file.isDirectory()) {
            for (File son : file.listFiles()) {
                ret = deleteFile(son);
                if (ret == false) {
                    return false;
                }
            }
        }

        return file.delete();
    }

    public static File whenExist(String path) {
        int index = 0;
        File o = new File(path);
        File d = new File(path);
        do {
            if (index == 0) {

            } else {
//				d = new File(path+"("+index+")");
                d = newFileWithIndex(o, index);
            }
            index++;
        } while (d.exists());

        if (!d.getParentFile().exists())
            d.getParentFile().mkdirs();

        return d;
    }

    public static File newFileWithIndex(File file, int index) {
        if (file.isDirectory()) {
            return new File(file.getPath() + "(" + index + ")");
        } else {
            return new File(file.getParent() + "/" + getFileNameWithoutExtension(file.getName()) + "(" + index + ")." + getExtension(file.getName()));
        }
    }

    public static boolean newFile(File directory, String name,
                                  boolean isDirectory) {
        try {
            File newFile = new File(directory.getPath() + "/" + name);
            boolean ret;
            if (isDirectory) {
                ret = newFile.mkdir();
            } else {
                ret = newFile.createNewFile();
            }
            if (!ret) {
                // TODO WAIT rename and new
                return false;
            }
            return true;
        } catch (IOException e) {
            LogUtils.error(e);
            return false;
        }
    }

    public static boolean rename(File file, String newname) {
        try {
            boolean ret;
            /**
             * Rename failed but show rename success
             *
             * get issue from this user:
             * bigggbetty36@googlemail.com  Moto G 5.0.2
             *
             * Rename, copy, cut will not show in other app, just show in Fo.
             * arunmuthananikal@gmail.com   Xperia M dual jb4.3
             */
            ret = file.renameTo(new File(file.getParent() + "/" + newname));
            if (!ret) {
                // TODO WAIT rename and new
                LogUtils.warn("need rename");
                return false;
            }
            return true;
        } catch (SecurityException e) {
            LogUtils.error(e);
            return false;
        }
    }

    public static String md5(String data) {
        String result = "";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(data.getBytes());
            byte bytes[] = md5.digest();
            for (int i = 0; i < bytes.length; i++) {
                result += Integer.toHexString((0x000000ff & bytes[i]) | 0xffffff00).substring(6);
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return result.substring(8, 24);
    }
}
