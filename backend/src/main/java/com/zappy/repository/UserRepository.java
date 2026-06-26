package com.zappy.repository;

import com.zappy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Tim tat ca nhan vien cua 1 nha hang
    List<User> findByRestaurantId(Integer resId);
    // Dang nhap theo username
    Optional<User> findByUsername(String username);
    // Dang nhap theo username trong 1 nha hang cu the
    Optional<User> findByUsernameAndRestaurantId(String username, Integer resId);
    // Tim user theo email.
    User findByEmail(String email);
	
}
