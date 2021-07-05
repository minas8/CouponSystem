package app.core.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;

import app.core.services.JwtUtilService;
import app.core.services.JwtUtilService.UserDetails.UserType;

public class LoginFilter implements Filter {

	public LoginFilter() {
		super();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		String method = req.getMethod();
		String token = req.getHeader("token");
		String acrh = req.getHeader("access-control-request-headers");
		String url = req.getRequestURI();

		UserType userType = null;
		String errorMsg = null;

		if (token != null) {
			try {
				// check if token is not expired and get userType from token
				userType = JwtUtilService.extractUserDetails(token).getUserType();

			} catch (Exception e) {
				// token expired
				errorMsg = "You are not authorized. See details: " + e.getMessage();
			}

			// if we are here => token is valid
			if (errorMsg == null) {

				// check if user sent his own token with the right privileges.
				// this is the message to send if not:
				String noPrivilegesMsg = "You do not have ";

				if (url.contains("/u-admin/") && !userType.equals(UserType.ADMIN)) {
					errorMsg = noPrivilegesMsg + userType.name().toLowerCase() + " privileges.";

				} else if (url.contains("/u-company/") && !userType.equals(UserType.COMPANY)) {
					errorMsg = noPrivilegesMsg + userType.name().toLowerCase() + " privileges.";

				} else if (url.contains("/u-customer/") && !userType.equals(UserType.CUSTOMER)) {
					errorMsg = noPrivilegesMsg + userType.name().toLowerCase() + " privileges.";
				}
			}

			if (errorMsg != null) {
				res.setHeader("UNAUTHORIZED", errorMsg);
				res.setHeader("Access-Control-Allow-Origin", "*");
				res.setHeader("Access-Control-Allow-Headers", "*");
				res.sendError(HttpStatus.UNAUTHORIZED.value(), errorMsg);
				return;
			} else {
				chain.doFilter(request, response);
			}

		} else {

			// token was not sent
			if (acrh != null && method.equals("OPTIONS")) {
				res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
				res.setHeader("Access-Control-Allow-Origin", "*");
				res.setHeader("Access-Control-Allow-Headers", "*");
				res.setStatus(HttpStatus.OK.value());
			} else {
				// if token was not sent and the user is trying to get to a restricted address
				if (url.contains("/api/")) {
					res.sendError(HttpStatus.UNAUTHORIZED.value(), "You are not logged in");
				} else {
					chain.doFilter(request, response);
				}
			}
		}
	}
}
