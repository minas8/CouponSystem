package app.core.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.core.entities.Company;
import app.core.entities.Customer;
import app.core.exceptions.CouponSystemException;
import app.core.exceptions.ItemNotFoundException;
import app.core.exceptions.JwtExpiredException;
import app.core.exceptions.NotUniqueException;
import app.core.services.AdminService;

@RestController
@CrossOrigin
@RequestMapping("/api/u-admin")
public class AdminController extends ClientController {

	// field
	private AdminService adminService;

	// CTOR
	@Autowired
	public AdminController(AdminService adminService) {
		super();
		this.adminService = adminService;
	}

	// == Company Related Methods == //

	@PostMapping("/company")
	public ResponseEntity<?> addCompany(@RequestHeader String token, @RequestBody Company company) {
		try {
			Company comp = adminService.addCompany(company);
			return ResponseEntity.ok(comp);
		} catch (NotUniqueException e) {
			// if company's name or email are not unique
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		} catch (JwtExpiredException e) {
			// if JWT issue
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (CouponSystemException e) {
			// if company sent is null
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@PutMapping("/company")
	public ResponseEntity<?> updateCompany(@RequestHeader String token, @RequestBody Company company) {
		try {
			Company comp = adminService.updateCompany(company);
			return ResponseEntity.ok(comp);
		} catch (ItemNotFoundException e) {
			// if company does not exist in the system
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (NotUniqueException e) {
			// if company's name or email are not unique
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		} catch (JwtExpiredException e) {
			// if JWT issue
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (CouponSystemException e) {
			// if company sent is null
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@DeleteMapping("/company/{companyId}")
	public ResponseEntity<?> deleteCompany(@RequestHeader String token, @PathVariable int companyId) {
		try {
			adminService.deleteCompany(companyId);
			return ResponseEntity.ok("Company has been deleted! id: " + companyId);
		} catch (ItemNotFoundException e) {
			// if company with the specified companyId does not exist in the system
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (JwtExpiredException e) {
			// if JWT issue
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (CouponSystemException e) {
			// if companyId is not valid
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@GetMapping("/company/{companyId}")
	public ResponseEntity<?> getCompany(@RequestHeader String token, @PathVariable int companyId) {
		try {
			Company comp = adminService.getCompany(companyId);
			return ResponseEntity.ok(comp);
		} catch (JwtExpiredException e) {
			// if JWT issue
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (CouponSystemException e) {
			// if companyId is not valid
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@GetMapping("/companies")
	public ResponseEntity<?> getCompanies(@RequestHeader String token) {
		try {
			List<Company> companies = adminService.getCompanies();
			return ResponseEntity.ok(companies);
		} catch (JwtExpiredException e) {
			// if JWT issue
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		}
	}

	// == Company Related Methods == //

	@PostMapping("/customer")
	public ResponseEntity<?> addCustomer(@RequestHeader String token, @RequestBody Customer customer) {
		try {
			Customer cust = adminService.addCustomer(customer);
			return ResponseEntity.ok(cust);
		} catch (NotUniqueException e) {
			// if customer's email is not unique
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		} catch (JwtExpiredException e) {
			// if JWT issue
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (CouponSystemException e) {
			// if customer sent is null
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@PutMapping("/customer")
	public ResponseEntity<?> updateCustomer(@RequestHeader String token, @RequestBody Customer customer) {
		try {
			Customer cust = adminService.updateCustomer(customer);
			return ResponseEntity.ok(cust);
		} catch (ItemNotFoundException e) {
			// if customer does not exist in the system
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (NotUniqueException e) {
			// if customer's email is not unique
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		} catch (JwtExpiredException e) {
			// if JWT issue
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (CouponSystemException e) {
			// if customer sent is null
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@DeleteMapping("/customer/{customerId}")
	public ResponseEntity<?> deleteCustomer(@RequestHeader String token, @PathVariable int customerId) {
		try {
			adminService.deleteCustomer(customerId);
			return ResponseEntity.ok("Customer has been deleted! id: " + customerId);
		} catch (ItemNotFoundException e) {
			// if customer with the specified customerId does not exist in the system
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (JwtExpiredException e) {
			// if JWT issue
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (CouponSystemException e) {
			// if customerId is not valid
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@GetMapping("/customer/{customerId}")
	public ResponseEntity<?> getCustomer(@RequestHeader String token, @PathVariable int customerId) {
		try {
			Customer cust = adminService.getCustomer(customerId);
			return ResponseEntity.ok(cust);
		} catch (JwtExpiredException e) {
			// if JWT issue
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		} catch (CouponSystemException e) {
			// if customerId is not valid
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@GetMapping("/customers")
	public ResponseEntity<?> getCustomers(@RequestHeader String token) {
		try {
			List<Customer> customers = adminService.getCustomers();
			return ResponseEntity.ok(customers);
		} catch (JwtExpiredException e) {
			// if JWT issue
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		}
	}
}
