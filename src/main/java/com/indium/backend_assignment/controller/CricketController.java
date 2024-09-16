package com.indium.backend_assignment.controller;

import com.indium.backend_assignment.entity.*;
import com.indium.backend_assignment.service.CricketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cricket")
public class CricketController {

    @Autowired
    private CricketService cricketService;
    @PostMapping("/upload")
    public ResponseEntity<String> uploadJsonFile(@RequestParam("file") MultipartFile file) {
        try {
            String result = cricketService.uploadJsonFile(file);

            if (result == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Result is null");
            }

            // Check the result and respond with appropriate status code
            if ("Already exists".equals(result)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
            } else if (result.startsWith("Error")) {
                return ResponseEntity.badRequest().body(result);
            } else {
                return ResponseEntity.ok(result);
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error uploading file: " + e.getMessage());
        }
    }


    @GetMapping("/matches/player/{playerName}")
    public ResponseEntity<String> getMatchesPlayedByPlayer(@PathVariable String playerName) {
        return ResponseEntity.ok(cricketService.getMatchesPlayedByPlayer(playerName));
    }

    @GetMapping("/score/player/{playerName}")
    public ResponseEntity<Integer> getCumulativeScoreOfPlayer(@PathVariable String playerName) {
        return ResponseEntity.ok(cricketService.getCumulativeScoreOfPlayer(playerName));
    }

    @GetMapping("/batsmen/top")
    public ResponseEntity<String> getTopBatsmenPaginated(Pageable pageable) {
        return ResponseEntity.ok(cricketService.getTopBatsmenPaginated(pageable));
    }

    @GetMapping("/matches/date/{date}")
    public ResponseEntity<String> getMatchScoresByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(cricketService.getMatchScoresByDate(date));
    }
}
