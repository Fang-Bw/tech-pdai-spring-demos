package tech.pdai.springframework.service;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

// AOP 切面
@Aspect
@Component
class LoggingAspect {

    @Before("execution(* MyServiceCallClassMethod.*(..))")
    public void logBefore() {
        System.out.println("Before method execution");
    }
}

// 目标类
@Service
public class MyServiceCallClassMethod {

    @Autowired
    MyServiceCallClassMethod myServiceCallClassMethod;

    /**
     * privateMethod() 不会被增强
     */
    public void publicMethod1() {
        System.out.println("Inside publicMethod");
        this.privateMethod(); // 直接调用 private 方法，不通过代理
    }

    /**
     * privateMethod() 不会被增强
     */
    public void publicMethod2() {
        System.out.println("Inside publicMethod");
        myServiceCallClassMethod.privateMethod();
    }
    /**
     * protectedMethod() 会被增强
     */
    public void publicMethod3() {
        System.out.println("Inside publicMethod");
        myServiceCallClassMethod.protectedMethod();
    }

    private void privateMethod() {
        System.out.println("Inside privateMethod");
    }

    protected void protectedMethod() {
        System.out.println("Inside protectedMethod");
    }
}
