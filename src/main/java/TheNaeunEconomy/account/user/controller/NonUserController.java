package TheNaeunEconomy.account.user.controller;


import TheNaeunEconomy.account.user.service.NonUserServiceImpl;
import TheNaeunEconomy.account.user.service.request.AddSuggestionsRequest;
import TheNaeunEconomy.account.user.service.response.LoginResponse;
import TheNaeunEconomy.account.user.service.request.AddUserRequest;
import TheNaeunEconomy.account.user.service.request.LoginUserRequest;
import TheNaeunEconomy.account.user.service.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class NonUserController {

    private final NonUserServiceImpl nonUserService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody @Valid LoginUserRequest loginUserRequest) {
        return ResponseEntity.ok().body(nonUserService.loginUser(loginUserRequest));
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> saveUser(@RequestBody @Valid AddUserRequest request) {
        return ResponseEntity.ok().body(new UserResponse(nonUserService.saveUser(request)));
    }

    @PostMapping("suggestions")
    public ResponseEntity<String> saveSuggestions(@RequestBody AddSuggestionsRequest request) {
        return ResponseEntity.ok().body(nonUserService.saveSuggestions(request));
    }
}