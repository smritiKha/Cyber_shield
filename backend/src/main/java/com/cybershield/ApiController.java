package com.cybershield;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
@RestController @RequestMapping("/api")
public class ApiController {
 private final AccountRepository accounts;
 private final SessionRepository sessions;
 private final ProgressRepository progress;
 private final ObjectMapper mapper;
 private final BCryptPasswordEncoder passwords=new BCryptPasswordEncoder(12);
 private final SecureRandom random=new SecureRandom();
 public ApiController(AccountRepository a,SessionRepository s,ProgressRepository p,ObjectMapper m){accounts=a;sessions=s;progress=p;mapper=m;}
 public record Credentials(@NotBlank @Email String email,@NotBlank String password){}
 public record Signup(@NotBlank @Email String email,@NotBlank @Size(min=2,max=80) String displayName,@NotBlank @Size(min=12,max=128) String password){}
 public record AccountView(Long id,String email,String displayName,String role){}
 public record LoginResponse(String token,AccountView account){}
 public record ProgressPayload(JsonNode progress){}
 public record ProgressResponse(JsonNode progress,Instant updatedAt){}
 private String hash(String token){
  try {byte[] digest=MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));return HexFormat.of().formatHex(digest);}
  catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}
 }
 private AccountView view(UserAccount a){return new AccountView(a.id,a.email,a.displayName,a.role);}
 private LoginResponse issue(UserAccount a){
  byte[] bytes=new byte[32];random.nextBytes(bytes);
  String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  sessions.save(new SessionToken(a,hash(token),Instant.now().plus(7,ChronoUnit.DAYS)));
  return new LoginResponse(token,view(a));
 }
 private String bearer(String authorization){
  if(authorization==null || !authorization.startsWith("Bearer ")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Sign in required");
  String token=authorization.substring(7);
  if(token.length()<30 || token.length()>128) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid session");
  return token;
 }
 UserAccount authenticated(String authorization){
  SessionToken session=sessions.findByTokenHash(hash(bearer(authorization))).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid session"));
  if(!session.expiresAt.isAfter(Instant.now())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Session expired");
  return session.account;
 }
 @PostMapping("/auth/signup") @Transactional
 public LoginResponse signup(@Valid @RequestBody Signup input){
  String email=input.email().trim().toLowerCase(Locale.ROOT);
  if(accounts.findByEmail(email).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Account already exists");
  if(input.password().length()<12) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Password must contain at least 12 characters");
  return issue(accounts.save(new UserAccount(email,input.displayName().trim(),passwords.encode(input.password()))));
 }
 @PostMapping("/auth/login")
 public LoginResponse login(@Valid @RequestBody Credentials input){
  UserAccount a=accounts.findByEmail(input.email().trim().toLowerCase(Locale.ROOT)).orElse(null);
  if(a==null || !passwords.matches(input.password(),a.passwordHash)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password");
  return issue(a);
 }
 @GetMapping("/auth/me")
 public AccountView me(@RequestHeader(value="Authorization",required=false) String auth){return view(authenticated(auth));}
 @PostMapping("/auth/logout") @Transactional
 public ResponseEntity<Void> logout(@RequestHeader(value="Authorization",required=false) String auth){
  sessions.deleteByTokenHash(hash(bearer(auth)));return ResponseEntity.noContent().build();
 }
 @GetMapping("/progress")
 public ProgressResponse getProgress(@RequestHeader(value="Authorization",required=false) String auth) throws Exception {
  UserAccount a=authenticated(auth);
  LearningProgress stored=progress.findById(a.id).orElse(null);
  return stored==null?new ProgressResponse(null,null):new ProgressResponse(mapper.readTree(stored.progressJson),stored.updatedAt);
 }
 @PutMapping("/progress") @Transactional
 public ProgressResponse saveProgress(@RequestHeader(value="Authorization",required=false) String auth,@RequestBody ProgressPayload payload) throws Exception {
  UserAccount a=authenticated(auth);
  JsonNode value=payload.progress();
  if(value==null || !value.isObject() || !value.path("completedLessons").isArray()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid progress");
  String json=mapper.writeValueAsString(value);
  if(json.length()>150000) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,"Progress too large");
  LearningProgress record=progress.findById(a.id).orElseGet(()->new LearningProgress(a,json));
  record.progressJson=json;record.updatedAt=Instant.now();progress.save(record);
  return new ProgressResponse(value,record.updatedAt);
 }
 @GetMapping("/health")
 public Map<String,String> health(){return Map.of("status","ok");}
 @RestControllerAdvice
 static class Errors {
  @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
  ResponseEntity<Map<String,String>> validation(){return ResponseEntity.badRequest().body(Map.of("error","Please check the submitted fields."));}
  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<Map<String,String>> status(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("error",Objects.toString(e.getReason(),"Request failed")));}
 }
}
