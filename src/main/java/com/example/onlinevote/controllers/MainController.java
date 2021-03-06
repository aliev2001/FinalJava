package com.example.onlinevote.controllers;

import com.example.onlinevote.dto.AnswerSheet;
import com.example.onlinevote.dto.Choice;
import com.example.onlinevote.dto.GroupStat;
import com.example.onlinevote.dto.QuestionDto;
import com.example.onlinevote.models.*;
import com.example.onlinevote.repositories.*;
import com.example.onlinevote.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.summarizingDouble;

@Controller
public class MainController {
    @Autowired
    private final QuizRepository quizRepository;
    @Autowired
    private final UserService userService;
    @Autowired
    private final GroupRepository groupRepository;
    @Autowired
    private final ResultRepository resultRepository;

    @Autowired
    private final QuestionRepository questionRepository;

    @Autowired
    private final ScoreRepository scoreRepository;

    public MainController(QuizRepository quizRepository, UserService userService, GroupRepository groupRepository, ResultRepository resultRepository, QuestionRepository questionRepository, ScoreRepository scoreRepository) {
        this.quizRepository = quizRepository;
        this.userService = userService;
        this.groupRepository = groupRepository;
        this.resultRepository = resultRepository;
        this.questionRepository = questionRepository;
        this.scoreRepository = scoreRepository;
    }

    //    @GetMapping("/")
//    public String mainPage(Model model){
//        Iterable<Quiz> quizzes=quizRepository.findAll();
//        model.addAttribute("quizzes",quizzes);
//        return "main-page";
//    }
    @GetMapping("/")
    public String index(
            @RequestParam(required = false, defaultValue = "") String filter,
            Model model
    ) {
        Iterable<Quiz> quizzes;

        if (filter != null && !filter.isEmpty()) {
            quizzes = quizRepository.findByTag(filter);
        } else {
            quizzes = quizRepository.findAll();
        }
        model.addAttribute("quizzes", quizzes);

        return "main-page";
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/delete")
    public String delete(@RequestParam Long id) {
        quizRepository.deleteById(Math.toIntExact(id));
        return "redirect:/";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/statistics/{quiz}")
    public String getStatistics(Model model, @PathVariable(name = "quiz") Quiz quiz) {
//        List<GroupDto> groupDtos = new ArrayList<>();
        Iterable<Group> groupList = groupRepository.findAll();

//        groupList.forEach(e -> groupDtos.add(new GroupDto(e)));

        List<GroupStat> groupStats = new ArrayList<>();
        groupList.forEach(e -> groupStats.add(getStatistics(quiz, e)));


        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = null;

        try {
            json = ow.writeValueAsString(groupStats);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } finally {
            model.addAttribute("groups", json);
            model.addAttribute("scores",scoreRepository.getAllByQuiz(quiz));
        }
        return "statistics-page";
    }

    @GetMapping("/pass/{quiz}")
    public String passQuiz(Model model, @PathVariable(name = "quiz") Quiz quiz) {
        model.addAttribute("quiz", quiz);
        model.addAttribute("form",new AnswerSheet(quiz));
        model.addAttribute("questionDto",new QuestionDto());
        return "quiz-pass-page";

    }

    @PostMapping("/pass/{quiz}")
    public String passQuizPost(Principal principal, Model model, @PathVariable(name = "quiz") Quiz quiz, HttpServletRequest httpServletRequest) {
        User user = userService.getUserByUsername(principal.getName());

        List<Choice> choiceList=new ArrayList<>();

        quiz.getQuestions().forEach(e->choiceList.add(new Choice(e.getId(),httpServletRequest.getParameter(String.valueOf(e.getId())))));

        System.out.println(choiceList);

        choiceList.forEach(e -> resultRepository.save(new Result(
                user, questionRepository.getById(e.getQuestionID()), e.getAnswerID())));

        List<Result> resultList = resultRepository.getByQuestionQuizAndUser(quiz, user);

        Score score = new Score();
        score.setQuiz(quiz);
        score.setUser(user);
        System.out.println("Size:"+resultList.size());

        resultList.forEach(e -> {
            if (e.isTrue()) {
                System.out.println("TRUE");
                score.addScore();
            }
        });
        Score score1=scoreRepository.save(score);

        return "redirect:/score/" + score1.getId();

    }

    @GetMapping("/score/{score}")
    public String getScore(Model model, @PathVariable(name = "score") int score) {
        Optional<Score> score1=scoreRepository.findById(score);
        if(score1.isPresent()){
            model.addAttribute("score", score1.get());
            return "score-page";
        }
        return "error-page";

    }


    public GroupStat getStatistics(Quiz quiz, Group group) {
        GroupStat groupStat = new GroupStat(group.getName());
        List<Score> scores = scoreRepository.getAllByQuizAndUserGroup(quiz, group);

        DoubleSummaryStatistics result = scores.stream()
                .collect(summarizingDouble(Score::getScore));
        groupStat.setDoubleSummaryStatistics(result);

        return groupStat;
    }

}
