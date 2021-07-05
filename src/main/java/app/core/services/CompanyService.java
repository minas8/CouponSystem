package app.core.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.transaction.Transactional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import app.core.entities.Company;
import app.core.entities.Coupon;
import app.core.entities.Coupon.Category;
import app.core.exceptions.CouponSystemException;
import app.core.exceptions.FileStorageException;
import app.core.exceptions.ItemNotFoundException;
import app.core.exceptions.NotUniqueException;
import app.core.payloads.CouponPayload;
import app.core.services.JwtUtilService.UserDetails.UserType;

@Service
@Transactional
public class CompanyService extends ClientService {

	// fields
	private static final String IMGBB_API_KEY = "ccd329f09a7866bc1a431dbac0737958";
	private static final String IMGBB_DEFAULT_IMG = "https://i.ibb.co/9bNWnXc/design-2461957-640.png";

	// methods
	@Override
	public boolean login(String email, String password, UserType userType) throws CouponSystemException {
		if (userType != UserType.COMPANY || email == null || password == null) {
			throw new CouponSystemException(
					"Company login failed. Email or password or userType sent are null or not valid.");
		}

		// if company exists (validate email and password) => return true
		Company company = getCompany(email, password);
		if (company != null) {
			return true;
		}

		// else => return false
		return false;
	}

	/**
	 * Add coupon to the system. A coupon, with the same title as an existing
	 * coupon, of the same company, cannot be added.
	 * 
	 * @param coupon coupon to add
	 * @return the coupon added
	 * @throws NotUniqueException    if coupon title is not unique
	 * @throws FileStorageException  if coupon image file failed to save
	 * @throws CouponSystemException if company id or coupon sent are null or
	 *                               invalid
	 */
	public CouponPayload addCoupon(int companyId, CouponPayload couponToAdd)
			throws NotUniqueException, FileStorageException, CouponSystemException {
		try {
			// 1. make sure coupon sent is not null and company id is valid
			if (companyId <= 0 || couponToAdd == null)
				throw new CouponSystemException("addCoupon failed. Company id or coupon sent are null or invalid.");

			// 2- make sure the coupon title is unique as required in the system
			if (isCouponUnique(companyId, couponToAdd.getTitle())) {

				// 3- upload the coupon image to IMGBB and get the file URL
				String fileUrl = uploadImageToImgbb(couponToAdd.imageFile);
				// 4- set the file URL in the coupon object
				couponToAdd.setImage(fileUrl);

				Coupon coupon = createCouponFromPayload(couponToAdd);

				// 5- add the coupon to company coupons
				Company comp = getCompanyDetails(companyId);
				comp.addCoupon(coupon);

				// Set the ID to update the client side
				couponToAdd.setId("" + coupon.getId());

				// No need to return the file to client side
				couponToAdd.setImageFile(null);
				return couponToAdd;
			} else
				throw new NotUniqueException(
						"addCoupon failed. Coupon title sent already exists in the sysytem. [title: "
								+ couponToAdd.getTitle() + "]");
		} catch (Exception e) {
			throw new CouponSystemException("addCoupon failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Update coupon in the system. Coupon id and company id cannot be updated.
	 * Coupon title must be unique for the company.
	 * 
	 * @param coupon coupon to update
	 * @return the coupon updated
	 * @throws NotUniqueException    if coupon title is not unique
	 * @throws FileStorageException  if coupon image file failed to save
	 * @throws ItemNotFoundException if coupon was not found in the system
	 * @throws CouponSystemException if company id or coupon sent are null or
	 *                               invalid
	 */
	public CouponPayload updateCoupon(int companyId, CouponPayload couponToUpdate)
			throws NotUniqueException, FileStorageException, ItemNotFoundException, CouponSystemException {
		try {
			// 1. make sure coupon sent is not null and company id is valid
			if (companyId <= 0 || couponToUpdate == null)
				throw new CouponSystemException("updateCoupon failed. Company id or coupon sent are null or invalid.");

			// 2- make sure the coupon exists in the system
			int id = Integer.parseInt(couponToUpdate.getId());
			Coupon couponFromDb = getCoupon(id);
			if (couponFromDb != null) {
				// 3- make sure the coupon title is unique as required in the system
				if (isCouponUnique(companyId, couponToUpdate.getTitle(), id)) {

					if (couponToUpdate.imageFile != null) {
						// 4- upload the coupon image to IMGBB and get the file URL
						String fileUrl = uploadImageToImgbb(couponToUpdate.imageFile);
						// 5- set the file URL in the coupon object
						couponToUpdate.setImage(fileUrl);
					}

//					// 4- save the new coupon file, if sent, and get the file name
//					if (couponToUpdate.imageFile != null) {
//						try {
//							String fileName = storageService.storeFile(couponToUpdate.imageFile);
//
//							// 5- set the file name in the coupon object
//							couponToUpdate.setImage(fileName);
//						} catch (FileStorageException e) {
//							throw new FileStorageException("updateCoupon failed. Inner error: " + e.getMessage());
//						}
//					}

					Coupon coupon = createCouponFromPayload(couponToUpdate);

					// 6- update the coupon; Fields which are forbidden to update : coupon id
					if (coupon.getCategory() != null)
						couponFromDb.setCategory(coupon.getCategory());
					if (coupon.getTitle() != null)
						couponFromDb.setTitle(coupon.getTitle());
					if (coupon.getDescription() != null)
						couponFromDb.setDescription(coupon.getDescription());
					if (coupon.getStartDate() != null)
						couponFromDb.setStartDate(coupon.getStartDate());
					if (coupon.getEndDate() != null)
						couponFromDb.setEndDate(coupon.getEndDate());
					if (coupon.getAmount() >= 0)
						couponFromDb.setAmount(coupon.getAmount());
					if (coupon.getPrice() >= 0)
						couponFromDb.setPrice(coupon.getPrice());
					if (coupon.getImage() != null)
						couponFromDb.setImage(coupon.getImage());

					// No need to return the file to client side
					couponToUpdate.setImageFile(null);
					return couponToUpdate;
				} else
					throw new NotUniqueException(
							"updateCoupon failed. Coupon title sent already exists in the sysytem. [title: "
									+ couponToUpdate.getTitle() + "]");
			} else
				throw new ItemNotFoundException("updateCoupon failed. Coupon was not found in the system.");
		} catch (Exception e) {
			throw new CouponSystemException("updateCoupon failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Delete the specified coupon in the system. In addition, the history of the
	 * purchase of the coupon by customers must also be deleted.
	 * 
	 * @param id id of the coupon to delete
	 * @throws CouponSystemException if id is invalid or if coupon does not exist in
	 *                               the system
	 */
	public void deleteCoupon(int id) throws CouponSystemException {
		try {
			// 1- make sure the id sent is valid
			if (id <= 0)
				throw new CouponSystemException("deleteCoupon failed. Id sent is invalid. [id: " + id + "]");

			// 2- make sure the coupon exists in the system
			if (!couponRepository.existsById(id))
				throw new ItemNotFoundException("deleteCoupon failed. Coupon was not found in the system.");

			// 3- delete the coupon
			couponRepository.deleteById(id);
		} catch (Exception e) {
			throw new CouponSystemException("deleteCoupon failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Get coupon details by the coupon id specified or null if not exists.
	 * 
	 * @param id id of the coupon to get
	 * @return a coupon with the id specified
	 * @throws CouponSystemException if id is invalid
	 */
	public Coupon getCoupon(int id) throws CouponSystemException {
		try {
			// 1- make sure the id sent is valid
			if (id <= 0)
				throw new CouponSystemException("getCoupon failed. id sent is invalid. [id: " + id + "]");

			// 2- get the coupon
			return (Coupon) getById(id, EntityType.Coupon);
		} catch (Exception e) {
			throw new CouponSystemException("getCoupon failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Get all company's coupons or null if company has no coupons.
	 * 
	 * @return a list of all company's coupons
	 * @throws CouponSystemException if company id is invalid
	 */
	public List<Coupon> getCompanyCoupons(int companyId) throws CouponSystemException {
		try {
			if (companyId <= 0)
				throw new CouponSystemException(
						"getCompanyCoupons failed. Company id sent is invalid. [companyId: " + companyId + "]");

			List<Coupon> coupons = couponRepository.findAllByCompanyId(companyId, Sort.by("id"));
			return coupons != null && coupons.size() > 0 ? coupons : null;
		} catch (Exception e) {
			throw new CouponSystemException("getCompanyCoupons failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Get all company's coupons for the specified category or null if company has
	 * no coupons for that category.
	 * 
	 * @param category the category according to which to receive the coupons
	 * @return a list of company's coupons by specific category
	 * @throws CouponSystemException if company id or category sent are null or
	 *                               invalid
	 */
	public List<Coupon> getCompanyCoupons(int companyId, Category category) throws CouponSystemException {
		try {
			if (companyId <= 0 || category == null)
				throw new CouponSystemException(
						"getCompanyCoupons failed. Company id or category sent are null or invalid. [companyId: "
								+ companyId + " category: " + category + "]");

			List<Coupon> coupons = couponRepository.findAllByCompanyIdAndCategory(companyId, category, Sort.by("id"));
			return coupons != null && coupons.size() > 0 ? coupons : null;
		} catch (Exception e) {
			throw new CouponSystemException("getCompanyCoupons failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Get all company's coupons up to a specific max price or null if company has
	 * no coupons up to that max price.
	 * 
	 * @param maxPrice The max price according to which to receive the coupons
	 * @return a list of company's coupons up to the max price specified
	 * @throws CouponSystemException if company id or maxPrice sent are invalid
	 */
	public List<Coupon> getCompanyCoupons(int companyId, double maxPrice) throws CouponSystemException {
		try {
			if (companyId <= 0 || maxPrice <= 0)
				throw new CouponSystemException(
						"getCompanyCoupons failed. Company id or maxPrice sent are invalid. [companyId: " + companyId
								+ " maxPrice: " + maxPrice + "]");

			List<Coupon> coupons = couponRepository.findAllByCompanyIdAndPrice(companyId, maxPrice, Sort.by("price"));
			return coupons != null && coupons.size() > 0 ? coupons : null;
		} catch (Exception e) {
			throw new CouponSystemException("getCompanyCoupons failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Get the company details or null if not exists.
	 * 
	 * @return a company if exists or null
	 * @throws CouponSystemException if company id sent is invalid
	 */
	public Company getCompanyDetails(int companyId) throws CouponSystemException {
		try {
			if (companyId <= 0)
				throw new CouponSystemException(
						"getCompanyDetails failed. Company id sent is invalid. [companyId: " + companyId + "]");

			return (Company) getById(companyId, EntityType.Company);
		} catch (Exception e) {
			throw new CouponSystemException("getCompanyDetails failed. Inner error: " + e.getMessage());
		}
	}

	/**
	 * Get a company by email and password specified.
	 * 
	 * @param email    company email
	 * @param password company password
	 * @return a company if exists or null
	 * @throws CouponSystemException if email or password are null
	 */
	public Company getCompany(String email, String password) throws CouponSystemException {
		try {
			// 1- make sure the email and the password sent are not null
			if (email == null || password == null)
				throw new CouponSystemException("getCompany failed. Email or password sent are null. [email: " + email
						+ ", password: " + password);

			// 2. return the company
			return companyRepository.findByEmailAndPassword(email, password);
		} catch (Exception e) {
			throw new CouponSystemException("getCompany failed.\n" + e);
		}
	}

	// *** Private methods *** //

	/**
	 * Check whether the company already has a coupon with the same title, as
	 * specified.
	 * 
	 * ** NOTE: Use this method when adding a coupon to the system, when coupon id
	 * is not yet available.
	 * 
	 * @param title title of the coupon to check
	 * @return true if the company already has a coupon with the title specified;
	 *         false if does not
	 * @throws CouponSystemException if coupon title or company id sent are null or
	 *                               invalid
	 */
	private boolean isCouponUnique(int companyId, String title) throws CouponSystemException {
		try {
			// 1- make sure the title is not null
			if (companyId <= 0 || title == null)
				throw new CouponSystemException(
						"isCouponUnique faild. Title or company id sent are null or invalid. [title: " + title
								+ " companyId: " + companyId + "]");

			// 2- check if the title is unique for current company
			Coupon coupon = couponRepository.findFirstByTitleAndCompanyId(title, companyId);
			return coupon == null ? true : false;
		} catch (Exception e) {
			throw new CouponSystemException("isCouponUnique failed.\n" + e);
		}
	}

	/**
	 * Check whether the company already has a coupon with the same title, as
	 * specified.
	 * 
	 * ** NOTE: Use this method when updating a coupon to the system, when coupon id
	 * is available.
	 * 
	 * @param title title of the coupon to check
	 * @param id    id of the coupon to check
	 * @return true if the company already has a coupon with the title specified;
	 *         false if does not
	 * @throws CouponSystemException if coupon title or id or company id sent are
	 *                               null or invalid
	 */
	private boolean isCouponUnique(int companyId, String title, int id) throws CouponSystemException {
		try {
			// 1- make sure the title is not null
			if (title == null || id <= 0 || companyId <= 0)
				throw new CouponSystemException(
						"isCouponUnique faild. Coupon title or id or company id sent are null or invalid. [title: "
								+ title + " id: " + id + " companyId: " + companyId + "]");

			// 2- check if the title is unique in the system
			Coupon coupon = couponRepository.findFirstByTitleAndCompanyIdAndIdNot(title, companyId, id);
			return coupon == null ? true : false;
		} catch (Exception e) {
			throw new CouponSystemException("isCouponUnique failed.\n" + e);
		}
	}

	/**
	 * Returns a coupon object from coupon's payload
	 * 
	 * @param couponPayload
	 * @return a coupon object
	 */
	private Coupon createCouponFromPayload(CouponPayload couponPayload) {
		Coupon coupon = null;
		try {
			coupon = new Coupon();

			if (couponPayload.category != null)
				coupon.setCategory(Category.valueOf(couponPayload.category));

			if (couponPayload.title != null)
				coupon.setTitle(couponPayload.title);

			if (couponPayload.description != null)
				coupon.setDescription(couponPayload.description);

			if (couponPayload.startDate != null) {
				LocalDate ld = getLocalDate(couponPayload.startDate);
				coupon.setStartDate(ld);
			}

			if (couponPayload.endDate != null) {
				LocalDate ld = getLocalDate(couponPayload.endDate);
				coupon.setEndDate(ld);
			}

			if (couponPayload.amount != null) {
				coupon.setAmount(Integer.parseInt(couponPayload.amount));
			}

			if (couponPayload.price != null) {
				coupon.setPrice(Double.parseDouble(couponPayload.price));
			}

			if (couponPayload.getId() != null) {
				coupon.setId(Integer.parseInt(couponPayload.getId()));
			}
			if (couponPayload.getImage() != null) {
				coupon.setImage(couponPayload.getImage());
			}
		} catch (Exception e) {
			System.out.println("--> createCouponFromPayload faild.\n" + e);

		}
		return coupon;
	}

	/**
	 * Formatter for dates of pattern 'EEE MMM dd yyyy HH:mm:ss 'GMT'xx (zzzz)'.
	 * 
	 * for example this date: 'Jun 15 2021 03:20:00 GMT+0300 (Israel Daylight Time)'
	 * 
	 * @param date a date as string
	 * @return a date as LocalDate
	 */
	private LocalDate getLocalDate(String date) {
		// a formatter for dates from client side with the following format:
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'xx (zzzz)",
				Locale.ROOT);

		LocalDate localDate = LocalDate.now();
		try {
			localDate = LocalDate.parse(date, formatter);
		} catch (Exception e) {
			// if the date is already formatted to pattern of 'yyyy-MM-dd' => just parse
			// string to LocalDate
			localDate = LocalDate.parse(date);
		}

		return localDate;
	}

	private String uploadImageToImgbb(MultipartFile file) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("image", file.getResource());
			HttpEntity<MultiValueMap<String, Object>> reqEntity = new HttpEntity<>(body, headers);
			String storageUrl = "https://api.imgbb.com/1/upload?key=" + IMGBB_API_KEY;
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> res = restTemplate.postForEntity(storageUrl, reqEntity, String.class);
			String json = res.getBody();
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(json);
			JSONObject data = (JSONObject) jsonObject.get("data");
			return (String) data.get("url");
		} catch (ParseException e) {
			// if something went wrong with uploading the image => return a default image
			return IMGBB_DEFAULT_IMG;
		}
	}

}
