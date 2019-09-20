package com.atguigu.jpa.helloworld;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

//@NamedQuery(name="testNamedQuery", query="FROM Customer c WHERE c.id = ?")
//@Cacheable(true)
@Table(name="JPA_CUTOMERS")
@Entity
public class Customer {

	private Integer id;
	private String lastName;

	private String email;
	private int age;
	
	private Date createdTime;
	private Date birth;
	
	public Customer() {
		// TODO Auto-generated constructor stub
	}
	
	public Customer(String lastName, int age) {
		super();
		this.lastName = lastName;
		this.age = age;
	}



//	private Set<Order> orders = new HashSet<>();

    // 使用第三方数据表的方式为当前的数据表生成主键值的方式,在一些特殊的业务场景中会用到
    @TableGenerator(name = "ID_GENERATOR",   // name 属性表示该主键生成策略的名称，它被引用在@GeneratedValue中设置的generator 值中
            table = "JPA_ID_GENERATORS",    // table 属性表示表生成策略所持久化的表名
            pkColumnName = "PK_NAME",        // pkColumnName 属性的值表示在持久化表中，该主键生成策略所对应键值的名称
            pkColumnValue = "CUSTOMER_ID",   // pkColumnValue 属性的值表示在持久化表中，该生成策略所对应的主键
            valueColumnName = "PK_VALUE",    // valueColumnName 属性的值表示在持久化表中，该主键当前所生成的值，它的值将会随着每次创建累加
            allocationSize = 100)             // allocationSize 表示每次主键值增加的大小, 默认值为 50
    @GeneratedValue(strategy = GenerationType.TABLE,generator = "ID_GENERATOR")
//	@GeneratedValue(strategy=GenerationType.AUTO)
	@Id
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	// @Column也可以映射字段的长度和非空等
	@Column(name="LAST_NAME",length=50,nullable=false)
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	// 对于日期类型,使用@Temporal注解来调整精度.
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	@Temporal(TemporalType.DATE)
	public Date getBirth() {
		return birth;
	}

	public void setBirth(Date birth) {
		this.birth = birth;
	}
	
	//映射单向 1-n 的关联关系
	//使用 @OneToMany 来映射 1-n 的关联关系
	//使用 @JoinColumn 来映射外键列的名称
	//可以使用 @OneToMany 的 fetch 属性来修改默认的加载策略
	//可以通过 @OneToMany 的 cascade 属性来修改默认的删除策略. 
	//注意: 若在 1 的一端的 @OneToMany 中使用 mappedBy 属性, 则 @OneToMany 端就不能再使用 @JoinColumn 属性了. 
//	@JoinColumn(name="CUSTOMER_ID")
//	@OneToMany(fetch=FetchType.LAZY,cascade={CascadeType.REMOVE},mappedBy="customer")
//	public Set<Order> getOrders() {
//		return orders;
//	}
//
//	public void setOrders(Set<Order> orders) {
//		this.orders = orders;
//	}

	//工具方法. 不需要映射为数据表的一列. @Transient忽略不会将属性映射为一列
	@Transient
	public String getInfo(){
		return "lastName: " + lastName + ", email: " + email;
	}

	@Override
	public String toString() {
		return "Customer [id=" + id + ", lastName=" + lastName + ", email="
				+ email + ", age=" + age + ", createdTime=" + createdTime
				+ ", birth=" + birth + "]";
	}

}
