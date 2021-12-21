package gmail.maihai86.exam.controller;

import gmail.maihai86.exam.dto.*;
import gmail.maihai86.exam.exception.UserAlreadyExistAuthenticationException;
import gmail.maihai86.exam.service.MessageService;
import gmail.maihai86.exam.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Locale;

@Controller
@Slf4j
public class AppController {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private PasswordValidator passwordValidator;

    @RequestMapping("/register")
    public String showRegister(Model model) {
        UserRegistrationForm userRegistrationForm = new UserRegistrationForm();
        model.addAttribute("userRegistrationForm", userRegistrationForm);

        return "register";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(@ModelAttribute("userRegistrationForm") @Valid UserRegistrationForm userRegistrationForm,
                           BindingResult result, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        if (result.hasErrors()) {
            return "register";
        }

        // continue validating
        RuleResult ruleResult = passwordValidator.validate(new PasswordData(userRegistrationForm.getPassword()));
        if (!ruleResult.isValid()) {
            ruleResult.getDetails().forEach(ruleResultDetail -> {
                ObjectError error = new ObjectError("globalError", messageService.getMessage(ruleResultDetail.getErrorCode()));
                result.addError(error);
            });
            return "register";
        }

        try {
            userService.registerNewUser(userRegistrationForm, request, false);
        } catch (UserAlreadyExistAuthenticationException e) {
            log.error("register LOCAL user ERROR", e);
            ObjectError error = new ObjectError("globalError", messageService.getMessage("message.regError"));
            result.addError(error);
            return "register";
        }

        redirectAttributes.addFlashAttribute("message", messageService.getMessage("message.regSucc"));
        return "redirect:/login";
    }

    @RequestMapping("/reset-password")
    public String showResetPassword(Model model) {
        ResetPasswordReq resetPasswordReq = new ResetPasswordReq();
        model.addAttribute("resetPasswordReq", resetPasswordReq);

        return "reset_password";
    }

    @RequestMapping(value = "/reset-password", method = RequestMethod.POST)
    public String resetPassword(@AuthenticationPrincipal UsernamePasswordAuthenticationToken authentication,
                                @ModelAttribute("resetPasswordReq") @Valid ResetPasswordReq resetPasswordReq,
                                BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "reset_password";
        }

        // continue validating
        RuleResult ruleResult = passwordValidator.validate(new PasswordData(resetPasswordReq.getPassword()));
        if (!ruleResult.isValid()) {
            ruleResult.getDetails().forEach(ruleResultDetail -> {
                ObjectError error = new ObjectError("globalError", messageService.getMessage(ruleResultDetail.getErrorCode()));
                result.addError(error);
            });
            return "reset_password";
        }

        String success = userService.resetPassword((LocalUser) authentication.getPrincipal(), resetPasswordReq);
        redirectAttributes.addFlashAttribute("message", "Reset password successfully");
        return "redirect:/";
    }

    @RequestMapping("/reset-display-name")
    public String showResetDisplayName(Model model) {
        ResetNameReq resetNameReq = new ResetNameReq();
        model.addAttribute("resetNameReq", resetNameReq);

        return "reset_display_name";
    }

    @RequestMapping(value = "/reset-display-name", method = RequestMethod.POST)
    public String resetDisplayName(@AuthenticationPrincipal UsernamePasswordAuthenticationToken authentication,
                                   @ModelAttribute("resetNameReq") @Valid ResetNameReq resetNameReq,
                                   BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "reset_display_name";
        }

        String success = userService.resetDisplayName((LocalUser) authentication.getPrincipal(), resetNameReq);
        redirectAttributes.addFlashAttribute("message", "Reset display name successfully");
        return "redirect:/";
    }

    @RequestMapping("/registration-confirm")
    public String confirmRegistration(Model model, final HttpServletRequest request, RedirectAttributes redirectAttributes, @RequestParam("token") final String token) {
        Locale locale = request.getLocale();
        redirectAttributes.addFlashAttribute("lang", locale.getLanguage());
        final String result = userService.validateVerificationToken(token);
        if (result.equals("valid")) {
            redirectAttributes.addFlashAttribute("message", messageService.getMessage("message.accountVerified"));
            redirectAttributes.addFlashAttribute("token", token);
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("message", messageService.getMessage("auth.message." + result));
            redirectAttributes.addAttribute("token", token);
            return "redirect:/resend-email-verify";
        }
    }

    @RequestMapping("/resend-email-verify")
    public String showResendEmailVerify(Model model, @RequestParam("token") final String token) {
        ResendTokenReq resendTokenReq = new ResendTokenReq();
        resendTokenReq.setToken(token);
        model.addAttribute("resendTokenReq", resendTokenReq);
        return "user_not_activated";
    }

    @RequestMapping(value = "/resend-email-verify", method = RequestMethod.POST)
    public String resendEmailVerification(final HttpServletRequest request,
                                          @ModelAttribute("resendTokenReq") @Valid ResendTokenReq resendTokenReq,
                                          final Model model, final BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addAttribute("token", resendTokenReq.getToken());
            return "redirect:/resend-email-verify";
        }

        String success = userService.resendEmailVerification(request, resendTokenReq.getToken());
        redirectAttributes.addFlashAttribute("message", "Resend email verification successfully");
        return "redirect:/";
    }

}
