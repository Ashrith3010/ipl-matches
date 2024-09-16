package com.indium.backend_assignment.service;

import com.indium.backend_assignment.entity.*;
import com.indium.backend_assignment.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CricketServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private InningsRepository inningsRepository;

    @Mock
    private OverRepository overRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CricketService cricketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testUploadJsonFile() throws IOException {
        // Mock MultipartFile
        MultipartFile file = mock(MultipartFile.class);

        // Mocking JSON data structure that the method will process
        Map<String, Object> mockJsonData = new HashMap<>();
        Map<String, Object> infoData = new HashMap<>();
        List<Map<String, Object>> inningsData = new ArrayList<>();

        // Add a list of innings as an example
        Map<String, Object> mockInnings = new HashMap<>();
        mockInnings.put("team", "TeamA");
        List<Map<String, Object>> mockOvers = new ArrayList<>();
        Map<String, Object> mockOver = new HashMap<>();
        List<Map<String, Object>> mockDeliveries = new ArrayList<>();
        Map<String, Object> mockDelivery = new HashMap<>();
        Map<String, Object> mockRuns = new HashMap<>();
        mockRuns.put("batter", 4);
        mockDelivery.put("batter", "Player1");
        mockDelivery.put("bowler", "Player2");
        mockDelivery.put("runs", mockRuns);
        mockDeliveries.add(mockDelivery);
        mockOver.put("deliveries", mockDeliveries);
        mockOvers.add(mockOver);
        mockInnings.put("overs", mockOvers);
        inningsData.add(mockInnings);

        // Add info data
        infoData.put("city", "CityA");
        infoData.put("venue", "VenueA");
        infoData.put("dates", Collections.singletonList("2024-09-13"));
        infoData.put("teams", Arrays.asList("TeamA", "TeamB"));
        Map<String, List<String>> playersMap = new HashMap<>();
        playersMap.put("TeamA", Arrays.asList("Player1", "Player2"));
        playersMap.put("TeamB", Arrays.asList("Player3", "Player4"));
        infoData.put("players", playersMap);

        mockJsonData.put("info", infoData);
        mockJsonData.put("innings", inningsData);

        // Mocking file's input stream reading
        InputStream inputStream = mock(InputStream.class);
        when(file.getInputStream()).thenReturn(inputStream);
        when(objectMapper.readValue(any(InputStream.class), eq(Map.class))).thenReturn(mockJsonData);

        // Mock repositories
        when(matchRepository.save(any(Match.class))).thenReturn(new Match());

        Team teamA = new Team();
        teamA.setTeamName("TeamA");
        when(teamRepository.findByTeamNameAndMatch(eq("TeamA"), any(Match.class))).thenReturn(teamA);

        Team teamB = new Team();
        teamB.setTeamName("TeamB");
        when(teamRepository.findByTeamNameAndMatch(eq("TeamB"), any(Match.class))).thenReturn(teamB);

        Player player1 = new Player();
        player1.setPlayerName("Player1");
        player1.setTotalRuns(0);
        when(playerRepository.findByPlayerName(eq("Player1"))).thenReturn(player1);

        Player player2 = new Player();
        player2.setPlayerName("Player2");
        player2.setTotalRuns(0);
        when(playerRepository.findByPlayerName(eq("Player2"))).thenReturn(player2);

        Player player3 = new Player();
        player3.setPlayerName("Player3");
        player3.setTotalRuns(0);
        when(playerRepository.findByPlayerName(eq("Player3"))).thenReturn(player3);

        Player player4 = new Player();
        player4.setPlayerName("Player4");
        player4.setTotalRuns(0);
        when(playerRepository.findByPlayerName(eq("Player4"))).thenReturn(player4);

        when(playerRepository.save(any(Player.class))).thenReturn(new Player());

        when(inningsRepository.save(any(Innings.class))).thenReturn(new Innings());
        when(overRepository.save(any(Over.class))).thenReturn(new Over());
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(new Delivery());

        // Call the method to test
        cricketService.uploadJsonFile(file);

        // Verify that repositories are called the correct number of times
        verify(matchRepository, times(1)).save(any(Match.class));
        verify(teamRepository, atLeastOnce()).save(any(Team.class));
        verify(playerRepository, atLeastOnce()).save(any(Player.class));
        verify(inningsRepository, atLeastOnce()).save(any(Innings.class));
        verify(overRepository, atLeastOnce()).save(any(Over.class));
        verify(deliveryRepository, atLeastOnce()).save(any(Delivery.class));
    }



    @Test
    void testGetMatchesPlayedByPlayer() {
        List<Delivery> deliveries = Arrays.asList(new Delivery(), new Delivery());
        when(deliveryRepository.findByBatter("John")).thenReturn(deliveries);

        String result = cricketService.getMatchesPlayedByPlayer("John");
        assertTrue(result.contains("John has played in"));
    }

    @Test
    void testGetCumulativeScoreOfPlayer() {
        Player player = new Player();
        player.setTotalRuns(150);
        when(playerRepository.findByPlayerName("John")).thenReturn(player);

        int score = cricketService.getCumulativeScoreOfPlayer("John");
        assertEquals(150, score);
    }

    @Test
    void testGetTopBatsmenPaginated() {
        // Create mock player data
        Player player1 = new Player();
        player1.setPlayerName("John");
        player1.setTotalRuns(150);
        Team team1 = new Team();
        team1.setTeamName("TeamA");
        player1.setTeam(team1);

        Player player2 = new Player();
        player2.setPlayerName("Mark");
        player2.setTotalRuns(120);
        Team team2 = new Team();
        team2.setTeamName("TeamB");
        player2.setTeam(team2);

        List<Player> players = Arrays.asList(player1, player2);
        Page<Player> page = new PageImpl<>(players);

        // Mock the repository call for pagination
        when(playerRepository.findAllByOrderByTotalRunsDesc(any(Pageable.class))).thenReturn(page);

        // Use a specific pageable object instead of unpaged to avoid UnsupportedOperationException
        Pageable pageable = PageRequest.of(0, 5);  // page 0 with page size 5

        // Call the service method
        String result = cricketService.getTopBatsmenPaginated(pageable);

        // Assert the expected results
        assertTrue(result.contains("John"));
        assertTrue(result.contains("Mark"));

        // Optionally, verify the repository was called with the expected pageable object
        verify(playerRepository, times(1)).findAllByOrderByTotalRunsDesc(pageable);
    }

    @Test
    void testGetMatchScoresByDate() {
        Match match = new Match();
        match.setVenue("Stadium");
        match.setMatchDate(LocalDate.now());

        Team team1 = new Team();
        team1.setTeamName("TeamA");

        Team team2 = new Team();
        team2.setTeamName("TeamB");

        match.setTeams(List.of(team1, team2));

        when(matchRepository.findByMatchDate(any())).thenReturn(List.of(match));

        String result = cricketService.getMatchScoresByDate(LocalDate.now());
        assertTrue(result.contains("Stadium"));
        assertTrue(result.contains("TeamA"));
        assertTrue(result.contains("TeamB"));
    }
}
