package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils;


import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

public class TestSecurityUtils {

    public static SecurityMockMvcRequestPostProcessors.CsrfRequestPostProcessor withCsrf() {
        return csrf();
    }

    public static String[] USER_AUTHORITIES = {"USER"};
    public static String[] ADMIN_AUTHORITIES = {"ADMIN"};
}