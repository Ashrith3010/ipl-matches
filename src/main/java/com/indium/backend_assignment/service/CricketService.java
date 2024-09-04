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
import java.util.*;
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
    private InningsRepository inningsRepository;

    @Autowired
    private OverRepository overRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OfficialRepository officialRepository;

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

        // Create and save Officials
        createOfficials(match, (Map<String, Object>) ((Map<String, Object>) jsonData.get("info")).get("officials"));

        // Create and save Innings, Overs, and Deliveries
        createInningsOversDeliveries(match, (List<Map<String, Object>>) jsonData.get("innings"));
    }

    private Match createMatchFromJson(Map<String, Object> info) {
        Match match = new Match();
        match.setCity((String) info.get("city"));
        match.setVenue((String) info.get("venue"));
        match.setMatchDate(LocalDate.parse((String) ((List<?>) info.get("dates")).get(0)));
        match.setMatchType((String) info.get("match_type"));
        match.setOvers((Integer) info.get("overs"));
        match.setBallsPerOver((Integer) info.get("balls_per_over"));

        Map<String, Object> event = (Map<String, Object>) info.get("event");
        match.setEventName((String) event.get("name"));
        match.setMatchNumber((Integer) event.get("match_number"));

        Map<String, Object> outcome = (Map<String, Object>) info.get("outcome");
        match.setWinnerTeam((String) outcome.get("winner"));

        match.setPlayerOfMatch((String) ((List<?>) info.get("player_of_match")).get(0));
        match.setTeamType((String) info.get("team_type"));

        Map<String, Object> toss = (Map<String, Object>) info.get("toss");
        match.setTossWinner((String) toss.get("winner"));
        match.setTossDecision((String) toss.get("decision"));

        return match;
    }

    private void createTeamsAndPlayers(Match match, Map<String, Object> info) {
        Map<String, List<String>> playersMap = (Map<String, List<String>>) info.get("players");
        List<String> teamNames = (List<String>) info.get("teams");

        for (String teamName : teamNames) {
            Team team = new Team();
            team.setTeamName(teamName);
            team.setMatch(match);
            team.setIsWinner(teamName.equals(match.getWinnerTeam()));
            teamRepository.save(team);

            for (String playerName : playersMap.get(teamName)) {
                Player player = new Player();
                player.setPlayerName(playerName);
                player.setTeam(team);
                playerRepository.save(player);
            }
        }
    }

    private void createOfficials(Match match, Map<String, Object> officialsData) {
        createOfficialsForRole(match, (List<String>) officialsData.get("match_referees"), "Match Referee");
        createOfficialsForRole(match, (List<String>) officialsData.get("umpires"), "Umpire");
        createOfficialsForRole(match, (List<String>) officialsData.get("reserve_umpires"), "Reserve Umpire");
        createOfficialsForRole(match, (List<String>) officialsData.get("tv_umpires"), "TV Umpire");
    }

    private void createOfficialsForRole(Match match, List<String> officialNames, String role) {
        for (String name : officialNames) {
            Official official = new Official();
            official.setName(name);
            official.setRole(role);
            official.setMatch(match);
            officialRepository.save(official);
        }
    }

    private void createInningsOversDeliveries(Match match, List<Map<String, Object>> inningsData) {
        for (Map<String, Object> inningData : inningsData) {
            Innings innings = new Innings();
            innings.setMatch(match);
            innings.setTeam(teamRepository.findByTeamNameAndMatch((String) inningData.get("team"), match));
            inningsRepository.save(innings);

            List<Map<String, Object>> overs = (List<Map<String, Object>>) inningData.get("overs");
            for (Map<String, Object> overData : overs) {
                Over over = new Over();
                over.setInnings(innings);
                over.setOverNumber((Integer) overData.get("over"));
                overRepository.save(over);

                List<Map<String, Object>> deliveries = (List<Map<String, Object>>) overData.get("deliveries");
                for (Map<String, Object> deliveryData : deliveries) {
                    Delivery delivery = new Delivery();
                    delivery.setOver(over);
                    delivery.setBatterName((String) deliveryData.get("batter"));
                    delivery.setBowlerName((String) deliveryData.get("bowler"));
                    delivery.setNonStrikerName((String) deliveryData.get("non_striker"));

                    Map<String, Object> runs = (Map<String, Object>) deliveryData.get("runs");
                    delivery.setRunsScored((Integer) runs.get("batter"));
                    delivery.setExtras((Integer) runs.get("extras"));
                    delivery.setTotalRuns((Integer) runs.get("total"));

                    // Handle wickets if present
                    if (deliveryData.containsKey("wicket")) {
                        delivery.setWicket(true);
                        Map<String, Object> wicket = (Map<String, Object>) deliveryData.get("wicket");
                        delivery.setWicketKind((String) wicket.get("kind"));
                        delivery.setPlayerOut((String) wicket.get("player_out"));
                        // Note: fielders might be a list, you may need to handle this differently
                    }

                    deliveryRepository.save(delivery);
                }
            }
        }
    }
    public List<Match> getMatchesPlayedByPlayer(String playerName) {
        return deliveryRepository.findByBatterNameOrBowlerName(playerName, playerName)
                .stream()
                .map(delivery -> delivery.getOver().getInnings().getMatch())
                .distinct()
                .collect(Collectors.toList());
    }

    public int getCumulativeScoreOfPlayer(String playerName) {
        return deliveryRepository.findByBatterName(playerName)
                .stream()
                .mapToInt(Delivery::getRunsScored)
                .sum();
    }

    public int getWicketsOfPlayer(String playerName) {
        return deliveryRepository.findByBowlerNameAndWicketIsTrue(playerName).size();
    }

    public List<Match> getMatchScoresByDate(LocalDate date) {
        return matchRepository.findByMatchDate(date);
    }

    public List<Player> getPlayersByTeamAndMatchNumber(String teamName, int matchNumber) {
        Match match = matchRepository.findByMatchNumber(matchNumber);
        Team team = teamRepository.findByTeamNameAndMatch(teamName, match);
        return playerRepository.findByTeam(team);
    }

    public List<Official> getMatchRefereesByMatchNumber(int matchNumber) {
        Match match = matchRepository.findByMatchNumber(matchNumber);
        return officialRepository.findByMatchAndRole(match, "Referee");
    }

    public Page<Player> getTopBatsmenPaginated(Pageable pageable) {
        return playerRepository.findAllByOrderByTotalRunsDesc(pageable);
    }

    public double getStrikeRateForBatsmanInMatch(String batsmanName, int matchNumber) {
        Match match = matchRepository.findByMatchNumber(matchNumber);
        List<Delivery> deliveries = deliveryRepository.findByBatterNameAndOver_Innings_Match(batsmanName, match);

        int totalRuns = deliveries.stream().mapToInt(Delivery::getRunsScored).sum();
        int totalBalls = deliveries.size();

        return totalBalls > 0 ? (totalRuns * 100.0) / totalBalls : 0.0;
    }

    public Page<Player> getTopWicketTakersPaginated(Pageable pageable) {
        return playerRepository.findAllByOrderByTotalWicketsDesc(pageable);
    }
}

    // ... (rest of the methods remain the same)
