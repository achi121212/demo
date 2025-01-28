package tw.com.demo.entity;

import tw.com.demo.annotation.MyEntity;
import tw.com.demo.annotation.MyId;

@MyEntity
public class Employee {
	
	@MyId
	private String id ;
	
	private String iden ;
	
	private String username ;
	
	private String password ;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIden() {
		return iden;
	}

	public void setIden(String iden) {
		this.iden = iden;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "Employee [id=" + id + ", iden=" + iden + ", username=" + username + ", password=" + password + "]";
	}
	
}
