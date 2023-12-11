package com.nowcoder.community.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    //第一个星号表示接受所有类型的返回，第二个星号指定类，第三个星号指定方法名，括号内的..表示接受所有类型的输入，该注释定义切入点
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut(){
        //函数名随意
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        //joinPoint是方便提取出原来方法
        //需要记录日志的格式为：用户[ip],在[Date],访问了[com.nowcoder.community.service.xxx()]
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringTypeName() + '.' +joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s],在[%s],访问了[%s]",ip,now,target));
    }
}
