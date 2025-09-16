//package tech.pdai.springframework.service.cycleDependence;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
///**
// * 循环依赖导致出错
// */
//@Component
//public class ServiceA {
//    ServiceB serviceB;
//
//    @Autowired
//    public ServiceA(ServiceB serviceB) {
//    }
//}
//
//@Component
//class ServiceB {
//    ServiceA serviceA;
//
//    @Autowired
//    public ServiceB(ServiceA serviceA) {
//        this.serviceA = serviceA;
//    }
//}
