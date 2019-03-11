
import java.lang.reflect.Method;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

/***cglib������ ��̬������ */
public class CglibProxyFactory implements MethodInterceptor {
	private Object target;
	private TxManager txManager;
	public CglibProxyFactory(Object target,TxManager txManager) {
		this.target = target;
		this.txManager = txManager;
	}

	// �����������
	public Object createProxy() {
		// 1.����Enhancer
		Enhancer enhance = new Enhancer();

		// 2.����:����Ŀ������Class ����enhance�������Ŀ���������� �ײ�ʹ������̳еķ�ʽ����ǿ���෽��
		enhance.setSuperclass(target.getClass());

		// 3.���ûص����� (�൱��InvocationHandler)
		//����Callback callback   CglibProxyFactory implements MethodInterceptor implements Callback
		//����  thisҲ����callback����
		enhance.setCallback(this);

		return enhance.create();
	}

	/**
	 * �ص�����  �ο� InvocationHandler�е�invoke�����ڵ�д��
	 * 	Object proxy : �������(��������)
	 *  Method method : ִ�еķ���
	 *  Object[] args : �����еĲ���
	 *  MethodProxy mehtodProxy : ������(�ò���) ���ิд�ķ���
	 *  ���ص��Ƿ���ִ��֮��ķ���ֵ
	 */
	@Override
	public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methdoProxy) throws Throwable {
		System.out.println("��־����....");
		return method.invoke(target, args); // ��jdk��proxy�в�������
		// return methdoProxy.invokeSuper(proxy, args); ���Ƽ�
	}
}

@Test
public void testProxy() throws Exception {
	User user = new User("С��");
	//��ʵ�����ɫ
	UserServiceImpl userService = new UserServiceImpl();
	TxManager txManager = new TxManager();

	//������������û��������
	CglibProxyFactory handler = new CglibProxyFactory(userService, txManager);
	UserService obj = (UserService)handler.createProxy();
	UserServiceImpl obj = (UserServiceImpl)handler.createProxy();
	// UserServiceImpl$$EnhancerByCGLIB$$2536a285
	System.out.println(obj.getClass());	
	obj.save(user);
}