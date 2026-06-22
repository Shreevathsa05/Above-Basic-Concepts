package com.shree.sse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/")
    public String index(){
        return "index";
    }

    @GetMapping("/status")
    public String status(){
        return "status";
    }
}
