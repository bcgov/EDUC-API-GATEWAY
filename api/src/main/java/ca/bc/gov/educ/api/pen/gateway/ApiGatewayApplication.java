package ca.bc.gov.educ.api.pen.gateway;

import ca.bc.gov.educ.api.pen.gateway.props.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
@EnableReactiveMethodSecurity
@Slf4j
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}
  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http
        .authorizeExchange().pathMatchers("/v3/api-docs/**",
        "/actuator/health", "/actuator/prometheus", "/actuator/**",
        "/swagger-ui/**","/webjars/**").permitAll()
        .anyExchange().authenticated()
        .and()
        .oauth2ResourceServer()
        .jwt();
    return http.build();
  }
  @Bean
  @Autowired
  public RouteLocator myRoutes(RouteLocatorBuilder builder, RedisRateLimiter redisRateLimiter,
                               ApplicationProperties applicationProperties) {
    log.info("building routes");
    return builder.routes()
        .route("student_api_v1_route", r -> r
            .host("*").and().path("/api/v1/student/**")
            .filters(f ->
                f.requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter)))
            .uri(applicationProperties.getStudentAPIURLV1()))

        .build();
  }
  @Bean
  RedisRateLimiter redisRateLimiter() {
    return new RedisRateLimiter(1, 2);
  }
}
