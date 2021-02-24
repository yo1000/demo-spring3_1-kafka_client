package com.yo1000.demo.spring3_1.kafka_client;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.io.File;
import java.util.Set;

@Configuration
@ComponentScan(basePackageClasses = Application.class)
public class Application extends WebMvcConfigurerAdapter implements WebApplicationInitializer {
    public static void main(final String[] args) throws LifecycleException {
        final Tomcat tomcat = new Tomcat();

        // The port that we should run on can be set into an environment variable
        // Look for that variable and default to 8080 if it isn't there.
        final String webPort = System.getenv("TOMCAT_PORT");
        tomcat.setPort(webPort != null && !webPort.isEmpty() ? Integer.parseInt(webPort) : 8080);

        final String docBasePath = System.getenv("TOMCAT_DOC_BASE");
        final String docBaseAbsolutePath = new File(docBasePath != null ? docBasePath : "").getAbsolutePath();
        tomcat.addWebapp("/", docBaseAbsolutePath);
        System.out.println("configuring app with basedir: " + docBaseAbsolutePath);

        tomcat.start();
        tomcat.getServer().await();
    }

    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {
        // Create the 'root' Spring application context
        final AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(Application.class);
        rootContext.refresh();

        // Manage the lifecycle of the root application context
        servletContext.addListener(new ContextLoaderListener(rootContext));

        // Create the dispatcher servlet's SpringMVC application context
        final AnnotationConfigWebApplicationContext mvcContext = new AnnotationConfigWebApplicationContext();
        mvcContext.register(Application.class);
        mvcContext.setParent(rootContext);

        // Register and map the dispatcher servlet
        final ServletRegistration.Dynamic dispatcherServlet = servletContext.addServlet("dispatcherServlet", new DispatcherServlet(mvcContext));
        dispatcherServlet.setLoadOnStartup(1);
        final Set<String> mappingConflicts = dispatcherServlet.addMapping("/");

        // Check the servlet mappings
        if (!mappingConflicts.isEmpty()) {
            for (String s : mappingConflicts) {
                System.out.println("[ERROR] Mapping conflict: " + s);
            }
            throw new IllegalStateException(
                    "'webservice' cannot be mapped to '/'");
        }
    }
}
