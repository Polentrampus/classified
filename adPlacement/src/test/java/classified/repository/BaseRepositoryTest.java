package classified.repository;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@EnableAutoConfiguration(
        exclude = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@EntityScan(basePackages = "classified.entity")
@ComponentScan(
        basePackages = "classified.repository",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "classified.repository\\.impl\\..*"
        )
)
public class BaseRepositoryTest {
}
