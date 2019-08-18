/**
 * @author  Hootan Parsa
 * @email 	w.parsa@gmail.com
 * 
 * Copyright (C) 2012 Hootan Parsa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.seazon.fo.root;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.pm.ApplicationInfo;
import android.os.Environment;
import android.text.TextUtils;

import com.seazon.utils.LogUtils;

public class ShellHelper {
	
	static ShellHelper instance;
	static Process process;
	static BufferedInputStream stderr;
	static BufferedOutputStream stdin;
	static BufferedInputStream stdout;
	boolean busybox;
	AtomicBoolean rooted;
	
	ShellHelper() {
		
		if (createProcess(new String[] { "/system/bin/sh" }))
			busybox = exec("busybox");
	}
	
	public static ShellHelper get(boolean needSU) {
		if (instance == null)
			instance = new ShellHelper();
		
		if (needSU && instance.su())
			instance.remount(true);
		
		return instance;
	}
	
	public static void destroy() {
		if (instance != null) {
			instance.closeProcess();
			instance = null;
		}
	}
	
	final public boolean busyboxExists() {
		return busybox;
	}
	
	boolean su() {
		synchronized (this) {	
			if (rooted != null)
				return rooted.get();
		
			rooted = new AtomicBoolean(false);
		
			if (createProcess(new String[] { "su", "-c", "/system/bin/sh" })) {
			
				StringBuilder sb = new StringBuilder();
				if (exec("id", sb) && sb.toString().contains("uid=0")) {
				
					rooted.set(true);
					return true;
				}
				else {
					rooted = null;
				}
			}
		
			createProcess(new String[] { "/system/bin/sh" });
			return false;
		}
	}
	
	final public boolean isRooted() {
		return rooted != null ? rooted.get() : false;
	}
	
	boolean createProcess(String[] progArray) {
		closeProcess();
		
		try {
			process = Runtime.getRuntime().exec(progArray);
			stdout = new BufferedInputStream(process.getInputStream());
			stderr = new BufferedInputStream(process.getErrorStream());
			stdin = new BufferedOutputStream(process.getOutputStream());
			return true;
		} catch (IOException e) {
            LogUtils.error(e);
		}
		return false;
	}
	
	void closeProcess() {
		try {
			if (process != null)
				process.destroy();
			if (stdout != null)
				stdout.close();
			if (stderr != null)
				stderr.close();
			if (stdin != null)
				stdin.close();
		} catch (Exception e) {
            LogUtils.error(e);
		}
	}
	
	public boolean exec(String command) {
		synchronized (this) {
			return exec(command, null);
		}
	}
	
	public boolean exec(String cmd, StringBuilder sb) {
		synchronized (this) {
			boolean result = false;
		
			try {
				if (stdout.available() > 0)
					read(stdout);
			
				if (stderr.available() > 0)
					read(stderr);

				stdin.write(("if ( ! " + cmd + " ) then echo ooops; fi; echo -n $\n").getBytes());
				stdin.flush();
			
				String output = read(stdout);
				result = !output.endsWith("ooops");
				if (!result)
					output = read(stderr);
	
				if (sb != null)
					sb.append(output);
			} 
			catch (Exception e) {
                LogUtils.error(e);
				if (sb != null)
					sb.append(e.getMessage());
			}

			return result;
		}
	}

	final static byte[] buffer = new byte[1024];
	
	String read(InputStream std) throws Exception {
		synchronized (this) {
			long start = System.currentTimeMillis();
			while(std.available() <= 0 && System.currentTimeMillis() - start < 2000L);
			
			if (std.available() <= 0)
				return "";
			
			if (std == stdout) {
				StringBuffer output = new StringBuffer();
				String str = "";
				
				do {
					str = read(std, buffer);
					output.append(str);
				} while (!str.endsWith("$"));
					
				return output.substring(0, output.lastIndexOf("$")).trim();
			}
			else {
				return read(std, new byte[50]).trim();
			}
		}
	}
	
	String read(InputStream std, byte[] buffer) throws Exception {
		StringBuffer output = new StringBuffer();
		int avail = 0;
		while ((avail = std.available()) > 0) {
            int len = std.read(buffer, 0, (avail > buffer.length - 1 ? buffer.length : avail));
            output.append(new String(buffer, 0, len, "UTF-8"));
		}
		return output.toString();
	}
  	
  	public boolean remount(boolean rw) {
  		try {
			int i = 0;
			Scanner scanner = new Scanner(new File("/proc/mounts"));
	    	while (scanner.hasNext()) {
  				String[] sp = scanner.nextLine().split("\\s+");
  				
  				if (sp.length > 2 && 
					(sp[1].equals("/system") || sp[1].equals("/")) &&
					rw != sp[3].replace("(", "").startsWith("rw")) {

  					if (!exec("mount " + (rw ? "-rw" : "-r") + " -o remount " + sp[0] + " '" + sp[1] + "'"))
  						return false;
  					else if (i++ > 2)
  	  					break;
  				}
	    	}
  		}
		catch (IOException e) {
            LogUtils.error(e);
			return false;
		}
  		return true;
  	}
  	
  	/**********************************************************/

  	static final Pattern MD5_REGEX = Pattern.compile("([0-9a-f]{32}) [ \\*](.+)");	// "([a-fA-F\d]{32})"
  	
  	public final String getMD5Checksum(String path) {
  		StringBuilder sb = new StringBuilder();
  		if (exec("md5sum '" + path + "'", sb)) {
  			
  			Matcher matcher = MD5_REGEX.matcher(sb.toString());
  			if (matcher.matches())
	  	    	return matcher.group(1);
  		}
		return sb.toString();
  	}
  	
	public FileInfo createDirectory(File file) {
		
		String dest = file.getAbsolutePath();
		
		if (exec("mkdir '" + dest + "'")) {
			/*setPermission(dest, f.flags);
            try {
	    		new File(dest).setLastModified(f.modified);
	    	} catch (Exception e) {
				LogUtils.error(e);
			}*/
            
			return getFileInfo(file);
		}
		
		return null;
	}

	public FileInfo createFile(File file) {
		if (exec("touch '" + file.getAbsolutePath() + "'"))
			return getFileInfo(file);

		return null;
	}

	public String createNewFileName(String path) {
		int i = 0;
		while (fileExists(new File(path))) {
			path = Utils.getNewPathName(path, i++);
		}
		return path;
	}
	
	public boolean saveText(String path, String text) {
		return exec("echo '" + text + "' > '" + path + "'");
	}
	
	public String readText(String path) {
		StringBuilder content = new StringBuilder();
		exec("cat '" + path + "'", content);
		return content.toString();
	}
	
	public boolean setOwnerGroup(String path, String owner, String group) {
		return !TextUtils.isEmpty(path) && !TextUtils.isEmpty(owner) && !TextUtils.isEmpty(group) ? 
				exec("chown " + owner + "." + group + " '" + path + "'")
				: false;
	}
  	
  	public boolean setPermission(String path, String perm) {
  		
  		if (perm.length() == 10)
  			perm = perm.substring(1);
  		
  		if (TextUtils.isEmpty(perm))
  			perm = "rwxrwxrwx";

  		String permNum = "";
  		
  		if (perm.length() == 9)
	        permNum = getOctal(perm) + "";
  		else if (perm.matches("[0-7]{3,4}"))
  			permNum = perm;
  		else
  			return false;

		return exec("chmod " + permNum + " '" + path + "'");
  	}
	
	static public final int getOctal(String perm) {
		
  		if (perm.length() == 10)
  			perm = perm.substring(1);
  		
  		char[] chars = perm.toCharArray();
  		
		int octal = 0;
		if (chars[0] != '-')
			octal += 400;
		if (chars[1] != '-')
			octal += 200;
		if (chars[2] != '-' && chars[2] != 'S')
			octal += 100;
		if (chars[3] != '-')
			octal += 40;
		if (chars[4] != '-')
			octal += 20;
		if (chars[5] != '-' && chars[5] != 'S')
			octal += 10;
		if (chars[6] != '-')
			octal += 4;
		if (chars[7] != '-')
			octal += 2;
		if (chars[8] != '-' && chars[8] != 'T')
			octal += 1;
		if (chars[2] == 's' || chars[2] == 'S')
			octal += 4000;
		if (chars[5] == 's' || chars[5] == 'S')
			octal += 2000;
		if (chars[8] == 't' || chars[8] == 'T')
			octal += 1000;
	      
		return Integer.valueOf(octal);
	}

  	public List<FileInfo> listFiles(File file, FilenameFilter filter, boolean showHidden) {
		List<FileInfo> files = new ArrayList<FileInfo>();

  		if (!file.canRead()) { 
  			if (!su())
  				return files;
  	  		
  			remount(true);
  		}
  		
		String[] details = getFileDetails(file);
  		for (String det : details) {

  			FileInfo fi = getFileInfo(det, file.getAbsolutePath());
	        if (fi != null && Utils.canShowFile(filter, showHidden, fi.getPath(), fi.isHidden()))
	        	files.add(fi);
  		}
  		
  		return files;
	}

	final char D = 'l';
	final String[] getFileDetails(File file) {
		
		String[] details = new String[0];
		String dir = file.getAbsolutePath();
		
		String cmd = "ls -a -l";
		//String cmd = "for f in $(ls -a -1 '" +  + "'); do\n if ( -d $f ) then c=$(ls -1 $f | wc -l); else c=0; fi; echo $(ls -ld $f) $c; done";
		
		StringBuilder sb = new StringBuilder();
		if (exec(cmd + " '" + dir + "'", sb)) {
		
			details = sb.toString().split("\n");
			
			if (details.length == 1 && details[0].charAt(0) == D) {
				sb.delete(0, sb.length());

				int index = details[0].indexOf("->");
				if (index >= 0)
					dir = details[0].substring(index + 2).trim();
				
				if (exec(cmd + " '" + dir + "'", sb))
					details = sb.toString().split("\n");
			}
		}
		
		return details;
	}

	public FileInfo getFileInfo(File file) {
		FileInfo fi = null;
		
		StringBuilder det = new StringBuilder();
  		if (exec("ls -ld '" + file.getAbsolutePath() + "'", det))
  			fi = getFileInfo(det.toString(), file.getParent());
		
		/*String dir = file.getParent();
		StringBuilder sb = new StringBuilder();
		if (exec("ls -a -l '" +  + "'", dir), sb)) {
			
			String[] lines = sb.toString().split("\n");
			for(String det : lines) {
				if (det.endsWith( file.getName() )) {
					fi = getFileInfo(det.toString(), dir);
					break;
				}
			}
		}*/
  		
  		return fi;
	}
	
	/*
	-  >> Regular file.
	-b >> is block special.
	-c >> is character special.
	-d >> is a directory.
	-e >> True if file exists.
	-f >> is a regular file.
	-g >> is set-group-id.
	-k >> True if file has its 'sticky' bit set.
	-L >> is a symbolic link.
	-p >> is a named pipe.
	-r >> is readable.
	-s >> has a size greater than zero.
	-S >> is a socket.
	-t $fd >> fd is opened on a terminal.
	-u >> its set-user-id bit is set.
	-w >> is writable.
	-x >> is executable.
	-O >> is owned by the effective user id.
	-G >> is owned by the effective group id.
	*/
	
	/*
		text    data     bss     dec     hex filename
		 341349   10772      16  352137   55f89 file.ext
		341349+10772+16 does give 352137. 
		But doing an ls -l file.ext gives:
		-rwxrwx---    1 xxxxx  xxxxx    505977 Sep 22 22:46 file.ext
	*/

    FileInfo getFileInfo(String details, String path) {
        // get directory files count from last segment
        /*String c = detLine.substring(detLine.lastIndexOf(" ") + 1);
        int count = TextUtils.isDigitsOnly(c) ? Integer.parseInt(c) : 0;
        detLine = detLine.substring(0, detLine.lastIndexOf(" "));*/

    	if (details.endsWith(".") || details.endsWith(".."))
			return null;
		
        String[] segments = details.split("\\s+");
        if (segments.length < 6)
        	return null;

  	  	boolean nineSegments = !segments[4].contains(":") && !segments[5].contains(":");

        FileData data = extractData(details, nineSegments);

    	FileInfo fi = FileInfo.getFileInfo(
    			Utils.makePath(path, data.name),
    			isDirectory(data.flags, data.symlink));
		fi.setFlags(data.flags);
		fi.setSymLink(data.symlink);
		fi.setOwnerGroup(data.owner, data.group);
		fi.setModified(data.timestamp);
		fi.setSize(fi.isDir() ? 0 : data.size);
		fi.setReadWrite(canRead(data.flags), canWrite(data.flags));
		return fi;
    }
    
    boolean isDirectory(String flags, String path) {
		boolean isdir = flags.charAt(0) == 'd';
		if (flags.charAt(0) == 'l') {
			File f = new File(path);
			if (f.canRead())
				isdir = f.isDirectory();
			else {
				return false;
				/*
				FileInfo fi2 = getFileInfo(f);
				isdir = fi2 != null ? fi2.isDir : false;*/
			}
		}
		return isdir;
    }
    
    FileData extractData(String details, boolean nineSegments) {
    	
    	Calendar mdate = new GregorianCalendar();
  	  	int year = mdate.get(Calendar.YEAR);
  	  	int month = mdate.get(Calendar.MONTH);
  	  	int day = mdate.get(Calendar.DAY_OF_MONTH);
  	  	String date = null;
  	  	String time = null;
  	  	char type = details.charAt(0);

  	  	if (type == 'c' || type == 'b')
  	  		details = details.replaceFirst(",\\s+", ",");
  	  	
    	FileData data = new FileData();
    	
    	// get other data
  	  	int num = 0;
	  	int index = 0;
	  	int len = details.length();
	  	
	  	while(num < 9 && index < len) {

	  		int segmentLength = details.indexOf(' ', index);

	  		String segment = (segmentLength != -1) ? 
	  					details.substring(index, segmentLength).trim() :
	  					details.substring(index).trim();

			if (nineSegments) {
  				switch(num) {
  				case 0:
  					data.flags = segment;
  					break;
  				case 1:
  					// hard link count
  					break;
  				case 2:
  					data.owner = checkOwnerGroup(segment);
  					break;
  				case 3:
  					data.group = checkOwnerGroup(segment);
  					break;
  				case 4:
  					if (type == 'c' || type == 'b')
  						break;
  					if (TextUtils.isDigitsOnly(segment))
  						data.size = Long.parseLong(segment);
					break;
  				case 5:
  					month = "Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec".indexOf(segment) / 4;
  					month++;
  					break;
  				case 6:
  					if (TextUtils.isDigitsOnly(segment))
  						day = Integer.parseInt(segment);
  					break;
  				case 7:
  					if (segment.length() == 5)
  						time = segment;
  					else if (segment.length() == 4){
  						if (TextUtils.isDigitsOnly(segment))
  							year = Integer.parseInt(segment);
  						time = "00:00";
  					}
  					break;
  				case 8:
  			        if (type == 'l') {
  						int symIndex = details.indexOf("->");
  						data.name = details.substring(index, symIndex).trim();
  						data.symlink = details.substring(symIndex + 2).trim();
  					}
  					else
  						data.name = details.substring(index).trim();
  					break;
  				}
  			}
  			else {
  	  			if (type != 'd' && 
  					type != 'l' &&
					type != 'p' &&
  					type != 's') {
  					switch(num) {
  	  				case 0:
  	  					data.flags = segment;
  	  					break;
	  				case 1:
	  					data.owner = checkOwnerGroup(segment);
  						break;
  					case 2:
  						data.group = checkOwnerGroup(segment);
  						break;	
  					case 3:
  						if (type == 'c' || type == 'b')
  	  						break;
  						
  						if (TextUtils.isDigitsOnly(segment))
  							data.size = Long.parseLong(segment);
  						break;	
  					case 4:
  						date = segment;
  						break;	
  					case 5:
  						time = segment;
  						break;
  					case 6:
  						data.name = details.substring(index).trim();
  						break;
  					}
  	  			}
				else {
  	  				switch(num) {
  	  				case 0:
  	  					data.flags = segment;
  	  					break;
	  				case 1:
	  					data.owner = checkOwnerGroup(segment);
  						break;
  					case 2:
  						data.group = checkOwnerGroup(segment);
  						break;
  					case 3:
  						date = segment;
  						break;
  					case 4:
  						time = segment;
  						break;
  	  				case 5:
	  	  				if (type == 'l') {
	  						int symIndex = details.indexOf("->");
	  						data.name = details.substring(index, symIndex).trim();
	  						data.symlink = details.substring(symIndex + 2).trim();
	  					}
	  					else
	  						data.name = details.substring(index).trim();
  	  					break;
  	  				}
  				}
  			}
			
			index = segmentLength + 1;
  	  		while (index < details.length() && details.charAt(index) == ' ')
  	  			index++;
  	  		
  	  		num++;
  	  	}
	  	
  	  	int hour = Integer.parseInt(time.substring(0, 2));
  	  	int min = Integer.parseInt(time.substring(3, 5));
  	  	int sec = 0;
  	  	if (!nineSegments) {
  	  		year = Integer.parseInt(date.substring(0, 4));
  	  		month = Integer.parseInt(date.substring(5, 7));
  	  		day = Integer.parseInt(date.substring(8));
  	  	}
 	  	data.timestamp = new GregorianCalendar(year, month - 1, day, hour, min, sec).getTimeInMillis();
	  	
	  	return data;
    }
    
    final static class FileData {
    	String flags = "";
        String symlink = "";
        String name = "";
        long size = 0L;
  	  	String owner = OWNER_GROUP_UNKNOWN;
        String group = OWNER_GROUP_UNKNOWN;
  	  	long timestamp;
    }
    
    String checkOwnerGroup(String value) {
    	if (TextUtils.isDigitsOnly(value)) {
    		String str = getOwnersGroupList().get(Integer.parseInt(value));
   			return str != null ? str : OWNER_GROUP_UNKNOWN;
		}
		return value;
    }
    
    final public int getFilesCount(String path) {
    	if (!su() && !new File(path).canRead())
    		return 0;
    	
    	StringBuilder sb = new StringBuilder();
    	try {
    		if (exec("ls -a -1 '" + path + "' | wc -l", sb)) {
	    		int i = Integer.parseInt(sb.toString());
	    		if (path.equals(Utils.getSdDirectory()))
	    			i--;
	    		return i - 2 < 0 ? i : i - 2;	// . , ..
    		}
    	}
    	catch(Exception e) {
            LogUtils.error(e);
		}
    	return 0;
    }

    final public boolean canRead(String permission) {
		switch (permission.length()) {
		case 10:
			return permission.charAt(8) == 'w';
		case 9:
			return permission.charAt(7) == 'w';
		default:
			return false;
		}
	}

	final public boolean canWrite(String permission) {
		switch (permission.length()) {
		case 10:
			return permission.charAt(9) == 'w';
		case 9:
			return permission.charAt(8) == 'w';
		default:
			return false;
		}
	}

    public boolean copyFile(FileInfo f, File destFile) {
    	String dest = destFile.getAbsolutePath();
    	
    	if (exec("cp '" + f.getPath() + "' '" + dest + "'")) {
        	setOwnerGroup(dest, f.getOwner(), f.getGroup());
	        setPermission(dest, f.getFlags());
	        try {
	    		new File(dest).setLastModified(f.getModified());
	    	} catch (Exception e) {
                LogUtils.error(e);
			}

	        return true;
    	}
        return false;
    }

    public boolean createSymbolicLink(FileInfo f, File destFile) {
    	String dest = destFile.getAbsolutePath();
    	boolean flag = exec("ln -s '" + f.getSymLink() + "' '" + dest + "'");
    	setPermission(dest, f.getFlags());
    	return flag;
    }

    public FileInfo rename(File f1, File f2) {
    	if (f2.exists())
    		f2.delete();
    	if (exec("mv '" + f1.getAbsolutePath() + "' '" + f2.getAbsolutePath() + "'"))
    		return getFileInfo(f2);
    	return null;
    }

    final public boolean fileExists(File f) {
    	return exec("if ( -f '" + f.getAbsolutePath() + "' )");
    }

    final public boolean directoryExist(File f) {
    	return exec("if ( -d '" + f.getAbsolutePath() + "' )");
    }

    public boolean delete(File f) {
    	
    	return f.isDirectory() ? 
      		exec("rm -r '" + f.getAbsolutePath() + "'") :
      		exec("rm '" + f.getAbsolutePath() + "'");
    }
    
    public static final String OWNER_GROUP_UNKNOWN = "unknown";
	
    TreeMap<Integer, String> ownersGroupList = null;
    final public TreeMap<Integer, String> getOwnersGroupList() {
    	
    	if (ownersGroupList == null) {
    		ownersGroupList = new TreeMap<Integer, String>();
		    ownersGroupList.put(-1, OWNER_GROUP_UNKNOWN);
		    ownersGroupList.put(0, "root");
		    ownersGroupList.put(1000, "system");
		    ownersGroupList.put(1001, "radio");
		    ownersGroupList.put(1002, "bluetooth");
		    ownersGroupList.put(1003, "graphics");
		    ownersGroupList.put(1004, "input");
		    ownersGroupList.put(1005, "audio");
		    ownersGroupList.put(1006, "camera");
		    ownersGroupList.put(1007, "log");
		    ownersGroupList.put(1008, "compass");
		    ownersGroupList.put(1009, "mount");
		    ownersGroupList.put(1010, "wifi");
		    ownersGroupList.put(1011, "adb");
		    ownersGroupList.put(1012, "install");
		    ownersGroupList.put(1013, "media");
		    ownersGroupList.put(1014, "dhcp");
		    ownersGroupList.put(1015, "sdcard_rw");
		    ownersGroupList.put(1016, "vpn");
		    ownersGroupList.put(1017, "keystore");
		    ownersGroupList.put(2000, "shell");
		    ownersGroupList.put(2001, "cache");
		    ownersGroupList.put(2002, "diag");
		    ownersGroupList.put(3001, "net_bt_admin");
		    ownersGroupList.put(3002, "net_bt");
		    ownersGroupList.put(3003, "inet");
		    ownersGroupList.put(3004, "net_raw");
		    ownersGroupList.put(3005, "net_admin");
		    ownersGroupList.put(9998, "misc");
		    ownersGroupList.put(9999, "nobody");
		    
//		    Iterator<ApplicationInfo> infos = Core.getContext().getPackageManager().getInstalledApplications(0).iterator();
//		    while (infos.hasNext()) {
//		    	ApplicationInfo appInfo = infos.next();
//		    	
//		    	if (appInfo.uid >= 10000 && !ownersGroupList.containsKey(Integer.valueOf(appInfo.uid)))
//		    		ownersGroupList.put(Integer.valueOf(appInfo.uid), "app_" + (-10000 + appInfo.uid));
//		    }
    	}
	    return ownersGroupList;
	}

	public boolean installPackage(String path) {

		return exec("pm install -r '" + path + "'");
	}
		
}
