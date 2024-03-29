package com.atguigu.jpa.test;

import com.atguigu.jpa.helloworld.*;
import org.hibernate.ejb.QueryHints;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

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

    //可以使用 JPQL 完成 UPDATE 和 DELETE 操作.
    @Test
    public void testExecuteUpdate(){
        String jpql = "UPDATE Customer c SET c.lastName = ? WHERE c.id = ?";
        Query query = entityManager.createQuery(jpql).setParameter(1, "YYY").setParameter(2, 12);

        query.executeUpdate();
    }

    //使用 jpql 内建的函数
    @Test
    public void testJpqlFunction(){
        // 字符串处理函数
        String jpql = "SELECT lower(c.email) FROM Customer c";
        List<String> emails = entityManager.createQuery(jpql).getResultList();
        System.out.println(emails);

        // 日期函数
//        String jpql = "SELECT current_date() FROM Customer c";
//        Object date = entityManager.createQuery(jpql).getSingleResult();
//        System.out.println(date);
    }

    // JPQL 子查询
    @Test
    public void testSubQuery(){
        //查询所有 Customer 的 lastName 为 YY 的 Order
        String jpql = "SELECT o FROM Order o "
                + "WHERE o.customer = (SELECT c FROM Customer c WHERE c.lastName = ?)";

        Query query = entityManager.createQuery(jpql).setParameter(1, "YY");
        List<Order> orders = query.getResultList();
        System.out.println(orders.size());
    }

    /**
     * JPQL 的关联查询同 HQL 的关联查询.
     */
    @Test
    public void testLeftOuterJoinFetch(){
        String jpql = "FROM Customer c LEFT OUTER JOIN FETCH c.orders WHERE c.id = ?";

        // 有FETCH的情况下返回的是一个对象,且对象内的集合也是有的
        Customer customer =
                (Customer) entityManager.createQuery(jpql).setParameter(1, 8).getSingleResult();
        System.out.println(customer.getLastName());
        System.out.println(customer.getOrders().size());

        // 语句中没有FETCH将返回的是一个数组类型的集合且order集合还没有初始化,很难处理
//		List<Object[]> result = entityManager.createQuery(jpql).setParameter(1, 12).getResultList();
//		System.out.println(result);
    }

    //查询 order 数量大于 2 的那些 Customer
    @Test
    public void testGroupBy(){
        String jpql = "SELECT o.customer FROM Order o "
                + "GROUP BY o.customer "
                + "HAVING count(o.id) >= 2";
        List<Customer> customers = entityManager.createQuery(jpql).getResultList();

        System.out.println(customers);
    }

    @Test
    public void testOrderBy () {
        String jpql = "FROM Customer c WHERE c.age > ? ORDER BY c.age DESC";
        // 在建立query时要设置QueryHints,同时配置文件中要启用查询缓存
        Query query = entityManager.createQuery(jpql).setHint(QueryHints.HINT_CACHEABLE, true);
        //占位符的索引是从 1 开始, 这里占位符会报错,不影响运行
        query.setParameter(1, 1);

        List<Customer> customers = query.getResultList();
        System.out.println(customers.size());
    }

    //使用 hibernate 的查询缓存.
    @Test
    public void testQueryCache () {
        String jpql = "FROM Customer c WHERE c.age > ?";
        // 在建立query时要设置QueryHints,同时配置文件中要启用查询缓存
        Query query = entityManager.createQuery(jpql).setHint(QueryHints.HINT_CACHEABLE, true);
        //占位符的索引是从 1 开始, 这里占位符会报错,不影响运行
        query.setParameter(1, 1);

        List<Customer> customers = query.getResultList();
        System.out.println(customers.size());

        // 第二次查询,有查询缓存的话不会再次发select
        query = entityManager.createQuery(jpql).setHint(QueryHints.HINT_CACHEABLE, true);
        query.setParameter(1, 1);

        customers = query.getResultList();
        System.out.println(customers.size());
    }

    //createNativeQuery 适用于本地 SQL
    @Test
    public void testNativeQuery(){
        String sql = "SELECT age FROM jpa_cutomers WHERE id = ?";
        Query query = entityManager.createNativeQuery(sql).setParameter(1, 3);

        Object result = query.getSingleResult();
        System.out.println(result);
    }

    //createNamedQuery 适用于在实体类前使用 @NamedQuery 标记的查询语句
    // JPA的命名查询
    @Test
    public void testNamedQuery(){
        Query query = entityManager.createNamedQuery("testNamedQuery")
                .setParameter(1, 3);
        Customer customer = (Customer) query.getSingleResult();

        System.out.println(customer);
    }

    //默认情况下, 若只查询部分属性, 则将返回 Object[] 类型的结果. 或者 Object[] 类型的 List.
    //也可以在实体类中创建对应的构造器, 然后再 JPQL 语句中利用对应的构造器返回实体类的对象.
    @Test
    public void testPartlyProperties(){
        String jpql = "SELECT new Customer(c.lastName, c.age) FROM Customer c WHERE c.id > ?";
        List result = entityManager.createQuery(jpql).setParameter(1, 1).getResultList();

        System.out.println(result);
    }

    // JPQL的helloWorld查询
    @Test
    public void testHelloJPQL(){
        String jpql = "FROM Customer c WHERE c.age > ?";
        //占位符的索引是从 1 开始, 这里占位符会报错,不影响运行
        Query query = entityManager.createQuery(jpql)
                .setParameter(1, 1);

        List<Customer> customers = query.getResultList();
        System.out.println(customers.size());
    }

    // 测试二级缓存
    @Test
    public void testSecondLevelCache(){
        Customer customer1 = entityManager.find(Customer.class, 8);

        entityTransaction.commit();
        entityManager.close();

        // 不加二级缓存的情况下这里会发第二次sql
        entityManager = entityManagerFactory.createEntityManager();
        entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        Customer customer2 = entityManager.find(Customer.class, 8);
    }

    //对于关联的集合对象, 默认使用懒加载的策略.
    //使用维护关联关系的一方获取, 还是使用不维护关联关系的一方获取, SQL 语句相同.
    @Test
    public void testManyToManyFind(){
//		Item item = entityManager.find(Item.class, 5);
//		System.out.println(item.getItemName());
//
//		System.out.println(item.getCategories().size());

        Category category = entityManager.find(Category.class, 3);
        System.out.println(category.getCategoryName());
        System.out.println(category.getItems().size());
    }

    //多对多的保存
    @Test
    public void testManyToManyPersist(){
        Item i1 = new Item();
        i1.setItemName("i-1");

        Item i2 = new Item();
        i2.setItemName("i-2");

        Category c1 = new Category();
        c1.setCategoryName("C-1");

        Category c2 = new Category();
        c2.setCategoryName("C-2");

        //设置关联关系
        i1.getCategories().add(c1);
        i1.getCategories().add(c2);

        i2.getCategories().add(c1);
        i2.getCategories().add(c2);

        c1.getItems().add(i1);
        c1.getItems().add(i2);

        c2.getItems().add(i1);
        c2.getItems().add(i2);

        //执行保存
        entityManager.persist(i1);
        entityManager.persist(i2);
        entityManager.persist(c1);
        // 如果重复保存会报错object references an unsaved transient instance 对象关联一个未保存的游离对象, 这是JPA高级的地方
//        entityManager.persist(c1);
        entityManager.persist(c2);
    }

    // 双向一对一的查询1, 获取不维护关联关系的一方
    //1. 默认情况下, 若获取不维护关联关系的一方, 则也会通过左外连接获取其关联的对象.
    //可以通过 @OneToOne 的 fetch 属性来修改加载策略. 但依然会再发送 SQL 语句来初始化其关联的对象, 还不如一条呢
    //这说明在不维护关联关系的一方, 不建议修改 fetch 属性.
    // 为什么?
    // Hibernate 在不读取 Department 表的情况是无法判断是否有关联有 Deparmtment, 因此无法判断设置 null 还是代理对象,
    // 而统一设置为代理对象,也无法满足不关联的情况, 所以无法使用延迟加载,只 有显式读取 Department.
    @Test
    public void testOneToOneFind2(){
        Manager mgr = entityManager.find(Manager.class, 1);
        System.out.println(mgr.getMgrName());

        System.out.println(mgr.getDept().getClass().getName());
    }

    // 双向一对一的查询1, 获取维护关联关系的一方
    //1.默认情况下, 若获取维护关联关系的一方, 则会通过左外连接获取其关联的对象.
    //但可以通过 @OntToOne 的 fetch 属性来修改加载策略.
    @Test
    public void testOneToOneFind(){
        Department dept = entityManager.find(Department.class, 1);
        System.out.println(dept.getDeptName());
        System.out.println(dept.getMgr().getClass().getName());
    }

    //双向 1-1 的关联关系, 建议先保存不维护关联关系的一方, 即没有外键的一方, 这样不会多出 UPDATE 语句.
    @Test
    public void testOneToOnePersistence(){
        Manager mgr = new Manager();
        mgr.setMgrName("M-BB");

        Department dept = new Department();
        dept.setDeptName("D-BB");

        //设置关联关系
        mgr.setDept(dept);
        dept.setMgr(mgr);

        //执行保存操作
        entityManager.persist(mgr);
        entityManager.persist(dept);
    }

    //若是双向 1-n 的关联关系, 执行保存时
    //若先保存 n 的一端, 再保存 1 的一端, 默认情况下, 会多出 n 条 UPDATE 语句.
    //若先保存 1 的一端, 则会多出 n 条 UPDATE 语句
    //在进行双向 1-n 关联关系时, 建议使用 n 的一方来维护关联关系, 而 1 的一方不维护关联系, 这样会有效的减少 SQL 语句.
    //注意: 若在 1 的一端的 @OneToMany 中使用 mappedBy 属性, 则 @OneToMany 端就不能再使用 @JoinColumn 属性了.
    @Test
    public void testDoubleOneToManyPersist(){
        Customer customer = new Customer();
        customer.setAge(18);
        customer.setBirth(new Date());
        customer.setCreatedTime(new Date());
        customer.setEmail("yy@163.com");
        customer.setLastName("YY");

        Order order1 = new Order();
        order1.setOrderName("O-YY-1");

        Order order2 = new Order();
        order2.setOrderName("O-YY-2");

        //建立关联关系
        customer.getOrders().add(order1);
        customer.getOrders().add(order2);

        order1.setCustomer(customer);
        order2.setCustomer(customer);

        //执行保存操作
        entityManager.persist(customer);

        entityManager.persist(order1);
        entityManager.persist(order2);
    }

    // 单向一对多的更改
    @Test
    public void testUpdate(){
        Customer customer = entityManager.find(Customer.class, 10);

        customer.getOrders().iterator().next().setOrderName("O-XXX-10");
    }

    //默认情况下, 若删除 1 的一端, 则会先把关联的 n 的一端的外键置空, 然后进行删除.
    //可以通过 @OneToMany 的 cascade 属性来修改默认的删除策略.
    @Test
    public void testOneToManyRemove(){
        Customer customer = entityManager.find(Customer.class, 7);
        entityManager.remove(customer);
    }

    // 单向一对多的查询
    //默认对关联的多的一方使用懒加载的加载策略.
    //可以使用 @OneToMany 的 fetch 属性来修改默认的加载策略
    @Test
    public void testOneToManyFind(){
        Customer customer = entityManager.find(Customer.class, 7);
        System.out.println(customer.getLastName());

        System.out.println(customer.getOrders().size());
    }

    // 单向一对多的保存
    //单向 1-n 关联关系执行保存时, 一定会多出 UPDATE 语句.
    //因为 n 的一端在插入时不会同时插入外键列.
    @Test
    public void testOneToManyPersist(){
        Customer customer = new Customer();
        customer.setAge(18);
        customer.setBirth(new Date());
        customer.setCreatedTime(new Date());
        customer.setEmail("mm@163.com");
        customer.setLastName("MM");

        Order order1 = new Order();
        order1.setOrderName("O-MM-1");

        Order order2 = new Order();
        order2.setOrderName("O-MM-2");

        //建立关联关系
        customer.getOrders().add(order1);
        customer.getOrders().add(order2);

//        order1.setCustomer(customer);
//        order2.setCustomer(customer);

        //执行保存操作
        entityManager.persist(customer);

        entityManager.persist(order1);
        entityManager.persist(order2);
    }

//    // 单向多对一的更改
//    @Test
//    public void testManyToOneUpdate(){
//        Order order = entityManager.find(Order.class, 2);
//        order.getCustomer().setLastName("FFF");
//    }
//
//    // 单向多对一的删除
//    //不能直接删除 1 的一端, 因为有外键约束.
//    @Test
//    public void testManyToOneRemove () {
////        Order order = entityManager.find(Order.class, 1);
////		entityManager.remove(order);
//
//        Customer customer = entityManager.find(Customer.class, 6);
//        entityManager.remove(customer);
//    }
//
//    // 单向多对一的查询
//    //默认情况下, 使用左外连接的方式来获取 n 的一端的对象和其关联的 1 的一端的对象.
//    //可使用 @ManyToOne 的 fetch 属性来修改默认的关联属性的加载策略
//    @Test
//    public void testManyToOneFind () {
//        Order order = entityManager.find(Order.class,1);
//        System.out.println(order.getOrderName());
//
//        System.out.println(order.getCustomer().getLastName());
//    }
//
//    /**
//     * 单向多对一的保存
//     * 保存多对一时, 建议先保存 1 的一端, 后保存 n 的一端, 这样不会多出额外的 UPDATE 语句.
//     */
//	@Test
//	public void testManyToOnePersist(){
//		Customer customer = new Customer();
//		customer.setAge(18);
//		customer.setBirth(new Date());
//		customer.setCreatedTime(new Date());
//		customer.setEmail("GG@163.com");
//		customer.setLastName("GG");
//
//		Order order1 = new Order();
//		order1.setOrderName("O-GG-1");
//
//		Order order2 = new Order();
//		order2.setOrderName("O-GG-2");
//
//		//设置关联关系
//		order1.setCustomer(customer);
//		order2.setCustomer(customer);
//
//		//执行保存操作
//        entityManager.persist(customer);
//
//		entityManager.persist(order1);
//		entityManager.persist(order2);
//	}

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
