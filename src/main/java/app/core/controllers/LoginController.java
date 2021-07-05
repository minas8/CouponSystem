package app.core.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.core.entities.Company;
import app.core.entities.Customer;
import app.core.exceptions.CouponSystemException;
import app.core.login.LoginManager;
import app.core.services.ClientService;
import app.core.services.CompanyService;
import app.core.services.CustomerService;
import app.core.services.JwtUtilService;
import app.core.services.JwtUtilService.UserDetails;
import app.core.utils.GeneralUtil;

@RestController
@CrossOrigin
@RequestMapping("/auth")
public class LoginController {

	@Autowired
	private LoginManager loginManager;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody UserDetails user) {
		// 1. check if either of the method's parameters: email || password || user type
		// is null or empty
		if (user == null || GeneralUtil.isParamNullOrEmpty(user.getEmail())
				|| GeneralUtil.isParamNullOrEmpty(user.getPassword())
				|| GeneralUtil.isParamNullOrEmpty(user.getUserType())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("One or more of the parameters [email || password || user type] is null or empty");
		}

		try {
			// 2. login
			UserDetails userDetails = loginManager.login(user.getEmail(), user.getPassword(), user.getUserType());
			if (userDetails == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body("You do not have " + user.getUserType().name().toLowerCase() + " privileges.");
			}

			// if logged-in successfully:
			ClientService clientSrv = userDetails.getUserService();
			if (clientSrv != null) {

				// 3. set the user name and id after login
				switch (user.getUserType()) {
				case ADMIN:
					user.setName("Admin");
					user.setId(0);
					break;
				case COMPANY:
					Company company = ((CompanyService) clientSrv).getCompany(user.getEmail(), user.getPassword());
					user.setName(GeneralUtil.capitalizeAll(company.getName()));
					user.setId(company.getId());
					break;
				case CUSTOMER:
					Customer customer = ((CustomerService) clientSrv).getCustomer(user.getEmail(), user.getPassword());
					user.setName(GeneralUtil.capitalizeAll(customer.getFirstName() + " " + customer.getLastName()));
					user.setId(customer.getId());
					break;
				}

				// 4. generate a token
				String token = JwtUtilService.generateToken(user);
				// 5. set the user token
				user.setToken(token);

				// 6. return the logged-in user (with token) to the client
				return ResponseEntity.ok(user);
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No " + user.getUserType().name().toLowerCase()
						+ " user with the email and password sent exists in the system.");
			}
		} catch (CouponSystemException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("An error occurred: " + e.getMessage());
		}
	}

}
