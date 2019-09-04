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
		userList.add(new User(1,"����"));
		//�ڶ���������̧ͷ����
		excelTemplateExporter.exportExcel(userList, "���°�-����", "����", User.class, "���°�-����.xls", response);
		
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
		//1.�����û�
		List<User> userList = getTxt(user,User.class);
		List<String> userStrList=new ArrayList<String>();
		for(User userModel:userList) {
			userStrList.add(userModel.getName());
		}
		//2.������¼
		List<UserRecord> txtRecordList = getTxt(record,UserRecord.class);
		System.out.println("��ȡexcel�õ�������"+JSON.toJSONString(txtRecordList));
		List<String> payLoadTwo=new ArrayList<String>();
		List<String> payLoadOne=new ArrayList<String>();
		//���ս��
		List<RecodeModel> result=new ArrayList<RecodeModel>();
		//�����ʾ�м����ݣ�֧�ֶ��ε��룬Ϊ�˲鿴ȱʧʲô
		List<UserRecord> commenResult=new ArrayList<UserRecord>();
		for(UserRecord userRecord:txtRecordList) {
			String txtRecord = userRecord.getTxtRecord();
			System.out.println("���жϵļ�¼��"+txtRecord);
			//��1�������������ƥ�����ʱ���ʽ
			String patternTime =".*(\\d+:\\d+:\\d+).*";
			Matcher matcherTime = Pattern.compile(patternTime).matcher(txtRecord);
			if(matcherTime.find()) {
				System.out.println("��1��������¼��ʱ�䣨����ð���жϣ�������������¼"+txtRecord);
				continue;
			}
		
			//��2��ֻƥ��2����������
			String patternTwoMatch ="\\D*(\\d+\\.?\\d*)\\D*(\\d+\\.?\\d*)\\D*";
			Matcher matcherTwo = Pattern.compile(patternTwoMatch).matcher(txtRecord);
			if(matcherTwo.find()) {
				System.out.println("������¼����Ч��¼����������"+txtRecord);
				//�ѻ�������д�������鿴ȱʧ�������µ���
				commenResult.add(new UserRecord(txtRecord));
				payLoadTwo.add(txtRecord);
				RecodeModel recodeModel=getRecodeModel(txtRecord,matcherTwo,userStrList);
				if(recodeModel!=null) {
					result.add(recodeModel);
				}
				continue;
			}
			//��3��û�б���ֻ����ǰ���
			String patternOneMatch =".*(\\d+(\\.)?\\d*).*";
			Matcher matcherOne = Pattern.compile(patternOneMatch).matcher(txtRecord);
			if(matcherOne.find()) {
				System.out.println("ֻ��һ�����ļ�¼��һ����"+txtRecord);
				payLoadOne.add(txtRecord);
				continue;
			}
			System.out.println("û�б��������ص���Ч��¼"+txtRecord);
		}
		System.out.println("����������Ч��¼"+JSON.toJSONString(payLoadTwo));
		System.out.println(payLoadTwo.size());
		System.out.println("һ��������Ч��¼"+JSON.toJSONString(payLoadOne));
		System.out.println("������¼����"+result.size());
		for(RecodeModel RecodeModel:result) {
			System.out.println(JSON.toJSONString(RecodeModel));
		}
		//����excle  1��ʾ����0��ʾ������3��ʾҪ�м���
		if("0".equals(type)) {
			excelTemplateExporter.exportExcel(result, "���°�-����", "����", RecodeModel.class, "���°�-����-������.xls", response);
		}else if("1".equals(type)){
			Collections.sort(result,new Comparator() {

				public int compare(Object o1, Object o2) {
					RecodeModel b=	(RecodeModel)o1;
					RecodeModel a=	(RecodeModel)o2;
					return a.getAllMoney().subtract(b.getAllMoney()).intValue();
				}
				
			});
			excelTemplateExporter.exportExcel(result, "���°�-����", "����", RecodeModel.class, "���°�-����-����.xls", response);
		}else {
			excelTemplateExporter.exportExcel(commenResult, null, "����", UserRecord.class, "���°�-����-����.xls", response);
		}
		
		
	
	}
	
	
	/**
	 * ����������ƥ��
	 * eg��
	 * "����?9.5?���248?��һλƤ����","Ƥ����?7.5?���241?��һλ?������","������?7.5?���?233.81?��һλ?Τ��","�޳�?7.5?��173.81?��һλ?�ܱ�","������?7.5?���181?��һλ?�޳�","�ܱ�?7.5���166?��һλ?����","����?5.5?���160?��һλ������","ͯ��?8.5?���188?��һλ������","����?14.5?���203.5?��һλ����׳","Τ��?15.5?���?218?��һλ?����","����׳??6.5??���?196?��һλ?ͯ��","��С��??7.5??���?145?��һλ?���Ԫ","���Ԫ13���132.8��һλ��Ϊ","������7.5?���153?��һλ?��С��","��Ϊ?8.5?���124.31?��һλ?ͯ��","ͯ��?9?���113?��һλ?Τ��","�ؿ���?6?���?109?��һλ?���","���������115","��Ѻ�?2.5?���?83.81?��һҳ?�����","�����?1?���?82.81?��һλ?��̳�","����?2.5?���?86.31��һλ��Ѻ�","��̳�??14??���???68.81??��һλnull","���?5?���?104?��һλ?Τ��","����֮��ȱ��һ�����˱��ˣ��м����15.5Ԫ"]
24
	 * @param txtRecord
	 * @return
	 */
	private RecodeModel getRecodeModel(String txtRecord,Matcher matcherOne,List<String> userStrList) {
		BigDecimal money =new BigDecimal(matcherOne.group(1));
		BigDecimal allMoney = new BigDecimal(matcherOne.group(2));
		String userName=null;
		String nexName=null;
		//��2.1��ֻȡ��ͷ��ĩβ����
		String patternMatch ="(\\D*)\\d+\\.?\\d*\\D*\\d+\\.?\\d*(\\D*)";
		Matcher matcher = Pattern.compile(patternMatch).matcher(txtRecord);
		if(!matcher.find()) {
			System.err.println("ȡ��ͷ�ͽ�βû��ƥ���ϣ�"+txtRecord);
			return null;
		}
		System.out.println("�ɹ���ȡ���֮ǰ���˺����֮�����:"+txtRecord);
		String one = matcher.group(1);
		String two = matcher.group(2);
		//��2.2����������ݿ��û�������һ�£���������ظ�����һλ�����ȡ
		if (StringUtils.isNotBlank(one)) {
			for(String str:userStrList) {
				if(one.indexOf(str)!=-1) {
					userName=str;
					break;
				}
				
			}
			if(userName==null) {
				System.out.println("�޷��ҵ������ˣ��Թ����μ�¼��"+txtRecord);
				return null;
			}
			System.out.println("��ȡ���Է���Ϊ��" + userName);
		}
		if (StringUtils.isNotBlank(two)) {
			if (two.contains("��һλ")) {
				two = two.replace("��һλ", "");
			}
			nexName = two.trim();
		}
		System.out.println("��ȡ����һλΪ��" + nexName);

		RecodeModel recodeModel = new RecodeModel(userName, money, allMoney, nexName);
		System.out.println("���������ļ�¼Ϊ��"+JSON.toJSONString(recodeModel));
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
	 * ͨ�õĶ�ȡexcle����
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
