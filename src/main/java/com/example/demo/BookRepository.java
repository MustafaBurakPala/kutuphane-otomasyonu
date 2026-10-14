package com.example.demo;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface BookRepository extends JpaRepository<Book,Long> {
    boolean existsByIsbnAndIdNot(String isbn,Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select b from Book b where b.id = :id")
    Optional<Book> lockById(Long id);
}
