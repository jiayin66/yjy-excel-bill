package com.yjy.test;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class JsonTest {
	public static void main(String[] args) {
		JSONArray ja=new JSONArray();
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("1", "1");
		JSONObject jsonObject = new JSONObject(map);
		ja.add(jsonObject);
		System.out.println(ja.toJSONString());
		System.out.println(ja.toString());
		
	}
}
