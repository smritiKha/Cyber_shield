package com.cybershield;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="learning_progress")
public class LearningProgress {
 @Id public Long accountId;
 @OneToOne(optional=false,fetch=FetchType.LAZY) @MapsId public UserAccount account;
 @Lob @Column(nullable=false) public String progressJson="{}";
 @Column(nullable=false) public Instant updatedAt=Instant.now();
 protected LearningProgress(){}
 public LearningProgress(UserAccount account,String json){this.account=account;this.progressJson=json;}
}