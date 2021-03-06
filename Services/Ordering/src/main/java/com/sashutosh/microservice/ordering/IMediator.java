package com.sashutosh.microservice.ordering;

import com.sashutosh.microservice.ordering.commands.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;

public class IMediator {

    final Map<String, Class<IRequestHandler<IRequest<?>, Object>>> commandHandlermap = new HashMap<>();

    public Map<String, Class<IRequestHandler<IRequest<?>, Object>>> getCommandHandlermap() {
        return commandHandlermap;
    }

    public void loadCommandHandlers(String packageName, String handlerType, String commandType )
    {
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
        final Set<BeanDefinition> classes = provider.findCandidateComponents(packageName);
        for (BeanDefinition bean: classes)
        {
            try
            {
                Class<?> clazz = Class.forName(bean.getBeanClassName());
                Type[] interfaces = clazz.getGenericInterfaces();
                if (Arrays.stream(interfaces).anyMatch(t -> t.getTypeName().contains(handlerType))) {
                    for (Type genericType : interfaces) {
                        ParameterizedType parameterizedType=(ParameterizedType)genericType;
                        Type[] typeArguments= parameterizedType.getActualTypeArguments();
                        for(Type typeargumment:typeArguments){
                            if(typeargumment.getTypeName().contains(commandType)){
                                commandHandlermap.put(typeargumment.getTypeName(), (Class<IRequestHandler<IRequest<?>, Object>>) clazz);
                            }

                        }

                    }
                }

            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Done loading the handlers");
    }

    public void build()
    {
        loadCommandHandlers("com.sashutosh.microservice.ordering.commands","IRequestHandler","Command");
        loadCommandHandlers("com.sashutosh.microservice.ordering.domain","INotificationHandler","DomainEvent");
    }
    public boolean send(IRequest<?> cmd)
    {

        Class<IRequestHandler<IRequest<?>, Object>> requestHandler= commandHandlermap.get(cmd.getClass().getName());
        try {
            IRequestHandler<IRequest<?>, Object> handler = requestHandler.newInstance();
            Object result=handler.handle(cmd);
            return result != null;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;

    }
}
