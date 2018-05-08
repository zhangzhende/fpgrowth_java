package com.zzd.myfpgrowth.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.zzd.myfpgrowth.model.FPTree;
import com.zzd.myfpgrowth.util.Constant;
import com.zzd.myfpgrowth.util.UtilTool;

@Service
public class SolveService {
	private static final Logger logger = LoggerFactory.getLogger(SolveService.class);

	@SuppressWarnings("static-access")
	public String  solveByPath(String filename, double minSupport) {
		logger.info("get the filename:\n " + filename + ",\n minSupport: " + minSupport);
		JSONObject json = new JSONObject();
		ArrayList<int[]> transactions = new ArrayList<int[]>();
		// 加载文件,将数据转化到transactions中
		UtilTool.loadFileToArray(filename, transactions);
		logger.debug("transactions:" + json.toJSONString(transactions));
		// 获取各项频数
		Map<Integer, Integer> frequencyMap = UtilTool.getFrequency(transactions);
		// 获取最小支持度
		int count = (int) (minSupport * transactions.size());
		FPTree tree = new FPTree();
		tree.buildTree(transactions, frequencyMap, count);
		List<List<Integer>> List = UtilTool.findFrequentItemsetWithSuffix(tree, new ArrayList<Integer>(), count);
		logger.info("result:\n" + json.toJSONString(List));
		return json.toJSONString(List);
	}

	public String makeFile(Integer row, Integer col, Integer max) {
		String filename = Constant.BASEPATH + new Date().getTime()+".txt";
		List<List<Integer>> list = new ArrayList<List<Integer>>();
		for (int i = 0; i < row; i++) {
			List<Integer> intlist = UtilTool.getRandomNumToList(col, max);
			list.add(intlist);
		}
		File file = new File(filename);
		FileWriter fileWriter = null;
		BufferedWriter writer = null;
		try {
			fileWriter = new FileWriter(file);
			writer = new BufferedWriter(fileWriter);
			for (List<Integer> intlist : list) {
				StringBuilder builder=new StringBuilder();
				for (Integer integer : intlist) {
					builder.append(integer+" ");
				}
				writer.write(builder.toString().trim());
				writer.newLine();
			}
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				writer.close();
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return filename;
	}
	/*
	 * public static void main(String[] args) { String
	 * filename="D:\\mywork\\data\\fpgrowth\\fpdata.txt"; double minSupport=0.5;
	 * solveByPath(filename, minSupport); }
	 */
}
