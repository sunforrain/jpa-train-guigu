package com.atguigu.jpa.helloworld;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class Main {
	
	public static void main(String[] args) {
		
		//1. 创建 EntitymanagerFactory,
		// 获取持久化单元的名字,这个名字见persistence.xml 的 persistence-unit标签
		String persistenceUnitName = "jpa-1";

		// 用于createEntityManagerFactory 重载方法的参数
		Map<String, Object> properites = new HashMap<String, Object>();
		properites.put("hibernate.show_sql", true);

		// Persistence  类是用于获取 EntityManagerFactory 实例
        // 和spring集成的时候连这个方法都不用
		EntityManagerFactory entityManagerFactory = 
				Persistence.createEntityManagerFactory(persistenceUnitName);
//				Persistence.createEntityManagerFactory(persistenceUnitName, properites);
				
		//2. 创建 EntityManager. 类似于 Hibernate 的SessionFactory 获取session
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		
		//3. 开启事务
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		
		//4. 进行持久化操作
		Customer customer = new Customer();
		customer.setAge(12);
		customer.setEmail("tom@atguigu.com");
		customer.setLastName("Tom");
		customer.setBirth(new Date());
		customer.setCreatedTime(new Date());
		
		entityManager.persist(customer);
		
		//5. 提交事务
		transaction.commit();
		
		//6. 关闭 EntityManager
		entityManager.close();
		
		//7. 关闭 EntityManagerFactory
		entityManagerFactory.close();
	}
	
}
