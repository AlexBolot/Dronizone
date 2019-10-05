package fr.unice.polytech.config;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImplExporter;
import fr.unice.polytech.service.OrderService;
import fr.unice.polytech.service.impl.OrderServiceImpl;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableJpaRepositories(basePackages = "fr.unice.polytech.repo")
@EnableTransactionManagement
public class OrderConfig {

    @Bean
    public static AutoJsonRpcServiceImplExporter autoJsonRpcServiceImplExporter() {
        AutoJsonRpcServiceImplExporter exp = new AutoJsonRpcServiceImplExporter();
        //in here you can provide custom HTTP status code providers etc. eg:
//        exp.setHttpStatusCodeProvider();
//        exp.setErrorResolver();
        return exp;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }


}
