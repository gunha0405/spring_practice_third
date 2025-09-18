package com.example.answer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.question.Question;

public interface AnswerRepository extends JpaRepository<Answer, Integer>{
	Page<Answer> findByAuthorUsername(String username, Pageable pageable);
    Page<Answer> findByQuestion(Question question, Pageable pageable);

    @Query("SELECT a FROM Answer a " +
           "LEFT JOIN a.voter v " +
           "WHERE a.question = :question " +
           "GROUP BY a " +
           "ORDER BY COUNT(v) DESC, a.createDate DESC")
    Page<Answer> findByQuestionOrderByVoteCount(@Param("question") Question question, Pageable pageable);
    
    
}
