package com.seazon.fo.zip;

import java.util.Comparator;

import com.seazon.fo.Core;
import com.seazon.fo.Helper;

public class ZipFileComparator implements Comparator<MyZipFile>
{
	private String order;	//以什么排序？名称，大小等
	private int order2;	//顺序、倒序

	public ZipFileComparator(String order, String order2)
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

	public int compare(MyZipFile file1, MyZipFile file2)
	{
		if (file1.isDirectory && file2.isDirectory == false)
			return -1*order2;
		
		if (file1.isDirectory == false && file2.isDirectory)
			return 1*order2;

		if (file1.isDirectory && file2.isDirectory)
			return file1.name.toLowerCase().compareTo(file2.name.toLowerCase())*order2;
		
		if (order.equals(Core.ORDER_NAME))
		{
			return file1.name.toLowerCase().compareTo(file2.name.toLowerCase())*order2;
		}
		
		if (order.equals(Core.ORDER_TYPE))
		{
			int ret = Helper.getExtension(file1.name).compareTo(Helper.getExtension(file2.name));
			if (ret != 0)
				return ret*order2;
			else
				return file1.name.toLowerCase().compareTo(file2.name.toLowerCase())*order2;
		}
		
		if (order.equals(Core.ORDER_SIZE))
		{
			int ret = (file1.compressedSize < file2.compressedSize ? -1 : 1);
			if (ret != 0)
				return ret*order2;
			else
				return file1.name.toLowerCase().compareTo(file2.name.toLowerCase())*order2;
		}
		
		if (order.equals(Core.ORDER_DATEMODIFIED))
		{
			int ret = (file1.time < file2.time ? -1 : 1);
			if (ret != 0)
				return ret*order2;
			else
				return file1.name.toLowerCase().compareTo(file2.name.toLowerCase())*order2;
		}
		
		return 0;
	}
}
