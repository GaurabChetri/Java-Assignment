package com.smart.controllers;

import java.io.File;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.aspectj.weaver.NewConstructorTypeMunger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.repositories.ContactRepository;
import com.smart.repositories.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {
	 
	 @Autowired
	 private BCryptPasswordEncoder bCryptPasswordEncoder;
	 
	 @Autowired
	 private UserRepository userRepository;
	 
	 @Autowired
	 private ContactRepository contactRepository;
	 
	 @ModelAttribute
	 public void addCommonData(Model model,Principal principal) {
		 
			String userName=principal.getName();
			System.out.println("Username "+userName);
			
			User user = userRepository.getUserByUserName(userName);
			System.out.println("User "+user);
			
			model.addAttribute("user",user);
			
		 
	 }
	
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		
	
		return "normal/user_dashboard";
	}
	
	//open add form handler
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
		
	}
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute("contact") @Valid Contact contact, BindingResult bindingResult, Model model,@RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session) {
	
		
	 try {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		if(file.isEmpty()) {
			
			System.out.println("file is empty");
			contact.setImage("contact.png");	
		}else {
			contact.setImage(file.getOriginalFilename());
			
			File saveFile=new ClassPathResource("static/image").getFile();
			
			Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path ,StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image is uploaded");
		}
		
		contact.setUser(user);
		user.getContacts().add(contact);
		this.userRepository.save(user);
		System.out.println("Data "+contact);
		System.out.println("Added to the database");
		
		session.setAttribute("message", new Message("Succesfully Added", "success"));
		
		
		
	 }
	 catch (Exception e) {
		System.out.println("ERROR"+e.getMessage());
		e.printStackTrace();
		session.setAttribute("message", new Message("Something Went Wrong", "danger"));	

		
	
		}
	 return "normal/add_contact_form";
 }
	//Show contacts
	//per page=5[n]
	//current page =0
	@GetMapping("/show-contacts/{page}")
	public String showContacs(@PathVariable("page") Integer page,Model m,Principal principal) {
		
		m.addAttribute("title","Show User Contacts");
		
		String userName =  principal.getName();
		
		User user =  this.userRepository.getUserByUserName(userName);
		
		Pageable pageable = PageRequest.of(page, 5);
		
		Page<Contact> contacts =   this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalpages",contacts.getTotalPages());
		
		return "normal/show_contacts";
		
	}
	
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal) {
		
		System.out.println("CID"+cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		
		Contact contact = contactOptional.get();
		
		String userName =  principal.getName();
		
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
		
		   model.addAttribute("contact",contact);
		   model.addAttribute("title", contact.getName());
		}
		 
		return"normal/contact_detail";
	}
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Model model,Principal principal,HttpSession session) {
		
		
		Contact contact =  this.contactRepository.findById(cId).get();
		
		String userName = principal.getName();
		
		User user = this.userRepository.getUserByUserName(userName);
		
	
		
		if(user.getId()==contact.getUser().getId()) {
			
			User user2 = this.userRepository.getUserByUserName(principal.getName());
			user2.getContacts().remove(contact);
			this.userRepository.save(user);
			
			session.setAttribute("message", new Message("Contact deleted Successfully","success"));
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId")Integer cId,Model model) {
		
		model.addAttribute("title","update Contact");	
        Contact contact = this.contactRepository.findById(cId).get();
        model.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	@RequestMapping(value="/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile file,Model model,HttpSession session,Principal principal) {
		
		try {
			
			 Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			if(!file.isEmpty()) {
				
				File deleFile=new ClassPathResource("static/image").getFile();
				
				File file2 = new File(deleFile,oldContactDetail.getImage());
				file2.delete();
				
		
				
				
				
				File saveFile=new ClassPathResource("static/image").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path ,StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
				
			}else {
				
				contact.setImage(oldContactDetail.getImage());
				
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("your contact is updated", "success"));
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		System.out.println("CONTACT NAME"+contact.getName());
		System.out.println("CONTACT ID"+contact.getcId());
		return"redirect:/user/"+contact.getcId()+"/contact";
	}
	
	 @GetMapping("/profile")
	 public String yourProfile(Model model) {
		 
		 model.addAttribute("title","Profile Page");
		 return"normal/profile";
	 }
	 
	 
	 @GetMapping("/settings")
	 public String openSettings() {
		 
		 return"normal/settings";
	 }
	
	 @PostMapping("/change-password")
	 public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword")String newPassword,Principal principal,HttpSession session) {
		 
		 System.out.println("Old Password "+oldPassword);
		 System.out.println("New Password "+newPassword);
		 
		 String userName = principal.getName();
		 
		 User currentUser = this.userRepository.getUserByUserName(userName);
		 
		 System.out.println(currentUser.getPassword());
		 
		 if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			  
			 currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			 this.userRepository.save(currentUser);
			 session.setAttribute("message", new Message("your password is updated", "success"));
			 
			 
		 }else {
			 
			 session.setAttribute("message", new Message("wrong old password", "danger"));
			 return "redirect:/user/settings";
		 }
		 
		 return"redirect:/user/index";
	 }
}
