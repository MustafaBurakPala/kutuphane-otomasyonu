package com.example.demo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @Transactional
class LibraryServiceTests {
    @Autowired LibraryService service;
    @Autowired BookRepository books;
    @Autowired MemberRepository members;
    @Autowired LoanRepository loans;
    Book book(){Book b=new Book();b.setTitle("Test kitabı");b.setAuthor("Yazar");b.setIsbn("test-isbn");return books.save(b);}
    Member member(String email){Member m=new Member();m.setName("Test Üye");m.setEmail(email);return members.save(m);}
    @Test void stockIsRestoredAfterReturn(){Book b=book();Member a=member("a@example.test"),c=member("c@example.test");Loan l=service.borrow(b.getId(),a.getId(),LocalDate.now().plusDays(14));assertThrows(IllegalArgumentException.class,()->service.borrow(b.getId(),c.getId(),LocalDate.now().plusDays(14)));service.returnLoan(l.getId());assertNotNull(service.borrow(b.getId(),c.getId(),LocalDate.now().plusDays(14)));assertThrows(IllegalArgumentException.class,()->service.returnLoan(l.getId()));}
    @Test void duplicateLoanAndPastDueDateAreRejected(){Book b=book();b.setCopies(2);Member m=member("m@example.test");assertThrows(IllegalArgumentException.class,()->service.borrow(b.getId(),m.getId(),LocalDate.now().minusDays(1)));service.borrow(b.getId(),m.getId(),LocalDate.now().plusDays(7));assertThrows(IllegalArgumentException.class,()->service.borrow(b.getId(),m.getId(),LocalDate.now().plusDays(7)));}
    @Test void historyAndActiveStockAreProtected(){Book b=book();Member m=member("m@example.test");service.borrow(b.getId(),m.getId(),LocalDate.now().plusDays(7));assertThrows(IllegalArgumentException.class,()->service.deleteBook(b.getId()));assertThrows(IllegalArgumentException.class,()->service.deleteMember(m.getId()));Book edit=new Book();edit.setId(b.getId());edit.setTitle("Yeni");edit.setAuthor("Yazar");edit.setIsbn(b.getIsbn());edit.setCopies(0);assertThrows(IllegalArgumentException.class,()->service.saveBook(edit));}
}
