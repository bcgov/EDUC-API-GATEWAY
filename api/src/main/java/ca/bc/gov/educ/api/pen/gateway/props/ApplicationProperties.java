package ca.bc.gov.educ.api.pen.gateway.props;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter
public class ApplicationProperties {
  @Value("${url.api.student.v1}")
  String studentAPIURLV1;
}
