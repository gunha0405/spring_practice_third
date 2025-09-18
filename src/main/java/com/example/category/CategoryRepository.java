package com.example.category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer>{
	Optional<Category> findByName(String name);
	List<Category> findAll();
}
