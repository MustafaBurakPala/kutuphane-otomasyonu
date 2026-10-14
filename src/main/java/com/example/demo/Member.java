package com.example.demo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity @Table(name="library_member")
public class Member {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Positive(message="Kayıt numarası geçersiz.") private Long id;
    @NotBlank(message="Ad soyad boş olamaz.") @Size(max=150,message="Ad soyad en fazla 150 karakter olabilir.") private String name;
    @NotBlank(message="E-posta boş olamaz.") @Email(message="Geçerli e-posta girin.") @Pattern(regexp="^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",message="Geçerli e-posta girin.") @Size(max=200,message="E-posta en fazla 200 karakter olabilir.") @Column(unique=true) private String email;
    @Pattern(regexp="^$|^(?=(?:\\D*\\d){7,15}\\D*$)\\+?[0-9][0-9 ()-]*[0-9]$",message="Telefon 7–15 rakam içermeli; rakam, boşluk, +, - ve parantez kullanın.") @Size(max=30,message="Telefon en fazla 30 karakter olabilir.") private String phone;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v==null?null:v.strip();}
    public String getEmail(){return email;} public void setEmail(String v){email=v==null?null:v.strip().toLowerCase(java.util.Locale.ROOT);}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v==null?null:v.strip();}
}
