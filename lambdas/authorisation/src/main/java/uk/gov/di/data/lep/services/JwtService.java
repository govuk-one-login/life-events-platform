package uk.gov.di.data.lep.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import uk.gov.di.data.lep.library.config.Config;

public class JwtService {
    private final JWTVerifier jwtVerifier;

    public JwtService() {
        this(new Config());
    }

    public JwtService(Config config) {
        var keyProvider = new AwsCognitoRSAKeyProvider(config.getAwsRegion(), config.getUserPoolId());
        var algorithm = Algorithm.RSA256(keyProvider);
        jwtVerifier =  JWT.require(algorithm).build();
    }

    public DecodedJWT decode(String authorisationToken) {
        return jwtVerifier.verify(authorisationToken);
    }
}
