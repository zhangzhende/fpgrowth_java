package com.zzd.myfpgrowth.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class FPNode implements Serializable {
	private static final long serialVersionUID = -1121610811642216117L;
	/**
	 * 本节点
	 */
	private int item;
	/**
	 * 数量
	 */
	private int count;
	/**
	 * 父节点
	 */
	private FPNode parent;
	/**
	 * 邻节点
	 */
	private FPNode neighbor;
	/**
	 * 子节点
	 */
	private Map<Integer, FPNode> childrens;
	/**
	 * 是否为根节点
	 */
	private boolean isRootNode;

	/**
	 * 创建根节点的构造方法
	 */
	public FPNode() {
		this.childrens = new HashMap<Integer, FPNode>();
		this.isRootNode = true;
	}

	/**
	 * 创建普通节点的构造方法
	 * 
	 * @param item
	 * @param count
	 */
	public FPNode(int item, int count) {
		this.item = item;
		this.count = count;
		this.childrens = new HashMap<Integer, FPNode>();
		this.isRootNode = false;
	}

	public int incrementCount(Integer count) {
		this.count += count;
		return this.count;
	}

	/**
	 * 按照key获取一个子节点
	 * 
	 * @param item
	 * @return
	 */
	public FPNode getChild(int item) {
		return this.childrens.get(item);
	}

	/**
	 * 如果当前节点不包含这个子节点，就把这个节点添加到子节点中 并把该子节点的父节点设置为当前节点
	 * 
	 * @param child
	 */
	public void addChild(FPNode child) {
		if (!this.childrens.containsKey(child.getItem())) {
			this.childrens.put(child.getItem(), child);
			child.setParent(this);
		}
	}

	public void removeChild(FPNode childNode) {
		this.childrens.remove(childNode.getItem());
		childNode.parent = null;
	}

	/**
	 * p判断是否有兄弟节点，有则TRUE，无则false
	 * 
	 * @return
	 */
	public boolean hasNeighbor() {
		return this.neighbor != null;
	}

	public boolean hasParent() {
		return this.parent != null;
	}

	@Override
	public String toString() {
		return this.toString(0);
	}

	public String toString(int indent) {
		String pre = "";
		for (int i = 0; i < indent; i++) {
			pre += " | ";
		}
		String str = pre + "(" + item + ":" + count + ")\n";
		for (FPNode child : childrens.values()) {
			str += child.toString(indent + 1);
		}
		return str;
	}
	
	
	
	public int getItem() {
		return item;
	}

	public void setItem(int item) {
		this.item = item;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public FPNode getParent() {
		return parent;
	}

	public void setParent(FPNode parent) {
		this.parent = parent;
	}

	public FPNode getNeighbor() {
		return neighbor;
	}

	public void setNeighbor(FPNode neighbor) {
		this.neighbor = neighbor;
	}

	public Map<Integer, FPNode> getChildrens() {
		return childrens;
	}

	public void setChildrens(Map<Integer, FPNode> childrens) {
		this.childrens = childrens;
	}

	public boolean isRootNode() {
		return isRootNode;
	}

	public void setRootNode(boolean isRootNode) {
		this.isRootNode = isRootNode;
	}

}
