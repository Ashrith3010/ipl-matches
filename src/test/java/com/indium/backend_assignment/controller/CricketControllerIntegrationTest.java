package com.indium.backend_assignment.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CricketControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void testUploadJsonFile() throws IOException {
        String url = "http://localhost:" + port + "/api/cricket/upload";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Load the test file
        ClassPathResource fileResource = new ClassPathResource("335982.json");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = testRestTemplate.postForEntity(url, requestEntity, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("File uploaded successfully", response.getBody());
    }

    @Test
    public void testGetMatchesPlayedByPlayer() {
        String playerName = "V Kohli";
        String url = "http://localhost:" + port + "/api/cricket/matches/player/" + playerName;

        String response = testRestTemplate.getForObject(url, String.class);
        assertEquals("V Kohli has played in 1 match(es).", response);
    }

    @Test
    public void testGetCumulativeScoreOfPlayer() {
        String playerName = "V Kohli";
        String url = "http://localhost:" + port + "/api/cricket/score/player/" + playerName;

        Integer response = testRestTemplate.getForObject(url, Integer.class);
        assertEquals(23, response);
    }

    @Test
    public void testGetTopBatsmenPaginated() {
        String url = "http://localhost:" + port + "/api/cricket/batsmen/top?page=0&size=5";

        String response = testRestTemplate.getForObject(url, String.class);
        assertEquals("RV Uthappa (Mumbai Indians): 48 runs\nMV Boucher (Royal Challengers Bangalore): 39 runs\nR Dravid (Royal Challengers Bangalore): 32 runs\nST Jayasuriya (Mumbai Indians): 29 runs\nSM Pollock (Mumbai Indians): 28 runs", response);
    }

    @Test
    public void testGetScoreDetailsByDate() {
        String date = "2008-04-20"; // The date for the test
        String url = "http://localhost:" + port + "/api/cricket/matches/date/" + date;

        // Call the API
        ResponseEntity<String> response = testRestTemplate.getForEntity(url, String.class);

        // Expected response
        String expectedResponse = "Scores for matches on 2008-04-20:\n" +
                "Match at Wankhede Stadium between teams: Mumbai Indians\n" +
                "Team: Mumbai Indians scored 154 runs\n" +
                "Team: Royal Challengers Bangalore scored 161 runs";

        // Normalize both expected and actual responses to remove extra spaces, tabs, and newlines
        String normalizedExpected = expectedResponse.replaceAll("\\s+", " ").trim();
        String normalizedActual = response.getBody().replaceAll("\\s+", " ").trim();

        // Compare normalized strings
        assertEquals(normalizedExpected, normalizedActual);
    }

}