package com.trxyzng.trung.service.userdetail;

import com.trxyzng.trung.entity.User;
import com.trxyzng.trung.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserByEmailService {
    @Autowired
    private UserRepo userrepo;
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException{
        User user = userrepo.findByEmail(email).orElse(new User());
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return new UserDetail(user);
    }
}
