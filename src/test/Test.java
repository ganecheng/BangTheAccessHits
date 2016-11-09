package test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test
{
	/**
	 * 需要刷的博客链接
	 */
	public static Map<String, String> blogMap;
	/**
	 * 找到的代理ip和port
	 */
	public static List<IpAndPort> ipAndPortList;
	/**
	 * 开启多少个线程来跑程序
	 */
	public static int threadNum = 500;

	public static void main(String[] args) throws IOException
	{
		for (int times = 0; times < 100; times++)
		{
			System.out.println("当前循环次数" + times);

			// 设置要刷的博客名字
			String blogName = "hhxin635612026";

			blogMap = CM.getBlogLinkList(blogName);

			if (blogMap == null || blogMap.size() <= 0)
			{
				System.out.println("没有找到需要刷的博客链接");
				return;
			}
			for (Map.Entry<String, String> entry : blogMap.entrySet())
			{
				System.out.println(entry.getKey());
			}

			//1.从网上在线爬数据
			ipAndPortList = CM.getTotalList();
			//2.从本地列表中取数据(建议网上爬完数据后,就切换为本地列表)
			//ipAndPortList = CM.getTotalListFromFile("D:/proxy.txt");
			
			if (ipAndPortList == null || ipAndPortList.size() <= 0)
			{
				System.out.println("没有找到任何代理");
				return;
			}

			FindProxy[] threadArr = new FindProxy[threadNum];
			for (int i = 0; i < threadArr.length; i++)
			{
				threadArr[i] = new FindProxy(i);
				threadArr[i].start();
			}

			try
			{
				Thread.sleep(1000 * 60 * 30);
			}
			catch (InterruptedException e)
			{
				continue;
			}
		}

	}

}
