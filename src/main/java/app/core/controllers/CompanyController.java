package app.core.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import app.core.entities.Company;
import app.core.entities.Coupon;
import app.core.entities.Coupon.Category;
import app.core.exceptions.CouponSystemException;
import app.core.exceptions.FileStorageException;
import app.core.exceptions.ItemNotFoundException;
import app.core.exceptions.JwtExpiredException;
import app.core.exceptions.NotUniqueException;
import app.core.payloads.CouponPayload;
import app.core.services.CompanyService;
import app.core.services.JwtUtilService;

@RestController
@CrossOrigin
@RequestMapping("/api/u-company")
public class CompanyController extends ClientController {

	// fields
	private CompanyService companyService;

	// CTOR
	@Autowired
	public CompanyController(CompanyService companyService) {
		super();
		this.companyService = companyService;
	}

	@PostMapping("/coupon")
	public CouponPayload addCoupon(@RequestHeader String token, @ModelAttribute CouponPayload couponPayload) {
		try {
			// get the company id
			int companyId = JwtUtilService.extractUserDetails(token).getId();
			return companyService.addCoupon(companyId, couponPayload);

		} catch (NotUniqueException e) {
			// if coupon's title is not unique
			throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
		} catch (FileStorageException e) {
			// if coupon's title is not unique
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
		} catch (JwtExpiredException e) {
			// if JWT issue
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (CouponSystemException e) {
			// if coupon is null
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@PutMapping("/coupon")
	public CouponPayload updateCoupon(@RequestHeader String token, @ModelAttribute CouponPayload couponPayload) {
		try {
			// get the company id
			int companyId = JwtUtilService.extractUserDetails(token).getId();

			return companyService.updateCoupon(companyId, couponPayload);

		} catch (ItemNotFoundException e) {
			// if coupon was not found in the system
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (NotUniqueException e) {
			// if coupon's title is not unique
			throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
		} catch (FileStorageException e) {
			// if coupon's title is not unique
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
		} catch (JwtExpiredException e) {
			// if JWT issue
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (CouponSystemException e) {
			// if coupon is null
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
		}
	}

	@DeleteMapping("/coupon/{couponId}")
	public void deleteCoupon(@RequestHeader String token, @PathVariable int couponId) {
		try {
			companyService.deleteCoupon(couponId);
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

	@GetMapping("/coupon/{couponId}")
	public Coupon getCoupon(@RequestHeader String token, @PathVariable int couponId) {
		try {
			return companyService.getCoupon(couponId);
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

	@GetMapping("/coupons")
	public List<Coupon> getCompanyCoupons(@RequestHeader String token) {
		try {
			int companyId = JwtUtilService.extractUserDetails(token).getId();
			return companyService.getCompanyCoupons(companyId);
		} catch (JwtExpiredException e) {
			// if JWT issue
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (CouponSystemException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@GetMapping("/coupons-by-category/{category}")
	public List<Coupon> getCompanyCoupons(@RequestHeader String token, @PathVariable Category category) {
		try {
			int companyId = JwtUtilService.extractUserDetails(token).getId();
			return companyService.getCompanyCoupons(companyId, category);
		} catch (JwtExpiredException e) {
			// if JWT issue
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (CouponSystemException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@GetMapping("/coupons-up-to-price/{maxPrice}")
	public List<Coupon> getCompanyCoupons(@RequestHeader String token, @PathVariable double maxPrice) {
		try {
			int companyId = JwtUtilService.extractUserDetails(token).getId();
			return companyService.getCompanyCoupons(companyId, maxPrice);
		} catch (JwtExpiredException e) {
			// if JWT issue
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (CouponSystemException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@GetMapping
	public Company getCompanyDetails(@RequestHeader String token) {
		try {
			int companyId = JwtUtilService.extractUserDetails(token).getId();
			return companyService.getCompanyDetails(companyId);
		} catch (JwtExpiredException e) {
			// if JWT issue
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
		} catch (CouponSystemException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}
}
