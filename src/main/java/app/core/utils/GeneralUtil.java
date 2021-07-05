package app.core.utils;

import java.util.EnumSet;
import java.util.regex.Pattern;

import app.core.services.JwtUtilService.UserDetails.UserType;

/**
 * This is a util class which contains some helpful static methods.
 * 
 * @author Mina Shtraicher
 *
 */
public final class GeneralUtil {

	/**
	 * Don't let anyone instantiate this class.
	 */
	private GeneralUtil() {
		super();
	}

	/**
	 * Tests whether an enum contains the specified value.
	 * 
	 * @param <E>       the enum
	 * @param enumClass the class of the relevant enum
	 * @param value     a value whose presence in this enum is to be tested
	 * @return true if this enum contains the specified value | false otherwise
	 */
	public static <E extends Enum<E>> boolean containsValue(Class<E> enumClass, String value) {
		try {
			return EnumSet.allOf(enumClass).contains(Enum.valueOf(enumClass, value));
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Capitalize every word in the string sent.
	 * 
	 * @param str the string sent
	 * @return the string capitalized
	 */
	public static String capitalizeAll(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}

		return Pattern.compile("\\b(.)(.*?)\\b").matcher(str)
				.replaceAll(match -> match.group(1).toUpperCase() + match.group(2));
	}

	/**
	 * Test if a String parameter is null or empty
	 * 
	 * @param param String parameter
	 * @return true if parameter is null or empty | false otherwise
	 */
	public static boolean isParamNullOrEmpty(String param) {
		if (param == null || param.equals(""))
			return true;
		return false;
	}

	/**
	 * Test if a UserType parameter is null
	 * 
	 * @param userType UserType parameter
	 * @return true if parameter is null | false otherwise
	 */
	public static boolean isParamNullOrEmpty(UserType userType) {
		if (userType == null)
			return true;
		return false;
	}
}
