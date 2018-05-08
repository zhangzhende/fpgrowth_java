package com.zzd.myfpgrowth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zzd.myfpgrowth.service.SolveService;

@RestController
public class Controller {
	@Autowired
	private SolveService solveService;

	/**
	 * 创建一个随机矩阵,取值为max内的整数 row*col
	 * 
	 * @param row
	 * @param col
	 * @param max
	 * @return
	 */
	@RequestMapping(value = "/makeTestFile")
	public Object makeATestFile(@RequestParam(value = "row", required = true) Integer row,
			@RequestParam(value = "col", required = true) Integer col,
			@RequestParam(value = "max", required = true) Integer max) {

		if (col > max / 2) {
			return "所选列数过多或者随机范围太小";
		}
		String filname = solveService.makeFile(row, col, max);
		return "filname=" + filname;
	}

	/**
	 * 获取结果
	 * 
	 * @param filename
	 *            文件名
	 * @param minSupport
	 *            最小支持度
	 * @return
	 */
	@RequestMapping(value = "/getResult")
	public Object solveByPath(@RequestParam(value = "filename") String filename,
			@RequestParam(value = "minSupport") Double minSupport) {
		String result = solveService.solveByPath(filename, minSupport);
		return result;
	}
}
