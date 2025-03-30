package demo.controller;

import demo.model.ArchivedMessage;
import demo.service.ArchivedMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/archived-messages")
public class ArchivedMessageController {

    @Autowired
    private ArchivedMessageService archivedMessageService;

    /**
     * Trigger manual archiving of read messages
     * @return List of archived messages
     */
    @PostMapping("/archive-now")
    public ResponseEntity<List<ArchivedMessage>> triggerArchiving() {
        List<ArchivedMessage> archived = archivedMessageService.archiveNow();
        return new ResponseEntity<>(archived, HttpStatus.OK);
    }

    /**
     * Get all archived messages
     */
    @GetMapping
    public ResponseEntity<List<ArchivedMessage>> getAllArchivedMessages() {
        List<ArchivedMessage> archived = archivedMessageService.getAllArchivedMessages();
        return new ResponseEntity<>(archived, HttpStatus.OK);
    }

    /**
     * Get archived messages by original queue
     * @param queueId ID of the original queue
     */
    @GetMapping("/by-queue/{queueId}")
    public ResponseEntity<List<ArchivedMessage>> getByQueue( @PathVariable("queueId") String queueId) {
        List<ArchivedMessage> archived = archivedMessageService.getArchivedMessagesByQueueId(queueId);
        return new ResponseEntity<>(archived, HttpStatus.OK);
    }

    /**
     * Search archived messages by content
     * @param keyword Search term
     */
    @GetMapping("/search")
    public ResponseEntity<List<ArchivedMessage>> searchArchived( @RequestParam("keyword") String keyword) {
        List<ArchivedMessage> archived = archivedMessageService.searchArchivedMessagesContainingKeyword(keyword);
        return new ResponseEntity<>(archived, HttpStatus.OK);
    }
}