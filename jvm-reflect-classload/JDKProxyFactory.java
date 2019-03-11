import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import cn.it.TxManager;

/**创始一个使用jdk的proxy完成动态代理工具 
 *  implements InvocationHandler 自己创建的一个回调的处理器
 * */
public class JDKProxyFactory implements InvocationHandler {
	/**用于接收真实主题角色(目标对象) 多态的调用 */
	private Object target;
	
	/**用于接收事务对象*/
	private TxManager txManager;
	
	/**使用构造方法传递目标对象与事务对象*/
	public JDKProxyFactory(Object target,TxManager txManager) {
		this.target = target;
		this.txManager = txManager;
	}
	/**创建代理对象*/
	public Object createProxy() {
		/** 使用java.lang.reflect.Proxy完成代理对象创建
		 * Proxy的static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h) ClassLoader 类加载器 
		 * 	所有Class都可以拿到ClassLoader(只要拿到它就可以了)  用于加载 class类文件
		interfaces 得到目标对象的实现接口的Class[]  
			抽象主题角色 ,要从真实主题角色中拿到
		java.lang.reflect.InvocationHandler 代理执行程序接口
			主要是让咱们来完成相应的处理(添加上咱们想要在代理方法加添加的代码)
			由于JDKProxyFactory本类implements实现InvocationHandler接口
			InvocationHandler h 参数就是this对象
		 */
		return Proxy.newProxyInstance(
				this.getClass().getClassLoader(), 
				target.getClass().getInterfaces(), //真实主题角色的接口
				this
			);
	}

	/**
	 * 通过反射执行回调方法 在代理实例上处理方法调用并返回结果。
	 * 	Object proxy : 代理对象(几乎不用)
	 *  java.lang.reflect.Method method : 实际执行的方法对象
	 *  Object[] args : 实际调用方法中的参数
	 *  返回的是方法执行之后的返回值
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		Object result = null;
		try {
			txManager.begin();//开始事务 调用事务对象的方法
			/**
			 java.lang.reflect.Method method 的 Object invoke(Object obj, Object... args) 
				相当于result=target.invoke(args)*/
			
			/**	加强  target.getName() 能获得执行的对象的类名  能够针对某些类来进行增强
				if(target.getName().endwith("Dao")){
					//增强的代码
				}
			*/

			/**对执行的方法进行判断 针对某些方法对象增强
				save可以通过配置文件读取 dog4j解析  
				如 startwith  endwith  equals 来进行判断
			*/
			if(method.endwith("save")){
				//增强save方法执行前的代码
				result = method.invoke(target, args);
				//增强save方法执行后的代码
			}else{
				result = method.invoke(target, args);
			}

			txManager.commit();//提交事务
		} catch (Exception e) {
			txManager.rollback();//回滚事务
			e.printStackTrace();
		}
		return result;
	}
}

@Test
public void testProxy() throws Exception {
	User user = new User("小红");
	IUserService userService = new UserServiceImpl();
	TxManager txManager = new TxManager();

	JDKProxyFactory handler = new JDKProxyFactory(userService,txManager);
	IUserService proxy = (IUserService)handler.createProxy();
	// JDK底层使用的是装饰设计模式的代理
	//UserServiceImpl proxy = (UserServiceImpl)handler.createProxy();会报错 CGlib不会报错

	//proxy.update();
	proxy.save(user);
}