package com.competition.controller;

import com.competition.common.Result;
import com.competition.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @GetMapping("/list")
    public Result getPaymentList(@RequestParam(required = false) Integer status,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size) {
        return paymentService.getPaymentList(status, keyword, page, size);
    }

    @PostMapping("/refund/{id}")
    public Result refund(@PathVariable Integer id, @RequestParam String reason) {
        return paymentService.refund(id, reason);
    }

    @PostMapping("/notice/{id}")
    public Result sendPaymentNotice(@PathVariable Integer id) {
        return paymentService.sendPaymentNotice(id);
    }

    @GetMapping("/statistics")
    public Result getPaymentStatistics() {
        return paymentService.getPaymentStatistics();
    }
}





