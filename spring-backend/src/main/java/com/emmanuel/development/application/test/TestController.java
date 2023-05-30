package com.emmanuel.development.application.test;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping(path = "/test")
public class TestController {

    /** Protected route only employees with the role ADMIN can hit **/
    @GetMapping(path = "/admin")
    @PreAuthorize(value = "hasAuthority('ADMIN')")
    public String getAuthenticated() {
        return "Route Admin " + SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();
    }

    /** Protected route. Any authenticated employee can his this **/
    @GetMapping(path = "/user")
    public String onlyEmployeesCanHitThisRoute() {
        return "Route Authenticated " + SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().toString();
    }

}
