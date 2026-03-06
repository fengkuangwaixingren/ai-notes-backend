package com.example.demo.service;

import com.example.demo.dto.NoteRequest;
import com.example.demo.dto.NoteResponse;
import com.example.demo.model.Note;
import com.example.demo.model.User;
import com.example.demo.repository.NoteRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final AiSummaryService aiSummaryService;
    private final ApplicationContext applicationContext;

    @Cacheable(value = "notes", key = "'user:' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().authentication.name")
    public List<NoteResponse> getUserNotes() {
        User currentUser = getCurrentUser();
        List<Note> notes = noteRepository.findByUserId(currentUser.getId());
        return notes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "notes", key = "'note:' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().authentication.name + ':' + #id")
    public NoteResponse getNoteById(Long id) {
        User currentUser = getCurrentUser();
        Note note = noteRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        return toResponse(note);
    }

    @Transactional
    @CacheEvict(value = "notes", allEntries = true)
    public NoteResponse createNote(NoteRequest request) {
        User currentUser = getCurrentUser();

        Note note = new Note();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setUser(currentUser);

        Note saved = noteRepository.save(note);
        asyncSelf().updateSummaryAsync(saved.getId(), currentUser.getId());
        return toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "notes", allEntries = true)
    public NoteResponse updateNote(Long id, NoteRequest request) {
        User currentUser = getCurrentUser();

        Note existing = noteRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            existing.setTitle(request.getTitle());
        }
        existing.setContent(request.getContent());

        Note updated = noteRepository.save(existing);
        asyncSelf().updateSummaryAsync(updated.getId(), currentUser.getId());
        return toResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "notes", allEntries = true)
    public void deleteNote(Long id) {
        User currentUser = getCurrentUser();
        noteRepository.findByIdAndUserId(id, currentUser.getId())
                .ifPresent(noteRepository::delete);
    }

    @Async
    public CompletableFuture<Void> updateSummaryAsync(Long noteId, String title, String content) {
        log.info("异步摘要任务开始，笔记ID: {}, title: {}", noteId, title);
        String summary = aiSummaryService.generateSummary(title, content);
        if (summary != null && !summary.isEmpty()) {
            noteRepository.findById(noteId).ifPresent(note -> {
                note.setSummary(summary);
                noteRepository.save(note);
                log.info("笔记 {} 摘要更新成功", noteId);
            });
        }
        return CompletableFuture.completedFuture(null);
    }
    @Transactional
    @CacheEvict(value = "notes", allEntries = true)

    public void updateSummaryAsync(Long noteId, Long userId) {
        noteRepository.findByIdAndUserId(noteId, userId).ifPresent(note -> {
            String summary = aiSummaryService.generateSummary(note.getTitle(), note.getContent());
            note.setSummary(summary);
            noteRepository.save(note);
            log.debug("Note summary updated. noteId={}, userId={}", noteId, userId);
        });
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("No authenticated user found in security context");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }

    private NoteResponse toResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getSummary(),
                note.getCreatedAt(),
                note.getUpdatedAt(),
                note.getUser().getUsername()
        );
    }

    private NoteService asyncSelf() {
        return applicationContext.getBean(NoteService.class);
    }
}

