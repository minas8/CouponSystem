package app.core.services;

import java.time.LocalDate;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.core.repositories.CouponRepository;

@Service
@Transactional
public class DailyJobService {

	// field
	private CouponRepository couponRepo;

	// CTOR
	@Autowired
	public DailyJobService(CouponRepository couponRepo) {
		super();
		this.couponRepo = couponRepo;
	}

	// method
	// Delete all expired coupons and return the count of the deleted records
	public int deleteExpiredCoupons(LocalDate now) {
		return couponRepo.deleteAllByEndDateBefore(now);
	}
}
