package com.yjy.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Text;
import com.yjy.model.RecodeModel;
import com.yjy.model.User;
import com.yjy.model.UserRecord;
import com.yjy.service.ExcelService;
import com.yjy.util.ExcelTemplateExporter;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
@Service
public class ExcelServiceImpl implements ExcelService{
	
	private volatile List<String> userList;
	
	@Autowired
	private ExcelTemplateExporter excelTemplateExporter;


	public void exportExcel(HttpServletResponse response) {
		List<User> userList=new ArrayList<User>();
		userList.add(new User(1,"张三"));
		//第二个参数是抬头名称
		excelTemplateExporter.exportExcel(userList, "炊事班-记账", "记账", User.class, "炊事班-记账.xls", response);
		
	}

	public void readExcel(MultipartFile file) {
		try {
			ImportParams params = new ImportParams();
			params.setNeedVerfiy(true);
			ExcelImportResult<User> resul = ExcelImportUtil.importExcelMore(file.getInputStream(), User.class,
					params);
			List<User> list = resul.getList();
			System.out.println(JSON.toJSONString(list));
			for(User user:list) {
				userList.add(user.getName());
			}
		} catch (Exception e) {
			
		}
		
	}

	public void readJiluExcel(MultipartFile record,MultipartFile user,HttpServletResponse response,String type) {
		//1.解析用户拿到用户id集合
		List<User> userList = getTxt(user,User.class);
		List<String> userStrList=new ArrayList<String>();
		for(User userModel:userList) {
			userStrList.add(userModel.getName());
		}
		//2.解析报账记录，拿到有效行
		List<UserRecord> txtRecordList = getTxt(record,UserRecord.class);
		System.out.println("读取excel拿到的数据"+JSON.toJSONString(txtRecordList));
		List<String> payLoadTwo=new ArrayList<String>();
		List<String> payLoadOne=new ArrayList<String>();
		//（1）最终结果
		List<RecodeModel> result=new ArrayList<RecodeModel>();
		//（2）这个表示中间数据，支持二次导入，为了查看缺失什么
		List<UserRecord> commenResult=new ArrayList<UserRecord>();
		for(int i=0;i<txtRecordList.size();i++) {
			String txtRecord = txtRecordList.get(i).getTxtRecord();
			System.out.println("待判断的记录："+txtRecord);
			//【1】按照这个正则匹配过滤时间格式: 熊东飞(211435812) 11:51:27
			String patternTime =".*(\\d{1,2}:\\d{1,2}:\\d{1,2}).*";
			Matcher matcherTime = Pattern.compile(patternTime).matcher(txtRecord);
			if(matcherTime.find()) {
				System.out.println("【1】此条记录是时间（两个冒号判断），过滤这条记录"+txtRecord);
				continue;
			}
		
			//【2】只匹配2个金额的数据: 聂鑫勇5.5 余额819下一位王亮明，李谨延 3  下一位/个 张志龙 余额 917
			String patternTwoMatch ="\\D*(\\d+\\.?\\d*)[^0-9\\.]+(\\d+\\.?\\d*)\\D*";
			Matcher matcherTwo = Pattern.compile(patternTwoMatch).matcher(txtRecord);
			if(matcherTwo.find()) {
				System.out.println("此条记录是有效记录（两个金额）："+txtRecord);
				//（2.1）把基础数据写这里，方便查看缺失又能重新导入
				commenResult.add(new UserRecord(txtRecord));
				payLoadTwo.add(txtRecord);
				RecodeModel recodeModel=getRecodeModel(txtRecord,matcherTwo,userStrList);
				if(recodeModel!=null) {
					//(1.1)添加有效数据
					result.add(recodeModel);
				}
				continue;
			}
			//【3】匹配这种 ：严志凌下一位 镇阳
			String patternNext ="(\\D*)下一位(\\D*)";
			Matcher matcherNext = Pattern.compile(patternNext).matcher(txtRecord);
			if(matcherNext.find()) {
				//(1.2)修改有效数据
				//(2.2)修改中间表数据
				setNext(result,matcherNext,commenResult);
				continue;
			}
			
			//【4】没有报余额，只报当前金额  ：熊东飞 4.5 下一个 魏冲
			String patternOneMatch ="(\\D*)(\\d+\\.?\\d*)(\\D*)";
			Matcher matcherOne = Pattern.compile(patternOneMatch).matcher(txtRecord);
			if(matcherOne.find()) {
				System.out.println("只有一个金额的记录（一个金额）"+txtRecord);
				payLoadOne.add(txtRecord);
				RecodeModel recodeModel=setOneMatch(matcherOne,txtRecord,userStrList);
				result.add(recodeModel);
				continue;
			}
			System.err.println("没有被规则拦截的无效记录:"+txtRecord);
		}
		//System.out.println("两个金额的有效记录"+JSON.toJSONString(payLoadTwo));
		//System.out.println(payLoadTwo.size());
		//System.out.println("一个金额的有效记录"+JSON.toJSONString(payLoadOne));
		System.out.println("完整记录个数:"+result.size()+",所有记录列表如下：");
		for(RecodeModel RecodeModel:result) {
			System.out.println(JSON.toJSONString(RecodeModel));
		}
		//导出excle  1表示排序，0表示不排序，3表示要中间结果
		if("0".equals(type)) {
			excelTemplateExporter.exportExcel(result, "炊事班-记账", "记账", RecodeModel.class, "炊事班-记账-不排序.xls", response);
		}else if("1".equals(type)){
			Collections.sort(result,new Comparator() {

				public int compare(Object o1, Object o2) {
					RecodeModel b=	(RecodeModel)o1;
					RecodeModel a=	(RecodeModel)o2;
					return a.getAllMoney().subtract(b.getAllMoney()).intValue();
				}
				
			});
			excelTemplateExporter.exportExcel(result, "炊事班-记账", "记账", RecodeModel.class, "炊事班-记账-排序.xls", response);
		}else {
			excelTemplateExporter.exportExcel(commenResult, null, "记账", UserRecord.class, "炊事班-记账-基础.xls", response);
		}
		
		
	
	}
	
	
	private RecodeModel setOneMatch(Matcher matcherOne, String txtRecord,List<String> userStrList) {
		RecodeModel recodeModel=null;
		String one = matcherOne.group(1);
		String two = matcherOne.group(2);
		String three = matcherOne.group(3);
		if(three.contains("下一位")) {
			three=three.replace("下一位", "");
		}
		if(three.contains("下一个")) {
			three=three.replace("下一个", "");
		}
		for(String userName:userStrList) {
			if(one.contains(userName)) {
				recodeModel=new RecodeModel(userName,new BigDecimal(two),null,three);
				return recodeModel;
			}
		}
		return null;
	}

	private void setNext(List<RecodeModel> result, Matcher matcherNext,List<UserRecord> commenResult) {
		String name = matcherNext.group(1);
		String next = matcherNext.group(2);
		String realName=null;
		for(RecodeModel recodeModel:result) {
			if(name.indexOf(recodeModel.getName())!=-1) {
				realName=recodeModel.getName();
				recodeModel.setNext(next);
				System.out.println("成功设置下一位："+JSON.toJSONString(recodeModel));
			}
			
		}
		//修改中间表
		if(realName==null) {
			//说明上面的结果没有匹配上，本条数据是无效的
			return;
		}
		for(UserRecord userRecord:commenResult) {
			String txtRecord = userRecord.getTxtRecord();
			//为了避免别人的下一位也有这个名字再次匹配
			/*String patternPreName ="(\\D*)\\d+.*";
			Matcher matcherPreName = Pattern.compile(patternPreName).matcher(txtRecord);
			String preName = matcherPreName.group(1);*/
			String preName=txtRecord.substring(0, 9);
			if(preName.contains(realName)) {
				userRecord.setTxtRecord(txtRecord+"|补|"+next);
			}
		}
	}

	/**
	 * 两个参数的匹配
	 * eg：
	 * "徐雷?9.5?余额248?下一位皮家鑫","皮家鑫?7.5?余额241?下一位?罗亚丽","罗亚丽?7.5?余额?233.81?下一位?韦聪","罗冲?7.5?余173.81?下一位?熊宾","张雅雯?7.5?余额181?下一位?罗冲","熊宾?7.5余额166?下一位?王翼","王翼?5.5?余额160?下一位聂鑫勇","童敏?8.5?余额188?下一位张雅雯","梁敏?14.5?余额203.5?下一位龚韩壮","韦聪?15.5?余额?218?下一位?梁敏","龚韩壮??6.5??余额?196?下一位?童敏","许小花??7.5??余额?145?下一位?左成元","左成元13余额132.8下一位任为","聂鑫勇7.5?余额153?下一位?许小花","任为?8.5?余额124.31?下一位?童贝","童贝?9?余额113?下一位?韦聪","贺俊凯?6?余额?109?下一位?苗刚","更正：余额115","李佳豪?2.5?余额?83.81?下一页?雷宇恒","雷宇恒?1?余额?82.81?下一位?祁程畅","雷哲?2.5?余额?86.31下一位李佳豪","祁程畅??14??余额???68.81??下一位null","苗刚?5?余额?104?下一位?韦聪","你们之间缺少一两个人报账，中间亏损15.5元"]
24
	 * @param txtRecord
	 * @return
	 */
	private RecodeModel getRecodeModel(String txtRecord,Matcher matcherOne,List<String> userStrList) {
		BigDecimal money =new BigDecimal(matcherOne.group(1));
		BigDecimal allMoney = new BigDecimal(matcherOne.group(2));
		String userName=null;
		String nexName=null;
		//【2.1】只取开头和末尾数据
		String patternMatch ="(\\D*)\\d+\\.?\\d*(\\D*)\\d+\\.?\\d*(\\D*)";
		Matcher matcher = Pattern.compile(patternMatch).matcher(txtRecord);
		if(!matcher.find()) {
			System.err.println("取开头和结尾没有匹配上："+txtRecord);
			return null;
		}
		System.out.println("成功读取金额之前的人和余额之后的人:"+txtRecord);
		String one = matcher.group(1);
		String two = matcher.group(3);
		if(matcher.group(2).contains("下一位")) {
			two=matcher.group(2).replace("下一位", "");
			if(two.contains("余额")) {
				two=two.replace("余额", "");
			}
		}
		
		//【2.2】必须跟数据库用户名保持一致，否则记账重复。下一位无需截取
		if (StringUtils.isNotBlank(one)) {
			for(String str:userStrList) {
				if(one.indexOf(str)!=-1) {
					userName=str;
					break;
				}
				
			}
			if(userName==null) {
				System.out.println("无法找到报账人，略过本次记录："+txtRecord);
				return null;
			}
			System.out.println("截取到吃饭人为：" + userName);
		}
		if (StringUtils.isNotBlank(two)) {
			if (two.contains("下一位")) {
				two = two.replace("下一位", "");
			}
			nexName = two.trim();
		}
		System.out.println("截取到下一位为：" + nexName);

		RecodeModel recodeModel = new RecodeModel(userName, money, allMoney, nexName);
		System.out.println("本次完整的记录为："+JSON.toJSONString(recodeModel));
		return recodeModel;
	}

	public static String removeSpecilChar(String str){
		String result = "";
		if(null != str){
		Pattern pat = Pattern.compile("\\s*|\n|\r|\t");
		Matcher mat = pat.matcher(str);
		result = mat.replaceAll("");
		}
		return result;
	}
	/**
	 * 通用的读取excle方法
	 * @param file
	 * @param cla
	 * @return
	 */
	private <T> List<T> getTxt(MultipartFile file,Class cla){
		try {
			ImportParams params = new ImportParams();
			params.setNeedVerfiy(true);
			ExcelImportResult<T> resul = ExcelImportUtil.importExcelMore(file.getInputStream(), cla,
					params);
			List<T> list = resul.getList();
			return list;
		} catch (Exception e) {
			
		}
		return null;
	}

}
