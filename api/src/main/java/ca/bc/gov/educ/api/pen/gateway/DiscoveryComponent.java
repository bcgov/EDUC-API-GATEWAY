package ca.bc.gov.educ.api.pen.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class DiscoveryComponent {
  private final DiscoveryClient discoveryClient;

  @Autowired
  public DiscoveryComponent(DiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
  }

  @PostConstruct
  public void init(){
    this.discoveryClient.getServices().forEach(log::info);
  }
}
