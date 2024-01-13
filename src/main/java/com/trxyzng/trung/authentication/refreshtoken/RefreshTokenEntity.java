package com.trxyzng.trung.authentication.refreshtoken;

import com.trxyzng.trung.user.shared.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "refresh_token", schema = "SECURITY")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RefreshTokenEntity {
    @Column(name = "uid", nullable = false, unique = true)
    private int uid;

    @Id
    @Column(name = "refresh_token", nullable = false)
    private String refresh_token;

    @ManyToOne
    @JoinColumn(name = "uid",  nullable = false, insertable = false, updatable = false)
    private UserEntity userEntity;

    public RefreshTokenEntity(int uid, String token) {
        this.uid = uid;
        this.refresh_token = token;
    }
}
