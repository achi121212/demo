package tw.com.demo.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tw.com.demo.annotation.MyRequestMapping;
import tw.com.demo.annotation.MyRestController;
import tw.com.demo.annotation.MyTransaction;
import tw.com.demo.basic.RestServlet;
import tw.com.demo.dao.EntityManager;
import tw.com.demo.entity.Employee;

@MyRestController
public class DemoController {
	
	@MyRequestMapping(url = "/get")
	public void get(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("hello demo get");
		// http://localhost:8080/demo/get
		try (EntityManager em = new EntityManager()) {
			List<Employee> employees = em.findAll(Employee.class);
			System.out.println("allEmployee：" + employees.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@MyRequestMapping(url = "/post", type = RestServlet.METHOD_POST)
	public void post(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("hello demo post");
		// http://localhost:8080/demo/post 測試：postman
		try (EntityManager em = new EntityManager()) {
			Employee employee = new Employee();
			employee.setId("1234");
			employee.setIden("iden");
			employee.setPassword("pwd");
			employee.setUsername("user");
			em.save(employee);
			Employee employee2 = em.findById("1234", Employee.class);
			System.out.println("employee2：" + employee2.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@MyTransaction
	@MyRequestMapping(url = "/delete", type = RestServlet.METHOD_POST)
	public void delete(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("hello demo delete");
		// http://localhost:8080/demo/post 測試：postman
		try (EntityManager em = new EntityManager()) {
			Employee employee = em.findById("1234", Employee.class);
			System.out.println("employee：" + employee.toString());
			
			em.deleteById(employee);
			if (1 == 1) {
				throw new RuntimeException("測試transaction");
			}
			Employee employee2 = em.findById("1234", Employee.class);
			System.out.println("after delete employee2：" + employee2 == null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
