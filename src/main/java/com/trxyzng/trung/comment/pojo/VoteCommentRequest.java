package com.trxyzng.trung.comment.pojo;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class VoteCommentRequest {
    public int post_id;
    public int uid;
    public int _id;
    public int vote;
    public String vote_type;
}
