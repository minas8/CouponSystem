package app.core.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import app.core.exceptions.CouponSystemException;
import app.core.services.AdminService;
import app.core.services.CompanyService;
import app.core.services.CustomerService;
import app.core.services.JwtUtilService.UserDetails;
import app.core.services.JwtUtilService.UserDetails.UserType;

@Component
public class LoginManager {

	// fields
	private ApplicationContext ctx;

	@Autowired
	public LoginManager(ApplicationContext ctx) {
		super();
		this.ctx = ctx;
	}

	// method

	/**
	 * Login the system by confirming both email and password sent according to the
	 * client type.
	 * 
	 * @param email    the email to confirm
	 * @param password the password to confirm
	 * @param userType one of the following user types: [Admin, Company, Customer]
	 * @return UserDetails with the relevant ClientService according to the email,
	 *         password and client type sent or null if not exists
	 * @throws CouponSystemException if the email or the password sent are null
	 */
	public UserDetails login(String email, String password, UserType userType) throws CouponSystemException {
		UserDetails user = new UserDetails(email, userType, password);
		switch (userType) {
		case ADMIN:
			AdminService adminSrv = ctx.getBean("adminService", AdminService.class);
			if (adminSrv.login(email, password, userType)) {
				user.setUserService(adminSrv);
				return user;
			}
			break;
		case COMPANY:
			CompanyService companySrv = ctx.getBean("companyService", CompanyService.class);
			if (companySrv.login(email, password, userType)) {
				user.setUserService(companySrv);
				return user;
			}
			break;
		case CUSTOMER:
			CustomerService customerSrv = ctx.getBean("customerService", CustomerService.class);
			if (customerSrv.login(email, password, userType)) {
				user.setUserService(customerSrv);
				return user;
			}
			break;
		}
		return null;
	}
}
