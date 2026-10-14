package com.example.demo;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public class LoanForm {
 @NotNull(message="Kitap seçin.") @Positive(message="Geçerli kitap seçin.") private Long bookId;
 @NotNull(message="Üye seçin.") @Positive(message="Geçerli üye seçin.") private Long memberId;
 @NotNull(message="İade tarihi seçin.") @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) private LocalDate dueAt;
 public Long getBookId(){return bookId;} public void setBookId(Long v){bookId=v;}
 public Long getMemberId(){return memberId;} public void setMemberId(Long v){memberId=v;}
 public LocalDate getDueAt(){return dueAt;} public void setDueAt(LocalDate v){dueAt=v;}
}
