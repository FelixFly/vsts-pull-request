package top.felixfly.vsts.vstspullrequest.autoconfigure;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import top.felixfly.vsts.vstspullrequest.event.WorkItemEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@link EventBus} 自动装配
 *
 * @author FelixFly <chenglinxu@yeah.net>
 * @date 2020/4/17
 */
@Configuration
public class EventBusAutoConfiguration {


    @Autowired
    private RestTemplate workItemRestTemplate;

    @PostConstruct
    public void registerEvent() {
        EventBus eventBus = eventBus();
        eventBus.register(workItemEvent());
    }

    @Bean
    public EventBus eventBus() {
        return new AsyncEventBus(executors());
    }


    @Bean
    public ExecutorService executors(){
        return Executors.newFixedThreadPool(5);
    }


    @Bean
    public WorkItemEvent workItemEvent(){
        return new WorkItemEvent(workItemRestTemplate);
    }

    @PreDestroy
    public void destroy(){
        eventBus().unregister(workItemEvent());
        executors().shutdown();
    }
}
