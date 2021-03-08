package com.example.onlinevote.repositories;

import com.example.onlinevote.models.Group;
import com.example.onlinevote.models.Quiz;
import com.example.onlinevote.models.Score;
import com.example.onlinevote.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRepository extends CrudRepository<Score,Integer> {
    List<Score> getAllByQuiz(Quiz quiz);
    List<Score> getAllByQuizAndUserGroup(Quiz quiz, Group user_group);
    Score getByUser(User user);
    boolean existsByUserAndQuiz(User user, Quiz quiz);
    Score getByUserAndQuiz(User user, Quiz quiz);
    @Query("select o.quiz from Score o where o.user=?1")
    List<Quiz> getUserPassedQuizzes(User user);



}
