package skillstorm.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.meanbean.test.BeanVerifier;

import com.skillstorm.model.JwtValidationDto;

public class JwtValidationDtoTest {
    @Test
    public void meanBeanTests(){
        BeanVerifier.verifyBean( JwtValidationDto.class);
    }

    @Test
    public void constructorTest(){
        JwtValidationDto jwt = new JwtValidationDto("testSubject", "testClaim");
        assertEquals(jwt.getJwtSubject(), "testSubject");
        assertEquals(jwt.getJwtClaim(), "testClaim");
    }

}