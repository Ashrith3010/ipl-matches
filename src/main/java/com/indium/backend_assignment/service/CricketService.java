package com.indium.backend_assignment.service;

import com.indium.backend_assignment.entity.*;
import com.indium.backend_assignment.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CricketService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private InningsRepository inningsRepository;

    @Autowired
    private OverRepository overRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public void uploadJsonFile(MultipartFile file) throws IOException {
        Map<String, Object> jsonData = objectMapper.readValue(file.getInputStream(), Map.class);

        // Create and save Match
        Match match = createMatchFromJson((Map<String, Object>) jsonData.get("info"));
        matchRepository.save(match);

        // Create and save Teams and Players
        createTeamsAndPlayers(match, (Map<String, Object>) jsonData.get("info"));

        // Create and save Innings, Overs, and Deliveries
        createInningsOversDeliveries(match, (List<Map<String, Object>>) jsonData.get("innings"));
    }

    private Match createMatchFromJson(Map<String, Object> info) {
        Match match = new Match();
        match.setCity((String) info.get("city"));
        match.setVenue((String) info.get("venue"));
        match.setMatchDate(LocalDate.parse((String) ((List<?>) info.get("dates")).get(0)));
        return match;
    }

    private void createTeamsAndPlayers(Match match, Map<String, Object> info) {
        Map<String, List<String>> playersMap = (Map<String, List<String>>) info.get("players");
        List<String> teamNames = (List<String>) info.get("teams");

        for (String teamName : teamNames) {
            Team team = new Team();
            team.setTeamName(teamName);
            team.setMatch(match);
            teamRepository.save(team);

            for (String playerName : playersMap.get(teamName)) {
                Player player = new Player();
                player.setPlayerName(playerName);
                player.setTeam(team);
                player.setTotalRuns(0); // Initialize total runs to 0
                playerRepository.save(player);
            }
        }
    }

    private void createInningsOversDeliveries(Match match, List<Map<String, Object>> inningsData) {
        int inningsCount = 0;
        for (Map<String, Object> inningData : inningsData) {
            inningsCount++;
            Team team = teamRepository.findByTeamNameAndMatch((String) inningData.get("team"), match);

            Innings innings = new Innings();
            innings.setMatch(match);
            innings.setTeam(team);
            inningsRepository.save(innings);

            List<Map<String, Object>> overs = (List<Map<String, Object>>) inningData.get("overs");
            for (int overNumber = 0; overNumber < overs.size(); overNumber++) {
                Map<String, Object> overData = overs.get(overNumber);

                Over over = new Over();
                over.setOverNumber(overNumber + 1);
                over.setInnings(innings);
                overRepository.save(over);

                List<Map<String, Object>> deliveries = (List<Map<String, Object>>) overData.get("deliveries");
                for (Map<String, Object> deliveryData : deliveries) {
                    Delivery delivery = new Delivery();
                    delivery.setBatter((String) deliveryData.get("batter"));
                    delivery.setBowler((String) deliveryData.get("bowler"));
                    delivery.setOver(over);

                    Map<String, Object> runs = (Map<String, Object>) deliveryData.get("runs");
                    int runsScored = (Integer) runs.get("batter");
                    delivery.setRuns(runsScored);

                    // Update player's total runs
                    Player player = playerRepository.findByPlayerName(delivery.getBatter());
                    player.setTotalRuns(player.getTotalRuns() + runsScored);
                    playerRepository.save(player);

                    // Check for wicket
                    if (deliveryData.containsKey("wicket")) {
                        delivery.setWicket(true);
                    } else {
                        delivery.setWicket(false);
                    }

                    deliveryRepository.save(delivery);
                }
            }
        }
    }

    public List<Match> getMatchesPlayedByPlayer(String playerName) {
        return deliveryRepository.findByBatter(playerName)
                .stream()
                .map(delivery -> delivery.getOver().getInnings().getMatch())
                .distinct()
                .collect(Collectors.toList());
    }

    public int getCumulativeScoreOfPlayer(String playerName) {
        Player player = playerRepository.findByPlayerName(playerName);
        return player != null ? player.getTotalRuns() : 0;
    }

    public List<Match> getMatchScoresByDate(LocalDate date) {
        return matchRepository.findByMatchDate(date);
    }

    public Page<Player> getTopBatsmenPaginated(Pageable pageable) {
        return playerRepository.findAllByOrderByTotalRunsDesc(pageable);
    }
}