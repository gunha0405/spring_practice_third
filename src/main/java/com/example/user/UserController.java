package com.example.user;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.example.DataNotFoundException;
import com.example.answer.Answer;
import com.example.comment.Comment;
import com.example.question.Question;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/user")
@RequiredArgsConstructor
@Controller
public class UserController {

	private final UserService userService;
	
	@GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }
	
	@PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", 
                    "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }

        userService.create(userCreateForm.getUsername(), 
                userCreateForm.getEmail(), userCreateForm.getPassword1(), userCreateForm.getCustomerId());

        return "redirect:/";
    }
	
	@GetMapping("/login")
    public String login() {
        return "login_form";
    }
	
	 @GetMapping("/forgot-password")
	    public String forgotPasswordForm() {
	        return "forgot_password";
	    }

	    @PostMapping("/forgot-password")
	    public String forgotPassword(@RequestParam("email") String email, Model model) {
	        try {
	            String message = userService.resetPassword(email);
	            model.addAttribute("message", message);
	        } catch (DataNotFoundException e) {
	            model.addAttribute("error", e.getMessage());
	        } catch (MessagingException e) {
				e.printStackTrace();
			}
	        return "forgot_password";
	    }

	    @GetMapping("/change-password")
	    @PreAuthorize("isAuthenticated()")
	    public String changePasswordForm() {
	        return "change_password";
	    }

	    @PostMapping("/change-password")
	    @PreAuthorize("isAuthenticated()")
	    public String changePassword(@RequestParam("currentPassword") String currentPassword,
	                                @RequestParam("newPassword") String newPassword,
	                                @RequestParam("confirmPassword") String confirmPassword,
	                                Principal principal, Model model) {
	        try {
	            String message = userService.changePassword(principal.getName(), currentPassword, newPassword, confirmPassword);
	            model.addAttribute("message", message);
	        } catch (IllegalArgumentException e) {
	            model.addAttribute("error", e.getMessage());
	        }
	        return "change_password";
	    }
	    
	    @GetMapping("/profile/{username}")
	    @PreAuthorize("isAuthenticated()")
	    public String profile(@PathVariable("username") String username, 
	    					  Principal principal,
	    					  @RequestParam(value = "questionPage", defaultValue = "0") int questionPage,
	    					  @RequestParam(value = "answerPage", defaultValue = "0") int answerPage,
	    					  @RequestParam(value = "commentPage", defaultValue = "0") int commentPage,
	    					  Model model) {
	    	
	    	if (!principal.getName().equals(username)) {
	            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "프로필 접근 권한이 없습니다.");
	        }

	        SiteUser user = userService.getUser(username);
	        Page<Question> questionPaging = userService.getUserQuestions(username, questionPage);
	        Page<Answer> answerPaging = userService.getUserAnswers(username, answerPage);
	        Page<Comment> commentPaging = userService.getUserComments(username, commentPage);

	        model.addAttribute("user", user);
	        model.addAttribute("questionPaging", questionPaging);
	        model.addAttribute("answerPaging", answerPaging);
	        model.addAttribute("commentPaging", commentPaging);
	    	
	    	return "profile";
	    }
	
}
