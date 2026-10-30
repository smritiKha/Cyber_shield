package com.cybershield;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="learning_content")
public class LearningContent {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,length=16) public String type;
 @Column(nullable=false,length=160) public String title;
 @Column(length=1000) public String summary="";
 @Column(nullable=false,columnDefinition="text") public String body="{}";
 @Column(nullable=false) public boolean published=false;
 @Column(nullable=false) public Instant updatedAt=Instant.now();
 protected LearningContent(){}
 public LearningContent(String type,String title,String summary,String body,boolean published){this.type=type;this.title=title;this.summary=summary;this.body=body;this.published=published;}
}