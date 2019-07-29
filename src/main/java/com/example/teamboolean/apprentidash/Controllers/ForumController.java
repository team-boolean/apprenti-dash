package com.example.teamboolean.apprentidash.Controllers;

import com.example.teamboolean.apprentidash.Models.AppUser;
import com.example.teamboolean.apprentidash.Models.Comment;
import com.example.teamboolean.apprentidash.Models.Discussion;
import com.example.teamboolean.apprentidash.Repos.AppUserRepository;
import com.example.teamboolean.apprentidash.Repos.CommentRepository;
import com.example.teamboolean.apprentidash.Repos.DiscussionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;

@Controller
public class ForumController {

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    DiscussionRepository discussionRepository;

    //POST Mapping to create new Discussion
    @PostMapping("/forum")
    public RedirectView createThread(Principal p, String title, String body) {
        AppUser author = appUserRepository.findByUsername(p.getName());
        Discussion newDiscussion = new Discussion(author, title, body);
        discussionRepository.save(newDiscussion);

        return new RedirectView("/forum/" + newDiscussion.getId());
    }

    @PostMapping("/forum/{id}")
    public RedirectView createComment(@PathVariable long id, Principal p, String body) {
        AppUser author = appUserRepository.findByUsername(p.getName());
        Discussion parentDiscussion = discussionRepository.findById(id);
        Comment newComment = new Comment(author, parentDiscussion, body);

        commentRepository.save(newComment);

        return new RedirectView("/forum/" + id);
    }

}
