package org.superbiz.moviefun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Application {

    @Bean
    public ServletRegistrationBean servletRegistrationBean(ActionServlet actionServlet) {
        ServletRegistrationBean bean = new ServletRegistrationBean(actionServlet, "/moviefun/*");
        //bean.addUrlMappings("/moviefun/*");
//        List<String> mappings = new ArrayList<>();
//        mappings.add("/moviefun/*");
//        bean.setUrlMappings(mappings);
        return bean;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
