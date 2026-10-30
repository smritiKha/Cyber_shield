package com.cybershield;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="enrollments",uniqueConstraints=@UniqueConstraint(columnNames={"student_id","course_id"}))
public class Enrollment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="student_id") public UserAccount student;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="course_id") public LearningContent course;
 @Column(nullable=false) public Instant enrolledAt=Instant.now();
 @Column(nullable=false) public boolean completed=false;
 @Column(nullable=false) public Instant lastActivity=Instant.now();
 protected Enrollment(){}
 public Enrollment(UserAccount student,LearningContent course){this.student=student;this.course=course;}
}