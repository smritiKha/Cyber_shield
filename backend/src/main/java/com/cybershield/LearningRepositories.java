package com.cybershield;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
interface ContentRepository extends JpaRepository<LearningContent,Long> {
 List<LearningContent> findByPublishedTrueOrderByUpdatedAtDesc();
 List<LearningContent> findByTypeAndPublishedTrue(String type);
}
interface EnrollmentRepository extends JpaRepository<Enrollment,Long> {
 List<Enrollment> findByStudentId(Long studentId);
 Optional<Enrollment> findByStudentIdAndCourseId(Long studentId,Long courseId);
 long countByCompletedTrue();
}
interface AttemptRepository extends JpaRepository<AssessmentAttempt,Long> {
 List<AssessmentAttempt> findByStudentIdOrderByAttemptedAtDesc(Long studentId);
 List<AssessmentAttempt> findByAssessmentId(Long assessmentId);
}
