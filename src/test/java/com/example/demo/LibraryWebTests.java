package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties="spring.datasource.url=jdbc:h2:mem:web-test;DB_CLOSE_DELAY=-1")
class LibraryWebTests {
    @Autowired Environment environment;
    @Autowired BookRepository books;
    @Autowired MemberRepository members;
    @Autowired LoanRepository loans;
    private final HttpClient client=HttpClient.newBuilder().cookieHandler(new java.net.CookieManager()).followRedirects(HttpClient.Redirect.NORMAL).build();
    URI uri(String path){return URI.create("http://127.0.0.1:"+environment.getProperty("local.server.port")+path);}
    String post(String path,String form) throws Exception {
        var response=client.send(HttpRequest.newBuilder(uri(path)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(form)).build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,response.statusCode(),response.uri()+" "+response.body());return response.body();
    }
    String encode(String value){return URLEncoder.encode(value,StandardCharsets.UTF_8);}
    @Test void invalidFormsKeepValuesAndDoNotCreateRecords() throws Exception {
        long before=books.count();
        String page=post("/books","title="+encode("  Kitap Adı  ")+"&author=&isbn=FORM-001&copies=abc");
        assertTrue(page.contains("Yazar boş olamaz."));
        assertTrue(page.contains("Geçerli bir değer girin."));
        assertTrue(page.contains("Kitap Adı"));
        assertEquals(before,books.count());
        long memberBefore=members.count();
        page=post("/members","name=&email=gecersiz&phone=harfler");
        assertTrue(page.contains("Ad soyad boş olamaz."));
        assertTrue(page.contains("Geçerli e-posta girin."));
        assertTrue(page.contains("Telefon 7"));
        assertEquals(memberBefore,members.count());
    }
    @Test void invalidLoanIdsAndDatesShowUsefulMessages() throws Exception {
        var home=client.send(HttpRequest.newBuilder(uri("/")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertTrue(home.body().contains("value=\""+LocalDate.now().plusDays(14)+"\""));
        long before=loans.count();
        assertTrue(post("/loans","bookId=&memberId=&dueAt=").contains("Kitap seçin."));
        assertTrue(post("/loans","bookId=abc&memberId=1&dueAt=bozuk").contains("Geçerli bir değer girin."));
        assertTrue(post("/loans","bookId=1&memberId=1&dueAt="+LocalDate.now().minusDays(1)).contains("İade tarihi bugünden önce olamaz."));
        assertTrue(post("/members/999999/delete","").contains("Üye bulunamadı."));
        assertTrue(post("/loans/abc/return","").contains("İstek geçersiz."));
        assertEquals(before,loans.count());
    }
    @Test void duplicateIsbnIsShownAtTheFieldAndWhitespaceIsNormalized() throws Exception {
        Book b=new Book();b.setTitle("Orijinal");b.setAuthor("Yazar");b.setIsbn("DUP-VALIDATION");books.save(b);
        try {
            String page=post("/books","title="+encode("   Yeni Kitap   ")+"&author=Yazar&isbn=DUP-VALIDATION&copies=2");
            assertTrue(page.contains("Bu ISBN zaten kayıtlı."));
            assertTrue(page.contains("value=\"Yeni Kitap\""));
            assertTrue(page.contains("id=\"book-isbn-error\""));
        } finally {books.deleteById(b.getId());}
    }
    @Test void bookMemberBorrowAndReturnRenderCorrectly() throws Exception {
        assertTrue(post("/books","title="+encode("Deneme Kitabı")+"&author=Yazar&isbn=WEB-001&copies=1").contains("Kitap kaydedildi."));
        assertTrue(post("/members","name="+encode("Deneme Üye")+"&email=deneme%40example.test&phone=").contains("Üye kaydedildi."));
        Long bookId=books.findAll().get(0).getId(),memberId=members.findAll().get(0).getId();
        String loanForm="bookId="+bookId+"&memberId="+memberId+"&dueAt="+LocalDate.now().plusDays(14);
        assertTrue(post("/loans",loanForm).contains("Kitap ödünç verildi."));
        assertTrue(post("/loans",loanForm).contains("tüm kopyaları ödünçte"));
        Long loanId=loans.findAll().get(0).getId();
        assertTrue(post("/loans/"+loanId+"/return","").contains("İade tamamlandı."));
        var page=client.send(HttpRequest.newBuilder(uri("/?q=WEB-001&editBook="+bookId+"&editMember="+memberId)).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,page.statusCode());assertTrue(page.body().contains("Deneme Kitabı"));assertTrue(page.body().contains("İade edildi"));assertTrue(page.body().contains("Kitabı düzenle"));
    }
}


