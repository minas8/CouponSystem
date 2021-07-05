package app.core.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import app.core.entities.Coupon;
import app.core.entities.Coupon.Category;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {

	// *** Daily job related method *** //

	int deleteAllByEndDateBefore(LocalDate endDate);

	// *** Company related methods *** //

	// Company-Coupon relations are One-To-Many unidirectional
	// => Therefore, a @Query is needed for company_id field

	@Query("from Coupon where title=:title and company_id=:companyId")
	Coupon findFirstByTitleAndCompanyId(String title, int companyId);

	@Query("from Coupon where title=:title and company_id=:companyId and id!=:id")
	Coupon findFirstByTitleAndCompanyIdAndIdNot(String title, int companyId, int id);

	@Query("from Coupon where company_id=:companyId")
	List<Coupon> findAllByCompanyId(int companyId, Sort sort);

	@Query("from Coupon where company_id=:companyId and category=:category")
	List<Coupon> findAllByCompanyIdAndCategory(int companyId, Category category, Sort sort);

	@Query("from Coupon where company_id=:companyId and price<=:maxPrice")
	List<Coupon> findAllByCompanyIdAndPrice(int companyId, double maxPrice, Sort sort);

	// *** Customer related methods *** //

	boolean existsByIdAndCustomersId(int id, int customerId);

	Coupon findByIdAndCustomersId(int id, int customerId);

	List<Coupon> findAllByCustomersId(int customerId, Sort sort);

	List<Coupon> findAllByCustomersIdAndCategory(int customerId, Category category, Sort sort);

	List<Coupon> findAllByCustomersIdAndPriceLessThanEqual(int customerId, double maxPrice, Sort sort);

	// Methods in addition to system requirements:

	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////// get all coupons that the customer can purchase

	@Query(value = "select * from coupon where amount > :amount and end_date >= :endDate"
			+ " and id not in ( select coupon_id from customers_coupons where customer_id = :customerId )", nativeQuery = true)
	List<Coupon> findAllCouponsCustomerCanPurchase(int amount, LocalDate endDate, int customerId);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////// get all coupons that are about to expire

	@Query(value = "select * from coupon where amount > 0 and end_date = :endDate"
			+ " and id not in ( select coupon_id from customers_coupons where customer_id = :customerId )", nativeQuery = true)
	List<Coupon> findAllCouponsAboutToExpire(LocalDate endDate, int customerId);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
