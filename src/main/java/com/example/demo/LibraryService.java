package com.example.demo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

@Service @Transactional
public class LibraryService {
    private final jakarta.validation.Validator validator; private final BookRepository books; private final MemberRepository members; private final LoanRepository loans;
    public LibraryService(BookRepository b,MemberRepository m,LoanRepository l,jakarta.validation.Validator v){books=b;members=m;loans=l;validator=v;}
    public void saveBook(Book input){
        validate(input);
        input.setTitle(input.getTitle().trim()); input.setAuthor(input.getAuthor().trim()); input.setIsbn(input.getIsbn().trim());
        if(books.existsByIsbnAndIdNot(input.getIsbn(),input.getId()==null ? -1L:input.getId())) throw new FieldValidationException("isbn","Bu ISBN zaten kayıtlı.");
        if(input.getId()!=null){
            Book existing=books.lockById(input.getId()).orElseThrow(()->new IllegalArgumentException("Kitap bulunamadı."));
            if(input.getCopies()<loans.countByBookIdAndReturnedAtIsNull(input.getId())) throw new FieldValidationException("copies","Adet, ödünçteki kitap sayısından az olamaz.");
            existing.setTitle(input.getTitle()); existing.setAuthor(input.getAuthor()); existing.setIsbn(input.getIsbn()); existing.setCopies(input.getCopies());
        } else books.save(input);
    }
    public void saveMember(Member input){
        validate(input);
        input.setName(input.getName().trim()); input.setEmail(input.getEmail().trim().toLowerCase(java.util.Locale.ROOT));
        if(members.existsByEmailAndIdNot(input.getEmail(),input.getId()==null ? -1L:input.getId())) throw new FieldValidationException("email","Bu e-posta zaten kayıtlı.");
        if(input.getId()!=null){
            Member existing=members.findById(input.getId()).orElseThrow(()->new IllegalArgumentException("Üye bulunamadı."));
            existing.setName(input.getName()); existing.setEmail(input.getEmail()); existing.setPhone(input.getPhone());
        } else members.save(input);
    }
    public void deleteBook(Long id){requireId(id);books.lockById(id).orElseThrow(()->new IllegalArgumentException("Kitap bulunamadı.")); if(loans.existsByBookId(id)) throw new IllegalArgumentException("Ödünç geçmişi olan kitap silinemez."); books.deleteById(id);}
    public void deleteMember(Long id){requireId(id);members.lockById(id).orElseThrow(()->new IllegalArgumentException("Üye bulunamadı."));if(loans.existsByMemberId(id)) throw new IllegalArgumentException("Ödünç geçmişi olan üye silinemez."); members.deleteById(id);}
    public Loan borrow(Long bookId,Long memberId,LocalDate due){
        LocalDate today=LocalDate.now(ZoneId.of("Europe/Istanbul"));
        if(due==null || due.isBefore(today)) throw new FieldValidationException("dueAt","İade tarihi bugünden önce olamaz.");
        if(due.getYear()>9999)throw new FieldValidationException("dueAt","İade tarihi 9999 yılını geçemez.");
        if(bookId==null || bookId<=0)throw new FieldValidationException("bookId","Geçerli kitap seçin.");
        Book book=books.lockById(bookId).orElseThrow(()->new IllegalArgumentException("Kitap bulunamadı."));
        if(memberId==null || memberId<=0)throw new FieldValidationException("memberId","Geçerli üye seçin.");
        Member member=members.lockById(memberId).orElseThrow(()->new IllegalArgumentException("Üye bulunamadı."));
        if(loans.countByBookIdAndReturnedAtIsNull(bookId)>=book.getCopies()) throw new FieldValidationException("bookId","Bu kitabın tüm kopyaları ödünçte.");
        if(loans.existsByBookIdAndMemberIdAndReturnedAtIsNull(bookId,memberId)) throw new FieldValidationException("bookId","Üye bu kitabı zaten ödünç aldı.");
        Loan loan=new Loan(); loan.setBook(book);loan.setMember(member);loan.setBorrowedAt(today);loan.setDueAt(due);return loans.save(loan);
    }
    public void returnLoan(Long id){requireId(id); Long bookId=loans.findBookId(id).orElseThrow(()->new IllegalArgumentException("Ödünç kaydı bulunamadı.")); books.lockById(bookId); Loan loan=loans.lockById(id).orElseThrow(()->new IllegalArgumentException("Ödünç kaydı bulunamadı.")); if(loan.getReturnedAt()!=null) throw new IllegalArgumentException("Kitap zaten iade edilmiş."); loan.setReturnedAt(LocalDate.now(ZoneId.of("Europe/Istanbul")));}
    private void requireId(Long id){if(id==null||id<=0)throw new IllegalArgumentException("Kayıt numarası geçersiz.");}
    private void validate(Object input){
        if(input==null)throw new IllegalArgumentException("Form bilgileri eksik.");
        var violations=validator.validate(input);
        if(!violations.isEmpty()){var v=violations.iterator().next();throw new FieldValidationException(v.getPropertyPath().toString(),v.getMessage());}
    }
}
