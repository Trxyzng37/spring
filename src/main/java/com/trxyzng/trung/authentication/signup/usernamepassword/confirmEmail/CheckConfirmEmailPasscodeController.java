package com.trxyzng.trung.authentication.signup.usernamepassword.confirmEmail;

import com.trxyzng.trung.authentication.shared.POJO.PasscodeResponse;
import com.trxyzng.trung.authentication.shared.POJO.Passcode;
import com.trxyzng.trung.authentication.shared.user.UserEntity;
import com.trxyzng.trung.authentication.shared.user.services.UserEntityService;
import com.trxyzng.trung.authentication.signup.usernamepassword.tempSignupData.TempSignUpDataEntity;
import com.trxyzng.trung.authentication.signup.usernamepassword.tempSignupData.TempSignUpDataService;
import com.trxyzng.trung.utility.EmptyEntityUtils;
import com.trxyzng.trung.utility.servlet.HttpServletRequestUtils;
import com.trxyzng.trung.utility.JsonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@CrossOrigin(origins = "http://127.0.0.1:4200")
@RestController
public class CheckConfirmEmailPasscodeController {
    @Autowired
    ConfirmEmailPasscodeService confirmEmailPasscodeService;
    @Autowired
    TempSignUpDataService tempSignUpDataService;
    @Autowired
    UserEntityService userEntityService;
    @RequestMapping(value = "/check-confirm-email-passcode", method = RequestMethod.POST)
    public ResponseEntity<String> checkPasscode(HttpServletRequest request) {
        try {
            String body = HttpServletRequestUtils.readRequestBody(request);
            Passcode jsonObj = JsonUtils.getObjectFromString(body, Passcode.class, Passcode::new);
            String email = jsonObj.getEmail();
            int passcode = jsonObj.getPasscode();
            Instant sendAt = jsonObj.getSendAt();
            System.out.println(email);
            System.out.println(passcode);
            System.out.println(sendAt);
            boolean isTimeValid = confirmEmailPasscodeService.isSendTimeValid(email, sendAt);
            boolean checkPasscode = confirmEmailPasscodeService.isPasscodeMatch(email, passcode);
            PasscodeResponse passcodeResponse = new PasscodeResponse(checkPasscode, !isTimeValid);
            HttpHeaders headers = new HttpHeaders();
            if (checkPasscode && isTimeValid) {
                System.out.println("Check passcode correct. Sign-up OK");
                TempSignUpDataEntity tempSignUpDataEntity = tempSignUpDataService.findTempSignUpDataEntityByEmail(email);
                if (EmptyEntityUtils.isEmptyEntity(tempSignUpDataEntity)) {
                    return new ResponseEntity<>("Error find sign-up data", headers, HttpStatus.BAD_REQUEST);
                }
                String username = tempSignUpDataEntity.getUsername();
                String password = tempSignUpDataEntity.getPassword();
                System.out.println("username: " + username);
                System.out.println("password: " + password);
                System.out.println("email: " + email);
                UserEntity userEntity = new UserEntity(username, password, email);
                System.out.println("Save new user to USER_DATA.users");
                UserEntity savedUserEntity = userEntityService.saveUserEntity(userEntity);
                System.out.println("uid of new saved user:" + savedUserEntity.getId());
                System.out.println("Delete temp sign-up data");
                tempSignUpDataService.deleteTempSignUpDataEntityByEmail(email);
            }
            String responseBody = JsonUtils.getStringFromObject(passcodeResponse);
            System.out.println(responseBody);
            if (responseBody.equals("")) {
                return new ResponseEntity<>("Error get string from json", headers, HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
        }
        catch (ConstraintViolationException e) {
            System.out.println("Constraint error");
            HttpHeaders headers = new HttpHeaders();
            return new ResponseEntity<>("Error in sign-up data", headers, HttpStatus.BAD_REQUEST);
        }
    }
}
