package com.example.onlinevote.repositories;


import com.example.onlinevote.models.Quiz;
import org.springframework.data.repository.CrudRepository;

public interface QuizRepository extends CrudRepository<Quiz, Integer> {
    Iterable<Quiz> findByTag(String filter);
}
