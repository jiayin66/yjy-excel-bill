package com.yjy.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson.JSON;
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
	
	public void readJiluTxt(MultipartFile txt, MultipartFile user, HttpServletResponse response, String type) {
		//1.解析用户拿到用户id集合。增加用户中不能重复的判断。
		List<User> userList = getTxt(user,User.class);
		List<String> userStrList=new ArrayList<String>();
		Set<String> ss=new HashSet<String>();
		
		for(User userModel:userList) {
			String name = userModel.getName();
			userStrList.add(name);
			ss.add(name);
		}
		if(userStrList.size()!=ss.size()) {
			throw new RuntimeException("全量用户中有重复数据，请核实并删除，保持数据准确性！");
		}
		//2.解析报账记录，拿到有效行
		List<String> txtRecordList=new ArrayList<String>();	
		 try {
			InputStreamReader is = new InputStreamReader(txt.getInputStream());
			BufferedReader bf = new BufferedReader(is);
			String readLine = bf.readLine();
			 while (readLine != null) {  
				 readLine = bf.readLine(); // 一次读入一行数据  
				 if(!StringUtils.isEmpty(readLine)) {
					 txtRecordList.add(readLine);
				 }
	            }  
		} catch (Exception e) {
			System.out.println("错误原因："+e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("读取txt数据失败");
		}
		excelCreate(response, type, userStrList, txtRecordList);
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
		System.out.println("本次判断总数："+txtRecordList.size());
		
		List<String> recodeStringList=new ArrayList<String>();
		for(UserRecord userRecord:txtRecordList) {
			recodeStringList.add(userRecord.getTxtRecord());
		}
		excelCreate(response, type, userStrList, recodeStringList);
	}

	private void excelCreate(HttpServletResponse response, String type, List<String> userStrList,
			List<String> txtRecordList) {
		//（1）最终结果
		List<RecodeModel> result=new ArrayList<RecodeModel>();
		//（2）这个表示中间数据，支持二次导入，为了查看缺失什么
		List<UserRecord> commenResult=new ArrayList<UserRecord>();
		for(int i=0;i<txtRecordList.size();i++) {
			String txtRecord = txtRecordList.get(i);
			System.out.println("第【"+(i+1)+"】条待判断的记录："+txtRecord);
			//【1】按照这个正则匹配过滤时间格式: 熊东飞(211435812) 11:51:27
			String patternTime =".*(\\d{1,2}:\\d{1,2}:\\d{1,2}).*";
			Matcher matcherTime = Pattern.compile(patternTime).matcher(txtRecord);
			if(matcherTime.find()) {
				System.out.println("-->无效过滤，此条记录是时间（两个冒号判断）"+txtRecord);
				continue;
			}
			//【1.0】按照这个正则匹配过滤时间格式:  2019-09-11 
			String patternDate =".*(\\d{4}-\\d{2}-\\d{2}).*";
			Matcher matchDate = Pattern.compile(patternDate).matcher(txtRecord);
			if(matchDate.find()) {
				System.out.println("-->无效过滤，此条记录是时间（日期格式）"+txtRecord);
				continue;
			}
		
			//【2】只匹配2个金额的数据: 聂鑫勇5.5 余额819下一位王亮明，李谨延 3  下一位/个 张志龙 余额 917
			//String patternTwoMatch ="\\D*(\\d+\\.?\\d*)[^0-9\\.]+(\\d+\\.?\\d*)\\D*";
			//此规则，开头必须有名字。末尾可以没名字
			String patternTwoMatch ="[^0-9\\.]*([0-9\\.]+)[^0-9\\.]+([0-9\\.]+)[^0-9\\.]*";
			Matcher matcherTwo = Pattern.compile(patternTwoMatch).matcher(txtRecord);
			if(matcherTwo.find()) {
				System.out.println("-->有效待处理，此条记录有两个金额："+txtRecord);
				//（2.1）把基础数据写这里，方便查看缺失又能重新导入
				commenResult.add(new UserRecord(txtRecord));
				//真正的处理数据
				RecodeModel recodeModel=getRecodeModel(txtRecord,matcherTwo,userStrList);
				if(recodeModel!=null) {
					//(1.1)添加有效数据
					result.add(recodeModel);
				}
				continue;
			}
			//【3】没有报余额，只报当前金额  ：熊东飞 4.5 下一个 魏冲，熊东飞 4.5
			//String patternOneMatch ="(\\D*)(\\d+\\.?\\d*)(\\D*)";
			String patternOneMatch ="([^0-9\\.]*)([0-9\\.]+)([^0-9\\.]*)";
			Matcher matcherOne = Pattern.compile(patternOneMatch).matcher(txtRecord);
			if(matcherOne.find()) {
				System.out.println("-->有效待处理，只有一个金额，没有报余额的记录："+txtRecord);
				RecodeModel recodeModel=setOneMatch(matcherOne,txtRecord,userStrList);
				result.add(recodeModel);
				continue;
			}
			System.err.println("-->无效过滤,没有被规则拦截的记录:"+txtRecord);
			
			//【4】匹配这种 ：严志凌下一位 镇阳 /下一个 （这个要在3之后，避免把正常数据拦截）
			String patternNext ="(\\D*)下一(\\D*)";
			Matcher matcherNext = Pattern.compile(patternNext).matcher(txtRecord);
			if(matcherNext.find()) {
				//(1.2)修改有效数据
				//(2.2)修改中间表数据
				System.out.println("-->有效待处理，此条记录是单纯的报下一位"+txtRecord);
				setNext(result,matcherNext,commenResult);
				continue;
			}
			
			
		}
		System.out.println("----------------------解析完成-------------------------------");
		System.out.println("完整记录个数:"+result.size()+",所有数据原顺序如下请核对：");
		for(RecodeModel recodeModel:result) {
			//对next中包含的各种乱数据处理
			String next = recodeModel.getNext();
			if(StringUtils.isNotBlank(next)) {
				try {
					next = subSpacialChar(next).replace("下一位", "").replace("下一个", "").replace("，", "").replace(",", "")
							.replace("个", "").replace("位", "").replace(" ", "");
				} catch (Exception e) {
					next=null;
				}
				recodeModel.setNext(next);
			}
			//对余额进行初始化
			if(recodeModel.getAllMoney()==null) {
				recodeModel.setAllMoney(new BigDecimal(0));
			}
			System.out.println(JSON.toJSONString(recodeModel));
		}
		//导出excle  1表示排序，0表示不排序，3表示要中间结果
		if("0".equals(type)) {
			excelTemplateExporter.exportExcel(result, "炊事班-记账", "记账", RecodeModel.class, "炊事班-记账-不排序.xls", response);
		}else if("1".equals(type)){
			Collections.sort(result,new Comparator() {
				//排序规则原因，如果没有就默认0把
				public int compare(Object o1, Object o2) {
					RecodeModel b=	(RecodeModel)o1;
					RecodeModel a=	(RecodeModel)o2;
					return	a.getAllMoney().subtract(b.getAllMoney()).intValue();
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
		String realName=getRealName(txtRecord,userStrList,one);
		recodeModel=new RecodeModel(realName,new BigDecimal(two),null,three);
		return recodeModel;
	}

	private void setNext(List<RecodeModel> result, Matcher matcherNext,List<UserRecord> commenResult) {
		String name = matcherNext.group(1);
		String next = matcherNext.group(2);
		String realName=null;
		for(RecodeModel recodeModel:result) {
			if(name.indexOf(recodeModel.getName())!=-1) {
				realName=recodeModel.getName();
				recodeModel.setNext(next);
				System.out.println("---->>成功设置下一位："+JSON.toJSONString(recodeModel));
			}
			
		}
		//下面是修改中间表的代码
		if(realName==null) {
			//说明上面的结果没有匹配上，本条数据是无效的
			return;
		}
		for(UserRecord userRecord:commenResult) {
			String txtRecord = userRecord.getTxtRecord();
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
		//【2.1】只取开头和末尾数据
		String patternMatch ="([^0-9\\.]*)[0-9\\.]+([^0-9\\.]+)[0-9\\.]+([^0-9\\.]*)";
		//String patternMatch ="(\\D*)\\d+\\.?\\d*([^0-9\\.]+)\\d+\\.?\\d*(\\D*)";
		Matcher matcher = Pattern.compile(patternMatch).matcher(txtRecord);
		if(!matcher.find()) {
			System.err.println("取开头和结尾没有匹配上："+txtRecord);
			return null;
		}
		String userName = matcher.group(1);
		String nexName = matcher.group(3);
		if(matcher.group(2).contains("下一")) {
			//下一等数据放在后面清除
			nexName=matcher.group(2);
			if(nexName.contains("余额")) {
				nexName=nexName.replace("余额", "").replace("余", "");
			}
		}
		
		//【2.2】必须跟数据库用户名保持一致，否则记账重复。下一位无需截取
		userName = getRealName(txtRecord, userStrList, userName);
		
		
		RecodeModel recodeModel = new RecodeModel(userName, money, allMoney, nexName);
		return recodeModel;
	}

	/**
	 * 根据提取的name和用户列表的name比对
	 * @param txtRecord
	 * @param userStrList
	 * @param userName
	 * @return
	 */
	private String getRealName(String txtRecord, List<String> userStrList, String userName) {
		if (StringUtils.isBlank(userName)) {
			throw new RuntimeException("报餐未报消费者名字！核实此条消费记录归属哪位人员，手动补充姓名重新尝试！记录为：" + txtRecord);
		}
		String result=null;
		for (String str : userStrList) {
			if (userName.indexOf(str) != -1) {
				result = str;
				break;
			}
		}
		if (result==null) {
			throw new RuntimeException("用户列表缺失此人，补充用户列表重新尝试!记录为：" + txtRecord);
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
			 params.setTitleRows(0);
			params.setNeedVerfiy(false);
			ExcelImportResult<T> resul = ExcelImportUtil.importExcelMore(file.getInputStream(), cla,
					params);
			List<T> list = resul.getList();
			return list;
		} catch (Exception e) {
			System.out.println("错误原因："+e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("读取:"+cla.getSimpleName()+"excel数据失败");
			
		}
		//return null;
	}

	/**
	 * 截取特殊63的字符
	 * @param str
	 * @return
	 */
	private String subSpacialChar(String str) {
		byte[] bytes=null;
		try {
			bytes = str.getBytes("gbk");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		int count=0;
		for(int i=0;i<bytes.length;i++) {
			if(bytes[i]==63) {
				count++;
			}
		}
		//不包含63的特殊字符
		if(count==0) {
			return str;
		}
		byte[]  result=new byte[bytes.length-count];
		//遇到63就自增2个
		int x=0;
		int y=0;
		for(int i=0;i<bytes.length;i++) {
			if(bytes[i]==63) {
				y++;
				continue;
			}
			result[x]=bytes[y];
			y++;
			x++;
		}
		String resultStr=null;
		try {
			resultStr = new String(result,"gbk");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return resultStr;
	}

	
	
	
}
