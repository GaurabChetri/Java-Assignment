package com.smart.controllers;

import java.util.Random;

import javax.swing.border.Border;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entities.User;
import com.smart.repositories.UserRepository;
import com.smart.services.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private EmailService emailService;
   
	@Autowired
	private UserRepository userRepository;
	
	 @RequestMapping("/forgot")
	 public String openEmailForm() {
		 
		 return"forgot_password_form";
	 }
	 
	 @PostMapping("/send-otp")
	 public String sendOTP(@RequestParam("email")String email,HttpSession session){
		 
		 System.out.println("Email "+email);
		 
		 Random random = new Random();
		 int otp = 1000 + random.nextInt(9000);
		 
		 
		 System.out.println("OTP "+otp);
		 
		 String subject ="OTP From SCM";
		 String message = ""
				 +"<div style ='border:1px solid #e2e2e2; padding:20px'>"
				 +"<h1>"
				 +"OTP is "
				 +"<b>"+otp
				 +"</b>"
				 +"</h1>" 
				 +"</div>";
		 String to = email;
		 
		 boolean flag = this.emailService.sendEmail(subject, message, to);
		 
		 if(flag) {
			    
			    session.setAttribute("myotp", otp);
			    session.setAttribute("email", email);
		        return"verify_otp";
	       }else {
	    	  session.setAttribute("message", "Check Your Email id");
	    	  
	    	  return"forgot_password_form";
	       }
		 
	       
		 }
	 
	 @PostMapping("/verify-otp")
	 public String verifyOtp(@RequestParam("otp")int otp,HttpSession session) {
		 
		 int myOtp = (int) session.getAttribute("myotp");
		 
		 String email = (String) session.getAttribute("email");
		 
		 if (myOtp==otp) {
			 
			User user = this.userRepository.getUserByUserName(email);
			
			if(user==null) {
				
				//send error message
				session.setAttribute("message", "Check Your Email id");
				return"forgot_password_form";
				
			}
			else {
				
				//send change password form
				
			}
			 
			 return"password_change_form";
		 }else {
			 
			 session.setAttribute("message", "you have entered wrong otp");
			 return"verify_otp";
		 }
		 
		 
	 }
	 //change password
	 
	 @PostMapping("/change-password")
	 public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session) {
		 
		 String email = (String) session.getAttribute("email");
		 User user = this.userRepository.getUserByUserName(email);
		 user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		 this.userRepository.save(user);
		
		 return "redirect:/signin?change=password changed successfully..";
	 }
	 
	 
	 
	 
	
}
