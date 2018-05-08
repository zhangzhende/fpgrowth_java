package com.zzd.myfpgrowth.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zzd.myfpgrowth.service.SolveService;
import com.zzd.myfpgrowth.util.UtilTool;

/**
 * FP-Tree：将事务数据表中的各个事务数据项按照支持度排序后， 把每个事务中的数据项按降序依次插入到一棵以 NULL为根结点的树中，
 * 同时在每个结点处记录该结点出现的支持度。
 * 
 * @author Administrator
 *
 */
public class FPTree implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(SolveService.class);
	private static final long serialVersionUID = 419861114319759608L;
	private FPNode root;
	private Map<Integer, FPNode> headerTable;
	private int numberOfTransactions;

	public FPTree() {
		this.root = new FPNode();// 创建了根节点
		this.headerTable = new HashMap<Integer, FPNode>();
		this.numberOfTransactions = 0;
	}

	/**
	 * 构建路径树，各节点，节点上的频数，各节点的邻节点【兄弟节点】 各项的数据按照频数降序排列， 然后按照顺序构建树，有同样结构的 会公用一个节点
	 * 【1，2，3】，【1，2，4】，会公用1，2，节点，两节点频数+1， 3，4出开始分叉，各节点频数为1 {1，2} {2，2}
	 * {3，1}，{4，1}
	 * 
	 * @param routes
	 *            原始矩阵
	 * @param frequencyMap
	 *            各项频数
	 * @param minCount
	 *            最小支持度
	 */
	public void buildTree(List<int[]> routes, Map<Integer, Integer> frequencyMap, int minCount) {
		for (int[] route : routes) {
			// 符合要求的，即频数大于最小支持度的数据
			List<Integer> qualified = new ArrayList<Integer>();
			// 按照频数降序排列
			route = UtilTool.sortTransaction(route, frequencyMap);
			for (Integer item : route) {
				// 筛选保留频数大于最小支持度的数据
				if (frequencyMap.get(item) >= minCount) {
					qualified.add(item);
				} else {
					break;
				}
			}
			// 拿到一条符合条件的路径，来画出树的一条路径
			buildRoute(qualified);
		}
	}

	/**
	 * 各项的数据按照频数降序排列(相当于一条路径)，然后按照顺序构建树， 有同样结构的会公用一个节点【1，2，3】，【1，2，4】
	 * ，会公用1，2，节点，两节点频数+1， 3，4出开始分叉，各节点频数为1
	 * 
	 * @param route
	 */
	public void buildRoute(List<Integer> route) {
		FPNode root = this.getRoot();
		for (Integer item : route) {
			FPNode childNode = root.getChild(item);
			if (childNode == null) {
				// 如果root节点没有对应子节点，则通过将item为key新建一个节点
				childNode = new FPNode(item, 1);
				// 将新建的节点作为node的子节点加入
				root.addChild(childNode);
				// 更新headerTable
				updateNeighbors(childNode);
			} else {
				// 如果有这个节点则将这个节点频数+1
				childNode.incrementCount(1);
			}
			// 将更新后的节点覆盖原节点，等待下一个item来更新节点
			root = childNode;
		}
		numberOfTransactions++;
	}

	/**
	 * 如果当前FPtree里面的headerTable里面没有该节点，则添加进去， 如果有则加入到headerTable的邻节点里面
	 * 
	 * @param node
	 */
	public void updateNeighbors(FPNode node) {
		FPNode head = headerTable.get(node.getItem());
		if (head == null) {
			// headerTable如果没有该节点，则将这个节点添加进去
			headerTable.put(node.getItem(), node);
		} else {
			// 如果head节点有邻节点，则循环深入到没有邻节点的项
			while (head.hasNeighbor()) {
				head = head.getNeighbor();
			}
			// 给这个没有邻节点的项添加该邻节点
			head.setNeighbor(node);
		}
	}

	/**
	 * 从headeryable中按照key获取他的支持度
	 * 
	 * @param item
	 * @return
	 */
	public int getSupportForItem(int item) {
		List<FPNode> nodesList = getNodes(item);
		int support = 0;
		for (FPNode fpNode : nodesList) {
			support += fpNode.getCount();
		}
//		logger.debug("key=" + item + ",support=" + support);
		return support;
	}

	/**
	 * 从headertable中拿出key为item的节点，包括他的邻节点neighbor
	 * 
	 * @param item
	 * @return
	 */
	public List<FPNode> getNodes(Integer item) {
		List<FPNode> nodesList = new ArrayList<>();
		FPNode node = headerTable.get(item);
		nodesList.add(node);
		while (node.hasNeighbor()) {
			node = node.getNeighbor();
			nodesList.add(node);
		}
		return nodesList;
	}

	/**
	 * 条件模式基：包含FP-Tree中与后缀模式一起出现的前缀路径的集合
	 * 条件模式基：顺着item的链表，找出所有包含item的前缀路径，这些前缀路径就是item的条件模式基
	 * 寻找包含item的所有路径
	 * 
	 * 从headertable中拿到key为item的节点们，然后将其作为子节点往上爬找，
	 * 直到根节点，这条路径倒序以后放入prefixPaths中，其中每一个都变成从根 节点往下的路径
	 * 
	 * @param item
	 * @return
	 */
	public List<List<FPNode>> getPrefixPaths(int item) {
		List<List<FPNode>> prefixPaths = new ArrayList<List<FPNode>>();
		for (FPNode node : getNodes(item)) {
			List<FPNode> path = new ArrayList<FPNode>();
			while (node != null && !node.isRootNode()) {
				path.add(node);
				node = node.getParent();
			}
			Collections.reverse(path);
			prefixPaths.add(path);
		}
		return prefixPaths;
	}

	public Map<Integer, List<FPNode>> getItems() {
		Map<Integer, List<FPNode>> itemMap = new HashMap<Integer, List<FPNode>>();
		for (Integer item : headerTable.keySet()) {
			itemMap.put(item, getNodes(item));
		}
		return itemMap;
	}

	public FPNode getRoot() {
		return root;
	}

	public void setRoot(FPNode root) {
		this.root = root;
	}

	public Map<Integer, FPNode> getHearderTable() {
		return headerTable;
	}

	public void setHearderTable(Map<Integer, FPNode> hearderTable) {
		this.headerTable = hearderTable;
	}

	public int getNumberOfTransactions() {
		return numberOfTransactions;
	}

	public void setNumberOfTransactions(int numberOfTransactions) {
		this.numberOfTransactions = numberOfTransactions;
	}

}
