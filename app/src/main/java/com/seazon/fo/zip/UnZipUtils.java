package com.seazon.fo.zip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnZipUtils {
	
	public static final String EXT = ".zip";  
    private static final String BASE_DIR = "";  
    private static final String PATH = File.separator;  
    private static final int BUFFER = 524288;  
  
    /** 
     * 文件 解压缩 
     *  
     * @param srcPath 
     *            源文件路径 
     *  
     * @throws Exception 
     */  
    public static void decompress(String srcPath) throws Exception {  
        File srcFile = new File(srcPath);  
  
        decompress(srcFile);  
    }  
  
    /** 
     * 解压缩 
     *  
     * @param srcFile 
     * @throws Exception 
     */  
    public static void decompress(File srcFile) throws Exception {  
        String basePath = srcFile.getParent();  
        decompress(srcFile, basePath);  
    }  
  
    /** 
     * 解压缩 
     *  
     * @param srcFile 
     * @param destFile 
     * @throws Exception 
     */  
    public static void decompress(File srcFile, File destFile) throws Exception {  
  
        CheckedInputStream cis = new CheckedInputStream(new FileInputStream(  
                srcFile), new CRC32());  
  
        ZipInputStream zis = new ZipInputStream(cis);  
  
        decompress(destFile, zis);  
  
        zis.close();  
  
    }  
  
    /** 
     * 解压缩 
     *  
     * @param srcFile 
     * @param destPath 
     * @throws Exception 
     */  
    public static void decompress(File srcFile, String destPath)  
            throws Exception {  
        decompress(srcFile, new File(destPath));  
  
    }  
  
    /** 
     * 文件 解压缩 
     *  
     * @param srcPath 
     *            源文件路径 
     * @param destPath 
     *            目标文件路径 
     * @throws Exception 
     */  
    public static void decompress(String srcPath, String destPath)  
            throws Exception {  
  
        File srcFile = new File(srcPath);  
        decompress(srcFile, destPath);  
    }  
  
    /** 
     * 文件 解压缩 
     *  
     * @param destFile 
     *            目标文件 
     * @param zis 
     *            ZipInputStream 
     * @throws Exception 
     */  
    private static void decompress(File destFile, ZipInputStream zis)  
            throws Exception {  
  
        ZipEntry entry = null;  
        while ((entry = zis.getNextEntry()) != null) {  
  
            // 文件  
            String dir = destFile.getPath() + File.separator + entry.getName();  
  
            File dirFile = new File(dir);  
  
            // 文件检查  
            fileProber(dirFile);  
  
            if (entry.isDirectory()) {  
                dirFile.mkdirs();  
            } else {  
                decompressFile(dirFile, zis);  
            }  
  
            zis.closeEntry();  
        }  
    }  
  
    /** 
     * 文件探针 
     *  
     *  
     * 当父目录不存在时，创建目录！ 
     *  
     *  
     * @param dirFile 
     */  
    private static void fileProber(File dirFile) {  
  
        File parentFile = dirFile.getParentFile();  
        if (!parentFile.exists()) {  
  
            // 递归寻找上级目录  
            fileProber(parentFile);  
  
            parentFile.mkdir();  
        }  
  
    }  
  
    /** 
     * 文件解压缩 
     *  
     * @param destFile 
     *            目标文件 
     * @param zis 
     *            ZipInputStream 
     * @throws Exception 
     */  
    private static void decompressFile(File destFile, ZipInputStream zis)  
            throws Exception {  
  
        BufferedOutputStream bos = new BufferedOutputStream(  
                new FileOutputStream(destFile));  
  
        int count;  
        byte data[] = new byte[BUFFER];  
        while ((count = zis.read(data, 0, BUFFER)) != -1) {  
            bos.write(data, 0, count);  
        }  
  
        bos.close();  
    }  
  
}  
