package com.fastjavaframework.util;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 公共工具类
 */
public class CommonUtil {
	private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);

	/**
	 * 通过 HttpServletRequest 返回IP地址
	 * @param request
	 * @return IP
	 */
	public static String getIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		// 如果是多级代理取第一个IP地址
		if (ip != null && ip.indexOf(",") != -1) {
			ip = ip.substring(ip.lastIndexOf(",") + 1, ip.length()).trim();
		}
		return ip;
	}

	/**
	 * 验证码
	 * @param type 类型：str字符串 num数字 strAndNum字符串数字随机
	 * @param number 个数
	 * @return 随机验证码
	 */
	public static String randomCode(String type, int number) {
		ArrayList<Object> list = new ArrayList<>();

		if (!"num".equals(type)) {
			for (char c = 'a'; c <= 'z'; c++) {
				list.add(c);
			}
		} else if (!"str".equals(type)) {
			for (int i = 0; i <= 9; i++) {
				list.add(i);
			}
		}

		StringBuffer str = new StringBuffer();
		for (int i = 0; i < number; i++) {
			int num = (int) (Math.random() * list.size());
			str.append(list.get(num));
		}
		return str.toString();
	}
	
	/**
	 * Response设置返回值错误
	 * @param response
	 * @param message 返回值
	 */
	public static void setResponseReturnValue(HttpServletResponse response,int code, String message) {
		PrintWriter out=null;
		response.setStatus(code);
		response.setHeader("Content-type", "application/json;charset=UTF-8");

		try {
			out = response.getWriter();
			out.print(message);
		} catch (Exception e) {
			logger.error("Response设置返回值错误：" + e.getMessage());
		} finally {
			if(null != out) {
				out.flush();
				out.close();
			}
		}
	}
	
	public static Object setDefValue(Class clz, Class genericityClz) {
		String clzName = clz.getName();

		if(clzName.equals(Object.class.getName())) {
			return "Object";
		} else if(clzName.equals(String.class.getName())) {
			return "String";
		} else if(clzName.equals(Boolean.class.getName())) {
			return true;
		} else if(clzName.equals(Byte.class.getName()) || clzName.equals(Short.class.getName()) ||
					clzName.equals(Integer.class.getName()) || clzName.equals(Long.class.getName())) {
			return 0;
		} else if(clzName.equals(Float.class.getName()) || clzName.equals(Double.class.getName())) {
			return 0.0;
		} else if(clzName.equals(List.class.getName()) || clzName.equals(ArrayList.class.getName()) ||
					clzName.equals(Set.class.getName())) {
			List<Object> list = new ArrayList<>();
			list.add(setDefValue(genericityClz, null));
			list.add(setDefValue(genericityClz, null));
 			return list;
		} else if(clzName.equals(Map.class.getName()) || clzName.equals(HashMap.class.getName())) {
			Map<String, String> map = new HashMap<>();
			map.put("key1", "value1");
			map.put("key2", "value2");
			return map;
		} else if(clzName.equals(Date.class.getName())) {
			return new Date();
		} else {
			Object reObj = null;
			try {
				reObj = clz.newInstance();
			} catch (Exception e) {
				return "Object";
			}

			Method[] methods = clz.getMethods();
			for(Method method : methods) {
				if(method.getParameterTypes().length != 1) {
					continue;
				}

				try {
					method.invoke(reObj, setDefValue(method.getParameterTypes()[0], genericityClz));
				} catch (Exception e) {
					continue;
				}
			}

			if(null == reObj) {
				return "Object";
			}

			return reObj;
		}
	}

	/**
	 * 获取注释的值
	 * @param annotation 注释
	 * @return 注释值得键值对
     */
	public static Map<String, Object> getAnnotationValue(Annotation annotation) {
		Map<String, Object> map = new HashMap<>();
		for(Method method : annotation.annotationType().getDeclaredMethods()) {
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			try {
				Object invoke = method.invoke(annotation);
				map.put(method.getName(), invoke);
			} catch (Exception e){
				logger.error("获取注解值错误：" + e.getMessage());
			}
		}

		return map;
	}

	/**
	 * 运行js脚本
	 * @param js 脚本
	 * @return 返回""运算错误
	 */
	public static Object runJS(String js) {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine se = manager.getEngineByName("js");
		try {
			return se.eval(js);
		} catch (ScriptException e) {
			logger.error("js运算错误：" + e.getMessage());
			return "";
		}
	}

}
