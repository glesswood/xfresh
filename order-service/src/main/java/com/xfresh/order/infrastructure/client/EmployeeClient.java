package com.xfresh.order.infrastructure.client;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.xfresh.product.model.Employee;

public interface EmployeeClient {

	@GetMapping("/department/{departmentId}")
	List<Employee> findByDepartment(@PathVariable("departmentId") Long departmentId);
	
}
