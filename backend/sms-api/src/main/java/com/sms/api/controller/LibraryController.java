package com.sms.api.controller;

import com.sms.api.dto.library.*;
import com.sms.api.security.UserPrincipal;
import com.sms.api.service.LibraryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/library")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Library", description = "Book management and issue/return")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping("/books")
    public ResponseEntity<List<BookDto>> listBooks(@AuthenticationPrincipal UserPrincipal p) {
        return ResponseEntity.ok(libraryService.listBooks(p.schoolId()));
    }

    @PostMapping("/books")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','LIBRARIAN')")
    public ResponseEntity<BookDto> addBook(@Valid @RequestBody CreateBookRequest req,
                                           @AuthenticationPrincipal UserPrincipal p) {
        BookDto dto = libraryService.addBook(p.schoolId(), req);
        return ResponseEntity.created(URI.create("/api/v1/library/books/" + dto.id())).body(dto);
    }

    @DeleteMapping("/books/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','LIBRARIAN')")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id,
                                           @AuthenticationPrincipal UserPrincipal p) {
        libraryService.deleteBook(p.schoolId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/issued")
    public ResponseEntity<List<BookIssueDto>> listIssued(@AuthenticationPrincipal UserPrincipal p) {
        return ResponseEntity.ok(libraryService.listIssued(p.schoolId()));
    }

    @PostMapping("/issue")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','LIBRARIAN')")
    public ResponseEntity<BookIssueDto> issueBook(@Valid @RequestBody IssueBookRequest req,
                                                   @AuthenticationPrincipal UserPrincipal p) {
        BookIssueDto dto = libraryService.issueBook(p.schoolId(), req);
        return ResponseEntity.created(URI.create("/api/v1/library/issued/" + dto.id())).body(dto);
    }

    @PostMapping("/return/{issueId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','LIBRARIAN')")
    public ResponseEntity<BookIssueDto> returnBook(@PathVariable UUID issueId,
                                                    @AuthenticationPrincipal UserPrincipal p) {
        return ResponseEntity.ok(libraryService.returnBook(p.schoolId(), issueId));
    }
}
