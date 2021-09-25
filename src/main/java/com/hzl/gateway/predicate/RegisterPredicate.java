package com.hzl.gateway.predicate;

import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * description
 *
 * @author hzl 2021/09/26 1:07 AM
 */
public class RegisterPredicate {

	private static final List<PredicateDefinition> list = new ArrayList<>();
	//多个条件逗号隔开，后面可以改成读取配置文件
	private static final PredicateDefinition  predicateDefinition = new PredicateDefinition("Path=/rgst/**");

	public static List<PredicateDefinition> getList() {
		list.add(predicateDefinition);
		return list;
	}
}
