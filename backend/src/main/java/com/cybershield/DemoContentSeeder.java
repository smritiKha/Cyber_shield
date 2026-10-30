package com.cybershield;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
@Configuration
public class DemoContentSeeder {
 @Bean CommandLineRunner seedLearningContent(ContentRepository repository){
  return args -> {
   if(repository.count()>0) return;
   repository.save(new LearningContent("COURSE","Cybersecurity Foundations","A practical introduction to safer digital habits.",
    "{\"text\":\"Start with account security, phishing awareness, safe browsing, and privacy. Practise in the interactive activities and review your answers. Never share passwords or verification codes.\"}",true));
   repository.save(new LearningContent("LESSON","Verify before you trust","Learn to identify suspicious requests without opening links.",
    "{\"text\":\"A familiar logo or sender name is not proof of authenticity. Unexpected payment, password, or verification-code requests deserve independent verification. Open your trusted app or bookmarked website rather than using an unexpected link.\"}",true));
   repository.save(new LearningContent("LESSON","Privacy and account protection","Develop safer habits around data and authentication.",
    "{\"text\":\"Use unique passwords or passkeys, turn on multi-factor authentication, and review app permissions. Think carefully before posting personal details. If an account prompt appears unexpectedly, deny it and investigate through official channels.\"}",true));
   repository.save(new LearningContent("QUIZ","Account Security Check","Test practical authentication decisions.",
    "{\"questions\":[{\"question\":\"What should you do with an unexpected verification code request?\",\"options\":[\"Share the code\",\"Decline and check your account independently\",\"Post it publicly\"],\"correct\":1,\"explanation\":\"Never share verification codes.\"},{\"question\":\"Which habit helps protect different accounts?\",\"options\":[\"Reuse one password\",\"Use unique passwords or passkeys\",\"Disable MFA\"],\"correct\":1,\"explanation\":\"Unique authentication limits the damage from one compromise.\"}]}",true));
   repository.save(new LearningContent("PHISHING","Message Verification Challenge","Practise recognizing pressure tactics in fictional messages.",
    "{\"questions\":[{\"question\":\"A message asks you to send a one-time code to prevent account closure. What is safest?\",\"options\":[\"Send the code\",\"Verify through the official app\"],\"correct\":1,\"explanation\":\"Unexpected code requests are suspicious.\"},{\"question\":\"A library reminder says to check your usual portal, with no link or payment request. Which response is most appropriate?\",\"options\":[\"Check the portal independently if needed\",\"Send your password to the sender\"],\"correct\":0,\"explanation\":\"Use your known portal and never send credentials.\"}]}",true));
   repository.save(new LearningContent("CHALLENGE","URL and Privacy Decisions","Practise safer choices around URLs and personal data.",
    "{\"questions\":[{\"question\":\"Does HTTPS alone prove a website is legitimate?\",\"options\":[\"Yes\",\"No\"],\"correct\":1,\"explanation\":\"HTTPS encrypts a connection but does not prove the site's identity.\"},{\"question\":\"A QR code from an unknown flyer leads to a login page. What should you do?\",\"options\":[\"Enter credentials immediately\",\"Verify the destination independently\"],\"correct\":1,\"explanation\":\"QR codes can hide unfamiliar destinations.\"},{\"question\":\"Should you share a classmate's phone number without permission?\",\"options\":[\"No, ask for consent\",\"Yes, if the group is large\"],\"correct\":0,\"explanation\":\"Respect personal information and consent.\"}]}",true));
  };
 }
}
