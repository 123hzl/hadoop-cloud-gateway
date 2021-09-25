package com.hzl.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
/**
 * 查看端口占用：netstat -na|grep 8080
 * description
 * 注册中心http://127.0.0.1:8848/nacos/index.html
 * 启动nacos命令：sh startup.sh -m standalone
 * 默认密码：nacos/nacos
 * druid的管理界面http://localhost:8888/druid/login.html
 * 账号密码：admin,admin
 * @EnableDiscoveryClient去掉后不使用注册中心
 * @author hzl 2019/12/27 3:44 PM
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}
