package com.diligesoft.gwifiddns.routes;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Endpoints {

    @ConfigProperty(name = "timerUrl", defaultValue = "timer://check-ip?fixedRate=true")
    String timerUrl;



    String getTimerUrl(){
        return timerUrl;
    }

    String vendorsRoute(){return "direct:public-vendors";}

    String vendorRoute(){return "direct:vendor";}
}
