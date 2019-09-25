package com.atguigu.jpa.spring;

import com.atguigu.jpa.service.PersonService;
import com.atguigu.jpa.spring.entities.Person;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.sql.SQLException;

public class JPATest {

    private ApplicationContext ctx = null;
    private PersonService personService = null;

    {
        ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        personService = ctx.getBean(PersonService.class);
    }

    @Test
    public void testPersonService(){
        Person p1 = new Person();
        p1.setAge(11);
        p1.setEmail("aa@163.com");
        p1.setLastName("AA");

        Person p2 = new Person();
        p2.setAge(12);
        p2.setEmail("bb@163.com");
        p2.setLastName("BB");

        // 这里手动制造一个异常,在两个保存中间,会发现事务是好用的
        System.out.println(personService.getClass().getName());
        personService.savePersons(p1, p2);
    }

    // 测试下数据源
    @Test
    public void testDataSource () throws SQLException {
        DataSource dataSource = ctx.getBean(DataSource.class);
        System.out.println(dataSource.getConnection());
    }
}
