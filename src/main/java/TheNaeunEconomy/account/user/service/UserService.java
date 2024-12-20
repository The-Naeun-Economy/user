package TheNaeunEconomy.account.user.service;

import TheNaeunEconomy.account.naverapi.service.request.NaverAccountInfo;
import TheNaeunEconomy.account.user.domain.User;
import TheNaeunEconomy.account.user.domain.UserSuggestions;
import TheNaeunEconomy.account.user.service.response.LoginResponse;
import TheNaeunEconomy.account.admin.service.response.UserCountResponse;
import TheNaeunEconomy.account.kakaoapi.service.request.KakaoAccountInfo;
import TheNaeunEconomy.account.user.service.request.UpdateUserRequest;
import TheNaeunEconomy.account.user.service.response.UserBillingResponse;
import TheNaeunEconomy.account.user.service.response.UserMyPageResponse;
import TheNaeunEconomy.account.user.service.response.UserNickNameResponse;
import TheNaeunEconomy.jwt.Token;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    void logoutUser(String token);

    User updateUser(UpdateUserRequest request, String token);

    void deleteUser(String token);

    UserNickNameResponse getUserName(String token);

    LoginResponse registerKakaoUser(KakaoAccountInfo kakaoAccountInfo);

    boolean naverUserCheck(String email);

    LoginResponse naverLoginUser(String email);

    LoginResponse registerNaverUser(NaverAccountInfo naverAccountInfo);

    Page<User> findAll(Pageable pageable);

    User findByEmail(String email);

    User deactivateUserId(Long userId);

    User activateUserId(Long userId);

    Map<String, Long> getUsersCountByMonth();

    UserCountResponse getUserCount();

    List<Object[]> getUserGenderCount();

    Token getUserToken(Long userId);

    List<Object[]> getUserBillingCount();

    UserMyPageResponse getUserInfo(String token);

    Page<UserSuggestions> findAllSuggestions(Pageable pageable);

    UserBillingResponse billingUser(String token);
}