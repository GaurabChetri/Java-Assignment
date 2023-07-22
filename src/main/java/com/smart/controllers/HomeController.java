package com.smart.controllers;

import javax.crypto.interfaces.DHPublicKey;

import org.aspectj.weaver.NewConstructorTypeMunger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.repositories.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home(Model model ){
		model.addAttribute("title", "Home-Smart contact manager");
		
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model ){
		model.addAttribute("title", "About-Smart contact manager");
		
		return "about";
		
	}
		
	@RequestMapping("/signup")
	public String signup(Model model ){
			model.addAttribute("title", "Sign up-Smart contact manager");
			model.addAttribute("user",new User());
			
			return "signup";
	}
	
	//this is handler for registering user
	
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user")User user,BindingResult result1,@RequestParam(value = "agreement",defaultValue = "false")boolean agreement,Model model,HttpSession session){
		
		try {
			if(!agreement) {
				System.out.println("you have not agreed the terms and conditions");
				throw new Exception("you have not agreed the terms and conditions");
			}
			
			if(result1.hasErrors()) {
				
				System.out.println("errors" +result1.toString());
				
				model.addAttribute("user",user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement" +agreement);
			System.out.println("USER"+user);
			
			User result = this.userRepository.save(user);
			
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("succesfully registered!! ","alert-success"));
			
			return "signup";
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Something Went wrong"+e.getMessage(), "alert-danger"));
			return"signup";
		}
		
	}
	//handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model)
	{
		model.addAttribute("title","Login Page");
		return "login";
		
	}
	
	
}


