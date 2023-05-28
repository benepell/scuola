package online.vrscuola.controllers.securities;

import online.vrscuola.models.RisorsePhpModel;
import online.vrscuola.models.SetupModel;
import online.vrscuola.services.StudentService;
import online.vrscuola.services.devices.VRDeviceManageDetailService;
import online.vrscuola.services.devices.VRDeviceManageService;
import online.vrscuola.services.log.EventLogService;
import online.vrscuola.utilities.Constants;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.account.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class KeycloakController {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Value("${keycloak.redirect-uri}")
    private String redirectURI;

    @Autowired
    EventLogService logService;

    @Autowired
    StudentService studentService;

    @Autowired
    VRDeviceManageDetailService manageDetailService;

    @RequestMapping("/sso/login")
    public RedirectView ssoLogin() {
        // Esegui il redirect alla abilita-classe se si verifica l'errore 401
        return new RedirectView("/login");
    }

    @GetMapping("/login")
    public RedirectView login(Principal principal, HttpSession session) {
        String redirectLogin = redirectURI + "/login";
        try {
            // Controlla se l'utente è autenticato con Keycloak
            KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
            AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();

            if (accessToken != null) {
                // L'utente è autenticato, esegui le azioni necessarie e reindirizzalo alla pagina desiderata
                session.setAttribute("main_username", accessToken.getPreferredUsername());
                logService.sendLog(session, Constants.EVENT_LOG_IN);
                return new RedirectView("/abilita-classe");
            } else {
                // L'utente non è autenticato, reindirizzalo alla pagina di login di Keycloak
                session.removeAttribute("main_username");
                return new RedirectView("{authServerUrl}/realms/{realm}/protocol/openid-connect/auth?client_id={clientId}&redirect_uri={redirectLogin}&response_type=code");
            }
        } catch (Exception e) {
            // Si è verificato un errore, reindirizza l'utente alla pagina di login di Keycloak
            return new RedirectView("{authServerUrl}/realms/{realm}/protocol/openid-connect/auth?client_id={clientId}&redirect_uri={redirectLogin}&response_type=code");
        }
    }


    @GetMapping("/logout")
    public RedirectView logout(HttpServletRequest request, HttpSession session) throws ServletException {
        logService.sendLog(session, Constants.EVENT_LOG_OUT);

        // chiude tutti i visori prima del logout se viene richiesto dalla pagina di gestione della classe
        boolean closeVisors = session.getAttribute("isCloseVisorLogout") != null ? (Boolean) session.getAttribute("isCloseVisorLogout") : false;
        if (closeVisors) {
          // effettua la chiamata a chiudi i visori
            String[] username = session.getAttribute("username") != null ? (String[])session.getAttribute("username") : null;
            if (username != null && username.length > 0) {
                // chiude tutti i visori
                studentService.closeAllVisor(username, manageDetailService, session);
            }
        }

        if (session != null){
            session.invalidate();
        }
        request.logout();
        return new RedirectView("/");
    }

    @GetMapping("/test")
    public String test(HttpServletRequest request) throws ServletException {
        return "Successfully  admins";
    }

    @PostMapping("/checkRes")
    public String checkRes(@ModelAttribute("setupModel") RisorsePhpModel res, HttpSession session) {
        String param = res.getKey();
        if(param.equals("risorse")) {
            logService.sendLog(session, Constants.EVENT_LOG_CHECK_RES);
            return "admins";
        }
        return "";
    }

    @GetMapping("/test1")
    public String test1(HttpServletRequest request) throws ServletException {
        return "Successfully  users";
    }

    // A new endpoint to get the user's information in JSON format
    @GetMapping("/userinfo")
    public Map<String, Object> getUserInfo(Principal principal, HttpSession session) {
        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) principal;
        AccessToken accessToken = token.getAccount().getKeycloakSecurityContext().getToken();

        // Create a map to hold the user's information
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", accessToken.getName());
        userInfo.put("preferred_username", accessToken.getPreferredUsername());
        userInfo.put("email", accessToken.getEmail());
        userInfo.put("roles", accessToken.getRealmAccess().getRoles().stream().collect(Collectors.toList()));

        // Get the user's resource roles
        Map<String, Set<String>> resourceRoles = new HashMap<>();
        accessToken.getResourceAccess().forEach((k, v) -> {
            if (v != null) {
                resourceRoles.put(k, v.getRoles());
            }
        });
        userInfo.put("resource_roles", resourceRoles);

        logService.sendLog(session, Constants.EVENT_LOG_CHECK_INFO);

        return userInfo;
    }


}