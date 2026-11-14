package sn.mndiaye.agenda.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param secret clé HMAC de signature des jetons (32 caractères minimum)
 * @param ttl    durée de validité d'un jeton
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration ttl) {
}
