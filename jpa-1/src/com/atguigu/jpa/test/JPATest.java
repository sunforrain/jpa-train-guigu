package com.atguigu.jpa.test;

import com.atguigu.jpa.helloworld.Customer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.Date;

public class JPATest {
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private EntityTransaction entityTransaction;

    @Before
    public void init () {
        entityManagerFactory = Persistence.createEntityManagerFactory("jpa-1");
        entityManager = entityManagerFactory.createEntityManager();
        // hibernate里直接begin,没有get
        entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();
    }

    @After
    public void destory () {
        entityTransaction.commit();
        entityManager.close();
        entityManagerFactory.close();
    }

    /**
     * 同 hibernate 中 Session 的 refresh 方法.
     */
    @Test
    public void testRefresh(){
        Customer customer = entityManager.find(Customer.class, 1);
        customer = entityManager.find(Customer.class, 1);
        // 调refresh也会强制发一条sql,让内存和数据库的状态一致
        entityManager.refresh(customer);
    }

    /**
     * 同 hibernate 中 Session 的 flush 方法.
     */
    @Test
    public void testFlush(){
        Customer customer = entityManager.find(Customer.class, 1);
        System.out.println(customer);

        customer.setLastName("AA");
        // 调flush就会强制发语句,但是没有提交事务
        entityManager.flush();
    }

    //若传入的是一个游离对象, 即传入的对象有 OID.
    //1. 若在 EntityManager 缓存中有对应的对象
    //2. JPA 会把游离对象的属性复制到查询到EntityManager 缓存中的对象中.
    //3. EntityManager 缓存中的对象执行 UPDATE.
    // 这里是和hibernate的saveOrUpdate()的主要区别,hibernate不允许session与两个同样OID的对象关联,
    // 而merge虽然也不允许,但是因为有一个复制的机制在,不会报错
    @Test
    public void testMerge4(){
        Customer customer = new Customer();
        customer.setAge(18);
        customer.setBirth(new Date());
        customer.setCreatedTime(new Date());
        customer.setEmail("dd@163.com");
        customer.setLastName("DD");

        customer.setId(4);
        // 这里再去查询,会有两个OID为4的对象
        Customer customer2 = entityManager.find(Customer.class, 4);

        entityManager.merge(customer);

        System.out.println(customer == customer2); //false
    }

    //若传入的是一个游离对象, 即传入的对象有 OID.
    //1. 若在 EntityManager 缓存中没有该对象
    //2. 若在数据库中也有对应的记录
    //3. JPA 会查询对应的记录, 然后返回该记录对一个的对象, 再然后会把游离对象的属性复制到查询到的对象中.
    //4. 对查询到的对象执行 update 操作.
    // 可以通过在customer类的set方法打断点的方式来看过程,一共会调三次
    @Test
    public void testMerge3(){
        Customer customer = new Customer();
        customer.setAge(18);
        customer.setBirth(new Date());
        customer.setCreatedTime(new Date());
        customer.setEmail("ee@163.com");
        customer.setLastName("EE"); // 第一次调

        customer.setId(4); // 第二次调,设置id会根据id去数据库查有没有对象

        Customer customer2 = entityManager.merge(customer); // 第三次调,数据库有同id的数据,拷贝新对象的各个值去进行update

        System.out.println(customer == customer2); //false
    }

    //若传入的是一个游离对象, 即传入的对象有 OID.
    //1. 若在 EntityManager 缓存中没有该对象
    //2. 若在数据库中也没有对应的记录
    //3. JPA 会创建一个新的对象, 然后把当前游离对象的属性复制到新创建的对象中(这里OID可能和创建的新对象的id不同)
    //4. 对新创建的对象执行 insert 操作.
    // 和hibernate 的save()不同的是,save()会直接保存手动设置的那个id,不是自动生成的
    @Test
    public void testMerge2(){
        Customer customer = new Customer();
        customer.setAge(18);
        customer.setBirth(new Date());
        customer.setCreatedTime(new Date());
        customer.setEmail("dd@163.com");
        customer.setLastName("DD");

        customer.setId(100);

        Customer customer2 = entityManager.merge(customer);

        System.out.println("customer#id:" + customer.getId());
        System.out.println("customer2#id:" + customer2.getId());
    }

    /**
     * 总的来说: 类似于 hibernate Session 的 saveOrUpdate 方法.
     */
    //1. 若传入的是一个临时对象
    //会创建一个新的对象, 把临时对象的属性复制到新的对象中, 然后对新的对象执行持久化操作. 所以
    //新的对象中有 id, 但以前的临时对象中没有 id.
    @Test
    public void testMerge1(){
        Customer customer = new Customer();
        customer.setAge(18);
        customer.setBirth(new Date());
        customer.setCreatedTime(new Date());
        customer.setEmail("cc@163.com");
        customer.setLastName("CC");

        Customer customer2 = entityManager.merge(customer);

        System.out.println("customer#id:" + customer.getId());
        System.out.println("customer2#id:" + customer2.getId());
    }

    //类似于 hibernate 中 Session 的 delete 方法. 把对象对应的记录从数据库中移除
    //但注意: 该方法只能移除 持久化 对象. 而 hibernate 的 delete 方法实际上还可以移除 游离对象.
    @Test
    public void testRemove(){
        // 这种只是设定id的游离对象不能删
//		Customer customer = new Customer();
//		customer.setId(2);

        Customer customer = entityManager.find(Customer.class, 2);
        entityManager.remove(customer);
    }

    //类似于 hibernate 的 save 方法. 使对象由临时状态变为持久化状态.
    //和 hibernate 的 save 方法的不同之处: 若对象有 id, 则不能执行 insert 操作, 而会抛出异常.
    @Test
    public void testPersistence(){
        Customer customer = new Customer();
        customer.setAge(15);
        customer.setBirth(new Date());
        customer.setCreatedTime(new Date());
        customer.setEmail("bb@163.com");
        customer.setLastName("BB");

//        customer.setId(100);

        entityManager.persist(customer);
        System.out.println(customer.getId());
    }

    //类似于 hibernate 中 Session 的 load 方法
    @Test
    public void testGetReference () {
        // 这里只是查询的代理对象,要到实际使用时才发select
        Customer customer = entityManager.getReference(Customer.class, 1);
        System.out.println(customer.getClass().getName());

        System.out.println("-------------------------------------");
        // 如果这里关闭了entitManager,就和关闭了session,会出现懒加载异常
//		transaction.commit();
//		entityManager.close();

        System.out.println(customer);
    }

    //类似于 hibernate 中 Session 的 get 方法.
    @Test
    public void testFind () {
        // 调find的同时就发送了select语句
        Customer customer = entityManager.find(Customer.class, 1);
        System.out.println("-------------------------------------");

        System.out.println(customer);
    }
}