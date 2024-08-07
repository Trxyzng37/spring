package com.trxyzng.trung.search.user_profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {
    @Autowired
    UserProfileRepo userProfileRepo;

    public UserProfileEntity[] findUserProfileEntitiesByName(String name, int number) {
        name = name.toUpperCase();
        return userProfileRepo.findUserProfileEntitiesByName(name, number).orElse(new UserProfileEntity[]{});
    }

    public UserProfileEntity[] findUserProfileEntitiesIncludingKeyword(String keyword, int number) {
        keyword = keyword.toUpperCase();
        return userProfileRepo.findUserProfileEntitiesIncludeByName(keyword, number).orElse(new UserProfileEntity[]{});
    }

    public UserProfileEntity findByUid(int uid) {
        return userProfileRepo.findByUid(uid).orElse(new UserProfileEntity());
    }

    public UserProfileEntity findByUsername(String username) {
        return userProfileRepo.findByUsername(username).orElse(new UserProfileEntity());
    }

    public void UpdateUserProfile(int uid, String description, String avatar) {
        int found = userProfileRepo.existByUid(uid);
        if(found == 1) {
            userProfileRepo.updateUserProfile(uid, description, avatar);
        }
    }
}
