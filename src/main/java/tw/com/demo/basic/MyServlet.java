package tw.com.demo.basic;

import java.lang.reflect.Method;

public class MyServlet {
	
	private String url;
	private Method get;
	private Method post;
	private Method put;
	private Method delete;
	
	public enum Type {
		GET {
			@Override
			public void setMethod(MyServlet servlet, Method method) {
				servlet.setGet(method);
			}
		}
		, POST {
			@Override
			public void setMethod(MyServlet servlet, Method method) {
				servlet.setPost(method);
			}
		}
		, PUT {
			@Override
			public void setMethod(MyServlet servlet, Method method) {
				servlet.setPut(method);
			}
		}
		, DELETE {
			@Override
			public void setMethod(MyServlet servlet, Method method) {
				servlet.setDelete(method);
			}
		};
		
		public abstract void setMethod(MyServlet servlet, Method method);
		
	}
	
	public MyServlet(String url) {
		super();
		this.url = url;
	}
	
	public void setMethodByType(String type, Method method) {
		Type methodType = Type.valueOf(type.toUpperCase());
		methodType.setMethod(this, method);
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Method getGet() {
		return get;
	}
	public void setGet(Method get) {
		this.get = get;
	}
	public Method getPost() {
		return post;
	}
	public void setPost(Method post) {
		this.post = post;
	}
	public Method getPut() {
		return put;
	}
	public void setPut(Method put) {
		this.put = put;
	}
	public Method getDelete() {
		return delete;
	}
	public void setDelete(Method delete) {
		this.delete = delete;
	}

	@Override
	public String toString() {
		return "MyServlet [url=" + url + ", get=" + get + ", post=" + post + ", put=" + put + ", delete=" + delete
				+ "]";
	}	
	
}
