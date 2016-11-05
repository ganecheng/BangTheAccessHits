package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class CM
{
	/**
	 * 读取输入流中的数据
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static String readResponseBody(InputStream inputStream) throws IOException
	{

		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null)
		{
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}

	/**
	 * 测试代理是否有效
	 * 
	 * @param ip
	 * @param port
	 * @return
	 */
	public static boolean testIpEffective(String ip, int port)
	{
		try
		{
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
			URL obj = new URL("http://www.baidu.com");
			HttpURLConnection con = (HttpURLConnection) obj.openConnection(proxy);
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) ...");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setConnectTimeout(8000);
			con.setReadTimeout(8000);

			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			if (responseCode == 200)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public static synchronized void writeToFile(String str)
	{
		try
		{
			// 打开一个随机访问文件流，按读写方式
			RandomAccessFile randomFile = new RandomAccessFile("D:/proxy.txt", "rw");
			// 文件长度，字节数
			long fileLength = randomFile.length();
			// 将写文件指针移到文件尾。
			randomFile.seek(fileLength);
			randomFile.writeBytes(str + "\r\n");
			randomFile.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 获取所有页面的代理IP和port
	 * 
	 * @return
	 * @throws IOException
	 */
	public static List<IpAndPort> getTotalList() throws IOException
	{
		List<IpAndPort> totalList = new ArrayList<IpAndPort>();

		List<String> urlList = new ArrayList<String>();
		for (int i = 1; i <= 260; i++)
		{
			String strTemp = new String("http://www.xicidaili.com/wn/" + i);
			urlList.add(strTemp);
		}

		for (String url : urlList)
		{

			try
			{
				URL obj = new URL(url);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) ...");
				con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

				con.setRequestMethod("GET");
				int responseCode = con.getResponseCode();
				String responseBody = CM.readResponseBody(con.getInputStream());

				if (responseCode == 200)
				{
					List<IpAndPort> list = getIpList(responseBody, 200);
					totalList.addAll(list);

					System.out.println("查找到网页 " + url + " 上的代理IP" + list.size() + "个，现在已经找到 " + totalList.size() + " 个");
				}
				else
				{
					System.out.println("网页 " + url + " 返回错误码为" + responseCode + "，将跳过，现在已经找到 " + totalList.size() + " 个");
				}
			}
			catch (Exception e)
			{
				System.out.println("出现错误了，或者被封了IP，停下来，换一下本地IP地址再重新运行程序吧。");
				continue;
			}
		}

		return totalList;
	}

	/**
	 * 获取所有页面的代理IP和port(从文件获取)
	 * 
	 * @return
	 * @throws IOException
	 */
	public static List<IpAndPort> getTotalListFromFile(String fileName) throws IOException
	{
		List<IpAndPort> totalList = new ArrayList<IpAndPort>();
		File file = new File(fileName);

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
		String str;
		while ((str = br.readLine()) != null)
		{
			String[] ipAndPort = str.trim().split(":");
			IpAndPort ipPort = new IpAndPort(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
			totalList.add(ipPort);
		}
		br.close();

		return totalList;
	}

	/**
	 * 获取单个页面的ip和端口
	 * 
	 * @param htmlStr
	 * @param imgMaxNum
	 * @return
	 */
	public static List<IpAndPort> getIpList(String htmlStr, int imgMaxNum)
	{
		List<IpAndPort> imgSrcList = new ArrayList<IpAndPort>();
		Parser parser;
		try
		{
			parser = new Parser(htmlStr);
			// 获取tr标签
			NodeFilter filter = new TagNameFilter("tr");
			NodeList nodes = parser.extractAllNodesThatMatch(filter);

			if (nodes != null && nodes.size() != 0)
			{
				int imgNum = nodes.size() < imgMaxNum ? nodes.size() : imgMaxNum;
				for (int i = 1; i < imgNum; i++)
				{
					// 转换成普通的tag标签
					Tag tag = (Tag) nodes.elementAt(i);
					NodeList list = tag.getChildren();
					String ip = list.elementAt(3).toPlainTextString().trim();
					String port = list.elementAt(5).toPlainTextString().trim();
					imgSrcList.add(new IpAndPort(ip, Integer.parseInt(port)));
				}
				return imgSrcList;
			}
			else
			{
				return null;
			}
		}
		catch (ParserException e)
		{
			return null;
		}
	}

	/**
	 * 获取所有需要刷访问量的博客的链接
	 * 
	 * @param blogName
	 * @throws IOException
	 */
	public static Map<String, String> getBlogLinkList(String blogName) throws IOException
	{

		Map<String, String> blogMap = new HashMap<String, String>();

		for (int index = 1; index < 100; index++)
		{
			URL obj = new URL("http://blog.csdn.net/" + blogName + "/article/list/" + index);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) ...");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			String responseBody = CM.readResponseBody(con.getInputStream());

			if (responseCode != 200)
			{
				continue;
			}

			Parser parser;
			try
			{
				parser = new Parser(responseBody);

				// 获取a标签
				NodeFilter filter = new TagNameFilter("a");
				NodeList nodes = parser.extractAllNodesThatMatch(filter);

				if (nodes != null && nodes.size() != 0)
				{
					for (int i = 0; i < nodes.size(); i += 1)
					{
						// 转换成普通的tag标签
						Tag tag = (Tag) nodes.elementAt(i);

						String href = tag.getAttribute("href");

						if (href != null && href.equals("") == false)
						{
							if (href.indexOf("/" + blogName + "/article/details") >= 0 && href.indexOf("#comments") < 0)
							{
								href = "http://blog.csdn.net" + href;

								blogMap.put(href, href);
							}
						}
					}
				}
			}
			catch (ParserException e)
			{
				continue;
			}
		}

		return blogMap;
	}
}
