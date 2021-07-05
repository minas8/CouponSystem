package app.core.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import app.core.entities.Company;

public interface CompanyRepository extends JpaRepository<Company, Integer> {

	Company findByEmailAndPassword(String email, String password);

	Company findFirstByNameOrEmail(String name, String email);

	Company findFirstByEmailEqualsAndIdNot(String email, int id);

}
