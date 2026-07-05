package com.minsu.service;

public interface EmailCodeService {
    boolean sendRegisterCode(String email);

    boolean verifyRegisterCode(String email, String code);
}

