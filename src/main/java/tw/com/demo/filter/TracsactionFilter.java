package tw.com.demo.filter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import tw.com.demo.basic.MyApplicationContext;
import tw.com.demo.dao.EntityManager;

@WebFilter("/*")
public class TracsactionFilter implements Filter {
	
	private Set<String> transactionalClass;
	private Set<String> transactionalMethod;

	@Override
    public void init(FilterConfig filterConfig) throws ServletException {
		System.out.println("TracsactionFilter init");
		transactionalClass = MyApplicationContext.getInstance().getTransactionalClass();
		transactionalMethod = MyApplicationContext.getInstance().getTransactionalMethod();
		System.out.println("transactionalClass：" + transactionalClass.toString());
		System.out.println("transactionalMethod：" + transactionalMethod.toString());
    }
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String url = ((HttpServletRequest)request).getRequestURL().toString();
		url = url.replace("http://localhost:8080/demo", ""); // TODO
		if (isTransaction(url)) {
			try {
				EntityManager.beginTransaction();
				chain.doFilter(request, response);
				EntityManager.commit();
			} catch (Exception e) {
				try {
					EntityManager.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	private boolean isTransaction(String url) {
		for (String classUrl : transactionalClass) 
			if (url.contains(classUrl)) 
				return true;
		 if (transactionalMethod.contains(url))
				return true;
		return false;
	}
}
