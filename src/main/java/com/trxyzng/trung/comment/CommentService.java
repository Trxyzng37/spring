package com.trxyzng.trung.comment;

import com.mongodb.client.result.UpdateResult;
import com.trxyzng.trung.comment.comment_id.CommentID;
import com.trxyzng.trung.comment.comment_id.CommnentIDRepository;
import com.trxyzng.trung.comment.pojo.CommentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class CommentService {
    private final MongoTemplate mongoTemplate;
    @Autowired
    public CommentService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    @Autowired
    CommnentIDRepository commnentIDRepository;

    String collection_name = "check_comment_status";

    public boolean isCommentByIdExist(int post_id, int parent_id) {
        BasicQuery query = new BasicQuery("{_id:" + parent_id + "}");
        List<Comment> commentList = this.mongoTemplate.find(query, Comment.class, Integer.toString(post_id));
        System.out.println("result list size "+commentList.size());
        if(commentList.size() == 0) {
            return false;
        }
        return true;
    }

    public int getCommentID() {
        return commnentIDRepository.save(new CommentID()).getId();
    }

    public Comment saveComment(Comment comment, int post_id) {
        return this.mongoTemplate.insert(comment, String.valueOf(post_id));
    }

    public List<Comment> findAllCommentInCollection(String collection_name) {
        return this.mongoTemplate.findAll(Comment.class, collection_name);
    }

//    public List<Comment> getCommentByLevel(int collection_id, int level) {
//        BasicQuery query = new BasicQuery("{level:" + level + "}");
//        return this.mongoTemplate.find(query, Comment.class, String.valueOf(collection_id));
//    }

    public List<Comment> getCommentByParentIdAndLevel(int collection_id, int parent_id, int level) {
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("parent_id").is(parent_id),
                Criteria.where("level").is(level)
                ));
        query.with(Sort.by(Sort.Direction.ASC, "created_at"));
        return this.mongoTemplate.find(query, Comment.class, String.valueOf(collection_id));
    }

    public boolean isCollectionExist(int collection_id) {
        return this.mongoTemplate.collectionExists(String.valueOf(collection_id));
    }

    public int getMaxLevel(int collection_id) {
        if(this.isCollectionExist(collection_id)) {
            Query query = new Query();
            query.with(Sort.by(Sort.Direction.DESC, "level"));
            return this.mongoTemplate.find(query, Comment.class, String.valueOf(collection_id)).get(0).getLevel();
        }
        return 0;
    }

    public List<Comment> getAllCommentsInOrder(int collection_id, int level, int max_level, int parent_id, List<Comment> resultList) {
//                System.out.println("level: "+level);
                if (level == max_level+1)
                    return resultList;
                List<Comment> commentList = this.getCommentByParentIdAndLevel(collection_id, parent_id, level);
//                System.out.println("parent_id: "+parent_id);
//                System.out.println("commentList size: "+commentList.size());
//                System.out.println("resultList size: "+resultList.size());
                int k = 0;
                while (k < commentList.size()) {
//                    System.out.println("K value: "+k);
//                    System.out.println("parent_id in loop: "+commentList.get(k).getParent_id());
                    resultList.add(commentList.get(k));
                    int levell = commentList.get(k).getLevel();
                    getAllCommentsInOrder(collection_id, levell+1, max_level, commentList.get(k).get_id(), resultList);
                    k++;
                }
        return resultList;
    }

    public boolean updateCommentVote(int post_id, int _id, int newVote) {
        Query query = new Query();
        query.addCriteria(new Criteria().where("_id").is(_id));
        Update update = new Update().set("vote", newVote);
        UpdateResult result = mongoTemplate.updateFirst(query, update, Comment.class, String.valueOf(post_id));
        return result.getMatchedCount() == 0 ? false : true;
    }

    public void saveOrUpdateCommentStatus(int _id, int uid, String voteType) {
        Query existQuery = new Query();
        existQuery.addCriteria(new Criteria().andOperator(
                Criteria.where("_id").is(_id),
                Criteria.where("uid").is(uid)
        ));
        boolean found = mongoTemplate.exists(existQuery, CommentStatus.class, this.collection_name);
        if(found) {
            Query updateQuery = new Query();
            updateQuery.addCriteria(new Criteria().andOperator(
                    Criteria.where("_id").is(_id),
                    Criteria.where("uid").is(uid)
            ));
            Update update = new Update().set("vote_type", voteType);
            UpdateResult result = mongoTemplate.updateFirst(updateQuery, update, CommentStatus.class, collection_name);
            System.out.println("update: "+result.getModifiedCount());
            System.out.println("update comment status: _id: "+_id+" uid: "+uid+" vote type: "+voteType);
        }
        else {
            CommentStatus commentStatus = new CommentStatus(_id, uid, voteType);
            CommentStatus c = this.mongoTemplate.insert(commentStatus, collection_name);
            System.out.println("save: "+c);
            System.out.println("save comment status: _id: "+ c.get_id() + " uid: " + c.getUid() + " vote type: " + c.getVote_type());
        }
    }

    public CommentStatus findCommentStatus(int _id, int uid) {
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("_id").is(_id),
                Criteria.where("uid").is(uid)
        ));
        return mongoTemplate.findOne(query, CommentStatus.class, this.collection_name);
    }
}
