package com.knowology.km.gerneratewordpat;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;

import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.dal.Database;
import com.knowology.km.util.MyUtil;

public class ProduceMain {
	
	public final static String MSG = "调用方式：java -jar ProduceMain userid serviceid";
	
	public static List<WordpatProducer> producerPool = new ArrayList<WordpatProducer>();
	
	public static Map<String, String> NLP_SERVER_IP_MAP = new Hashtable<String, String>();
	
	static {
		Result rs = CommonLibMetafieldmappingDAO.getConfigKey("各省负载均衡配置表");
		if (rs != null && rs.getRowCount() > 0){
			for(SortedMap row : rs.getRows()){
				String key = (row.get("k") == null ? null : row.get("k").toString());
				if(key != null){
					String value = (row.get("name") == null ? "" : row.get("name").toString());
					NLP_SERVER_IP_MAP.put(value, "http://" + key + "/NLPWebService/NLPCallerWS?wsdl");
				}
			}
		}
	}
	
	/**
	 * 获得所有客户问题
	 * @param serviceid 业务id
	 * @return
	 */
	public static LinkedBlockingQueue<String> getAllTask(String servicetypes){
		List<String> queries = QuerymanageDAO.getAllQuery(servicetypes.split(","));
		if(queries == null || queries.size() == 0){
			return null;
		}
		
		LinkedBlockingQueue<String> taskQueue = new LinkedBlockingQueue<String>(queries);
		return taskQueue;
	};
	
	/**
	 * 所有producer是否都停止
	 * @return
	 */
	public static boolean allStoped(){
		for(WordpatProducer producer : producerPool){
			if(!producer.isNeedStop()){
				return false;
			}
		}
		return true;
	}
	
	public static int countSuccess(){
		int count = 0;
		for(WordpatProducer producer : producerPool){
			count += producer.getCounter();
		}
		return count;
	}
	
	public static void printStatistics(){
		System.out.println("==========各线程统计========");
		for(WordpatProducer producer : producerPool){
			System.out.println("线程[" + producer.getName() + "]成功生成" + producer.getCounter() + "个");
		}
	}
	
	public static boolean backupWordpatTable(String tableName){
		try {
			int n = Database.executeNonQuery("Create Table " + tableName + " As Select * From Wordpat");
			System.out.println("===================n:" +n);
			Result rs = Database.executeQuery("select count(*) n from " + tableName);
			if(rs != null && rs.getRowCount() > 0 && Integer.parseInt(rs.getRows()[0].get("N").toString()) > 0){
				return true;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void main(String[] args) {
		InputStream in = ProduceMain.class.getResourceAsStream("config.properties");
		Properties prop = new Properties();
		try {
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String userid = prop.getProperty("userid");
		String servicetypes = prop.getProperty("servicetypes");
		String apppath = prop.getProperty("apppath");
		int threadNum = Integer.parseInt(prop.getProperty("threadNum"));
		
		System.out.println("初始化参数...");
		
		
		System.out.println("servicetypes:" + servicetypes);
		System.out.println("wordpatpath:" + apppath);
		System.out.println("userid:" + userid);
		System.out.println("threadNum" + threadNum);
		
		String tableName = "wordpat_" + DateFormatUtils.format(new Date(), "yyyyMMdd");
		System.out.println("词模表备份中..." + tableName);
		if(backupWordpatTable(tableName)){
			System.out.println("备份成功：" +tableName);
		}else{
			System.out.println("备份失败, 结束程序");
			System.exit(0);
		}
		
		System.out.println("收集客户问题...");
		long startTime1 = System.currentTimeMillis();
		LinkedBlockingQueue<String> taskQueue = getAllTask(servicetypes);
		if(taskQueue == null || taskQueue.size() <= 0){
			System.out.println("未获取到客户问题");
			System.exit(0);
		}
		long endTime1 = System.currentTimeMillis();
		
		System.out.println("客户问题收集完成，共"+taskQueue.size()+"个。耗时："+ (endTime1-startTime1)/1000 +"秒");
		
		long startTime2 = System.currentTimeMillis();
		for(int i = 0; i < threadNum; i++){
			WordpatProducer producer = new WordpatProducer(taskQueue);
			producerPool.add(producer);
			producer.setUserid(userid);
			producer.setWordpatConfPath(apppath);
			producer.start();
		}
		
		while(true){
			try {
				Thread.sleep(1) ;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(allStoped()){
				long endTime2 = System.currentTimeMillis();
				System.out.println("词模生成完成，总成功" + countSuccess() + "个。共耗时：" + (endTime2 - startTime2)/1000 +"秒");
				printStatistics();
				return;
			}
		}
	}
	
	
}
