package com.bshf.spider.dorule;

import com.bshf.spider.dorule.abs.SpiderRuleAbstract;
import com.bshf.spider.entity.GoodsPO;
import com.bshf.util.DataFormatStatus;
import com.bshf.util.IPModel.IPMessage;
import com.bshf.util.database.MyRedis;
import com.bshf.util.httpbrowser.MyHttpResponse;
import com.bshf.util.parser.JsoupHtmlParser;
import com.bshf.util.savetomysql.ServiceImpl;

import java.util.LinkedList;
import java.util.List;

/**
 * 用药参考网
 * http://drugs.medlive.cn/index.jsp
 * 根据关键字 取得链接 并取得其内容
 * */
public class SpiderRuleDrugsDetail extends SpiderRuleAbstract {
	@Override
	public void runSpider(GoodsPO goodsPO)  {
		try {

			// 设置代理IP
			MyRedis redis = new MyRedis();
			//从redis数据库中随机拿出一个IP
			IPMessage ipMessage = redis.getIPByList();
			redis.close();
			String htmlSource = MyHttpResponse.getHtml(goodsPO.getWebUrl(), ipMessage.getIPAddress(), ipMessage.getIPPort());




			//	1-2 输出查看效果
				//System.out.println("htmlSource=============="+htmlSource);

			//	2-1 提取有效内容
			//  .blue-bar .blue-white 取出药品名称
			List<String> targetListPattern = new LinkedList<String>();
			targetListPattern.add(".blue-bar .blue-white");
			List<String> targetList = JsoupHtmlParser.getNodeContentBySelector(htmlSource, targetListPattern, DataFormatStatus.CleanTxt, true);
			//  .info-content .info-left 取出药品详情
			List<String> targetDetailListPattern = new LinkedList<String>();
			targetDetailListPattern.add(".info-content .info-left");
			List<String> targetDetailList = JsoupHtmlParser.getNodeContentBySelector(htmlSource, targetDetailListPattern, DataFormatStatus.CleanTxt, true);

			//  2-2输出查看
			//	System.out.println(targetList.toString());
			//  System.out.println(targetDetailList.toString().replace("：","：|"));

			//  3-1 往数据库中存
			goodsPO.setName(targetList.toString());
			//  做字符替换 好在Excel表中分列
			goodsPO.setProvide(targetDetailList.toString().replace("：", "：|"));
			goodsPO.setType(ipMessage.getIPAddress()+":"+ipMessage.getIPPort());
			ServiceImpl goodsPOServiceImpl = new ServiceImpl();
			goodsPOServiceImpl.add("goods", goodsPO);

		}catch (Exception e){
			//不处理异常
		}

	}



	public static void main(String[] args) {
		GoodsPO goodsPO = new GoodsPO();
		String srcUrl = "http://drugs.medlive.cn/drugref/html/1908298.shtml";
		goodsPO.setWebUrl(srcUrl);
		goodsPO.setOrderNum("1");
		SpiderRuleDrugsDetail spiderUtils = new SpiderRuleDrugsDetail();
		spiderUtils.runSpider(goodsPO);
	}
}

