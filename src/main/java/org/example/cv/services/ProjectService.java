package org.example.cv.services;

import org.example.cv.models.requests.ProjectRequest;
import org.example.cv.models.responses.ProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface ProjectService {
    Page<ProjectResponse> getAll(int page, int size, String sortBy, String sortDir, String filter);

    ProjectResponse getById(Long id);

    ProjectResponse create(ProjectRequest request);

    ProjectResponse update(Long id, ProjectRequest request);

    void softdelete(Long id);

    void restore(Long id);

    Page<ProjectResponse> getAllByOwnerId(
            Long ownerId, int page, int size, String sortBy, String sortDir, String filter);
}
