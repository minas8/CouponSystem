package app.core.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import app.core.entities.Coupon;
import app.core.entities.Coupon.Category;
import app.core.entities.Customer;
import app.core.exceptions.CouponSystemException;
import app.core.exceptions.ItemNotFoundException;
import app.core.exceptions.JwtExpiredException;
import app.core.exceptions.NotUniqueException;
import app.core.services.CustomerService;
import app.core.services.JwtUtilService;

@RestController
@CrossOrigin
@RequestMapping("/api/u-customer")
public class CustomerController extends ClientController {

	// field
	private CustomerService customerService;

	// CTOR
	@Autowired
	public CustomerController(CustomerService customerService) {
		super();
		this.customerService = customerService;
	}

	@PutMapping("/coupon/{couponId}")
	public void purchaseCoupon(@RequestHeader String token, @PathVariable int couponId) {
		try {
			// get the customer id
			int customerId = JwtUtilService.extractUserDetails(token).getId();
			customerService.purchaseCoupon(customerId, couponId);

		} catch (NotUniqueException e) {
			// if coupon was already purchased by the customer
			throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
		} catch (ItemNotFoundException e) {
			// if coupon was not found in the system or is sold out / expired
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (JwtExpiredException e) {
			// if JWT issue
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (CouponSystemException e) {
			// if coupon id is not valid
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@GetMapping("/coupons")
	public List<Coupon> getCustomerCoupons(@RequestHeader String token) {
		try {
			// get the customer id
			int customerId = JwtUtilService.extractUserDetails(token).getId();
			return customerService.getCustomerCoupons(customerId);
		} catch (JwtExpiredException e) {
			// if JWT issue
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (CouponSystemException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@GetMapping("/coupons-by-category/{category}")
	public List<Coupon> getCustomerCoupons(@RequestHeader String token, @PathVariable Category category) {
		try {
			int customerId = JwtUtilService.extractUserDetails(token).getId();
			return customerService.getCustomerCoupons(customerId, category);
		} catch (JwtExpiredException e) {
			// if JWT issue
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (CouponSystemException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@GetMapping("/coupons-up-to-price/{maxPrice}")
	public List<Coupon> getCustomerCoupons(@RequestHeader String token, @PathVariable double maxPrice) {
		try {
			int customerId = JwtUtilService.extractUserDetails(token).getId();
			return customerService.getCustomerCoupons(customerId, maxPrice);
		} catch (JwtExpiredException e) {
			// if JWT issue
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (CouponSystemException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@GetMapping("/coupons-can-purchase")
	public List<Coupon> getCouponsCustomerCanPurchase(@RequestHeader String token) {
		try {
			int customerId = JwtUtilService.extractUserDetails(token).getId();
			return customerService.getCouponsCustomerCanPurchase(customerId);
		} catch (JwtExpiredException e) {
			// if JWT issue
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (CouponSystemException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@GetMapping("/coupons-about-to-expire")
	public List<Coupon> getCouponsAboutToExpire(@RequestHeader String token) {
		try {
			int customerId = JwtUtilService.extractUserDetails(token).getId();
			return customerService.getCouponsAboutToExpire(customerId);
		} catch (JwtExpiredException e) {
			// if JWT issue
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (CouponSystemException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@GetMapping("/coupon/{couponId}")
	public Coupon getCoupon(@RequestHeader String token, @PathVariable int couponId) {
		try {
			return customerService.getCoupon(couponId);
		} catch (ItemNotFoundException e) {
			// if coupon with specified couponId does not exist in the system
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (JwtExpiredException e) {
			// if JWT issue
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (CouponSystemException e) {
			// if couponId is not valid
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@GetMapping
	public Customer getCustomerDetails(@RequestHeader String token) {
		try {
			int customerId = JwtUtilService.extractUserDetails(token).getId();
			return customerService.getCustomerDetails(customerId);
		} catch (JwtExpiredException e) {
			// if JWT issue
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (CouponSystemException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}
}
