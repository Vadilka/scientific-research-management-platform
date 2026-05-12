package pl.san.articlesubmission.user.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.san.articlesubmission.config.JwtService;
import pl.san.articlesubmission.user.dto.AuthResponse;
import pl.san.articlesubmission.user.dto.LoginRequest;
import pl.san.articlesubmission.user.dto.UserProfileResponse;
import pl.san.articlesubmission.user.dto.UserRegistrationRequest;
import pl.san.articlesubmission.user.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthController(
            UserService userService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtService jwtService
    ) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @GetMapping("/me")
    public UserProfileResponse me(Authentication authentication) {
        return userService.getCurrentUser(authentication);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        return createAuthResponse(authentication);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody UserRegistrationRequest request) {
        UserProfileResponse user = userService.register(request);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.email());
        return new AuthResponse(jwtService.generateToken(userDetails), user);
    }

    private AuthResponse createAuthResponse(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return new AuthResponse(jwtService.generateToken(userDetails), userService.getCurrentUser(authentication));
    }
}
