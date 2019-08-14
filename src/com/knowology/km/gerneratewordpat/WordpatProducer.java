package com.knowology.km.gerneratewordpat;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;

import com.knowology.bll.CommonLibQueryManageDAO;
import com.knowology.km.util.MyUtil;

public class WordpatProducer extends Thread{
	private boolean needStop = false;
	private final LinkedBlockingQueue<String> queryQueue;
	private final AtomicInteger counter = new AtomicInteger(0);
	
	private String userid;
	private String wordpatConfPath;
	
	public WordpatProducer(LinkedBlockingQueue<String> queryQueue) {
		this.queryQueue = queryQueue;
	}

	private void execute(){
		String material = queryQueue.poll();
		if(material == null){
			needStop = true;
			return;
		}

		String queryArray[] = material.split("@#@");
		String queryCityCode = queryArray[0];
		if ("".equals(queryCityCode) || queryCityCode == null) {
			queryCityCode = "全国";
		} else {
			queryCityCode = queryCityCode.replace(",", "|");
		}
		String query = queryArray[1];
		String kbdataid = queryArray[2];
		String province = queryArray[3];
		String servicetype = queryArray[4];
		String nlpServerIP = ProduceMain.NLP_SERVER_IP_MAP.get(province);
		if(StringUtils.isEmpty(nlpServerIP)){
			nlpServerIP = ProduceMain.NLP_SERVER_IP_MAP.get("全国");
		}
		
		// 获取高级分析的接口串中的serviceInfo
		String serviceInfo = MyUtil.getServiceInfo(servicetype, "问题生成词模", "", false, queryCityCode);
		// 获取高级分析的串
		String queryObject = MyUtil.getDAnalyzeQueryObject("问题生成词模",
				queryArray[1], servicetype, serviceInfo);
		// 调用生成词模的接口生成词模,可能是多个，以@_@分隔
		
		String now = DateFormatUtils.format(new Date(), "yyyy-dd-MM HH:mm:ss:SSS");
		System.out.println(MessageFormat.format("{0} [{1}] 调用NLP: {2}\n参数：{3}", now, getName(), nlpServerIP, queryObject));
		String wordpat = QuerymanageDAO.getWordpat(queryObject, nlpServerIP);
		now = DateFormatUtils.format(new Date(), "yyyy-dd-MM HH:mm:ss:SSS");
		System.out.println(MessageFormat.format("{0} [{1}] 调用NLP返回：{2}", now, getName(), wordpat));
		
		if (wordpat != null && !"".equals(wordpat)) {
			// 判断词模是否含有@_@
			if (wordpat.contains("@_@")) {
				// 有的话，按照@_@进行拆分,并只取第一个
				wordpat = wordpat.split("@_@")[0];
			}
//			// 获取词模中@前面的词模题，在加上@2#编者="问题库"&来源="(当前问题)"
//			wordpat = wordpat.split("@")[0] + "@2#编者=\"问题库\"&来源=\""
//					+ query.replace("&", "\\and") + "\"";

			 //保留自学习词模返回值，并替换 编者=\"自学习\""=>编者="问题库"&来源="(当前问题)" ---> modify 2017-05-24
			wordpat = wordpat.replace("编者=\"自学习\"", "编者=\"问题库\"&来源=\""+ query.replace("&", "\\and") + "\"");
			
			// 校验自动生成的词模是否符合规范
			if (QuerymanageDAO.CheckWordpat(wordpat, wordpatConfPath)) {
				List<String> tempList = new ArrayList<String>();
				tempList.add(wordpat);
				tempList.add(queryCityCode);
				tempList.add(query);
				tempList.add(kbdataid);

				int count = CommonLibQueryManageDAO.insertWordpat2(tempList, servicetype, userid);
				if(count > 0){
					counter.incrementAndGet();
					now = DateFormatUtils.format(new Date(), "yyyy-dd-MM HH:mm:ss:SSS");
					System.out.println(MessageFormat.format("{0} [{1}] 生成成功【kbdataid:{2}, query:{3}, wordpat:{4}】", now, getName(), kbdataid,query,wordpat));
				}else{
					now = DateFormatUtils.format(new Date(), "yyyy-dd-MM HH:mm:ss:SSS");
					System.out.println(MessageFormat.format("{0} [{1}] 生成失败【kbdataid:{2}, query:{3}, wordpat:{4}】", now, getName(),  kbdataid,query,wordpat));
				}
				
			} else{
				now = DateFormatUtils.format(new Date(), "yyyy-dd-MM HH:mm:ss:SSS");
				System.out.println(MessageFormat.format("{0} [{1}] 词模不符合规范【kbdataid:{2}, query:{3}, wordpat:{4}】", now, getName(),  kbdataid,query,wordpat));
			}
		}else{
			now = DateFormatUtils.format(new Date(), "yyyy-dd-MM HH:mm:ss:SSS");
			System.out.println(MessageFormat.format("{0} [{1}] 接口返回词模为空【kbdataid:{2}, query:{3}, wordpat:{4}】", now, getName(),  kbdataid,query,wordpat));
		}
	}
	
	@Override
	public void run() {
		while(!needStop){
			execute();
		}
		System.out.println("线程[" + getName() + "]运行结束");
	}

	public boolean isNeedStop() {
		return needStop;
	}

	public void setNeedStop(boolean needStop) {
		this.needStop = needStop;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getWordpatConfPath() {
		return wordpatConfPath;
	}

	public void setWordpatConfPath(String wordpatConfPath) {
		this.wordpatConfPath = wordpatConfPath;
	}

	public LinkedBlockingQueue<String> getQueryQueue() {
		return queryQueue;
	}

	public int getCounter() {
		return counter.get();
	}
}


