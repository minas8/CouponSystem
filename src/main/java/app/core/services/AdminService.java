package app.core.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import app.core.entities.Company;
import app.core.entities.Customer;
import app.core.exceptions.CouponSystemException;
import app.core.exceptions.ItemNotFoundException;
import app.core.exceptions.JwtExpiredException;
import app.core.exceptions.NotUniqueException;
import app.core.services.JwtUtilService.UserDetails.UserType;

@Service
@Transactional
public class AdminService extends ClientService {

	// constants
	private static final String EMAIL = "admin@admin.com";
	private static final String PASSWORD = "admin";

	// methods
	@Override
	public boolean login(String email, String password, UserType userType) throws CouponSystemException {
		// 1- make sure the email and the password sent are not null and the correct
		// userType is sent
		if (userType != UserType.ADMIN || email == null || password == null)
			throw new CouponSystemException(
					"Administrator login failed. Email or password or userType sent are null or not valid.");

		// 2- validate email and password
		if (EMAIL.equals(email) && PASSWORD.equals(password)) {
			return true;
		}

		return false;
	}

	// *** Company related methods *** //

	/**
	 * Add Company to the system. A company with the same name or email as an
	 * existing company cannot be added.
	 * 
	 * @param company a company to add
	 * @return the company added
	 * @throws CouponSystemException if company is null or if company name or email
	 *                               are not unique
	 */
	public Company addCompany(Company company) throws CouponSystemException {
		try {
			// 1- make sure the company sent is not null
			if (company == null)
				throw new CouponSystemException("addCompany failed. Company sent is null.");

			// 2- make sure the name and the email are unique as required in the system
			if (isCompanyUnique(company.getName(), company.getEmail()))
				// 3- add the company
				return companyRepository.save(company);
			else
				throw new NotUniqueException(
						"addCompany failed. Company name or email sent already exists in the sysytem. [name: "
								+ company.getName() + " email: " + company.getEmail() + "]");
		} catch (Exception e) {
			throw new CouponSystemException("addCompany failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Update the specified company in the system. The company's id and name cannot
	 * be updated. Both company name and company email must be unique.
	 * 
	 * @param company a company to update
	 * @return the company updated
	 * @throws CouponSystemException if company 1) is null 2) does not exist in the
	 *                               system 3) name or email are not unique
	 */
	public Company updateCompany(Company company) throws CouponSystemException {
		try {
			// 1- make sure the company sent is not null
			if (company == null)
				throw new CouponSystemException("updateCompany failed. Company sent is null.");

			// 2- make sure the company exists in the system
			Company companyFromDb = getCompany(company.getId());
			if (companyFromDb != null) {
				// 3- make sure the email is still unique as required in the system (same as in
				// addCompany method)
				if (isCompanyUnique(company.getEmail(), company.getId())) {
					// 4- fields which are forbidden to update : company id & name
					if (company.getEmail() != null)
						companyFromDb.setEmail(company.getEmail());
					if (company.getPassword() != null)
						companyFromDb.setPassword(company.getPassword());
					return companyFromDb;
				} else
					throw new NotUniqueException(
							"updateCompany failed. Company email sent already exists in the sysytem. [email: "
									+ company.getEmail() + "]");
			} else
				throw new ItemNotFoundException("updateCompany failed. Company with [id: " + company.getId()
						+ "] was not found in the system.");
		} catch (Exception e) {
			throw new CouponSystemException("updateCompany failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Delete the specified company in the system. In addition, the coupons created
	 * by the company must also be deleted, as well as the history of the purchase
	 * of the company's coupons by customers.
	 * 
	 * @param id id of the company to delete
	 * @throws CouponSystemException if id is not valid or company with the
	 *                               specified id does not exist in the system
	 */
	public void deleteCompany(int id) throws CouponSystemException {
		try {
			// 1- make sure the id sent is valid
			if (id <= 0)
				throw new CouponSystemException("deleteCompany failed. Id sent is not valid. [id: " + id + "]");

			// 2- make sure the company exists in the system
			if (!companyRepository.existsById(id))
				throw new ItemNotFoundException("deleteCompany failed. Company was not found in the system.");

			// 3- delete the company
			companyRepository.deleteById(id);
		} catch (Exception e) {
			throw new CouponSystemException("deleteCompany failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Get a company by id specified.
	 * 
	 * @param id id of the company to get
	 * @return a company if exists or null
	 * @throws CouponSystemException if id sent is not valid
	 */
	public Company getCompany(int id) throws CouponSystemException {
		// 1- make sure the id sent is valid
		if (id <= 0)
			throw new CouponSystemException("getCompany failed. id sent is not valid. [id: " + id + "]");

		// 2- get the company
		return (Company) getById(id, EntityType.Company);
	}

	/**
	 * Get a list of all the companies from the system or null if there are no
	 * companies.
	 * 
	 * @return a list of all the companies
	 */
	public List<Company> getCompanies() throws JwtExpiredException {
		List<Company> companies = companyRepository.findAll();
		return companies != null && companies.size() > 0 ? companies : null;
	}

	// *** Customer related methods *** //

	/**
	 * Add Customer to the system. A customer with the same email as an existing
	 * customer cannot be added.
	 * 
	 * @param customer customer to add
	 * @return the customer added
	 * @throws CouponSystemException if customer sent is null or if customer email
	 *                               is not unique
	 */
	public Customer addCustomer(Customer customer) throws CouponSystemException {
		try {
			// 1- make sure the customer sent is not null
			if (customer == null)
				throw new CouponSystemException("addCustomer failed. Customer sent is null.");

			// 2- make sure the email is unique as required in the system
			if (isCustomerUnique(customer.getEmail()))
				// 3- add the customer
				return customerRepository.save(customer);
			else
				throw new NotUniqueException(
						"addCustomer failed. Customer name or email sent already exists in the sysytem. [email: "
								+ customer.getEmail() + "]");
		} catch (Exception e) {
			throw new CouponSystemException("addCustomer failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Update the specified customer in the system. The customer's id cannot be
	 * updated. Customer email must be unique.
	 * 
	 * @param customer customer to update
	 * @return the customer updated
	 * @throws CouponSystemException if customer 1) is null 2) does not exist in the
	 *                               system 3) email is not unique
	 */
	public Customer updateCustomer(Customer customer) throws CouponSystemException {
		try {
			// 1- make sure the customer sent is not null
			if (customer == null)
				throw new CouponSystemException("updateCustomer failed. Customer sent is null.");

			// 2- make sure the customer exists in the system
			Customer customerFromDb = getCustomer(customer.getId());
			if (customerFromDb != null) {
				// 3- make sure the email is still unique as required in the system (same as in
				// addCompany method)
				if (isCustomerUnique(customer.getEmail(), customer.getId())) {
					// 4- fields which are forbidden to update : customer id
					if (customer.getFirstName() != null)
						customerFromDb.setFirstName(customer.getFirstName());
					if (customer.getLastName() != null)
						customerFromDb.setLastName(customer.getLastName());
					if (customer.getEmail() != null)
						customerFromDb.setEmail(customer.getEmail());
					if (customer.getPassword() != null)
						customerFromDb.setPassword(customer.getPassword());
					return customerFromDb;
				} else
					throw new NotUniqueException(
							"updateCustomer failed. Customer email sent already exists in the sysytem. [email: "
									+ customer.getEmail() + "]");
			} else
				throw new ItemNotFoundException("updateCustomer failed. Customer with [id: " + customer.getId()
						+ "] was not found in the system.");
		} catch (Exception e) {
			throw new CouponSystemException("updateCustomer failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Delete the specified customer in the system. The customer's purchase coupon
	 * history must also be deleted.
	 * 
	 * @param id id of the customer to delete
	 * @throws CouponSystemException if id is not valid or customer with the
	 *                               specified id does not exist in the system
	 */
	public void deleteCustomer(int id) throws CouponSystemException {
		try {
			// 1- make sure the id sent is valid
			if (id <= 0)
				throw new CouponSystemException("deleteCustomer failed. Id sent is not valid. [id: " + id + "]");

			// 2- make sure the customer exists in the system
			if (!customerRepository.existsById(id))
				throw new ItemNotFoundException("deleteCustomer failed. Customer was not found in the system.");

			// 3- delete the customer
			customerRepository.deleteById(id);
		} catch (Exception e) {
			throw new CouponSystemException("deleteCustomer failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Get a customer details by the customer id specified or null if not exists.
	 * 
	 * @param id id of the customer to get
	 * @return a customer if exists or null
	 * @throws CouponSystemException if id sent is not valid
	 */
	public Customer getCustomer(int id) throws CouponSystemException {
		// 1- make sure the id sent is valid
		if (id <= 0)
			throw new CouponSystemException("getCustomer failed. id sent is not valid. [id: " + id + "]");

		// 2- get the customer
		return (Customer) getById(id, EntityType.Customer);
	}

	/**
	 * Get a list of all the customers from the system or null if there are no
	 * customers.
	 * 
	 * @return a list of all the customers
	 */
	public List<Customer> getCustomers() throws JwtExpiredException {
		List<Customer> customers = customerRepository.findAll();
		return customers != null && customers.size() > 0 ? customers : null;
	}

	// *** Private methods *** //

	/**
	 * Check whether a company with the same email or name exists in storage. Every
	 * company must have a unique name and a unique email.
	 * 
	 * ** NOTE: Use this method when adding a company to the system, when id is not
	 * yet available.
	 * 
	 * @param name  company name
	 * @param email company email
	 * @return true if company is unique; false if not unique
	 * @throws CouponSystemException if name or email are null
	 */
	private boolean isCompanyUnique(String name, String email) throws CouponSystemException {
		// 1- make sure the name and the email are not null
		if (name == null || email == null)
			throw new CouponSystemException(
					"isCompanyUnique faild. Name or email sent are null. [name: " + name + ", email: " + email + "]");

		// 2- check if the name and the email are unique in the system
		Company company = companyRepository.findFirstByNameOrEmail(name, email);
		return company == null ? true : false;
	}

	/**
	 * Check whether a company with the same email exists in storage, except for the
	 * current company whose id is specified. Every company must have a unique
	 * email.
	 * 
	 * ** NOTE: Use this method when updating a company in the system, when id is
	 * available.
	 * 
	 * @param email company email
	 * @param id    current company id
	 * @return true if company is unique; false if not unique
	 * @throws CouponSystemException if email or id are null or not valid
	 */
	private boolean isCompanyUnique(String email, int id) throws CouponSystemException {
		// 1- make sure the email and the id are not null or not valid
		if (email == null || id <= 0)
			throw new CouponSystemException("isCompanyUnique failed. Email or id sent are null or not valid. [email: "
					+ email + ", id: " + id + "]");

		Company company = companyRepository.findFirstByEmailEqualsAndIdNot(email, id);
		return company == null ? true : false;
	}

	/**
	 * Check whether a customer with the same email exists in storage. Every
	 * customer must have a unique email.
	 * 
	 * ** NOTE: Use this method when adding a customer to the system, when id is not
	 * yet available.
	 * 
	 * @param email customer email
	 * @return true if customer is unique; false if not unique
	 * @throws CouponSystemException if email is null
	 */
	private boolean isCustomerUnique(String email) throws CouponSystemException {
		// 1- make sure the email is not null
		if (email == null)
			throw new CouponSystemException(
					"isCustomerUnique faild. Name or email sent are null. [email: " + email + "]");

		// 2- check if the email is unique in the system
		Customer customer = customerRepository.findFirstByEmail(email);
		return customer == null ? true : false;
	}

	/**
	 * Check whether a customer with the same email exists in storage, except for
	 * the current customer whose id is specified. Every customer must have a unique
	 * email.
	 * 
	 * ** NOTE: Use this method when updating a customer in the system, when id is
	 * available.
	 * 
	 * @param email customer email
	 * @param id    customer id
	 * @return true if customer is unique; false if not unique
	 * @throws CouponSystemException if email or id are null or not valid
	 */
	private boolean isCustomerUnique(String email, int id) throws CouponSystemException {
		// 1- make sure the email and the id are not null or not valid
		if (email == null || id <= 0)
			throw new CouponSystemException("isCustomerUnique failed. Email or id sent are null or not valid. [email: "
					+ email + ", id: " + id + "]");

		// 2- check if the email is unique in the system
		Customer customer = customerRepository.findFirstByEmailEqualsAndIdNot(email, id);
		return customer == null ? true : false;
	}

}
