package com.trxyzng.trung.authentication.accesstoken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessTokenRepo extends JpaRepository<AccessTokenEntity, Integer> {
    AccessTokenEntity save(AccessTokenEntity accessTokenEntity);
}
