package tech.pdai.springframework.service.problem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.pdai.springframework.service.CglibProxyDemoServiceImpl;

@Component
public class ServiceA {

    public void outCall() {
        System.out.println("serviceA.outCall()");
        this.innerCall();
    }

    public void innerCall() {
        System.out.println("serviceA.innerCall()");
    }
}
