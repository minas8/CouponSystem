package app.core.services;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import app.core.entities.Coupon;
import app.core.entities.Coupon.Category;
import app.core.entities.Customer;
import app.core.exceptions.CouponSystemException;
import app.core.exceptions.ItemNotFoundException;
import app.core.exceptions.NotUniqueException;
import app.core.services.JwtUtilService.UserDetails.UserType;

@Service
@Transactional
public class CustomerService extends ClientService {

	// methods
	@Override
	public boolean login(String email, String password, UserType userType) throws CouponSystemException {
		if (userType != UserType.CUSTOMER || email == null || password == null) {
			throw new CouponSystemException(
					"Customer login failed. Email or password or userType sent are null or not valid.");
		}

		// if customer exists (validate email and password) => return true
		Customer customer = getCustomer(email, password);
		if (customer != null) {
			return true;
		}

		// else => return false
		return false;
	}

	/**
	 * Purchase a coupon if exists. It is not allowed to purchase the same coupon
	 * more than once. The coupon cannot be purchased if its quantity is 0 or its
	 * expiration date has already been reached.
	 * 
	 * @param couponId a coupon to purchase
	 * @throws NotUniqueException    if coupon was already purchased by the customer
	 * @throws ItemNotFoundException if coupon was not found in the system or is
	 *                               sold out / expired
	 * @throws CouponSystemException if coupon id sent is not valid
	 */
	public void purchaseCoupon(int customerId, int couponId)
			throws NotUniqueException, ItemNotFoundException, CouponSystemException {
		try {
			// 1- check if coupon sent is null
			if (couponId <= 0 || customerId <= 0)
				throw new CouponSystemException("purchaseCoupon failed. Coupon id sent is not valid.");

			// 2- make sure the coupon exists in the system
			Coupon couponFromDB = getCoupon(couponId);
			if (couponFromDB == null)
				throw new ItemNotFoundException(
						"purchaseCoupon failed. Coupon with [id: " + couponId + "] was not found.");

			// 3- check if the same coupon was already purchased
			boolean isPurchased = couponRepository.existsByIdAndCustomersId(couponId, customerId);
			if (!isPurchased) {
				// 4- check if coupon's quantity > 0 AND its expiration date has not been
				// reached yet
				if (couponFromDB.getAmount() > 0 && (couponFromDB.getEndDate().isAfter(LocalDate.now())
						|| couponFromDB.getEndDate().isEqual(LocalDate.now()))) {

					Customer customer = getCustomerDetails(customerId);
					customer.addCoupon(couponFromDB);

					// 5- reduce the coupon's quantity in the system by 1
					couponFromDB.setAmount(couponFromDB.getAmount() - 1);

				} else
					throw new ItemNotFoundException(
							"purchaseCoupon failed. Coupon with [id: " + couponId + "] is sold out or expired.");

			} else {
				throw new NotUniqueException(
						"purchaseCoupon failed. Coupon with [id: " + couponId + "] already purchased by customer.");
			}
		} catch (Exception e) {
			throw new CouponSystemException("purchaseCoupon failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Get all customer's coupons or null if customer has no coupons.
	 * 
	 * @return a list of all customer's coupons
	 * @throws CouponSystemException if customer id is invalid
	 */
	public List<Coupon> getCustomerCoupons(int customerId) throws CouponSystemException {
		try {
			if (customerId <= 0)
				throw new CouponSystemException(
						"getCustomerCoupons failed. Customer id sent is not invalid. [customerId: " + customerId + "]");

			List<Coupon> coupons = couponRepository.findAllByCustomersId(customerId, Sort.by("id"));
			return coupons != null && coupons.size() > 0 ? coupons : null;
		} catch (Exception e) {
			throw new CouponSystemException("getCustomerCoupons failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Get all customer's coupons for the specified category or null if customer has
	 * no coupons for that category.
	 * 
	 * @param category the category according to which to receive the coupons
	 * @return a list of customer's coupons by specific category
	 * @throws CouponSystemException if customer id is invalid
	 */
	public List<Coupon> getCustomerCoupons(int customerId, Category category) throws CouponSystemException {
		try {
			if (customerId <= 0 || category == null)
				throw new CouponSystemException(
						"getCustomerCoupons failed. Customer id or category sent are null or invalid. [customerId: "
								+ customerId + " category: " + category + "]");

			List<Coupon> coupons = couponRepository.findAllByCustomersIdAndCategory(customerId, category,
					Sort.by("id"));
			return coupons != null && coupons.size() > 0 ? coupons : null;
		} catch (Exception e) {
			throw new CouponSystemException("getCustomerCoupons failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Get all customer's coupons up to a specific max price or null if customer has
	 * no coupons up to that max price.
	 * 
	 * @param maxPrice the max price according to which to receive the coupons
	 * @return a list of customer's coupons up to the max price specified
	 * @throws CouponSystemException if customer id is invalid
	 */
	public List<Coupon> getCustomerCoupons(int customerId, double maxPrice) throws CouponSystemException {
		try {
			if (customerId <= 0)
				throw new CouponSystemException(
						"getCustomerCoupons failed. Customer id sent is invalid. [customerId: " + customerId + "]");

			List<Coupon> coupons = couponRepository.findAllByCustomersIdAndPriceLessThanEqual(customerId, maxPrice,
					Sort.by("price"));
			return coupons != null && coupons.size() > 0 ? coupons : null;
		} catch (Exception e) {
			throw new CouponSystemException("getCustomerCoupons failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Get all coupons that the customer can purchase.
	 * 
	 * @return a list of coupons
	 * @throws CouponSystemException if customer id is invalid
	 */
	public List<Coupon> getCouponsCustomerCanPurchase(int customerId) throws CouponSystemException {
		try {
			if (customerId <= 0)
				throw new CouponSystemException(
						"getCouponsCustomerCanPurchase failed. Customer id sent is invalid. [customerId: " + customerId
								+ "]");

			List<Coupon> coupons = couponRepository.findAllCouponsCustomerCanPurchase(0, LocalDate.now(), customerId);
			return coupons != null && coupons.size() > 0 ? coupons : null;
		} catch (Exception e) {
			throw new CouponSystemException("getCouponsCustomerCanPurchase failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Get all coupons that the customer can purchase and are about to expire
	 * tomorrow.
	 * 
	 * @return a list of coupons
	 * @throws CouponSystemException if customer id is invalid
	 */
	public List<Coupon> getCouponsAboutToExpire(int customerId) throws CouponSystemException {
		try {
			if (customerId <= 0)
				throw new CouponSystemException(
						"getCouponsAboutToExpire failed. Customer id sent is invalid. [customerId: " + customerId
								+ "]");

			List<Coupon> coupons = couponRepository.findAllCouponsAboutToExpire(LocalDate.now(), customerId);
			return coupons != null && coupons.size() > 0 ? coupons : null;
		} catch (Exception e) {
			throw new CouponSystemException("getCouponsAboutToExpire failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Get a customer details by the customer id specified or null if not exists.
	 * 
	 * @param customerId
	 * 
	 * @return a customer if exists or null
	 * @throws CouponSystemException if customer id sent is not valid
	 */
	public Customer getCustomerDetails(int customerId) throws CouponSystemException {
		try {
			if (customerId <= 0)
				throw new CouponSystemException(
						"getCustomerDetails failed. Customer id sent is invalid. [customerId: " + customerId + "]");

			return (Customer) getById(customerId, EntityType.Customer);
		} catch (Exception e) {
			throw new CouponSystemException("getCustomerDetails failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Get a customer by email and password specified.
	 * 
	 * @param email
	 * @param password
	 * @return a customer if exists or null
	 * @throws CouponSystemException if email or password are null
	 */
	public Customer getCustomer(String email, String password) throws CouponSystemException {
		try {
			// 1- make sure the email and the password sent are not null
			if (email == null || password == null)
				throw new CouponSystemException("getCustomer failed. Email or password sent are null. [email: " + email
						+ ", password: " + password);

			// 2. return the customer
			return customerRepository.findByEmailAndPassword(email, password);
		} catch (Exception e) {
			throw new CouponSystemException("getCustomer failed.\n" + e);
		}
	}

	/**
	 * Get coupon details by the coupon id specified or null if not exists.
	 * 
	 * @param id id of the coupon to get
	 * @return a coupon with the id specified
	 * @throws CouponSystemException if id is not valid
	 */
	public Coupon getCoupon(int id) throws CouponSystemException {
		try {
			// 1- make sure the id sent is valid
			if (id <= 0)
				throw new CouponSystemException("getCoupon failed. id sent is not valid. [id: " + id + "]");

			// 2- get the coupon
			return (Coupon) getById(id, EntityType.Coupon);

		} catch (Exception e) {
			throw new CouponSystemException("getCoupon failed. Inner error: " + e.getMessage());
		}
	}

}
