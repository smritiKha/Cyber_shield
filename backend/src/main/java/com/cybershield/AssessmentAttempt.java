package com.cybershield;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="assessment_attempts")
public class AssessmentAttempt {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) public UserAccount student;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) public LearningContent assessment;
 @Column(nullable=false) public int score;
 @Column(nullable=false) public int total;
 @Column(nullable=false) public Instant attemptedAt=Instant.now();
 protected AssessmentAttempt(){}
 public AssessmentAttempt(UserAccount student,LearningContent assessment,int score,int total){this.student=student;this.assessment=assessment;this.score=score;this.total=total;}
}