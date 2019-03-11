
import java.lang.reflect.Method;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

/***cglib创建的 动态代理工具 */
public class CglibProxyFactory implements MethodInterceptor {
	private Object target;
	private TxManager txManager;
	public CglibProxyFactory(Object target,TxManager txManager) {
		this.target = target;
		this.txManager = txManager;
	}

	// 创建代理对象
	public Object createProxy() {
		// 1.创建Enhancer
		Enhancer enhance = new Enhancer();

		// 2.参数:传递目标对象的Class 这样enhance对象就是目标对象的子类 底层使用子类继承的方式来增强父类方法
		enhance.setSuperclass(target.getClass());

		// 3.设置回调操作 (相当于InvocationHandler)
		//传入Callback callback   CglibProxyFactory implements MethodInterceptor implements Callback
		//所以  this也就是callback对象
		enhance.setCallback(this);

		return enhance.create();
	}

	/**
	 * 回调方法  参考 InvocationHandler中的invoke方法内的写法
	 * 	Object proxy : 代理对象(几乎不用)
	 *  Method method : 执行的方法
	 *  Object[] args : 方法中的参数
	 *  MethodProxy mehtodProxy : 代理方法(用不到) 子类复写的方法
	 *  返回的是方法执行之后的返回值
	 */
	@Override
	public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methdoProxy) throws Throwable {
		System.out.println("日志操作....");
		return method.invoke(target, args); // 与jdk的proxy中操作类似
		// return methdoProxy.invokeSuper(proxy, args); 不推荐
	}
}

@Test
public void testProxy() throws Exception {
	User user = new User("小红");
	//真实主题角色
	UserServiceImpl userService = new UserServiceImpl();
	TxManager txManager = new TxManager();

	//代理主题主题没创建出来
	CglibProxyFactory handler = new CglibProxyFactory(userService, txManager);
	UserService obj = (UserService)handler.createProxy();
	UserServiceImpl obj = (UserServiceImpl)handler.createProxy();
	// UserServiceImpl$$EnhancerByCGLIB$$2536a285
	System.out.println(obj.getClass());	
	obj.save(user);
}