package app.core.services;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import app.core.exceptions.JwtExpiredException;
import app.core.services.JwtUtilService.UserDetails.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * This is a util class for JWT authentication.
 */
@Service
public final class JwtUtilService {

	private static String signatureAlgorithm = SignatureAlgorithm.HS256.getJcaName();
	private static String encodedSecretKey = "this+is+my+key+and+it+must+be+at+least+256+bits+long";
	private static Key decodedSecretKey = new SecretKeySpec(Base64.getDecoder().decode(encodedSecretKey),
			signatureAlgorithm);

	/**
	 * Don't let anyone instantiate this class.
	 */
	private JwtUtilService() {
		super();
	}

	public static String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("userId", userDetails.id);
		claims.put("userType", userDetails.userType);
		return createToken(claims, userDetails.email);
	}

	private static String createToken(Map<String, Object> claims, String subject) {

		Instant now = Instant.now();
		return Jwts.builder().setClaims(claims)

				.setSubject(subject)

				.setIssuedAt(Date.from(now))

				.setExpiration(Date.from(now.plus(10, ChronoUnit.HOURS)))

				.signWith(decodedSecretKey)

				.compact();
	}

	private static Claims extractAllClaims(String token) throws JwtExpiredException {
		try {
			JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(decodedSecretKey).build();
			return jwtParser.parseClaimsJws(token).getBody();
		} catch (ExpiredJwtException e) {
			throw new JwtExpiredException("An issue with the token sent. See details: " + e);
		}
	}

	/**
	 * returns the JWT subject - in our case the email address
	 * 
	 * @throws JwtExpiredException
	 */
	public static String extractUsername(String token) throws JwtExpiredException {
		return extractAllClaims(token).getSubject();
	}

	public static Date extractExpiration(String token) throws JwtExpiredException {
		return extractAllClaims(token).getExpiration();
	}

	public static UserDetails extractUserDetails(String token) throws JwtExpiredException {
		UserDetails user = new UserDetails();
		Map<String, Object> claims = extractAllClaims(token);
		user.setEmail(extractAllClaims(token).getSubject());
		user.setId(Integer.parseInt(claims.get("userId").toString()));
		user.setUserType(UserType.valueOf(claims.get("userType").toString()));
		return user;
	}

	private static boolean isTokenExpired(String token) throws JwtExpiredException {
		try {
			extractAllClaims(token);
			return false;
		} catch (ExpiredJwtException e) {
			return true;
		}
	}

	/**
	 * returns true if the user (email) in the specified token equals the one in the
	 * specified user details and the token is not expired
	 * 
	 * @throws JwtExpiredException
	 */
	public static boolean validateToken(String token, UserDetails userDetails) throws JwtExpiredException {
		final String username = extractUsername(token);
		return (username.equals(userDetails.email) && !isTokenExpired(token));
	}

	public static class UserDetails {
		private int id;
		private String email;
		private UserType userType;
		private String name;
		private String password;
		private String token;
		private ClientService userService;

		public enum UserType {
			ADMIN, COMPANY, CUSTOMER
		}

		public UserDetails() {
			super();
		}

		public UserDetails(String email, UserType userType, String password) {
			super();
			this.email = email;
			this.userType = userType;
			this.password = password;
		}

		public int getId() {
			return id;
		}

		public String getEmail() {
			return email;
		}

		public UserType getUserType() {
			return userType;
		}

		public String getName() {
			return name;
		}

		public String getPassword() {
			return password;
		}

		public String getToken() {
			return token;
		}

		public ClientService getUserService() {
			return userService;
		}

		public void setId(int id) {
			this.id = id;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public void setUserType(UserType userType) {
			this.userType = userType;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public void setUserService(ClientService userService) {
			this.userService = userService;
		}
	}
}
