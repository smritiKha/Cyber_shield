package com.cybershield;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Locale;
@Configuration
public class AdminBootstrap {
 @Bean CommandLineRunner provisionAdmin(AccountRepository accounts,
  @Value("${ADMIN_EMAIL:}") String email,@Value("${ADMIN_PASSWORD:}") String password){
  return args -> {
   if(email.isBlank() || password.isBlank()) return;
   if(password.length()<16) throw new IllegalStateException("ADMIN_PASSWORD must be at least 16 characters");
   String normalized=email.trim().toLowerCase(Locale.ROOT);
   if(accounts.findByEmail(normalized).isEmpty()){
    UserAccount admin=new UserAccount(normalized,"CyberShield Administrator",new BCryptPasswordEncoder(12).encode(password));
    admin.role="ADMIN";
    accounts.save(admin);
   }
  };
 }
}