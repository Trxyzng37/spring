package com.trxyzng.trung.search.user_profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trxyzng.trung.authentication.shared.user.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "user_profile", schema = "INFO")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserProfileEntity {
    @Id
    @NotNull
    @Column(name = "uid", nullable = false)
    private int uid;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant created_at;

    @NotNull
    @Column(name = "karma", nullable = false)
    private int karma;

    @NotNull
    @Column(name = "icon_base64", nullable = false)
    private String icon_base64;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", referencedColumnName = "uid", nullable = false)
    private UserEntity userEntity;

//    public UserProfileEntity(int uid, String name, String description, Instant created_at, int karma, String icon_base64 ) {
//        this.uid = uid;
//        this.name = name;
//        this.description = description;
//        this.created_at = created_at;
//        this.karma = karma;
//        this.icon_base64 = icon_base64;
//    }
}
