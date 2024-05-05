package com.trxyzng.trung.post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trxyzng.trung.authentication.shared.user.UserEntity;
import com.trxyzng.trung.search.community.CommunityEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "post", schema = "INFO")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PostEntity {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_id_generator")
    @SequenceGenerator(name="post_id_generator", sequenceName = "seq_post_id", allocationSize = 1)
    @Column(name = "post_id", nullable = false)
    private int post_id;

    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull
    @Column(name = "uid", nullable = false)
    private int uid;

    @NotNull
    @Column(name = "community_id", nullable = false)
    private int community_id;

    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Column(name = "content", nullable = false)
    private String content;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant created_at;

    @NotNull
    @Column(name = "vote", nullable = false)
    private int vote;

    @NotNull
    @Column(name = "deleted", nullable = false)
    private int deleted;

//    @JsonIgnore
//    @ManyToOne
////    @JsonManagedReference
//    @JoinColumn(name="community_name", nullable = false, insertable=false, updatable=false)
//    private CommunityEntity communityEntity;
//
//    @JsonIgnore
//    @ManyToOne
////    @JsonManagedReference
//    @JoinColumn(name="username", nullable = false, insertable=false, updatable=false)
//    private UserEntity userEntity;

//    @OneToMany(mappedBy="postEntity")
//    private Set<VotePostEntity> checkVotePostEntities;

    public PostEntity(String type, int uid, int community_id, String title, String content, Instant created_at) {
        this.type = type;
        this.uid = uid;
        this.community_id = community_id;
        this.title = title;
        this.content = content;
        this.created_at = created_at;
        this.vote = 0;
        this.deleted = 0;
    }
}
