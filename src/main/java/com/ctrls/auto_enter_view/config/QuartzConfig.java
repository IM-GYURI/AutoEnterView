package com.ctrls.auto_enter_view.config;

import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfig {

  @Bean
  public JobFactory jobFactory(AutowireCapableBeanFactory beanFactory) {
    AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
    jobFactory.setAutowireCapableBeanFactory(beanFactory);
    return jobFactory;
  }

  @Bean
  public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory) {
    SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
    schedulerFactory.setJobFactory(jobFactory);
    return schedulerFactory;
  }
}
