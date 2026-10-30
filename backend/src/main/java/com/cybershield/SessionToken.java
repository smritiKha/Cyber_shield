package com.cybershield;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="sessions",indexes=@Index(columnList="tokenHash",unique=true))
public class SessionToken {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) public UserAccount account;
 @Column(nullable=false,length=64) public String tokenHash;
 @Column(nullable=false) public Instant expiresAt;
 protected SessionToken(){}
 public SessionToken(UserAccount account,String tokenHash,Instant expiresAt){this.account=account;this.tokenHash=tokenHash;this.expiresAt=expiresAt;}
}