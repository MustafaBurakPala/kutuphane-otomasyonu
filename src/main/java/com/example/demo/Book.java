package com.example.demo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Positive(message="Kayıt numarası geçersiz.") private Long id;
    @NotBlank(message="Kitap adı boş olamaz.") @Size(max=200,message="Kitap adı en fazla 200 karakter olabilir.") private String title;
    @NotBlank(message="Yazar boş olamaz.") @Size(max=150,message="Yazar en fazla 150 karakter olabilir.") private String author;
    @NotBlank(message="ISBN boş olamaz.") @Size(max=40,message="ISBN en fazla 40 karakter olabilir.") @Column(unique=true) private String isbn;
    @Min(value=1,message="Adet en az 1 olmalı.") @Max(value=10000,message="Adet en fazla 10000 olabilir.") private int copies=1;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v==null?null:v.strip();}
    public String getAuthor(){return author;} public void setAuthor(String v){author=v==null?null:v.strip();}
    public String getIsbn(){return isbn;} public void setIsbn(String v){isbn=v==null?null:v.strip();}
    public int getCopies(){return copies;} public void setCopies(int v){copies=v;}
}
