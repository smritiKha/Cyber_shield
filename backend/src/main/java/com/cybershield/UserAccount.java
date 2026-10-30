package com.cybershield;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="accounts",uniqueConstraints=@UniqueConstraint(columnNames="email"))
public class UserAccount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,length=254) public String email;
 @Column(nullable=false,length=100) public String displayName;
 @Column(nullable=false) public String passwordHash;
 @Column(nullable=false) public Instant createdAt=Instant.now();
 protected UserAccount(){}
 public UserAccount(String email,String displayName,String passwordHash){this.email=email;this.displayName=displayName;this.passwordHash=passwordHash;}
}