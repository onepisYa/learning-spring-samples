package learning.spring.binarytea;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.JdbcUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Bean("/login")
    public UrlFilenameViewController loginController() {
        UrlFilenameViewController controller = new UrlFilenameViewController();
        controller.setSupportedMethods(HttpMethod.GET.name());
        controller.setSuffix(".html");
        return controller;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/", "/login", "/static/**").permitAll()  // 添加登录页例外
            .antMatchers("/h2-console/**").permitAll()
            .mvcMatchers("/actuator/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .formLogin()
            // 使用表单登录
            .loginPage("/login").permitAll() // 设置登录页地址，全员可访问
            .defaultSuccessUrl("/order")
            .failureUrl("/login?error=true")  // 建议添加错误参数
            .loginProcessingUrl("/doLogin")
            .usernameParameter("user")
            .passwordParameter("pwd")
            .and()
            .httpBasic()
            .and()
            // 使用HTTP Basic认证
            .logout()
            .logoutSuccessUrl("/")
            .logoutRequestMatcher(new OrRequestMatcher(
                    new AntPathRequestMatcher("/logout", "GET"),
                    new AntPathRequestMatcher("/logout", "POST")))
            .and()  // 关键连接点
            .csrf()
            .ignoringAntMatchers("/h2-console/**", "/actuator/**")  // 忽略多个路径
            .and()
            .headers()
            .frameOptions().disable()  // 允许 iframe
            .contentSecurityPolicy("script-src 'self' 'unsafe-inline';");  // 允许内联脚本
    }

    @Bean
    public UserDetailsService userDetailsService(ObjectProvider<DataSource> dataSources) {
        JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager();
        userDetailsManager.setDataSource(dataSources.getIfAvailable());
        UserDetails manager = User.builder()
                .username("HanMeimei")
                .password("{bcrypt}$2a$10$iAty2GrJu9WfpksIen6qX.vczLmXlp.1q1OHBxWEX8BIldtwxHl3u")
                .authorities("READ_ORDER", "WRITE_ORDER")
                .build();
        userDetailsManager.createUser(manager);
        return userDetailsManager;
    }
}
