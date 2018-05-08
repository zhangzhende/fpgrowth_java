package com.zzd.myfpgrowth.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.primitives.Ints;
import com.zzd.myfpgrowth.model.FPNode;
import com.zzd.myfpgrowth.model.FPTree;

public class UtilTool {
	private static final Logger logger = LoggerFactory.getLogger(UtilTool.class);

	/**
	 * 读取文件转换成类似矩阵的结构，一行一个整型数组
	 * 
	 * @param filename
	 * @param transactions
	 */
	public static void loadFileToArray(String filename, ArrayList<int[]> transactions) {
		File file = new File(filename);
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader reader = new BufferedReader(fileReader);
			String line;
			while ((line = reader.readLine()) != null) {
				transactions.add(parseStringToArray(line));
			}
			reader.close();
			fileReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 按照空格分割字符串，并转换为整型数组
	 * 
	 * @param str
	 * @return
	 */
	public static int[] parseStringToArray(String str) {
		String[] strArray = str.split(" ");
		int[] transaction = new int[strArray.length];
		for (int i = 0; i < strArray.length; i++) {
			transaction[i] = Integer.parseInt(strArray[i]);
		}
		return transaction;
	}

	/**
	 * 统计各数据的频数，出现次数 如数字1出现的次数，2出现的次数等
	 * 
	 * @param counts
	 * @param transactions
	 * @return
	 */
	public static Map<Integer, Integer> getFrequency(List<int[]> transactions) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int[] itemset : transactions) {
			for (int item : itemset) {
				Integer count = map.get(item);
				if (count == null)
					count = 0;
				count++;
				map.put(item, count);
			}
		}
		return map;
	}

	/**
	 * 将数组排序，按照频数降序排列
	 * 
	 * @param transaction
	 * @param oneItemsetCounts
	 * @return
	 */
	public static int[] sortTransaction(int[] transaction, Map<Integer, Integer> frequencyMap) {
		List<Integer> list = Ints.asList(transaction);// 数组转列表
		Collections.sort(list, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				// 返回值为int类型，大于0表示正序，小于0表示逆序
				return frequencyMap.get(o2) - frequencyMap.get(o1);
			}
		});// 按照频数降序排列
		return Ints.toArray(list);
	}

	/**
	 * 构建条件树 条件树：将条件模式基按照FP-Tree的构造原则形成的一个新的FP-Tree 然后删除小于最小支持度的节点，返回一个符合要求的条件树
	 * 
	 * @param paths
	 *            条件模式基：包含FP-Tree中与后缀模式一起出现的前缀路径的集合
	 * @param minSupport
	 *            最小支持度
	 * @return
	 */
	public static FPTree buildConditionalTree(List<List<FPNode>> paths, int minSupport) {
		FPTree tree = new FPTree();
		Set<Integer> items = new HashSet<Integer>();
		Integer conditionitem = null;
		// 构建fptree，此时的树只有叶子节点count有值，其他节点count为0
		for (List<FPNode> path : paths) {
			FPNode point = tree.getRoot();
			if (conditionitem == null) {
				conditionitem = path.get(path.size() - 1).getItem();
			}
			// 按照路径path，逐步构建fptree
			for (FPNode fpNode : path) {
				FPNode nextPoint = point.getChild(fpNode.getItem());
				if (nextPoint == null) {
					items.add(fpNode.getItem());
					int count;
					if (fpNode.getItem() == conditionitem) {
						count = fpNode.getCount();
					} else {
						count = 0;
					}
					nextPoint = new FPNode(fpNode.getItem(), count);
					point.addChild(nextPoint);
					tree.updateNeighbors(nextPoint);
				}
				point = nextPoint;
			}
		}
		for (List<FPNode> path : tree.getPrefixPaths(conditionitem)) {
			Integer count = path.get(path.size() - 1).getCount();
			for (int i = path.size() - 1; i >= 0; i--) {
				FPNode node = path.get(i);
				node.incrementCount(count);
			}
		}
		for (Integer item : items) {
			int support = 0;
			for (FPNode node : tree.getNodes(item)) {
				support += node.getCount();
			}
			for (FPNode node : tree.getNodes(item)) {
				if (support < minSupport) {
					if (node.hasParent()) {
						node.getParent().removeChild(node);
					}
				}
			}
		}
		for (FPNode node : tree.getNodes(conditionitem)) {
			if (node.hasParent()) {
				node.getParent().removeChild(node);
			}
		}
		return tree;
	}

	/**
	 * 查找符合要求的关联集合
	 * 
	 * @param tree
	 *            初步构建的FPTree
	 * @param suffix
	 * 
	 * @param minSupport
	 *            最小支持度
	 * @return
	 */
	public static List<List<Integer>> findFrequentItemsetWithSuffix(FPTree tree, List<Integer> suffix, int minSupport) {
		JSONObject json = new JSONObject();
		List<List<Integer>> frequentItemset = new ArrayList<List<Integer>>();
		for (Integer item : tree.getItems().keySet()) {
			int support = tree.getSupportForItem(item);
			if (support >= minSupport && !suffix.contains(item)) {
				List<Integer> found = new ArrayList<Integer>();
				found.addAll(suffix);
				found.add(item);
				frequentItemset.add(found);
				List<List<FPNode>> paths = tree.getPrefixPaths(item);
				FPTree conditionalTree = UtilTool.buildConditionalTree(paths, minSupport);
				List<List<Integer>> set = findFrequentItemsetWithSuffix(conditionalTree, found, minSupport);
				frequentItemset.addAll(set);
				logger.info("item=" + item + "   set" + json.toJSONString(set));
				logger.info("frequentItemset=" + json.toJSONString(frequentItemset));
			}
		}
		return frequentItemset;
	}

	/**
	 * 创建随机序列
	 * 
	 * @param col
	 *            随机数个数
	 * @param max
	 *            随机数最大值
	 * @return
	 */
	public static List<Integer> getRandomNumToList(Integer col, Integer max) {
		List<Integer> list = new ArrayList<Integer>();
		Random rand = new Random();
		for (int i = 0; i < col; i++) {
			int num = rand.nextInt(max);
			while (list.size() < col && list.contains(num)) {
				num = rand.nextInt(max);
			}
			list.add(num);
		}
		return list;

	}

}
