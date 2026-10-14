package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Loan {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private Book book;
    @ManyToOne(optional=false) private Member member;
    private LocalDate borrowedAt;
    private LocalDate dueAt;
    private LocalDate returnedAt;
    public Long getId(){return id;}
    public Book getBook(){return book;} public void setBook(Book v){book=v;}
    public Member getMember(){return member;} public void setMember(Member v){member=v;}
    public LocalDate getBorrowedAt(){return borrowedAt;} public void setBorrowedAt(LocalDate v){borrowedAt=v;}
    public LocalDate getDueAt(){return dueAt;} public void setDueAt(LocalDate v){dueAt=v;}
    public LocalDate getReturnedAt(){return returnedAt;} public void setReturnedAt(LocalDate v){returnedAt=v;}
    public boolean isOverdue(){return returnedAt==null && dueAt.isBefore(LocalDate.now(java.time.ZoneId.of("Europe/Istanbul")));}
}
