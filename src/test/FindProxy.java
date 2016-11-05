package test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class FindProxy extends Thread
{

	private int index;

	public FindProxy(int index)
	{
		super();
		this.index = index;
	}

	@Override
	public void run()
	{
		for (int i = getIndex(); i < Test.ipAndPortList.size(); i += Test.threadNum)
		{
			try
			{
				// 测试代理ip和port有效就开始访问所有博客链接10次。
				if (CM.testIpEffective(Test.ipAndPortList.get(i).getIp(), Test.ipAndPortList.get(i).getPort()))
				{
					System.out.println(Test.ipAndPortList.get(i).toString() + "有效,开始刷博客访问量");

					for (int j = 0; j < 10; j++)
					{
						visitBlogLink(Test.ipAndPortList.get(i).getIp(), Test.ipAndPortList.get(i).getPort());
					}
				}

			}
			catch (Exception e)
			{
				continue;
			}
		}
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	/**
	 * 访问博客,解析博客标题和访问次数
	 * 
	 * @param ip
	 * @param port
	 */
	private void visitBlogLink(String ip, int port)
	{
		for (Map.Entry<String, String> entry : Test.blogMap.entrySet())
		{
			String url = entry.getKey();

			try
			{
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
				URL obj = new URL(url);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection(proxy);
				con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) ...");
				con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

				con.setRequestMethod("GET");
				int responseCode = con.getResponseCode();
				String responseBody = CM.readResponseBody(con.getInputStream());
				if (responseCode == 200)
				{
					Parser parser;
					try
					{
						parser = new Parser(responseBody);
						// 获取标题标签
						NodeList titleNodes = parser.extractAllNodesThatMatch(new AndFilter(new TagNameFilter("a"), new HasAttributeFilter("href", url.replace(
								"http://blog.csdn.net", ""))));

						if (titleNodes != null && titleNodes.size() != 0)
						{
							// 转换成普通的tag标签
							Tag tag = (Tag) titleNodes.elementAt(0);
							System.out.print(ip + ":" + port + "\t\t" + tag.toPlainTextString().trim() + "\t\t");
						}
						Pattern p = Pattern.compile("title=\\\"阅读次数\\\">((.*?)人阅读)<");
						Matcher m = p.matcher(responseBody);
						while (m.find())
						{
							String b = m.group(1);
							System.out.println(b);
						}
					}
					catch (ParserException e)
					{
						continue;
					}
				}
			}
			catch (IOException e)
			{
				continue;
			}
		}
	}

}
