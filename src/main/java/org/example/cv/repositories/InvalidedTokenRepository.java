package org.example.cv.repositories;

import org.example.cv.models.entities.InvalidatedToken;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidedTokenRepository extends BaseRepository<InvalidatedToken, String> {}
