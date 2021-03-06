package com.example.onlinevote.repositories;

import com.example.onlinevote.models.Group;
import com.example.onlinevote.models.Quiz;
import com.example.onlinevote.models.Result;
import com.example.onlinevote.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ResultRepository extends CrudRepository<Result, Integer> {
    List<Result> getAllByQuestionQuiz(Quiz quiz);
    List<Result> getAllByQuestionQuizAndUserGroup(Quiz quiz, Group user_group);
    List<Result> getByQuestionQuizAndUser(Quiz question_quiz, User user);

}
