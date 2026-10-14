package com.example.demo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDate;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties="spring.datasource.url=jdbc:h2:mem:concurrent-test;DB_CLOSE_DELAY=-1")
class ConcurrentReturnTests {
 @Autowired BookRepository books;
 @Autowired MemberRepository members;
 @Autowired LoanRepository loans;
 @Autowired LibraryService service;
 @Test void onlyOneConcurrentReturnSucceeds() throws Exception {
  Book book=new Book();book.setTitle("Test");book.setAuthor("Yazar");book.setIsbn("CONCURRENT-001");books.save(book);
  Member member=new Member();member.setName("Test Üye");member.setEmail("concurrent@example.test");members.save(member);
  Loan loan=service.borrow(book.getId(),member.getId(),LocalDate.now().plusDays(7));
  ExecutorService executor=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
  Callable<Boolean> work=()->{start.await();try{service.returnLoan(loan.getId());return true;}catch(IllegalArgumentException e){assertEquals("Kitap zaten iade edilmiş.",e.getMessage());return false;}};
  try {
   Future<Boolean> a=executor.submit(work),b=executor.submit(work);start.countDown();
   assertNotEquals(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS));
   assertNotNull(loans.findById(loan.getId()).orElseThrow().getReturnedAt());
   assertEquals(0,loans.countByBookIdAndReturnedAtIsNull(book.getId()));
  }finally{executor.shutdownNow();}
 }
}
