package com.example.question;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.DataNotFoundException;
import com.example.answer.Answer;
import com.example.category.Category;
import com.example.category.CategoryRepository;
import com.example.file.FileMetaData;
import com.example.file.FileService;
import com.example.user.SiteUser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    
    private final CategoryRepository categoryRepository;
    
    private final FileService fileService;

    public Page<Question> getList(int page, String kw, String filter) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Order.desc("createDate"))); // 기본 최신순

        if ("answer".equals(filter)) {
            return questionRepository.findQuestionsOrderByLatestAnswer(pageable);
        } else if ("comment".equals(filter)) {
            return questionRepository.findQuestionsOrderByLatestComment(pageable);
        } else {
            return questionRepository.findByKeyword(kw, pageable);
        }
    }
    
    @Transactional
    public Question getQuestion(Long id) {  
        Optional<Question> question = this.questionRepository.findById(id);
        if (question.isPresent()) {
        	Question q = question.get();
        	q.setViewCount(q.getViewCount()+1);
            return q;
        } else {
            throw new DataNotFoundException("question not found");
        }
    }
    
    public void create(String subject, String content, Integer categoryId, List<MultipartFile> files, SiteUser user) throws IOException {
    	Question q = new Question();
        q.setSubject(subject);
        q.setContent(content);
        q.setAuthor(user);
        q.setViewCount(0);
        q.setCreateDate(LocalDateTime.now());
        if (categoryId != null) {
            Optional<Category> category = categoryRepository.findById(categoryId);
            q.setCategory(category.orElse(null));
        }
        Question savedQ = questionRepository.save(q);
        
        List<FileMetaData> fileList = fileService.saveFiles(files, "question", savedQ);
        savedQ.getFiles().addAll(fileList);
        
    }
    
    public void modify(Question question, String subject, String content) {
    	question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());
        this.questionRepository.save(question);
    }
    
    public void delete(Question question) {
        this.questionRepository.delete(question);
    }
    
    public void vote(Question question, SiteUser siteUser) {
        question.getVoter().add(siteUser);
        this.questionRepository.save(question);
    }
    
    private Specification<Question> search(String kw) {
        return new Specification<>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);  // 중복을 제거 
                Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
                Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
                Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
                return cb.or(cb.like(q.get("subject"), "%" + kw + "%"), // 제목 
                        cb.like(q.get("content"), "%" + kw + "%"),      // 내용 
                        cb.like(u1.get("username"), "%" + kw + "%"),    // 질문 작성자 
                        cb.like(a.get("content"), "%" + kw + "%"),      // 답변 내용 
                        cb.like(u2.get("username"), "%" + kw + "%"));   // 답변 작성자 
            }
        };
    }
    
}