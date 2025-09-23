package org.example.cv.configuration;

import com.nimbusds.jose.JOSEException;
import org.example.cv.models.requests.IntrospectRequest;
import org.example.cv.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.ParseException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.secret}")
    private String jwtSecret;

    private  final AuthenticationService authenticationService;
    private NimbusJwtDecoder jwtDecoder = null;

    public CustomJwtDecoder(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            var response = authenticationService.introspect(new IntrospectRequest(token));
            if (!response.isValid()) {
                throw new JwtException("Invalid JWT token");
            }
        }
        catch (ParseException e) {
            throw new JwtException(e.getMessage());
        }

        if(Objects.isNull(jwtDecoder)){
            SecretKeySpec  spec = new SecretKeySpec(jwtSecret.getBytes() , "HS512");
            jwtDecoder = NimbusJwtDecoder.withSecretKey(spec).macAlgorithm(MacAlgorithm.HS512).build();
        }
        return jwtDecoder.decode(token);
    }
}
