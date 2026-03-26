package com.example.demo1.repository;

import com.example.demo1.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface IRoleRepository extends JpaRepository<Role, Long>{
    Role findRoleById(Long id);
    java.util.Optional<Role> findByName(String name);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "INSERT IGNORE INTO role (id, name, description) VALUES (:id, :name, :description)", nativeQuery = true)
    void insertRoleWithId(@org.springframework.data.repository.query.Param("id") Long id, 
                          @org.springframework.data.repository.query.Param("name") String name, 
                          @org.springframework.data.repository.query.Param("description") String description);
}