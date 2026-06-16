package org.acme.login;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DataSeeder {

    @Transactional
    void onStart(@Observes StartupEvent ev) {
//Dataseeder.java onStart() 메서드에 추가
//User 초기 데이터 (챔피온 데이터와 별도 블록)
        if(User.count() == 0) {
            User guest = new User();
            guest.username = "guest";
            guest.password = "54d1bd1f394bdade66d506a441113e4830b01605b4eee370920d0df7f3b3a1ae";
            guest.persist();
        }
    }
}