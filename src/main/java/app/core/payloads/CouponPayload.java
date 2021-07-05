package app.core.payloads;

import java.io.Serializable;

import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

public class CouponPayload implements Serializable {

	private static final long serialVersionUID = 1L;

	public String id;
	public String category;
	public String title;
	public String description;
	public String startDate;
	public String endDate;
	public String amount;
	public String price;
	public String image;
	@Nullable
	public MultipartFile imageFile;

	public String getId() {
		return id;
	}

	public String getCategory() {
		return category;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public String getAmount() {
		return amount;
	}

	public String getPrice() {
		return price;
	}

	public String getImage() {
		return image;
	}

	public MultipartFile getImageFile() {
		return imageFile;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public void setImageFile(MultipartFile imageFile) {
		this.imageFile = imageFile;
	}
}
