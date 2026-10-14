package com.example.demo;
import org.springframework.data.jpa.repository.*;
import java.util.List;
public interface LoanRepository extends JpaRepository<Loan,Long> {
    @Query("select l.book.id from Loan l where l.id=:id") java.util.Optional<Long> findBookId(Long id);
    long countByBookIdAndReturnedAtIsNull(Long id);
    boolean existsByBookId(Long id);
    boolean existsByMemberId(Long id);
    boolean existsByBookIdAndMemberIdAndReturnedAtIsNull(Long bookId,Long memberId);
    @EntityGraph(attributePaths={"book","member"}) List<Loan> findAllByOrderByIdDesc();
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from Loan e where e.id=:id")
    java.util.Optional<Loan> lockById(Long id);
}
