package app.core.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import app.core.exceptions.CouponSystemException;
import app.core.repositories.CompanyRepository;
import app.core.repositories.CouponRepository;
import app.core.repositories.CustomerRepository;
import app.core.services.JwtUtilService.UserDetails.UserType;

public abstract class ClientService {

	@Autowired
	protected CompanyRepository companyRepository;
	@Autowired
	protected CustomerRepository customerRepository;
	@Autowired
	protected CouponRepository couponRepository;

	public enum EntityType {
		Company, Customer, Coupon
	}

	/**
	 * Login the system by confirming both email and password sent.
	 * 
	 * @param email    The email to confirm
	 * @param password The password to confirm
	 * @param userType The user type
	 * @return true if a user with the given email and password exists, false
	 *         otherwise.
	 * @throws CouponSystemException if the email or the password sent are null
	 */
	abstract boolean login(String email, String password, UserType userType) throws CouponSystemException;

	/**
	 * Get an entity by id specified or null if not exists.
	 * 
	 * @param id id of the entity to get
	 * @return an entity if exists or null
	 * @throws CouponSystemException if id sent is not valid
	 */
	protected Object getById(int id, EntityType entityType) throws CouponSystemException {
		try {
			// 1- make sure the id sent is valid
			if (id <= 0)
				throw new CouponSystemException("getById failed. id sent is not valid. [id: " + id + "]");

			// 2- get the entity
			Optional<?> opt = null;
			switch (entityType) {
			case Company:
				opt = companyRepository.findById(id);
				break;
			case Customer:
				opt = customerRepository.findById(id);
				break;
			case Coupon:
				opt = couponRepository.findById(id);
				break;
			}

			if (opt != null)
				if (opt.isPresent())
					return opt.get();
			return null;
		} catch (Exception e) {
			throw new CouponSystemException("getById failed.\n" + e);
		}
	}

}
