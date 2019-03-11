import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import cn.it.TxManager;

/**��ʼһ��ʹ��jdk��proxy��ɶ�̬������ 
 *  implements InvocationHandler �Լ�������һ���ص��Ĵ�����
 * */
public class JDKProxyFactory implements InvocationHandler {
	/**���ڽ�����ʵ�����ɫ(Ŀ�����) ��̬�ĵ��� */
	private Object target;
	
	/**���ڽ����������*/
	private TxManager txManager;
	
	/**ʹ�ù��췽������Ŀ��������������*/
	public JDKProxyFactory(Object target,TxManager txManager) {
		this.target = target;
		this.txManager = txManager;
	}
	/**�����������*/
	public Object createProxy() {
		/** ʹ��java.lang.reflect.Proxy��ɴ�����󴴽�
		 * Proxy��static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h) ClassLoader ������� 
		 * 	����Class�������õ�ClassLoader(ֻҪ�õ����Ϳ�����)  ���ڼ��� class���ļ�
		interfaces �õ�Ŀ������ʵ�ֽӿڵ�Class[]  
			���������ɫ ,Ҫ����ʵ�����ɫ���õ�
		java.lang.reflect.InvocationHandler ����ִ�г���ӿ�
			��Ҫ���������������Ӧ�Ĵ���(�����������Ҫ�ڴ���������ӵĴ���)
			����JDKProxyFactory����implementsʵ��InvocationHandler�ӿ�
			InvocationHandler h ��������this����
		 */
		return Proxy.newProxyInstance(
				this.getClass().getClassLoader(), 
				target.getClass().getInterfaces(), //��ʵ�����ɫ�Ľӿ�
				this
			);
	}

	/**
	 * ͨ������ִ�лص����� �ڴ���ʵ���ϴ��������ò����ؽ����
	 * 	Object proxy : �������(��������)
	 *  java.lang.reflect.Method method : ʵ��ִ�еķ�������
	 *  Object[] args : ʵ�ʵ��÷����еĲ���
	 *  ���ص��Ƿ���ִ��֮��ķ���ֵ
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		Object result = null;
		try {
			txManager.begin();//��ʼ���� �����������ķ���
			/**
			 java.lang.reflect.Method method �� Object invoke(Object obj, Object... args) 
				�൱��result=target.invoke(args)*/
			
			/**	��ǿ  target.getName() �ܻ��ִ�еĶ��������  �ܹ����ĳЩ����������ǿ
				if(target.getName().endwith("Dao")){
					//��ǿ�Ĵ���
				}
			*/

			/**��ִ�еķ��������ж� ���ĳЩ����������ǿ
				save����ͨ�������ļ���ȡ dog4j����  
				�� startwith  endwith  equals �������ж�
			*/
			if(method.endwith("save")){
				//��ǿsave����ִ��ǰ�Ĵ���
				result = method.invoke(target, args);
				//��ǿsave����ִ�к�Ĵ���
			}else{
				result = method.invoke(target, args);
			}

			txManager.commit();//�ύ����
		} catch (Exception e) {
			txManager.rollback();//�ع�����
			e.printStackTrace();
		}
		return result;
	}
}

@Test
public void testProxy() throws Exception {
	User user = new User("С��");
	IUserService userService = new UserServiceImpl();
	TxManager txManager = new TxManager();

	JDKProxyFactory handler = new JDKProxyFactory(userService,txManager);
	IUserService proxy = (IUserService)handler.createProxy();
	// JDK�ײ�ʹ�õ���װ�����ģʽ�Ĵ���
	//UserServiceImpl proxy = (UserServiceImpl)handler.createProxy();�ᱨ�� CGlib���ᱨ��

	//proxy.update();
	proxy.save(user);
}