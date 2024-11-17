package TheNaeunEconomy.user.contorller;

import TheNaeunEconomy.user.domain.User;
import TheNaeunEconomy.user.service.UserServiceImpl;
import TheNaeunEconomy.user.service.reponse.AccessTokenResponse;
import TheNaeunEconomy.user.service.reponse.UserNameResponse;
import TheNaeunEconomy.user.service.request.UpdateUserRequest;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/users")
public class UserSettingsController {
    private final UserServiceImpl userService;


    @GetMapping("/name")
    public ResponseEntity<UserNameResponse> getUserName(@RequestHeader HttpHeaders headers) {
        String token = getToken(headers, "Bearer");
        userService.getUserName(token);
        return ResponseEntity.ok(userService.getUserName(token));
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logoutUser(@RequestHeader HttpHeaders headers) {
        String token = getToken(headers, "Bearer");
        return ResponseEntity.ok(userService.logoutUser(token).toString());
    }


    @DeleteMapping("/delete")
    public ResponseEntity<User> deleteUser(@RequestHeader HttpHeaders headers) {
        String token = getToken(headers, "Bearer");
        return ResponseEntity.ok(userService.deleteUser(token));
    }

    @PutMapping("/update")
    public ResponseEntity<User> updateUser(@Valid @RequestBody UpdateUserRequest request,
                                           @RequestHeader HttpHeaders headers) {
        String token = getToken(headers, "Bearer ");
        return ResponseEntity.ok().body(userService.updateUser(request, token));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AccessTokenResponse> validateAndReissueAccessToken(@RequestHeader HttpHeaders headers, HttpServletResponse response) {
        String refreshToken = getToken(headers, "Bearer");
        AccessTokenResponse accessToken = (AccessTokenResponse) userService.refreshToken(refreshToken, response);
        return ResponseEntity.ok(accessToken);
    }

    @Nullable
    private static String getToken(HttpHeaders headers, String Bearer) {
        String token = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (token != null && token.startsWith(Bearer)) {
            token = token.substring(7);
        }
        return token;
    }
}