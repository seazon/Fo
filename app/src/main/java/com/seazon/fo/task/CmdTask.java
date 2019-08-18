package com.seazon.fo.task;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.os.AsyncTask;
import android.util.Log;

import com.seazon.fo.Helper;
import com.seazon.fo.activity.Shell;
import com.seazon.utils.LogUtils;

public class CmdTask extends AsyncTask<File, Object, String>
{
	public CmdTask()
	{
	}

	protected String doInBackground(File... files)
	{
		Runtime r = Runtime.getRuntime();
		File directory = files[0];
		try {
//				Shell s = new Shell();
//				String result = s.sendShellCommand(new String[]{"ls -l"});
//				Helper.d(result);
			if(directory.canRead()==false){
				Process process = r.exec("su", null, directory);
//				Helper.e(cmd(r.exec("su")));
				if(in(process, "ls -l")==false)
				{
					return "failed";
				}
				return out(process);
//				RootCommand(command)
			}
			return "";
//			return cmd(r.exec("ls -l", null, directory));
		} catch (Exception e) {
            LogUtils.error(e);
			return e.getLocalizedMessage();
		}
	}

	protected void onPostExecute(String o)
	{
        LogUtils.error(o);
	}
	
	private String out(Process process) {
		InputStreamReader is1 = null;
		InputStreamReader is2 = null;
		try {
			String AllText = "";
			String line;
			BufferedReader STDOUT = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			BufferedReader STDERR = new BufferedReader(new InputStreamReader(
					process.getErrorStream()));
			try {
				process.waitFor();
			} catch (InterruptedException e) {
                LogUtils.error(e);
			}
			while ((line = STDERR.readLine()) != null) {
				AllText = AllText + "\n" + line;
			}
			while ((line = STDOUT.readLine()) != null) {
				AllText = AllText + "\n" + line;
				while ((line = STDERR.readLine()) != null) {
					AllText = AllText + "\n" + line;
				}
			}
			return AllText;
		} catch (Exception e) {
            LogUtils.error(e);
			return e.getMessage();
		} finally
        {
            try
            {
                if (is1 != null)
                {
                	is1.close();
                }
                if (is2 != null)
                {
                	is2.close();
                }
//                process.destroy();
            } catch (Exception e)
            {
                LogUtils.error(e);
            }
        }
	}
	
	private boolean in(Process process, String command){
		DataOutputStream os = null;
		 try
	        {
	            os = new DataOutputStream(process.getOutputStream());
	            os.writeBytes(command + "\n");
	            os.flush();
	           // process.waitFor();
	            
	            return true;
	        } catch (Exception e)
	        {
                LogUtils.error(e);
	            return false;
	        } finally
	        {
	            try
	            {
	                if (os != null)
	                {
	                    os.close();
	                }
//	                process.destroy();
	            } catch (Exception e)
	            {
                    LogUtils.error(e);
	            	return false;
	            }
	        }
	}
	
	private boolean close(Process process){
		DataOutputStream os = null;
		 try
	        {
	            os = new DataOutputStream(process.getOutputStream());
	            os.writeBytes("exit\n");
	            os.flush();
	            process.waitFor();
	            
	            return true;
	        } catch (Exception e)
	        {
                LogUtils.error(e);
	            return false;
	        } finally
	        {
	            try
	            {
	                if (os != null)
	                {
	                    os.close();
	                }
	                process.destroy();
	            } catch (Exception e)
	            {
                    LogUtils.error(e);
	            	return false;
	            }
	        }
	}
	
//	private  String RootCommand(String command)
//    {
//        Process process = null;
//        DataOutputStream os = null;
//       
//        Log.d("*** DEBUG ***", "Root SUC ");
////        return true;
//        return cmd(process);
//    }

}
