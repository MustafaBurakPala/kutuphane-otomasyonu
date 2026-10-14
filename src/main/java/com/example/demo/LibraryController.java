package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.*;

@Controller
public class LibraryController {
    private final BookRepository books; private final MemberRepository members; private final LoanRepository loans; private final LibraryService service;
    public LibraryController(BookRepository b,MemberRepository m,LoanRepository l,LibraryService s){books=b;members=m;loans=l;service=s;}
    @InitBinder("bookForm") void bookBinder(org.springframework.web.bind.WebDataBinder b){b.setAllowedFields("id","title","author","isbn","copies");}
    @InitBinder("memberForm") void memberBinder(org.springframework.web.bind.WebDataBinder b){b.setAllowedFields("id","name","email","phone");}
    @InitBinder("loanForm") void loanBinder(org.springframework.web.bind.WebDataBinder b){b.setAllowedFields("bookId","memberId","dueAt");}
    @GetMapping("/") public String home(Model model,@RequestParam(defaultValue="") String q,@RequestParam(required=false) Long editBook,@RequestParam(required=false) Long editMember){
        if(editBook!=null) model.addAttribute("bookForm",books.findById(editBook).orElseThrow(()->new IllegalArgumentException("Düzenlenecek kitap bulunamadı.")));
        if(editMember!=null) model.addAttribute("memberForm",members.findById(editMember).orElseThrow(()->new IllegalArgumentException("Düzenlenecek üye bulunamadı.")));
        return page(model,q);
    }
    private String page(Model model,String q){
        var allBooks=books.findAll();var allLoans=loans.findAllByOrderByIdDesc();
        String search=q.strip().toLowerCase(Locale.ROOT);
        model.addAttribute("books",allBooks.stream().filter(b->(b.getTitle()+" "+b.getAuthor()+" "+b.getIsbn()).toLowerCase(Locale.ROOT).contains(search)).toList());
        model.addAttribute("allBooks",allBooks);model.addAttribute("members",members.findAll());model.addAttribute("loans",allLoans);
        Map<Long,Long> available=new HashMap<>();for(Book b:allBooks) available.put(b.getId(),b.getCopies()-allLoans.stream().filter(l->l.getBook().getId().equals(b.getId())&&l.getReturnedAt()==null).count());
        model.addAttribute("available",available);model.addAttribute("bookCount",allBooks.size());model.addAttribute("memberCount",members.count());
        model.addAttribute("activeCount",allLoans.stream().filter(l->l.getReturnedAt()==null).count());model.addAttribute("overdueCount",allLoans.stream().filter(Loan::isOverdue).count());
        if(!model.containsAttribute("bookForm"))model.addAttribute("bookForm",new Book());
        if(!model.containsAttribute("memberForm"))model.addAttribute("memberForm",new Member());
        if(!model.containsAttribute("loanForm")){LoanForm form=new LoanForm();form.setDueAt(LocalDate.now(java.time.ZoneId.of("Europe/Istanbul")).plusDays(14));model.addAttribute("loanForm",form);}
        for(String n:List.of("bookErrors","memberErrors","loanErrors"))if(!model.containsAttribute(n))model.addAttribute(n,Map.of());
        model.addAttribute("q",q);model.addAttribute("today",LocalDate.now(java.time.ZoneId.of("Europe/Istanbul")));model.addAttribute("defaultDue",LocalDate.now(java.time.ZoneId.of("Europe/Istanbul")).plusDays(14));return "index";
    }
    @PostMapping("/books") public String saveBook(@Valid @ModelAttribute("bookForm") Book book,BindingResult errors,Model model,RedirectAttributes flash){return save(errors,model,flash,"bookErrors",()->service.saveBook(book),"Kitap kaydedildi.");}
    @PostMapping("/members") public String saveMember(@Valid @ModelAttribute("memberForm") Member member,BindingResult errors,Model model,RedirectAttributes flash){return save(errors,model,flash,"memberErrors",()->service.saveMember(member),"Üye kaydedildi.");}
    @PostMapping("/books/{id}/delete") public String deleteBook(@PathVariable Long id,RedirectAttributes flash){return action(flash,()->service.deleteBook(id),"Kitap silindi.");}
    @PostMapping("/members/{id}/delete") public String deleteMember(@PathVariable Long id,RedirectAttributes flash){return action(flash,()->service.deleteMember(id),"Üye silindi.");}
    @PostMapping("/loans") public String borrow(@Valid @ModelAttribute("loanForm") LoanForm form,BindingResult errors,Model model,RedirectAttributes flash){return save(errors,model,flash,"loanErrors",()->service.borrow(form.getBookId(),form.getMemberId(),form.getDueAt()),"Kitap ödünç verildi.");}
    @PostMapping("/loans/{id}/return") public String returnLoan(@PathVariable Long id,RedirectAttributes flash){return action(flash,()->service.returnLoan(id),"İade tamamlandı.");}
    private String action(RedirectAttributes flash,Runnable work,String message){try{work.run();flash.addFlashAttribute("success",message);}catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}catch(org.springframework.dao.DataIntegrityViolationException e){flash.addFlashAttribute("error","Bu kayda bağlı işlemler var veya bilgiler çakıştı. Kayıt güncellenemedi.");}return "redirect:/";}
    private String save(BindingResult errors,Model model,RedirectAttributes flash,String name,Runnable work,String message){
        if(!errors.hasErrors())try{work.run();flash.addFlashAttribute("success",message);return "redirect:/";}
        catch(FieldValidationException e){errors.rejectValue(e.getField(),"business",e.getMessage());}
        catch(IllegalArgumentException e){errors.reject("business",e.getMessage());}
        catch(org.springframework.dao.DataIntegrityViolationException e){errors.reject("conflict","Kayıt çakıştı. ISBN veya e-postayı kontrol edin.");}
        catch(org.springframework.dao.TransientDataAccessException e){errors.reject("busy","İşlem çakıştı. Tekrar deneyin.");}
        Map<String,String> messages=new LinkedHashMap<>();
        errors.getFieldErrors().forEach(e->messages.putIfAbsent(e.getField(),e.isBindingFailure()?"Geçerli bir değer girin.":e.getDefaultMessage()));
        model.addAttribute(name,messages);model.addAttribute("error",errors.hasGlobalErrors()?errors.getGlobalError().getDefaultMessage():"İşaretli alanları kontrol edin. Girdiğiniz bilgiler korunmuştur.");return page(model,"");
    }
    @ExceptionHandler({org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,org.springframework.web.bind.MissingServletRequestParameterException.class})
    String badRequest(RedirectAttributes flash){flash.addFlashAttribute("error","İstek geçersiz. İşlemi sayfadaki form üzerinden tekrar deneyin.");return "redirect:/";}
    @ExceptionHandler(IllegalArgumentException.class)
    String missingRecord(IllegalArgumentException e,RedirectAttributes flash){flash.addFlashAttribute("error",e.getMessage());return "redirect:/";}
}
