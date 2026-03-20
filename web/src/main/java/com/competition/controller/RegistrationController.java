package com.competition.controller;

import com.competition.common.Result;
import com.competition.entity.Registration;
import com.competition.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registration")
public class RegistrationController {
    @Autowired
    private RegistrationService registrationService;

    @PostMapping("/register")
    public Result register(@RequestBody Registration registration, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return registrationService.register(registration, userId);
    }

    @PutMapping("/approve/{id}")
    public Result approveRegistration(@PathVariable Integer id,
                                      @RequestParam Integer status,
                                      @RequestParam(required = false) String rejectReason,
                                      HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        return registrationService.approveRegistration(id, status, rejectReason, userId, role);
    }

    @GetMapping("/list")
    public Result getRegistrationList(@RequestParam(required = false) Integer competitionId,
                                      @RequestParam(required = false) Integer status,
                                      @RequestParam(required = false) Integer isSystem,
                                      @RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size) {
        return registrationService.getRegistrationList(competitionId, status, isSystem, page, size);
    }

    @GetMapping("/my")
    public Result getMyRegistrations(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size,
                                     HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return registrationService.getMyRegistrations(userId, page, size);
    }

    @PostMapping("/pay/{id}")
    public Result pay(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return registrationService.pay(id, userId);
    }
}





