package org.example.cv.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;

import org.example.cv.exceptions.AppException;
import org.example.cv.exceptions.ErrorCode;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogListener {

    private static final Map<Class<?>, List<Method>> getterCache = new ConcurrentHashMap<>();
    private static EntityManagerFactory entityManagerFactory;

    @PersistenceUnit
    public void setEntityManagerFactory(@Lazy EntityManagerFactory emf) {
        entityManagerFactory = emf;
    }

    @PrePersist
    public void prePersist(Object object) {
        if (entityManagerFactory == null) {
            return;
        }
        if (object instanceof Auditable entity) {
            logEvent("CREATE", entity, null, object);
        }
    }

    @PostPersist
    public void postPersist(Object object) {
        if (entityManagerFactory == null) {
            return;
        }
        if (object instanceof Auditable entity) {
            HttpServletRequest request = getCurrentHttpRequest();
            if(request == null) {
                return;
            }
            request.setAttribute("entityId", entity.getId());
        }
    }

    @PreUpdate
    public void preUpdate(Object object) {
        if (object instanceof Auditable entity) {
            try(var em = entityManagerFactory.createEntityManager()) {
                Object oldEntity = em.find(object.getClass(), entity.getId());
                logEvent("UPDATE", entity, oldEntity, object);
            } catch (Exception e) {
                log.error("Error fetching old entity state for audit log", e);
            }
        }
    }

    @PreRemove
    public void preRemove(Object object) {
        if (entityManagerFactory == null) {
            return;
        }
        if (object instanceof Auditable entity) {
            logEvent("DELETE", entity, object, null);
        }
    }

    private List<Method> getCachedGetters(Class<?> clazz) {
        return getterCache.computeIfAbsent(clazz, c -> Arrays.stream(c.getDeclaredMethods())
                .filter(m -> m.getName().startsWith("get"))
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> !m.getName().equals("getId") && !m.getName().equals("getClass"))
                .filter(m -> m.getReturnType().getPackageName().startsWith("java"))
                .toList());
    }

    private String generateChangeDetails(Object oldEntity, Object newEntity) {
        if (oldEntity == null || newEntity == null) {
            return "No previous state found.";
        }

        StringBuilder changes = new StringBuilder();
        List<Method> getters = getCachedGetters(oldEntity.getClass());

        for (Method method : getters) {
            try {
                Object oldValue = method.invoke(oldEntity);
                Object newValue = method.invoke(newEntity);

                if (!Objects.equals(oldValue, newValue)) {
                    String fieldName = method.getName().substring(3); // Remove "get"
                    changes.append(fieldName)
                            .append(": '")
                            .append(oldValue)
                            .append("' -> '")
                            .append(newValue)
                            .append("'; ");
                }
            } catch (Exception e) {
                // Log or handle exception if needed
            }
        }

        return changes.isEmpty() ? "No changes detected." : changes.toString();
    }

    protected void logEvent(String actionType, Auditable entity, Object oldEntity, Object newEntity) {
        HttpServletRequest request = getCurrentHttpRequest();
        String details = generateChangeDetails(oldEntity, newEntity);
        assert request != null;
        request.setAttribute("details", details);
        request.setAttribute("action", actionType);
        request.setAttribute("entityId", entity.getId());
        request.setAttribute("entityType", entity.getEntityType());
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
