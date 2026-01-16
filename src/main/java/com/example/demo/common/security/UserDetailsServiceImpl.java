package com.example.demo.common.security;

import com.example.demo.user.entity.User;
import com.example.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. 去資料庫查這個人
        // 因為我們之前把 findByEmail 改成回傳 Optional，所以這裡可以用 orElseThrow 寫得很漂亮
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 2. 把我們的 User Entity 轉換成 Spring Security 的 UserDetails
        // 這裡我們直接使用 Spring 內建的 User Builder，簡單又標準
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),          // 當作 username
                user.getPasswordHash(),   // 密碼 (已加密的)
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name())) // 權限/角色
        );
    }
}
