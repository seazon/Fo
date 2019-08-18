package com.seazon.fo.entity;

import java.io.File;
import java.util.Comparator;

import com.seazon.fo.Core;
import com.seazon.fo.Helper;

public class FileComparator implements Comparator<File>
{
	private String order;	//以什么排序？名称，大小等
	private int order2;	//顺序、倒序

	public FileComparator(String order, String order2)
	{
		this.order = order;
		if(Core.ORDER2_ASC.equals(order2))
		{
			this.order2 = 1;
		}
		else
		{
			this.order2 = -1;
		}
	}

	public int compare(File file1, File file2)
	{
		if (file1.isDirectory() && file2.isDirectory() == false)  //TODO ANR File.isDirectory() 
			return -1*order2;
		
		if (file1.isDirectory() == false && file2.isDirectory())
			return 1*order2;

		if (file1.isDirectory() && file2.isDirectory())
			return file1.getName().toLowerCase().compareTo(file2.getName().toLowerCase())*order2;
		
		if(!file1.canRead() || !file2.canRead())
			return file1.getName().toLowerCase().compareTo(file2.getName().toLowerCase())*order2;
		
		if (order.equals(Core.ORDER_NAME))
		{
			return file1.getName().toLowerCase().compareTo(file2.getName().toLowerCase())*order2;
		}
		
		if (order.equals(Core.ORDER_TYPE))
		{
			int ret = Helper.getExtension(file1.getName()).compareTo(Helper.getExtension(file2.getName()));
			if (ret != 0)
				return ret*order2;
			else
				return file1.getName().toLowerCase().compareTo(file2.getName().toLowerCase())*order2;
		}
		
		if (order.equals(Core.ORDER_SIZE))
		{
			int ret = (file1.length() < file2.length() ? -1 : 1);
			if (ret != 0)
				return ret*order2;
			else
				return file1.getName().toLowerCase().compareTo(file2.getName().toLowerCase())*order2;
		}
		
		if (order.equals(Core.ORDER_DATEMODIFIED))
		{
			int ret = (file1.lastModified() < file2.lastModified() ? -1 : 1);
			if (ret != 0)
				return ret*order2;
			else
				return file1.getName().toLowerCase().compareTo(file2.getName().toLowerCase())*order2;
		}
		
		return 0;
	}
}
