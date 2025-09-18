package com.example.answer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.example.comment.Comment;
import com.example.question.Question;
import com.example.user.SiteUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Answer {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Lob
	@Column(length = 4000)
	private String content;

    private LocalDateTime createDate; 
    
    private LocalDateTime modifyDate;
    
    @ManyToOne 
    private Question question;  
    
    @ManyToOne
    private SiteUser author;
    
    @ManyToMany
    @JoinTable(
        name = "answer_voter",
        joinColumns = @JoinColumn(name = "answer_id"),
        inverseJoinColumns = @JoinColumn(name = "voter_id")
    )
    Set<SiteUser> voter;
    
    @OneToMany(mappedBy = "answer")
    private List<Comment> commentList;
	
}
