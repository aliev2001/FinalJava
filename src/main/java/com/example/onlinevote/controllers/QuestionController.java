package com.example.onlinevote.controllers;

import com.example.onlinevote.models.Question;
import com.example.onlinevote.repositories.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/question")
public class QuestionController {
    @Autowired
    private final QuestionRepository questionRepository;

    public QuestionController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @PostMapping("/remove/{question}")
    public String removeQuestion(@PathVariable(name = "question") Question question) {
        questionRepository.delete(question);
        return "redirect:/quiz/details/" + question.getQuiz().getId();
    }

    @GetMapping("/edit/{question}")
    public String editQuestion(Model model, @PathVariable(name = "question") Question question) {
        model.addAttribute("question",question);
        return "question-edit-page";
    }
    @PostMapping("/edit")
    public String editQuestionPost(@ModelAttribute Question question) {
        Question question1=questionRepository.save(question);
        return "redirect:/quiz/details/" + question1.getQuiz().getId();
    }

}