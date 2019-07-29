package com.example.teamboolean.apprentidash.Repos;

import com.example.teamboolean.apprentidash.Models.Discussion;
import org.springframework.data.repository.CrudRepository;

public interface DiscussionRepository extends CrudRepository<Discussion, Long> {
    Discussion findById(long id);
}
