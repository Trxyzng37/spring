package com.trxyzng.trung.post.edit_post;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.trxyzng.trung.post.PostEntity;
import com.trxyzng.trung.post.PostService;
import com.trxyzng.trung.post.create_post.pojo.Img;
import com.trxyzng.trung.post.create_post.pojo.Index;
import com.trxyzng.trung.post.edit_post.pojo.EditPostRequest;
import com.trxyzng.trung.post.edit_post.pojo.EditPostResponse;
import com.trxyzng.trung.post.get_post.pojo.LinkPostData;
import com.trxyzng.trung.utility.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class EditPostController {
    @Autowired
    PostService postService;
    @Value("${photo_storage_url}")
    private String photo_storage_url;

    @RequestMapping(value = "/edit-editor-post", method = RequestMethod.POST)
    public ResponseEntity<String> editEditorPost(@RequestBody EditPostRequest requestBody) throws IOException {
        PostEntity postEntity = postService.getPostEntityByPostId(requestBody.getPost_id());
        if (postEntity.getUid() == requestBody.getUid()) {
            String content = requestBody.getContent();
            String regex = "src=\"([^\"]*)\"";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);
            ArrayList<String> imgWithURL = new ArrayList<String>();
            ArrayList<Index> index = new ArrayList<Index>();
            while (matcher.find()) {
                String base64_img = matcher.group(1);
                int startIndex = matcher.start();
                int endIndex = matcher.end();
                index.add(new Index(startIndex, endIndex));
                System.out.println("start(): " + matcher.start());
                System.out.println("end(): " + matcher.end());
                if(base64_img.startsWith("https://res.cloudinary.com")) {
                    System.out.println("Content without replace: " + base64_img);
                }
                Cloudinary cloudinary = new Cloudinary(photo_storage_url);
                cloudinary.config.secure = true;
                Map response = cloudinary.uploader().upload(
                        base64_img,
                        ObjectUtils.asMap(
                                "folder", String.valueOf(requestBody.getPost_id()),
                                "use_filename", false,
                                "unique_filename", true,
                                "allowed_formats", "jpeg, jpg, png"
                        )
                );
                String imgUrl = (String) response.get("secure_url");
                imgWithURL.add(imgUrl);
            }
            System.out.println("Found " + imgWithURL.size() + " img src");
            for (int i = 0; i < imgWithURL.size(); i++) {
                System.out.println("url: " + imgWithURL.get(i));
            }
            String newContent = "";
            if (index.size() == 1) {
                newContent += content.substring(0, index.get(0).start + 5) + imgWithURL.get(0) + content.substring(index.get(0).end - 1, content.length());
            } else if (index.size() > 1) {
                for (int i = 0; i < index.size(); i++) {
                    if (i == 0) {
                        newContent += content.substring(0, index.get(i).start + 5) + imgWithURL.get(i);
//                        System.out.println("newContent: " + newContent);
                    } else if (i == index.size() - 1) {
                        newContent += content.substring(index.get(i - 1).end - 1, index.get(i).start + 5) + imgWithURL.get(i) + content.substring(index.get(i).end - 1, content.length());
//                        System.out.println("newContent: " + newContent);
                    } else {
                        newContent += content.substring(index.get(i - 1).end - 1, index.get(i).start + 5) + imgWithURL.get(i);
//                        System.out.println("newContent: " + newContent);
                    }
                }
            } else {
                newContent = content;
            }
            System.out.println("Content after replace: " + newContent);
            postService.updatePostEntityByPostId(requestBody.getPost_id(), requestBody.getTitle(), newContent);
            System.out.println("Update post with type " + postEntity.getType() + " and post_id: " + requestBody.getPost_id());
            String responseBody = JsonUtils.getStringFromObject(new EditPostResponse(true, ""));
            return new ResponseEntity<String>(responseBody, new HttpHeaders(), HttpStatus.OK);
        }
        String responseBody = JsonUtils.getStringFromObject(new EditPostResponse(false, "error edit post"));
        return new ResponseEntity<String>(responseBody, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/edit-img-post", method = RequestMethod.POST)
    public ResponseEntity<String> editImgPost(@RequestBody EditPostRequest requestBody) throws IOException {
        PostEntity postEntity = postService.getPostEntityByPostId(requestBody.getPost_id());
        if (postEntity.getUid() == requestBody.getUid()) {
            String imgData = requestBody.getContent();
            ObjectMapper objectMapper = new ObjectMapper();
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            ArrayList<Img> imgArr = objectMapper.readValue(imgData, typeFactory.constructCollectionType(ArrayList.class, Img.class));
            for (int i=0; i<imgArr.size(); i++) {
                String imgBase64 = imgArr.get(i).data;
                if(imgBase64.startsWith("https://res.cloudinary.com")) {
                    System.out.println("Content without replace: " + imgArr.get(i).data);
                }
                else {
                    Cloudinary cloudinary = new Cloudinary(photo_storage_url);
                    cloudinary.config.secure = true;
                    Map response = cloudinary.uploader().upload(
                            imgBase64,
                            ObjectUtils.asMap(
                                    "folder", String.valueOf(requestBody.getPost_id()),
                                    "use_filename", false,
                                    "unique_filename", true,
                                    "allowed_formats", "jpeg, jpg, png"
                            )
                    );
                    String imgUrl = (String) response.get("secure_url");
                    imgArr.get(i).data = imgUrl;
                    System.out.println("Content after replace: " + imgArr.get(i).data);
                }
            }
            String imgArrString = JsonUtils.getStringFromObject(imgArr);
            postService.updatePostEntityByPostId(requestBody.getPost_id(), requestBody.getTitle(), imgArrString);
            System.out.println("update post with post_id: " + requestBody.getPost_id());
            EditPostResponse editPostResponse = new EditPostResponse(true, "");
            String responseBody = JsonUtils.getStringFromObject(editPostResponse);
            return new ResponseEntity<String>(responseBody, new HttpHeaders(), HttpStatus.OK);
        }
        String responseBody = JsonUtils.getStringFromObject(new EditPostResponse(false, "error edit post with post_id: "+requestBody.getPost_id()+ " , type: img"));
        return new ResponseEntity<String>(responseBody, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/edit-link-post", method = RequestMethod.POST)
    public ResponseEntity<String> editLinkPost(@RequestBody EditPostRequest requestBody) throws IOException {
        PostEntity postEntity = postService.getPostEntityByPostId(requestBody.getPost_id());
        if (postEntity.getUid() == requestBody.getUid()) {
            if(postEntity.getContent() != requestBody.getContent()) {
                URL oracle = new URL(requestBody.getContent());
                BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));
                String htmlContent = "";
                String l;
                while ((l = in.readLine()) != null) {
                    htmlContent += l;
//                    System.out.println(l);
                }
                in.close();
                String titleRegex = "<meta property=\"og:title\" content=\"([^\"]*)\"";
                Pattern titlePattern = Pattern.compile(titleRegex);
                Matcher titleMatcher = titlePattern.matcher(htmlContent);
                String title = "";
                if (titleMatcher.find()) {
                    title = titleMatcher.group(1);
                }
                System.out.println("Title:"+title);
                String imageRegex = "<meta property=\"og:image\" content=\"([^\"]*)\"";
                Pattern imagePattern = Pattern.compile(imageRegex);
                Matcher imageMatcher = imagePattern.matcher(htmlContent);
                String image = "";
                if (imageMatcher.find()) {
                    image = imageMatcher.group(1);
                }
                System.out.println("image url:"+image);
                String urlRegex = "<meta property=\"og:url\" content=\"([^\"]*)\"";
                Pattern urlPattern = Pattern.compile(urlRegex);
                Matcher urlMatcher = urlPattern.matcher(htmlContent);
                String url = "";
                if (urlMatcher.find()) {
                    url = urlMatcher.group(1);
                }
                System.out.println("url:"+url);
                LinkPostData p = new LinkPostData(requestBody.getContent(), title, image, url);
                postService.updatePostEntityByPostId(requestBody.getPost_id(), requestBody.getTitle(), JsonUtils.getStringFromObject(p));
                String responseBody = JsonUtils.getStringFromObject(new EditPostResponse(true, ""));
                return new ResponseEntity<String>(responseBody, new HttpHeaders(), HttpStatus.OK);
            }
        }
        String responseBody = JsonUtils.getStringFromObject(new EditPostResponse(false, "error edit post type link"));
        return new ResponseEntity<String>(responseBody, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/edit-video-post", method = RequestMethod.POST)
    public ResponseEntity<String> editVideoPost(@RequestBody EditPostRequest requestBody) throws IOException {
        PostEntity postEntity = postService.getPostEntityByPostId(requestBody.getPost_id());
        if (postEntity.getUid() == requestBody.getUid()) {
            if(postEntity.getContent() != requestBody.getContent()) {
                String edit_content = requestBody.getContent();
                Cloudinary cloudinary = new Cloudinary(photo_storage_url);
                cloudinary.config.secure = true;
                Map response = cloudinary.uploader().upload(
                        edit_content,
                        ObjectUtils.asMap(
                                "folder", String.valueOf(requestBody.getPost_id()),
                                "use_filename", false,
                                "unique_filename", true,
                                "resource_type", "video",
                                "allowed_formats", "mp4, mov, wmv, WebM"
                        )
                );
                String videoUrl = (String) response.get("secure_url");
                System.out.println("Content after replace: " + videoUrl);
                postService.updatePostEntityByPostId(requestBody.getPost_id(), requestBody.getTitle(), videoUrl);
                System.out.println("update post with post_id: " + requestBody.getPost_id());
                EditPostResponse editPostResponse = new EditPostResponse(true, "");
                String responseBody = JsonUtils.getStringFromObject(editPostResponse);
                return new ResponseEntity<String>(responseBody, new HttpHeaders(), HttpStatus.OK);
            }
            else {
                System.out.println("Update without replace: "+requestBody.getContent());
                postService.updatePostEntityByPostId(requestBody.getPost_id(), requestBody.getTitle(), requestBody.getContent());
                System.out.println("update post with post_id: " + requestBody.getPost_id());
                EditPostResponse editPostResponse = new EditPostResponse(true, "");
                String responseBody = JsonUtils.getStringFromObject(editPostResponse);
                return new ResponseEntity<String>(responseBody, new HttpHeaders(), HttpStatus.OK);
            }
        }
        String responseBody = JsonUtils.getStringFromObject(new EditPostResponse(false, "error edit post with post_id: "+requestBody.getPost_id()+ " , type: img"));
        return new ResponseEntity<String>(responseBody, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
}
