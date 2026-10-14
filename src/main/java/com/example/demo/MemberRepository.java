package com.example.demo;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MemberRepository extends JpaRepository<Member,Long> {
    boolean existsByEmailAndIdNot(String email,Long id);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from Member e where e.id=:id")
    java.util.Optional<Member> lockById(Long id);
}
