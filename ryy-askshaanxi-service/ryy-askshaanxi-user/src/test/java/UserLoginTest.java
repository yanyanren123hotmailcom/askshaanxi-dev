import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ryy.model.user.dtos.LoginDto;
import com.ryy.model.user.pojos.ApUserLogin;
import com.ryy.user.mapper.ApUserLoginMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;

public class UserLoginTest {

    @Autowired
    private ApUserLoginMapper apUserLoginMapper;

    @Test
    public void setUserPassword(){
        LoginDto dto=new LoginDto();
        dto.setPhone("15091148044");
        dto.setPassword("123456ryy");
        LambdaQueryWrapper<ApUserLogin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApUserLogin::getPhone,dto.getPhone());
        ApUserLogin apUserLogin = apUserLoginMapper.selectOne(wrapper);
        String inputPs=dto.getPassword()+apUserLogin.getSalt();
        String inputPsMd5= DigestUtils.md5DigestAsHex(inputPs.getBytes());
        apUserLogin.setPassword(inputPsMd5);

        apUserLoginMapper.update(apUserLogin,wrapper);

    }
}
