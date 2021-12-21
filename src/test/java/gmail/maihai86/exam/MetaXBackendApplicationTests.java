package gmail.maihai86.exam;

import gmail.maihai86.exam.event.OnRegistrationCompleteEvent;
import gmail.maihai86.exam.event.RegistrationListener;
import gmail.maihai86.exam.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class MetaXBackendApplicationTests {

    @Autowired
    private RegistrationListener registrationListener;

    /*@Test
    public void contextLoads() {
        User user = new User();
        user.setEmail("maitienhai@so4eo.com");

        registrationListener.onApplicationEvent(new OnRegistrationCompleteEvent(user, null, null, "abc"));
        Assert.assertTrue(true);
    }*/

}
