package app.core.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import app.core.entities.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

	Customer findByEmailAndPassword(String email, String password);

	Customer findFirstByEmail(String email);

	Customer findFirstByEmailEqualsAndIdNot(String email, int id);

}
