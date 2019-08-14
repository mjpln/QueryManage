package com.knowology.km.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

public class CommonUtils {
	/**
	 * 处理高级分析返回的数据
	 * 
	 * @param str
	 *            json字符串 [{"id":"1","text":"你好","allSegments":["",""]}]
	 * @return 处理好的list集合
	 */
	public static List<String> stringToList(String str) {
		List<String> list = new ArrayList<String>();
		// 把字符串转换成json数组
		JSONArray jsonArray = JSONArray.fromObject(str);

		for (int i = 0; i < jsonArray.size(); i++) {
			// 把得到的字符串转换成json对象
			JSONObject jsonObject = JSONObject.fromObject(jsonArray.get(i)
					.toString());

			if (jsonObject.containsKey("allSegments")) {// 如果json对象里面有这个key
				// 得到json数组
				JSONArray segments = JSONArray.fromObject(jsonObject.get(
						"allSegments").toString());
				for (int j = 0; j < segments.size(); j++) {// 固定格式
					// "你好(!您好近类)  (1 words)"
					String word = segments.getString(j).split("\\)  \\(")[0]
							+ ")  ";
					if (word.equals("...(由于长度限制，还有几组分词未输出))  ")) {
						break;
					}
					list.add(word);
				}
			}
		}
		return list;
	}

	/**
	 * 把高级分析处理好的list集合以")  "分割 重新放到list集合里面
	 * 
	 * @param segs
	 *            高级分析处理好的list集合
	 * @return list集合
	 */
	public static List<String> listToList(List<String> segs) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < segs.size(); i++) {
			String str = segs.get(i);
			// String[] temp = str.split(" ");
			String[] temp = str.split("\\)  ");
			for (int j = 0; j < temp.length; j++) {
				if (!list.contains(temp[j] + ")")) {
					list.add(temp[j] + ")");
				}
			}
		}
		return list;
	}

	/**
	 * 把list转换成map
	 * 
	 * @param list
	 * @return 处理好的map
	 */
	public static Map<String, String> listToMap(List<String> list) {
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < list.size(); i++) {
			String[] str = list.get(i).split("\\(");
			if (!map.containsKey(str[0])) {
				map.put(str[0], str[1].replaceAll("\\)", "")
						.replaceAll("!", ""));
			}
		}
		return map;
	}

	/**
	 * 简要分析处理
	 * 
	 * @param str
	 * @return
	 */
	public static List<String> getNLPAppWSList(String str) {
		List<String> list = new ArrayList<String>();

		JSONObject jsonObject = JSONObject.fromObject(str);

		JSONArray jsonArray = JSONArray.fromObject(jsonObject
				.get("kNLPResults").toString());

		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject content = JSONObject.fromObject(jsonArray.get(i)
					.toString());
			String abstractStr = content.getString("abstractStr");
			// if(StringUtils.isNotEmpty(abstractStr))
			if (!"".equals(abstractStr) && abstractStr != null) {
				String credit = content.getString("credit");
				Double b = Double.parseDouble(credit);
				abstractStr = abstractStr.split("\\>")[1];
				if (!list.contains(abstractStr) && b > 0.3) {
					list.add(abstractStr);
				}
			}
		}

		return list;
	}

	/**
	 * list*list业务处理
	 * 
	 * @param mapQuerySeg
	 *            高级分析query分词的每一条分词
	 * @return list*list的结果
	 */
	public static List<String> getResults(Map<String, String> mapQuerySeg) {

		List<String> results = new ArrayList<String>();

		// 对每一个分词进行处理
		for (Map.Entry<String, String> entry : mapQuerySeg.entrySet()) {
			// 临时list集合
			List<String> temp = new ArrayList<String>();
			// 调用简要分析
			String nlpAppWSResult = KAnalyzeResult
					.getNLPAppWSClientResult(entry.getKey());

			List<String> nlpAppWSList = getNLPAppWSList(nlpAppWSResult);

			// if(CollectionUtils.isNotEmpty(nlpAppWSList))
			if (nlpAppWSList.size() > 0) {
				for (String str : nlpAppWSList) {
					results.add(str + "[子句]");
				}
			} else {
				nlpAppWSList.add(entry.getValue());
				// if(CollectionUtils.isNotEmpty(results))
				if (results.size() > 0) {
					results.add("*" + entry.getValue());
				} else {
					results.add(entry.getValue() + "*");
				}
			}

			int oneSize = results.size();
			int twoSize = oneSize - nlpAppWSList.size();
			temp = results;
			results = new ArrayList<String>();
			for (int i = 0; i < twoSize; i++) {
				for (int j = twoSize; j < oneSize; j++) {
					results.add(temp.get(i) + temp.get(j));
				}
			}
			// if(CollectionUtils.isEmpty(results))
			if (results.size() == 0) {
				results = temp;
			}
		}

		List<String> resultsList = new ArrayList<String>();
		for (String result : results) {
			resultsList.add(result + "#无序#编者=\"auto\"");
		}
		return resultsList;
	}

	/**
	 * 
	 * @param lists
	 * @return
	 */
	public static Map<String, List<String>> getResults(List<String> lists) {
		// List<String> results = new ArrayList<String>();

		Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();

		// 高级分析返回的字符串
		String nlpAppWebResult = "";

		List<String> nlpAppWebList = new ArrayList<String>();

		for (int i = 0; i < lists.size(); i++) {
			// 调用高级分析接口
			nlpAppWebResult = KAnalyzeResult.getNLPAppWebClientResult(lists
					.get(i));
			// 获取根据高级分析 处理返回的List集合
			nlpAppWebList = stringToList(nlpAppWebResult);

			for (int j = 0; j < nlpAppWebList.size(); j++) {
				String[] str = nlpAppWebList.get(j).split("\\)  ");
				Map<String, String> mapString = new LinkedHashMap<String, String>();
				for (int a = 0; a < str.length; a++) {
					String[] s = str[a].split("\\(");
					mapString.put(s[0], StringUtils.replace(s[1], "!", ""));
				}
				List<String> result = getResults(mapString);
				// 返回Map，用这个
				map.put(nlpAppWebList.get(j), result);
				// 返回List，用这个
				/*
				 * for(String s : result){ results.add(s); }
				 */
			}
		}
		return map;
	}

	private static Map<String, List<String>> mapResult = new HashMap<String, List<String>>();

	/**
	 * list*list业务处理
	 * 
	 * @param mapQuerySeg
	 *            高级分析query分词的每一条分词
	 * @return list*list的结果
	 */
	public static List<String> getResults2(Map<String, String> mapQuerySeg) {

		List<String> results = new ArrayList<String>();

		String nlpAppWSResult = "";

		List<String> nlpAppWSList = null;

		// 对每一个分词进行处理
		for (Map.Entry<String, String> entry : mapQuerySeg.entrySet()) {
			// 临时list集合
			List<String> temp = new ArrayList<String>();

			if (mapResult.containsKey(entry.getKey())) {
				nlpAppWSList = mapResult.get(entry.getKey());
			} else {
				// 调用简要分析
				nlpAppWSResult = KAnalyzeResult.getNLPAppWSClientResult(entry
						.getKey());

				nlpAppWSList = getNLPAppWSList(nlpAppWSResult);

				/*
				 * if(!CollectionUtils.isEmpty(nlpAppWSList)){
				 * mapResult.put(entry.getKey(), nlpAppWSList); }else{
				 * List<String> ls = new ArrayList<String>();
				 * ls.add(entry.getValue()); mapResult.put(entry.getKey(), ls);
				 * }
				 */
				// mapResult.put(entry.getKey(), (List<String>)
				// (CollectionUtils.isEmpty(nlpAppWSList) ? new
				// ArrayList<String>().add(entry.getValue()) : nlpAppWSList));
			}

			// if(CollectionUtils.isNotEmpty(nlpAppWSList))
			if (nlpAppWSList.size() > 0) {
				mapResult.put(entry.getKey(), nlpAppWSList);
				for (String str : nlpAppWSList) {
					results.add(str + "[子句]");
				}
			} else {
				nlpAppWSList.add(entry.getValue());
				// if(CollectionUtils.isNotEmpty(results))
				if (results.size() > 0) {
					results.add("*" + entry.getValue());
				} else {
					results.add(entry.getValue() + "*");
				}
			}

			int oneSize = results.size();
			int twoSize = oneSize - nlpAppWSList.size();
			temp = results;
			results = new ArrayList<String>();
			for (int i = 0; i < twoSize; i++) {
				for (int j = twoSize; j < oneSize; j++) {
					results.add(temp.get(i) + temp.get(j));
				}
			}
			// if(CollectionUtils.isEmpty(results))
			if (results.size() == 0) {
				results = temp;
			}
		}

		List<String> resultsList = new ArrayList<String>();
		for (String result : results) {
			resultsList.add(result + "#无序#编者=\"auto\"");
		}
		return resultsList;
	}

	/**
	 * 
	 * @param lists
	 * @return
	 */
	public static Map<String, List<String>> getResults2(List<String> lists) {
		// List<String> results = new ArrayList<String>();

		Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();

		// 高级分析返回的字符串
		String nlpAppWebResult = "";

		List<String> nlpAppWebList = new ArrayList<String>();

		for (int i = 0; i < lists.size(); i++) {
			// 调用高级分析接口
			nlpAppWebResult = KAnalyzeResult.getNLPAppWebClientResult(lists
					.get(i));
			// 获取根据高级分析 处理返回的List集合
			nlpAppWebList = stringToList(nlpAppWebResult);

			for (int j = 0; j < nlpAppWebList.size(); j++) {
				String[] str = nlpAppWebList.get(j).split("\\)  ");
				Map<String, String> mapString = new LinkedHashMap<String, String>();
				for (int a = 0; a < str.length; a++) {
					String[] s = str[a].split("\\(");
					mapString.put(s[0], StringUtils.replace(s[1], "!", ""));
				}
				List<String> result = getResults2(mapString);
				// 返回Map，用这个
				map.put(nlpAppWebList.get(j), result);
				// 返回List，用这个
				/*
				 * for(String s : result){ results.add(s); }
				 */
			}
		}
		return map;
	}

	/**
	 * 
	 * @param listQuerySeg
	 *            高级分析每条分词的集合
	 * @return
	 */
	public static List<String> getResults3_1(List<String> listQuerySeg) {

		List<String> results = new ArrayList<String>();

		String nlpAppWSResult = "";

		List<String> nlpAppWSList = null;

		for (int a = 0; a < listQuerySeg.size(); a++) {
			// 临时list集合
			List<String> temp = new ArrayList<String>();
			// listQuerySeg集合的偶数下标为key(表示简要分析输入框的值);如果简要分析搜不到结果就到 集合的 奇数下标取
			if (a % 2 == 0) {
				if (mapResult.containsKey(listQuerySeg.get(a))) {
					nlpAppWSList = mapResult.get(listQuerySeg.get(a));
				} else {
					// 调用简要分析
					nlpAppWSResult = KAnalyzeResult
							.getNLPAppWSClientResult(listQuerySeg.get(a));

					nlpAppWSList = getNLPAppWSList(nlpAppWSResult);

				}
				// if(!CollectionUtils.isEmpty(nlpAppWSList))
				if (nlpAppWSList.size() == 0)

				{
					mapResult.put(listQuerySeg.get(a), nlpAppWSList);
					for (String str : nlpAppWSList) {
						results.add(str + "[子句]");
					}
				} else {
					if ((a + 1) < listQuerySeg.size()) {
						nlpAppWSList.add(listQuerySeg.get(a + 1));
						mapResult.put(listQuerySeg.get(a), nlpAppWSList);

						// if(CollectionUtils.isNotEmpty(results))
						if (results.size() > 0)

						{
							results.add("*" + listQuerySeg.get(a + 1));
						} else {
							results.add(listQuerySeg.get(a + 1) + "*");
						}
					}
				}
				int oneSize = results.size();
				int twoSize = oneSize - nlpAppWSList.size();
				temp = results;
				results = new ArrayList<String>();
				for (int i = 0; i < twoSize; i++) {
					for (int j = twoSize; j < oneSize; j++) {
						results.add(temp.get(i) + temp.get(j));
					}
				}
				// if(CollectionUtils.isEmpty(results))
				if (results.size() == 0)

				{
					results = temp;
				}
			}
		}

		List<String> resultsList = new ArrayList<String>();
		// resultsList.add(segs);
		for (String result : results) {
			resultsList.add(result + "#无序#编者=\"auto\"");
		}
		return resultsList;
	}

	/**
	 * 
	 * @param lists
	 *            读取txt文档返回list集合
	 * @return
	 */
	public static Map<String, List<String>> getResults3(List<String> lists) {
		// List<String> results = new ArrayList<String>();

		Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();

		// 高级分析返回的字符串
		String nlpAppWebResult = "";
		// 存放高级分析集合
		List<String> nlpAppWebList = new ArrayList<String>();
		// 遍历txt文本文档集合
		for (int i = 0; i < lists.size(); i++) {
			// String segs = lists.get(i);

			// 调用高级分析接口
			nlpAppWebResult = KAnalyzeResult.getNLPAppWebClientResult(lists
					.get(i));
			// 获取根据高级分析 处理返回的List集合
			nlpAppWebList = stringToList(nlpAppWebResult);
			// 输出每条高级分析的输入框的值到文本文档
			List<String> oneList = new ArrayList<String>();
			oneList.add("	");
			map.put(lists.get(i), oneList);
			// 遍历高级分析list的集合
			for (int j = 0; j < nlpAppWebList.size(); j++) {
				// 把每一条分词以右括号空格分割
				String[] str = nlpAppWebList.get(j).split("\\)  ");
				// Map<String, String> mapString = new LinkedHashMap<String,
				// String>();//因为高级分析也会有重复的分词，map的key不能重复
				// 存放高级分析分词的原始数据
				List<String> list = new ArrayList<String>();
				for (int a = 0; a < str.length; a++) {
					String[] s = str[a].split("\\(");
					list.add(s[0]);
					list.add(StringUtils.replace(s[1], "!", ""));
					// mapString.put(s[0], StringUtils.replace(s[1], "!", ""));
				}
				// List<String> result = getResults3_1(list, segs);
				List<String> result = getResults3_1(list);
				// 返回Map，用这个
				map.put(nlpAppWebList.get(j), result);
				// 返回List，用这个
				/*
				 * for(String s : result){ results.add(s); }
				 */
			}
		}
		return map;
	}

	/**
	 * 
	 * @param listQuerySeg
	 *            高级分析每条分词的集合
	 * @return
	 */
	public static List<String> getResults4_1(List<String> listQuerySeg,
			Map<String, List<String>> mapResult, String channel,
			String business, String autor) {

		List<String> results = new ArrayList<String>();

		String nlpAppWSResult = "";

		List<String> nlpAppWSList = null;

		for (int a = 0; a < listQuerySeg.size(); a++) {
			// 临时list集合
			List<String> temp = new ArrayList<String>();
			// listQuerySeg集合的偶数下标为key(表示简要分析输入框的值);如果简要分析搜不到结果就到 集合的 奇数下标取
			if (a % 2 == 0) {
				if (mapResult.containsKey(listQuerySeg.get(a))) {
					nlpAppWSList = mapResult.get(listQuerySeg.get(a));
				} else {
					// 调用简要分析
					nlpAppWSResult = KAnalyzeResult.getNLPAppWSClientResult(
							listQuerySeg.get(a), channel, business);

					// System.out.println(listQuerySeg.get(a) + "    简要分析：" +
					// nlpAppWSResult);
					nlpAppWSList = getNLPAppWSList(nlpAppWSResult);

				}
				// if(!CollectionUtils.isEmpty(nlpAppWSList))
				if (nlpAppWSList.size() == 0) {
					mapResult.put(listQuerySeg.get(a), nlpAppWSList);
					for (String str : nlpAppWSList) {
						results.add(str + "子句*");
					}
				} else {
					System.out.println("简要分析为空");
					if ((a + 1) < listQuerySeg.size()) {
						nlpAppWSList.add(listQuerySeg.get(a + 1));
						mapResult.put(listQuerySeg.get(a), nlpAppWSList);

						// if(CollectionUtils.isNotEmpty(results))
						if (results.size() > 0)

						{
							results.add("*" + listQuerySeg.get(a + 1));
						} else {
							results.add(listQuerySeg.get(a + 1) + "*");
						}
					}
				}
				int oneSize = results.size();
				int twoSize = oneSize - nlpAppWSList.size();
				temp = results;
				results = new ArrayList<String>();
				for (int i = 0; i < twoSize; i++) {
					for (int j = twoSize; j < oneSize; j++) {
						results.add(temp.get(i) + temp.get(j));
					}
				}
				// if(CollectionUtils.isEmpty(results))
				if (results.size() == 0) {
					results = temp;
				}
			}
		}

		List<String> resultsList = new ArrayList<String>();
		// resultsList.add(segs);
		for (String result : results) {
			String wordpat = result + "#无序#" + autor;
			resultsList.add(wordpat.replace("*#", "#"));
		}
		return resultsList;
	}

	/**
	 * 分别调用NLP高级分析简要分析 封装成 MAP 高级分析分词结果={子句词模,...}
	 */
	public static Map<String, List<String>> getWordpatMap(List<String> lists,
			String channel, String business, String serviceids, String autor) {
		Map<String, List<String>> mapResult = new HashMap<String, List<String>>();

		Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();

		// 高级分析返回的字符串
		String nlpAppWebResult = "";
		// 存放高级分析集合
		List<String> nlpAppWebList = new ArrayList<String>();
		// 遍历txt文本文档集合
		for (int i = 0; i < lists.size(); i++) {

			// 调用高级分析接口
			nlpAppWebResult = KAnalyzeResult.getNLPAppWebClientResult(lists
					.get(i), business, serviceids);
			// 获取根据高级分析 处理返回的List集合
			nlpAppWebList = stringToList(nlpAppWebResult);
			// 输出每条高级分析的输入框的值到文本文档
			// oneList.add("	");
			// map.put(lists.get(i), oneList);
			// 遍历高级分析list的集合
			for (int j = 0; j < nlpAppWebList.size(); j++) {
				// 把每一条分词以右括号空格分割
				String[] str = nlpAppWebList.get(j).split("\\)  ");

				// 存放高级分析分词的原始数据
				List<String> list = new ArrayList<String>();
				for (int a = 0; a < str.length; a++) {
					String[] s = str[a].split("\\(");
					list.add(s[0]);
					list.add(StringUtils.replace(s[1], "!", ""));
				}
				List<String> result = getResults4_1(list, mapResult, channel,
						business, autor);
				// 返回Map，用这个
				map.put(nlpAppWebList.get(j), result);
			}
		}
		return map;
	}

	/**
	 * 分别调用NLP高级分析简要分析 封装成集合 {子句词模,...}
	 */
	public static List<String> getWordpatList(List<String> lists,
			String channel, String business, String serviceids, String autor) {
		Map<String, List<String>> mapResult = new HashMap<String, List<String>>();

		List<String> wordpatList = new ArrayList<String>();

		// 高级分析返回的字符串
		String nlpAppWebResult = "";
		// 存放高级分析集合
		List<String> nlpAppWebList = new ArrayList<String>();
		// 遍历txt文本文档集合
		for (int i = 0; i < lists.size(); i++) {

			// 调用高级分析接口
			nlpAppWebResult = KAnalyzeResult.getNLPAppWebClientResult(lists
					.get(i), business, serviceids);
			// 获取根据高级分析 处理返回的List集合
			nlpAppWebList = stringToList(nlpAppWebResult);
			// 遍历高级分析list的集合
			for (int j = 0; j < nlpAppWebList.size(); j++) {
				// 把每一条分词以右括号空格分割
				String[] str = nlpAppWebList.get(j).split("\\)  ");

				// 存放高级分析分词的原始数据
				List<String> list = new ArrayList<String>();
				for (int a = 0; a < str.length; a++) {
					String[] s = str[a].split("\\(");
					list.add(s[0]);
					list.add(StringUtils.replace(s[1], "!", ""));
				}
				List<String> result = getResults4_1(list, mapResult, channel,
						business, autor);

				wordpatList.addAll(result);
			}
		}
		return wordpatList;
	}

	/**
	 * 通用方法：把字符串(seg)以规定的字符(separatorChars)分割
	 * 
	 * @param seg
	 *            要处理的字符串 例："百思(!1),不得(!2),其解(!3)"
	 * @param separatorChars
	 *            以什么方式分割 例：以","分割
	 * @param searchString
	 *            字符串中包含的字符 例："!"
	 * @param replacement
	 *            要替换的字符 例：把"!"替换成空字符""
	 * @return
	 */
	public static List<String> getSplit(String seg, String separatorChars,
			String searchString, String replacement) {
		List<String> list = new ArrayList<String>();

		if (seg.contains(separatorChars)) {
			String[] str = StringUtils.split(seg, separatorChars);
			for (int i = 0; i < str.length; i++) {
				String text = str[i];
				if (text.contains(searchString)) {
					text = StringUtils.replace(text, searchString, replacement);
				}
				list.add(text);
			}
		}

		return list;
	}
}
