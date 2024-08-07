package com.trxyzng.trung.search.community;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommunityService {
    @Autowired
    CommunityRepo communityRepo;

    public CommunityEntity saveCommunityEntity(CommunityEntity communityEntity) {
        return communityRepo.save(communityEntity);
    }

    public CommunityEntity getCommunityEntityById(int id) {
        return communityRepo.getCommunityEntityById(id);
    }

    public CommunityEntity[] findCommunityEntitiesByName(String name, int number) {
        name = name.toUpperCase();
        return communityRepo.findCommunityEntitiesByName(name, number).orElse(new CommunityEntity[]{});
    }

    public CommunityEntity[] findCommunityEntitiesIncludeByName(String name, int number) {
        name = name.toUpperCase();
        return communityRepo.findCommunityEntitiesIncludeByName(name, number).orElse(new CommunityEntity[]{});
    }

    public CommunityEntity[] findCommunityEntitiesByUid(int uid) {
        return communityRepo.findByUid(uid);
    }

    public boolean isCommunityEntityByIdExist(int id) {
        return communityRepo.isCommunityEntityByUidExist(id) == 1;
    }

    public boolean isCommunityEntityByNameExist(String name) {
        return communityRepo.isCommunityEntityByNameExist(name) == 1;
    }

    public void updateCommunityEntity(int id, int uid, String description, String avatar, String banner, int scope) {
        communityRepo.updateCommunityEntity(id, uid, description, avatar, banner, scope);
    }

}
