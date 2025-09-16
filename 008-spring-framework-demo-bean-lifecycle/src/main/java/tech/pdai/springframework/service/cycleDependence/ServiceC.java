package tech.pdai.springframework.service.cycleDependence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 属性注入可以解决循环依赖
 */
@Component
public class ServiceC {
    @Autowired
    ServiceD serviceD;
}

@Component
class ServiceD {
    @Autowired
    ServiceC serviceC;
}
