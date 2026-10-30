package com.cybershield;
import com.fasterxml.jackson.databind.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
@RestController @RequestMapping("/api")
public class LearningController {
 private final ApiController auth;
 private final ContentRepository content;
 private final EnrollmentRepository enrollments;
 private final AttemptRepository attempts;
 private final AccountRepository accounts;
 private final ObjectMapper mapper;
 public LearningController(ApiController auth,ContentRepository content,EnrollmentRepository enrollments,AttemptRepository attempts,AccountRepository accounts,ObjectMapper mapper){
  this.auth=auth;this.content=content;this.enrollments=enrollments;this.attempts=attempts;this.accounts=accounts;this.mapper=mapper;
 }
 private UserAccount student(String token){
  UserAccount user=auth.authenticated(token);
  if(!"STUDENT".equals(user.role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Student access required");
  return user;
 }
 private UserAccount admin(String token){
  UserAccount user=auth.authenticated(token);
  if(!"ADMIN".equals(user.role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Administrator access required");
  return user;
 }
 public record ContentInput(@NotBlank String type,@NotBlank @Size(max=160) String title,@Size(max=1000) String summary,@NotNull JsonNode body,boolean published){}
 public record ContentView(Long id,String type,String title,String summary,JsonNode body,boolean published,Instant updatedAt){}
 public record EnrollmentView(Long courseId,String title,boolean completed,Instant enrolledAt){}
 public record AttemptView(Long id,Long assessmentId,String title,int score,int total,Instant attemptedAt){}
 public record Answers(List<Integer> answers){}
 private ContentView view(LearningContent item) {
  try {return new ContentView(item.id,item.type,item.title,item.summary,mapper.readTree(item.body),item.published,item.updatedAt);}
  catch(Exception e){throw new IllegalStateException(e);}
 }
 private LearningContent find(Long id){return content.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Content not found"));}
 private void validate(ContentInput input) {
  if(!Set.of("COURSE","LESSON","QUIZ","PHISHING","CHALLENGE").contains(input.type())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported content type");
  if(!input.body().isObject()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Content body must be an object");
  try {if(mapper.writeValueAsString(input.body()).length()>30000) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,"Content too large");}
  catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid content");}
  if(Set.of("QUIZ","PHISHING","CHALLENGE").contains(input.type())) {
   JsonNode questions=input.body().path("questions");
   if(!questions.isArray() || questions.isEmpty() || questions.size()>40) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Assessment needs 1 to 40 questions");
   for(JsonNode question:questions){
    JsonNode options=question.path("options");
    if(!question.path("question").isTextual() || !options.isArray() || options.size()<2 || options.size()>6 || !question.path("correct").canConvertToInt() || question.path("correct").asInt()<0 || question.path("correct").asInt()>=options.size())
     throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Each question needs a prompt, 2–6 choices and a valid answer index");
   }
  }
 }
 @GetMapping("/catalog")
 public List<ContentView> catalog(){return content.findByPublishedTrueOrderByUpdatedAtDesc().stream().map(this::view).map(v-> {
  if(Set.of("QUIZ","PHISHING","CHALLENGE").contains(v.type())) {
   JsonNode copy=v.body().deepCopy();
   if(copy instanceof com.fasterxml.jackson.databind.node.ObjectNode obj){
    JsonNode questions=obj.path("questions");
    if(questions.isArray()) for(JsonNode question:questions) if(question instanceof com.fasterxml.jackson.databind.node.ObjectNode q){q.remove("correct");q.remove("explanation");}
   }
   return new ContentView(v.id(),v.type(),v.title(),v.summary(),copy,v.published(),v.updatedAt());
  }
  return v;
 }).toList();}
 @GetMapping("/admin/content")
 public List<ContentView> adminContent(@RequestHeader(value="Authorization",required=false) String token){admin(token);return content.findAll().stream().map(this::view).toList();}
 @PostMapping("/admin/content") @Transactional
 public ContentView create(@RequestHeader(value="Authorization",required=false) String token,@Valid @RequestBody ContentInput input){
  admin(token);validate(input);
  return view(content.save(new LearningContent(input.type(),input.title().trim(),Objects.toString(input.summary(),""),input.body().toString(),input.published())));
 }
 @PutMapping("/admin/content/{id}") @Transactional
 public ContentView update(@RequestHeader(value="Authorization",required=false) String token,@PathVariable Long id,@Valid @RequestBody ContentInput input){
  admin(token);validate(input);
  LearningContent item=find(id);
  if(!item.type.equals(input.type())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cannot change existing content type");
  item.title=input.title().trim();item.summary=Objects.toString(input.summary(),"");item.body=input.body().toString();item.published=input.published();item.updatedAt=Instant.now();
  return view(content.save(item));
 }
 @DeleteMapping("/admin/content/{id}") @Transactional
 public ResponseEntity<Void> delete(@RequestHeader(value="Authorization",required=false) String token,@PathVariable Long id){
  admin(token);
  LearningContent item=find(id);
  if(enrollments.findAll().stream().anyMatch(e->e.course.id.equals(id)) || !attempts.findByAssessmentId(id).isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Content has student records; unpublish instead");
  content.delete(item);return ResponseEntity.noContent().build();
 }
 @PostMapping("/student/enroll/{id}") @Transactional
 public EnrollmentView enroll(@RequestHeader(value="Authorization",required=false) String token,@PathVariable Long id){
  UserAccount user=student(token);LearningContent course=find(id);
  if(!course.published || !"COURSE".equals(course.type)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Published course required");
  Enrollment record=enrollments.findByStudentIdAndCourseId(user.id,id).orElseGet(()->enrollments.save(new Enrollment(user,course)));
  return new EnrollmentView(id,course.title,record.completed,record.enrolledAt);
 }
 @PostMapping("/student/complete/{id}") @Transactional
 public EnrollmentView complete(@RequestHeader(value="Authorization",required=false) String token,@PathVariable Long id){
  UserAccount user=student(token);
  Enrollment record=enrollments.findByStudentIdAndCourseId(user.id,id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Enroll in this course first"));
  record.completed=true;record.lastActivity=Instant.now();enrollments.save(record);
  return new EnrollmentView(id,record.course.title,true,record.enrolledAt);
 }
 @PostMapping("/student/submit/{id}") @Transactional
 public AttemptView submit(@RequestHeader(value="Authorization",required=false) String token,@PathVariable Long id,@RequestBody Answers input){
  UserAccount user=student(token);LearningContent item=find(id);
  if(!item.published || !Set.of("QUIZ","PHISHING","CHALLENGE").contains(item.type)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Published assessment required");
  JsonNode questions;
  try {questions=mapper.readTree(item.body).path("questions");}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid assessment");}
  if(input.answers()==null || input.answers().size()!=questions.size()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Answer every question");
  int score=0;
  for(int i=0;i<questions.size();i++) if(Objects.equals(input.answers().get(i),questions.get(i).path("correct").asInt())) score++;
  AssessmentAttempt saved=attempts.save(new AssessmentAttempt(user,item,score,questions.size()));
  return new AttemptView(saved.id,item.id,item.title,score,questions.size(),saved.attemptedAt);
 }
 @GetMapping("/student/dashboard")
 public Map<String,Object> dashboard(@RequestHeader(value="Authorization",required=false) String token){
  UserAccount user=student(token);
  List<Enrollment> courses=enrollments.findByStudentId(user.id);
  List<AssessmentAttempt> history=attempts.findByStudentIdOrderByAttemptedAtDesc(user.id);
  List<EnrollmentView> courseViews=courses.stream().map(e->new EnrollmentView(e.course.id,e.course.title,e.completed,e.enrolledAt)).toList();
  List<AttemptView> attemptViews=history.stream().limit(50).map(a->new AttemptView(a.id,a.assessment.id,a.assessment.title,a.score,a.total,a.attemptedAt)).toList();
  Set<LocalDate> days=new HashSet<>();for(AssessmentAttempt a:history)days.add(a.attemptedAt.atZone(ZoneOffset.UTC).toLocalDate());
  int streak=0;LocalDate day=LocalDate.now(ZoneOffset.UTC);
  if(!days.contains(day)) day=day.minusDays(1);
  while(days.contains(day)){streak++;day=day.minusDays(1);}
  long completed=courses.stream().filter(e->e.completed).count();
  return Map.of("enrollments",courseViews,"attempts",attemptViews,"completed",completed,"streak",streak,"certificates",courses.stream().filter(e->e.completed).map(e->e.course.title).toList(),"recommendation",courses.stream().filter(e->!e.completed).findFirst().map(e->"Continue "+e.course.title).orElse("Explore a published course or assessment"));
 }
 @GetMapping("/admin/analytics")
 public Map<String,Object> analytics(@RequestHeader(value="Authorization",required=false) String token){
  admin(token);
  List<AssessmentAttempt> all=attempts.findAll();
  long studentCount=accounts.findAll().stream().filter(a->"STUDENT".equals(a.role)).count();
  long active=all.stream().filter(a->a.attemptedAt.isAfter(Instant.now().minusSeconds(30L*86400))).map(a->a.student.id).distinct().count();
  Map<String,List<AssessmentAttempt>> grouped=new TreeMap<>();
  for(AssessmentAttempt a:all)grouped.computeIfAbsent(a.assessment.title,k->new ArrayList<>()).add(a);
  List<Map<String,Object>> topics=new ArrayList<>();
  grouped.forEach((title,items)->{int total=items.stream().mapToInt(a->a.total).sum();int correct=items.stream().mapToInt(a->a.score).sum();topics.add(Map.of("title",title,"attempts",items.size(),"accuracy",total==0?0:Math.round(correct*100.0/total)));});
  topics.sort(Comparator.comparingDouble(a->((Number)a.get("accuracy")).doubleValue()));
  return Map.of("students",studentCount,"activeLearners30Days",active,"courses",content.findByTypeAndPublishedTrue("COURSE").size(),"enrollments",enrollments.count(),"completions",enrollments.countByCompletedTrue(),"assessmentAttempts",all.size(),"difficultTopics",topics);
 }
}
