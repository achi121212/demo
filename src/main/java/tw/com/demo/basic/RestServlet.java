package tw.com.demo.basic;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RestServlet extends HttpServlet {
	
	private Method getMethod;
	private Method postMethod;
	private Method putMethod;
	private Method deleteMethod;
	
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
	
	public RestServlet(Method getMethod, Method postMethod, Method putMethod, Method deleteMethod) {
		this.getMethod = getMethod;
		this.postMethod = postMethod;
		this.putMethod = putMethod;
		this.deleteMethod = deleteMethod;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (getMethod != null) {
			Class<?> clazz = getMethod.getDeclaringClass();
			try {
				getMethod.invoke(MyApplicationContext.getInstance().getBean(clazz.getName()), request, response);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			throw new RuntimeException("無此方法");
		}
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (postMethod != null) {
	    	Class<?> clazz = postMethod.getDeclaringClass();
			try {
				postMethod.invoke(MyApplicationContext.getInstance().getBean(clazz.getName()), request, response);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			throw new RuntimeException("無此方法");
		}
    }
}
