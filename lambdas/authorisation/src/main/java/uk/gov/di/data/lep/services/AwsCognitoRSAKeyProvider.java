package uk.gov.di.data.lep.services;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class AwsCognitoRSAKeyProvider implements RSAKeyProvider {
    private final URL awsKidStoreUrl;
    private final JwkProvider provider;

    private final Logger logger = LogManager.getLogger();
    public AwsCognitoRSAKeyProvider(String awsCognitoRegion, String awsUserPoolsId) {
        var url = String.format(
            "https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json",
            awsCognitoRegion,
            awsUserPoolsId
        );
        try {
            awsKidStoreUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(String.format("Invalid URL provided, URL=%s", url));
        }
        provider = new JwkProviderBuilder(awsKidStoreUrl).build();
        logger.info(provider);
    }

    @Override
    public RSAPublicKey getPublicKeyById(String kid) {
        try {
            logger.info(provider.get(kid));
            return (RSAPublicKey) provider.get(kid).getPublicKey();
        } catch (JwkException e) {
            throw new RuntimeException(String.format(
                "Failed to get JWT kid=%s from awsKidStore=%s",
                kid,
                awsKidStoreUrl
            ));
        }
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        return null;
    }

    @Override
    public String getPrivateKeyId() {
        return null;
    }
}
