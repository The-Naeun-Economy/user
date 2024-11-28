package TheNaeunEconomy.account.user.service;

import TheNaeunEconomy.jwt.RefreshTokenRepository;
import TheNaeunEconomy.account.user.repository.UserRepository;
import TheNaeunEconomy.jwt.domain.RefreshToken;
import TheNaeunEconomy.account.user.domain.User;
import TheNaeunEconomy.jwt.TokenProvider;
import TheNaeunEconomy.account.user.service.response.LoginResponse;
import TheNaeunEconomy.jwt.Token;
import TheNaeunEconomy.account.user.service.response.UserNameResponse;
import TheNaeunEconomy.account.user.service.request.AddUserRequest;
import TheNaeunEconomy.account.kakao_api.service.request.KakaoAccountInfo;
import TheNaeunEconomy.account.user.service.request.LoginUserRequest;
import TheNaeunEconomy.account.user.service.request.UpdateUserRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;

@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    @Value("${jwt.ACCESS_TOKEN_MINUTE_TIME}")
    private int ACCESS_TOKEN_MINUTE_TIME;
    @Value("${jwt.REFRESH_TOKEN_MINUTE_TIME}")
    private int REFRESH_TOKEN_MINUTE_TIME;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserServiceImpl(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                           TokenProvider tokenProvider, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenProvider = tokenProvider;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public User saveUser(AddUserRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        });
        return userRepository.save(new User(request));
    }

    @Override
    public LoginResponse registerUser(KakaoAccountInfo kakaoAccountInfo) {
        return userRepository.findByEmail(kakaoAccountInfo.getEmail())
                .map(existingUser -> kakaoLoginUser(existingUser.getEmail())).orElseGet(() -> {
                    userRepository.save(new User(kakaoAccountInfo));
                    return kakaoLoginUser(kakaoAccountInfo.getEmail());
                });
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    @Override
    public UserNameResponse getUserName(String token) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return userRepository.findById(userId).map(user -> new UserNameResponse(user.getName()))
                .orElseThrow(() -> new IllegalArgumentException("토큰에 대한 사용자를 찾을 수 없습니다. " + token));
    }


    @Override
    public User updateUser(UpdateUserRequest request, String token) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("토큰에 대한 사용자를 찾을 수 없습니다. " + token));

        if (user.getDeleteDate() != null) {
            throw new IllegalStateException("사용자는 비활성화 상태 입니다.");
        }

        user.updateUserDetails(request);

        return userRepository.save(user);
    }

    @Override
    public User deleteUser(String token) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("토큰에 대한 사용자를 찾을 수 없습니다. " + token));

        user.setDeleteDate(LocalDate.now());

        return userRepository.save(user);
    }

    @Override
    public LoginResponse loginUser(LoginUserRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자가 존재하지 않습니다."));

        validateUser(user, request.getPassword());

        Token accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_MINUTE_TIME);
        Token refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_MINUTE_TIME);

        refreshTokenRepository.save(new RefreshToken(user, refreshToken.getToken(),
                LocalDateTime.now().plusMinutes(REFRESH_TOKEN_MINUTE_TIME)));

        return new LoginResponse(accessToken, refreshToken);
    }

    private void validateUser(User user, String rawPassword) {
        if (user.getDeleteDate() != null) {
            throw new IllegalStateException("삭제된 사용자입니다. 관리자에게 문의하세요.");
        }

        if (!isPasswordMatch(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }

    @Override
    public void logoutUser(String token) {
        refreshTokenRepository.deleteByRefreshToken(token);
    }

    public boolean kakaoUserCheck(String email) {
        Optional<User> byEmail = userRepository.findByEmail(email);
        return byEmail.isPresent();
    }

    public LoginResponse kakaoLoginUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자가 존재하지 않습니다."));

        Token accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_MINUTE_TIME);
        Token refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_MINUTE_TIME);

        refreshTokenRepository.save(new RefreshToken(user, refreshToken.getToken(),
                LocalDateTime.now().plusMinutes(REFRESH_TOKEN_MINUTE_TIME)));

        return new LoginResponse(accessToken, refreshToken);
    }

    public LoginResponse refreshToken(String refreshToken) {
        refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        refreshTokenRepository.deleteByRefreshToken(refreshToken);

        User user = userRepository.findById(tokenProvider.getUserIdFromToken(refreshToken))
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        Token newAccessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_MINUTE_TIME);
        Token newRefreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_MINUTE_TIME);

        refreshTokenRepository.save(new RefreshToken(user, newRefreshToken.getToken(),
                LocalDateTime.now().plusMinutes(REFRESH_TOKEN_MINUTE_TIME)));
        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens();
    }

    public boolean isPasswordMatch(String rawPassword, String encodedPassword) {
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
    }
}